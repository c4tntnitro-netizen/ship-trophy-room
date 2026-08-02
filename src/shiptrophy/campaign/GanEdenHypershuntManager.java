package shiptrophy.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import shiptrophy.IsaTrophyManager;
import shiptrophy.campaign.GanEdenQuestManager.Stage;

/**
 * Owns the two quest-specific hypershunt blockades and survey state.
 *
 * <p>The state lives on the hypershunt entities, while each guard fleet carries
 * the stable key of the structure it belongs to. This keeps the encounter
 * idempotent across saves. Each guard also carries a small fleet listener so
 * winning the encounter clears the blockade even when surviving ships escape
 * the tactical battle.</p>
 */
public final class GanEdenHypershuntManager {
    public static final String GUARD_KEY =
            "$shipTrophyGanEdenHypershuntGuard";
    public static final String GUARD_FACTION_KEY =
            "$shipTrophyGanEdenHypershuntGuardFaction";
    public static final float PIRATE_PRICE = 250000f;

    private static final String GUARD_TARGET_KEY =
            "$shipTrophyGanEdenHypershuntGuardTarget";
    private static final String GUARD_COMMITTED_KEY =
            "$shipTrophyGanEdenHypershuntGuardCommitted";
    private static final String TAP_GUARD_FACTION_KEY =
            "$shipTrophyGanEdenHypershuntFaction";
    private static final String TAP_GUARD_SPAWNED_KEY =
            "$shipTrophyGanEdenHypershuntGuardSpawned";
    private static final String TAP_GUARD_CLEARED_KEY =
            "$shipTrophyGanEdenHypershuntGuardCleared";
    private static final String TAP_SURVEYED_KEY =
            "$shipTrophyGanEdenHypershuntSurveyed";

    private static final int REQUIRED_HYPERSHUNTS = 2;
    private static final float GUARD_OFFSET = 450f;
    private static final float GUARD_DURATION = 1000000f;

    private GanEdenHypershuntManager() {
    }

    /** Ensures one Pather and one pirate blockade exist while needed. */
    public static void ensureEncounters() {
        if (Global.getSector() == null
                || GanEdenQuestManager.getStage()
                        != Stage.INVESTIGATE_HYPERSHUNTS) {
            return;
        }

        List<SectorEntityToken> taps = getQuestHypershunts();
        for (int index = 0; index < taps.size(); index++) {
            SectorEntityToken tap = taps.get(index);
            MemoryAPI memory = tap.getMemoryWithoutUpdate();
            String factionId = memory.getString(TAP_GUARD_FACTION_KEY);
            if (!Factions.LUDDIC_PATH.equals(factionId)
                    && !Factions.PIRATES.equals(factionId)) {
                factionId = index == 0
                        ? Factions.LUDDIC_PATH : Factions.PIRATES;
                memory.set(TAP_GUARD_FACTION_KEY, factionId);
            }
            ensureEncounter(tap, factionId);
        }
    }

    private static void ensureEncounter(
            SectorEntityToken tap, String factionId) {
        if (tap == null || tap.getContainingLocation() == null) return;
        MemoryAPI tapMemory = tap.getMemoryWithoutUpdate();
        CampaignFleetAPI guard = findGuard(tap);

        // Repairs a battle completed under an older build, and also provides
        // a second line of defense if another mod strips our fleet listener.
        if (guard != null && guard.getMemoryWithoutUpdate().getBoolean(
                MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER)) {
            markGuardDefeated(guard);
            removeGuard(guard);
            return;
        }

        if (tapMemory.getBoolean(TAP_SURVEYED_KEY)
                || tapMemory.getBoolean(TAP_GUARD_CLEARED_KEY)) {
            if (guard != null) removeGuard(guard);
            return;
        }

        if (guard != null && !guard.isEmpty()) {
            configureGuard(guard, tap, factionId);
            return;
        }
        if (guard != null) removeGuard(guard);

        // A fleet that existed and is now gone was defeated. Non-combat
        // resolutions set CLEARED before removing it and arrive here too.
        if (tapMemory.getBoolean(TAP_GUARD_SPAWNED_KEY)) {
            tapMemory.set(TAP_GUARD_CLEARED_KEY, true);
            return;
        }

        CampaignFleetAPI created = createGuard(tap, factionId);
        if (created != null && !created.isEmpty()) {
            tapMemory.set(TAP_GUARD_SPAWNED_KEY, true);
        } else {
            // Do not strand the quest if another mod has removed every usable
            // variant from one of the guarding factions.
            tapMemory.set(TAP_GUARD_CLEARED_KEY, true);
            System.err.println(
                    "Hall of Triumph: unable to create " + factionId
                            + " hypershunt blockade; approach left clear.");
        }
    }

    private static CampaignFleetAPI createGuard(
            SectorEntityToken tap, String factionId) {
        Vector2f locationInHyper = tap.getLocationInHyperspace();
        float combatPoints = Factions.LUDDIC_PATH.equals(factionId)
                ? 230f : 210f;
        Random random = new Random(stableKey(tap).hashCode()
                ^ factionId.hashCode() ^ 0x6879706572736875L);
        FleetParamsV3 params = new FleetParamsV3(
                null,
                locationInHyper,
                factionId,
                1f,
                FleetTypes.PATROL_LARGE,
                combatPoints,
                0f,
                0f,
                0f,
                0f,
                0f,
                0f);
        params.ignoreMarketFleetSizeMult = true;
        params.withOfficers = true;
        params.averageSMods = 1;
        params.maxNumShips = 30;
        params.random = random;
        if (Factions.LUDDIC_PATH.equals(factionId)
                && Global.getSector().getFaction(factionId) != null) {
            params.commander = Global.getSector().getFaction(factionId)
                    .createRandomPerson(FullName.Gender.MALE, random);
        }

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) return null;

        tap.getContainingLocation().addEntity(fleet);
        fleet.setLocation(
                tap.getLocation().x + GUARD_OFFSET,
                tap.getLocation().y);
        configureGuard(fleet, tap, factionId);
        return fleet;
    }

    private static void configureGuard(
            CampaignFleetAPI fleet,
            SectorEntityToken tap,
            String factionId) {
        fleet.setName(Factions.LUDDIC_PATH.equals(factionId)
                ? "Pather Blockade" : "Claimant Flotilla");
        fleet.setNoFactionInName(true);
        fleet.setNoAutoDespawn(true);
        fleet.addTag(Tags.STORY_CRITICAL);
        fleet.addTag(Tags.HAS_INTERACTION_DIALOG);
        ensureBattleListener(fleet);

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(GUARD_KEY, true);
        memory.set(GUARD_FACTION_KEY, factionId);
        memory.set(GUARD_TARGET_KEY, stableKey(tap));
        memory.set(MemFlags.STORY_CRITICAL, true);
        memory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        memory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        memory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        memory.set(MemFlags.MEMORY_KEY_NO_JUMP, true);

        boolean committed = memory.getBoolean(GUARD_COMMITTED_KEY);
        if (!committed) {
            memory.set(MemFlags.NON_HOSTILE_OVERRIDES_MAKE_HOSTILE, true);
            memory.set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
            memory.set(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE, true);
            memory.unset(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
            memory.unset(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
        } else {
            memory.unset(MemFlags.NON_HOSTILE_OVERRIDES_MAKE_HOSTILE);
            memory.unset(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE);
            memory.unset(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE);
            memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
            memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
            memory.set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        }

        fleet.clearAssignments();
        if (committed
                && Global.getSector() != null
                && Global.getSector().getPlayerFleet() != null) {
            fleet.addAssignment(
                    FleetAssignment.INTERCEPT,
                    Global.getSector().getPlayerFleet(),
                    GUARD_DURATION,
                    "moving to engage your fleet");
        } else {
            fleet.addAssignment(
                    FleetAssignment.ORBIT_PASSIVE,
                    tap,
                    GUARD_DURATION,
                    Factions.LUDDIC_PATH.equals(factionId)
                            ? "guarding forbidden ground"
                            : "holding a salvage claim");
        }
    }

    public static boolean isGuard(
            SectorEntityToken target, String factionId) {
        if (!(target instanceof CampaignFleetAPI)
                || GanEdenQuestManager.getStage()
                        != Stage.INVESTIGATE_HYPERSHUNTS) {
            return false;
        }
        MemoryAPI memory = target.getMemoryWithoutUpdate();
        return memory.getBoolean(GUARD_KEY)
                && !memory.getBoolean(GUARD_COMMITTED_KEY)
                && factionId.equals(memory.getString(GUARD_FACTION_KEY));
    }

    public static void prepareGuard(InteractionDialogAPI dialog) {
        if (dialog == null) return;
        SectorEntityToken target = dialog.getInteractionTarget();
        if (target instanceof CampaignFleetAPI) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) target;
            if (fleet.getCommander() != null) {
                dialog.getVisualPanel().showPersonInfo(fleet.getCommander());
            }
        }
        showIsaAsSecondPerson(dialog);
    }

    public static void prepareInvestigation(InteractionDialogAPI dialog) {
        if (dialog == null) return;
        dialog.getVisualPanel().hideSecondPerson();
        showIsa(dialog);
    }

    public static boolean canPayPirates() {
        return Global.getSector() != null
                && Global.getSector().getPlayerFleet() != null
                && Global.getSector().getPlayerFleet().getCargo()
                        .getCredits().get() >= PIRATE_PRICE;
    }

    public static boolean payAndClearGuard(InteractionDialogAPI dialog) {
        if (!canPayPirates()) return false;
        Global.getSector().getPlayerFleet().getCargo().getCredits()
                .subtract(PIRATE_PRICE);
        clearGuard(dialog);
        return true;
    }

    public static void clearGuard(InteractionDialogAPI dialog) {
        if (dialog == null) return;
        SectorEntityToken target = dialog.getInteractionTarget();
        SectorEntityToken tap = findTapForTarget(target);
        if (tap != null) {
            tap.getMemoryWithoutUpdate().set(TAP_GUARD_CLEARED_KEY, true);
        }
        if (target instanceof CampaignFleetAPI) {
            removeGuard((CampaignFleetAPI) target);
        }
    }

    public static void engageGuard(InteractionDialogAPI dialog) {
        if (dialog == null
                || !(dialog.getInteractionTarget() instanceof CampaignFleetAPI)
                || Global.getSector() == null
                || Global.getSector().getPlayerFleet() == null) {
            return;
        }

        CampaignFleetAPI fleet =
                (CampaignFleetAPI) dialog.getInteractionTarget();
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(GUARD_COMMITTED_KEY, true);
        memory.unset(MemFlags.NON_HOSTILE_OVERRIDES_MAKE_HOSTILE);
        memory.unset(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE);
        memory.unset(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE);
        memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        fleet.clearAssignments();
        fleet.addAssignment(
                FleetAssignment.INTERCEPT,
                Global.getSector().getPlayerFleet(),
                GUARD_DURATION,
                "moving to engage your fleet");
    }

    public static boolean isBlockedTap(SectorEntityToken target) {
        ensureEncounters();
        return isQuestTap(target)
                && !target.getMemoryWithoutUpdate()
                        .getBoolean(TAP_GUARD_CLEARED_KEY)
                && !target.getMemoryWithoutUpdate()
                        .getBoolean(TAP_SURVEYED_KEY);
    }

    public static boolean canInvestigate(SectorEntityToken target) {
        ensureEncounters();
        if (!isQuestTap(target)) return false;
        MemoryAPI memory = target.getMemoryWithoutUpdate();
        return memory.getBoolean(TAP_GUARD_CLEARED_KEY)
                && !memory.getBoolean(TAP_SURVEYED_KEY);
    }

    public static boolean isSurveyedTap(SectorEntityToken target) {
        return isQuestTap(target)
                && target.getMemoryWithoutUpdate()
                        .getBoolean(TAP_SURVEYED_KEY);
    }

    public static GanEdenLogSpec markCurrentTapSurveyed(
            InteractionDialogAPI dialog) {
        if (dialog == null) return null;
        SectorEntityToken tap = findTapForTarget(
                dialog.getInteractionTarget());
        if (tap == null || !isQuestTap(tap)) return null;
        int previousCount = getSurveyedCount();
        MemoryAPI memory = tap.getMemoryWithoutUpdate();
        memory.set(TAP_GUARD_CLEARED_KEY, true);
        memory.set(TAP_SURVEYED_KEY, true);
        GanEdenLogSpec recovered = previousCount <= 0
                ? GanEdenLogSpec.PART_TWO
                : GanEdenLogSpec.PART_THREE;
        GanEdenLogManager.recover(recovered, dialog.getTextPanel());
        GanEdenQuestManager.checkHypershunts();
        return recovered;
    }

    public static int getSurveyedCount() {
        int result = 0;
        for (SectorEntityToken tap : getQuestHypershunts()) {
            if (tap.getMemoryWithoutUpdate().getBoolean(TAP_SURVEYED_KEY)) {
                result++;
            }
        }
        return result;
    }

    public static int getRequiredCount() {
        return Math.min(REQUIRED_HYPERSHUNTS, getQuestHypershunts().size());
    }

    public static SectorEntityToken getFirstUnsurveyedHypershunt() {
        for (SectorEntityToken tap : getQuestHypershunts()) {
            if (!tap.getMemoryWithoutUpdate().getBoolean(TAP_SURVEYED_KEY)) {
                return tap;
            }
        }
        return null;
    }

    /** Preserves progress for saves that completed the old repair objective. */
    public static void markAllSurveyedForLegacySave() {
        for (SectorEntityToken tap : getQuestHypershunts()) {
            MemoryAPI memory = tap.getMemoryWithoutUpdate();
            memory.set(TAP_GUARD_CLEARED_KEY, true);
            memory.set(TAP_SURVEYED_KEY, true);
            CampaignFleetAPI guard = findGuard(tap);
            if (guard != null) removeGuard(guard);
        }
    }

    public static List<SectorEntityToken> getQuestHypershunts() {
        List<SectorEntityToken> result = new ArrayList<SectorEntityToken>();
        if (Global.getSector() == null) return result;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (SectorEntityToken entity : location.getAllEntities()) {
                if (entity != null
                        && GanEdenQuestManager.CORONAL_TAP_TYPE.equals(
                                entity.getCustomEntityType())) {
                    result.add(entity);
                }
            }
        }
        Collections.sort(result, new Comparator<SectorEntityToken>() {
            @Override
            public int compare(SectorEntityToken first, SectorEntityToken second) {
                return stableKey(first).compareTo(stableKey(second));
            }
        });
        if (result.size() > REQUIRED_HYPERSHUNTS) {
            return new ArrayList<SectorEntityToken>(
                    result.subList(0, REQUIRED_HYPERSHUNTS));
        }
        return result;
    }

    private static boolean isQuestTap(SectorEntityToken target) {
        if (target == null
                || GanEdenQuestManager.getStage()
                        != Stage.INVESTIGATE_HYPERSHUNTS) {
            return false;
        }
        String targetKey = stableKey(target);
        for (SectorEntityToken tap : getQuestHypershunts()) {
            if (targetKey.equals(stableKey(tap))) return true;
        }
        return false;
    }

    private static CampaignFleetAPI findGuard(SectorEntityToken tap) {
        if (tap == null || tap.getContainingLocation() == null) return null;
        String targetKey = stableKey(tap);
        for (CampaignFleetAPI fleet : tap.getContainingLocation().getFleets()) {
            MemoryAPI memory = fleet.getMemoryWithoutUpdate();
            if (memory.getBoolean(GUARD_KEY)
                    && targetKey.equals(memory.getString(GUARD_TARGET_KEY))) {
                return fleet;
            }
        }
        return null;
    }

    private static void ensureBattleListener(CampaignFleetAPI fleet) {
        if (fleet == null) return;
        for (FleetEventListener listener : fleet.getEventListeners()) {
            if (listener instanceof GuardBattleListener) return;
        }
        fleet.addEventListener(new GuardBattleListener());
    }

    private static void markGuardDefeated(CampaignFleetAPI guard) {
        if (guard == null) return;
        SectorEntityToken tap = findTapForTarget(guard);
        if (tap != null) {
            tap.getMemoryWithoutUpdate().set(
                    TAP_GUARD_CLEARED_KEY, true);
        }
        // The low-frequency quest script removes the surviving campaign fleet
        // as soon as the post-battle interaction closes. Most importantly,
        // the tap is already unblocked before the player can approach it.
        guard.getMemoryWithoutUpdate().set(
                TAP_GUARD_CLEARED_KEY, true);
        guard.setNoAutoDespawn(false);
    }

    /** Clears the entire blockade when the player wins its campaign battle. */
    public static final class GuardBattleListener
            implements FleetEventListener {
        @Override
        public void reportBattleOccurred(
                CampaignFleetAPI guard,
                CampaignFleetAPI primaryWinner,
                BattleAPI battle) {
            if (guard == null
                    || battle == null
                    || primaryWinner == null
                    || !guard.getMemoryWithoutUpdate().getBoolean(GUARD_KEY)
                    || !battle.isPlayerInvolved()
                    || !battle.isInvolved(guard)
                    || battle.onPlayerSide(guard)
                    || !battle.onPlayerSide(primaryWinner)) {
                return;
            }
            markGuardDefeated(guard);
        }

        @Override
        public void reportFleetDespawnedToListener(
                CampaignFleetAPI fleet,
                CampaignEventListener.FleetDespawnReason reason,
                Object param) {
            if (fleet != null) fleet.removeEventListener(this);
        }
    }

    private static SectorEntityToken findTapForTarget(
            SectorEntityToken target) {
        if (target == null) return null;
        if (GanEdenQuestManager.CORONAL_TAP_TYPE.equals(
                target.getCustomEntityType())) {
            return target;
        }
        String targetKey = target.getMemoryWithoutUpdate()
                .getString(GUARD_TARGET_KEY);
        if (targetKey == null || targetKey.isEmpty()) return null;
        for (SectorEntityToken tap : getQuestHypershunts()) {
            if (targetKey.equals(stableKey(tap))) return tap;
        }
        return null;
    }

    private static void removeGuard(CampaignFleetAPI guard) {
        if (guard == null || guard.getContainingLocation() == null) return;
        guard.getContainingLocation().removeEntity(guard);
    }

    private static String stableKey(SectorEntityToken entity) {
        if (entity == null) return "";
        String location = entity.getContainingLocation() == null
                ? "" : entity.getContainingLocation().getId();
        String id = entity.getId();
        if (id != null && !id.isEmpty()) return location + "/" + id;
        return location + "/" + entity.getName() + "/"
                + Math.round(entity.getLocation().x) + "/"
                + Math.round(entity.getLocation().y);
    }

    private static void showIsa(InteractionDialogAPI dialog) {
        if (dialog == null) return;
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(
                IsaTrophyManager.findHomeMarket());
        if (isa != null) dialog.getVisualPanel().showPersonInfo(isa);
    }

    private static void showIsaAsSecondPerson(InteractionDialogAPI dialog) {
        if (dialog == null) return;
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(
                IsaTrophyManager.findHomeMarket());
        if (isa != null) dialog.getVisualPanel().showSecondPerson(isa);
    }
}
