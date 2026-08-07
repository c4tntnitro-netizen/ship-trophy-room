package shiptrophy.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import shiptrophy.IsaTrophyManager;
import shiptrophy.campaign.GanEdenQuestManager.Stage;
import shiptrophy.hullmods.HypershuntMkIVPaint;

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
    private static final String GUARD_DEFEATED_KEY =
            "$shipTrophyGanEdenHypershuntGuardDefeated";
    private static final String GUARD_SIGNATURE_APPLIED_KEY =
            "$shipTrophyGanEdenHypershuntSignatureApplied";
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
    private static final String PATHER_GUARD_NAME = "Order of Sanguinius";
    private static final String PIRATE_GUARD_NAME = "Tiger Shark Raiders";
    private static final String PATHER_SIGNATURE_HULL =
            "ship_trophy_mk4_pather_prometheus2";
    private static final String PATHER_SIGNATURE_VARIANT =
            PATHER_SIGNATURE_HULL + "_Standard";
    private static final String PIRATE_SIGNATURE_HULL =
            "ship_trophy_mk4_pirate_atlas2";
    private static final String PIRATE_SIGNATURE_VARIANT =
            PIRATE_SIGNATURE_HULL + "_Standard";
    private static final Map<String, String> PIRATE_MK_IV_SKINS;
    private static final Map<String, String> PATHER_MK_IV_SKINS;

    static {
        Map<String, String> pirates = new HashMap<String, String>();
        pirates.put("vanguard_pirates",
                "ship_trophy_mk4_pirate_vanguard");
        pirates.put("manticore_pirates",
                "ship_trophy_mk4_pirate_manticore");
        pirates.put("falcon_p", "ship_trophy_mk4_pirate_falcon");
        pirates.put("eradicator_pirates",
                "ship_trophy_mk4_pirate_eradicator");
        pirates.put("atlas2", "ship_trophy_mk4_pirate_atlas2");
        PIRATE_MK_IV_SKINS = Collections.unmodifiableMap(pirates);

        Map<String, String> pathers = new HashMap<String, String>();
        pathers.put("enforcer", "ship_trophy_mk4_pather_enforcer");
        pathers.put("hammerhead", "ship_trophy_mk4_pather_hammerhead");
        pathers.put("manticore_luddic_path",
                "ship_trophy_mk4_pather_manticore");
        pathers.put("eradicator", "ship_trophy_mk4_pather_eradicator");
        pathers.put("sunder", "ship_trophy_mk4_pather_sunder");
        pathers.put("prometheus2",
                "ship_trophy_mk4_pather_prometheus2");
        PATHER_MK_IV_SKINS = Collections.unmodifiableMap(pathers);
    }

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
        if (guard != null && (guard.getMemoryWithoutUpdate().getBoolean(
                GUARD_DEFEATED_KEY)
                || guard.getMemoryWithoutUpdate().getBoolean(
                        MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER))) {
            markGuardDefeated(guard);
            removeGuard(guard);
            return;
        }

        if (tapMemory.getBoolean(TAP_SURVEYED_KEY)
                || tapMemory.getBoolean(TAP_GUARD_CLEARED_KEY)) {
            // Combat victories are removed by the battle listener. A fleet
            // persuaded or paid to stand aside is released intact instead;
            // never turn a peaceful resolution into a synthetic defeat.
            if (guard != null) releaseGuard(guard, tap);
            return;
        }

        if (guard != null && !guard.isEmpty()) {
            configureGuard(guard, tap, factionId);
            return;
        }
        if (guard != null) removeGuard(guard);

        // A fleet that existed and is now gone was defeated or otherwise
        // removed. Peaceful resolutions set CLEARED before releasing it and
        // therefore return above without entering this fallback.
        if (tapMemory.getBoolean(TAP_GUARD_SPAWNED_KEY)) {
            tapMemory.set(TAP_GUARD_CLEARED_KEY, true);
            return;
        }

        CampaignFleetAPI created = createGuard(tap, factionId);
        if (created != null && !created.isEmpty()) {
            configureGuard(created, tap, factionId);
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
        // Put the authored capital through FleetFactoryV3's normal flagship
        // path. Vanilla applies the requested variant before it performs its
        // final fleet-wide readiness/provisioning pass; appending this ship
        // after createFleet() left it outside that initialization.
        params.flagshipVariantId = Factions.LUDDIC_PATH.equals(factionId)
                ? PATHER_SIGNATURE_VARIANT : PIRATE_SIGNATURE_VARIANT;
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
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        applyMkIVRefit(fleet, factionId);
        if (!memory.getBoolean(GUARD_SIGNATURE_APPLIED_KEY)
                && ensureSignatureMkIV(fleet, factionId)) {
            // Do not replenish battle losses. This key guarantees the authored
            // starting composition once and also upgrades older saved fleets.
            memory.set(GUARD_SIGNATURE_APPLIED_KEY, true);
        }
        fleet.setName(Factions.LUDDIC_PATH.equals(factionId)
                ? PATHER_GUARD_NAME : PIRATE_GUARD_NAME);
        fleet.setNoFactionInName(true);
        fleet.setNoAutoDespawn(true);
        // These story fleets hold position inside a stellar corona for an
        // arbitrary length of time. Use vanilla's scripted-fleet exemption
        // so they enter combat at their intended readiness instead of being
        // drained to 0 CR while orbiting the hypershunt.
        fleet.addTag(Tags.FLEET_IGNORES_CORONA);
        fleet.addTag(Tags.STORY_CRITICAL);
        fleet.addTag(Tags.HAS_INTERACTION_DIALOG);
        ensureBattleListener(fleet);

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

    /** Selects only predeclared Mk IV variants for quest guards. */
    private static void applyMkIVRefit(
            CampaignFleetAPI fleet, String factionId) {
        if (fleet == null) return;

        boolean changed = false;
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member == null
                    || member.isFighterWing()
                    || member.getVariant() == null) continue;
            changed |= refitMemberAsMkIV(member, factionId);
        }

        if (changed) {
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        }
    }

    /** Adds the faction's pre-rendered capital Mk IV exactly once. */
    private static boolean ensureSignatureMkIV(
            CampaignFleetAPI fleet, String factionId) {
        if (fleet == null) return false;
        boolean pather = Factions.LUDDIC_PATH.equals(factionId);
        if (!pather && !Factions.PIRATES.equals(factionId)) return false;

        String hullId = pather
                ? PATHER_SIGNATURE_HULL : PIRATE_SIGNATURE_HULL;
        String variantId = pather
                ? PATHER_SIGNATURE_VARIANT : PIRATE_SIGNATURE_VARIANT;
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member != null
                    && !member.isFighterWing()
                    && member.getVariant() != null
                    && member.getVariant().getHullSpec() != null
                    && hullId.equals(member.getVariant()
                            .getHullSpec().getHullId())) {
                return true;
            }
        }

        // Use only the predeclared variant tied to a static .skin hull.
        if (Global.getSettings().getVariant(variantId) == null) {
            System.err.println(
                    "Hall of Triumph: missing signature Mk IV variant "
                            + variantId + ".");
            return false;
        }
        FleetMemberAPI member = fleet.getFleetData().addFleetMember(variantId);
        if (member == null) return false;
        member.getRepairTracker().setMothballed(false);
        // This is only the compatibility fallback for a guard already stored
        // in an older save. Refresh stats before querying the new member's
        // maximum CR, matching the ordering expected by the repair tracker.
        member.updateStats();
        member.getRepairTracker().setCR(
                member.getRepairTracker().getMaxCR());
        fleet.getFleetData().sort();
        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();
        fleet.forceSync();
        return true;
    }

    /** Shared by the story blockades and post-victory faction-pool rolls. */
    static boolean refitMemberAsMkIV(
            FleetMemberAPI member, String factionId) {
        if (member == null || member.isFighterWing()
                || member.getVariant() == null) return false;
        boolean pather = Factions.LUDDIC_PATH.equals(factionId);
        if (!pather && !Factions.PIRATES.equals(factionId)) return false;

        ShipVariantAPI current = member.getVariant();
        String currentHullId = current.getHullSpec().getHullId();
        String mkIVHullId = getMkIVHullId(currentHullId, factionId);
        String mkIVVariantId = getMkIVVariantId(currentHullId, factionId);
        boolean legacy = hasLegacyMkIVMarker(current);

        if (mkIVHullId == null || mkIVVariantId == null) return false;
        if (mkIVHullId.equals(currentHullId)
                && isCompleteMkIV(current, factionId)
                && !legacy) {
            return false;
        }

        ShipVariantAPI preset = Global.getSettings().getVariant(mkIVVariantId);
        if (preset == null) return false;
        member.setVariant(preset.clone(), false, false);
        member.updateStats();
        return true;
    }

    static boolean canRefitMemberAsMkIV(
            FleetMemberAPI member, String factionId) {
        if (member == null || member.isFighterWing()
                || member.getVariant() == null
                || member.getVariant().getHullSpec() == null) {
            return false;
        }
        return getMkIVHullId(
                member.getVariant().getHullSpec().getHullId(), factionId)
                != null;
    }

    static void migrateLegacyMkIVMembers(
            CampaignFleetAPI fleet, String factionId) {
        if (fleet == null) return;
        boolean changed = false;
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member != null && member.getVariant() != null
                    && hasLegacyMkIVMarker(member.getVariant())) {
                changed |= refitMemberAsMkIV(member, factionId);
            }
        }
        if (changed) {
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
            fleet.forceSync();
        }
    }

    private static boolean isCompleteMkIV(
            ShipVariantAPI variant, String factionId) {
        String currentHullId = variant.getHullSpec().getHullId();
        return isStaticMkIVHull(currentHullId, factionId)
                && variant.hasHullMod(HullMods.UNSTABLE_INJECTOR)
                && variant.hasHullMod(HullMods.HEAVYARMOR);
    }

    private static String getMkIVHullId(
            String hullId, String factionId) {
        if (hullId == null) return null;
        Map<String, String> skins = Factions.LUDDIC_PATH.equals(factionId)
                ? PATHER_MK_IV_SKINS
                : Factions.PIRATES.equals(factionId)
                        ? PIRATE_MK_IV_SKINS : null;
        if (skins == null) return null;
        if (skins.containsValue(hullId)) return hullId;
        return skins.get(hullId);
    }

    private static boolean isStaticMkIVHull(
            String hullId, String factionId) {
        String mapped = getMkIVHullId(hullId, factionId);
        return hullId != null && hullId.equals(mapped);
    }

    private static String getMkIVVariantId(
            String hullId, String factionId) {
        String mappedHullId = getMkIVHullId(hullId, factionId);
        return mappedHullId == null ? null : mappedHullId + "_Standard";
    }

    private static boolean hasLegacyMkIVMarker(ShipVariantAPI variant) {
        return variant != null
                && (variant.hasTag(HypershuntMkIVPaint.PATH_TAG)
                        || variant.hasTag(HypershuntMkIVPaint.PIRATE_TAG)
                        || variant.hasHullMod(
                                HypershuntMkIVPaint.HULLMOD_ID));
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
            releaseGuard((CampaignFleetAPI) target, tap);
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
        // The dialogue pages the recovered record itself. Filing it silently
        // prevents the Intel card from dumping the entire entry a second time.
        GanEdenLogManager.recoverSilently(recovered);
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

    private static CampaignFleetAPI findFormerGuard(
            SectorEntityToken tap, String factionId) {
        if (tap == null || tap.getContainingLocation() == null) return null;
        String targetKey = stableKey(tap);
        for (CampaignFleetAPI fleet : tap.getContainingLocation().getFleets()) {
            MemoryAPI memory = fleet.getMemoryWithoutUpdate();
            if (targetKey.equals(memory.getString(GUARD_TARGET_KEY))
                    && factionId.equals(memory.getString(
                            GUARD_FACTION_KEY))) {
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
        MemoryAPI guardMemory = guard.getMemoryWithoutUpdate();
        MkIVFleetIntegrationListener.unlockFaction(
                guardMemory.getString(GUARD_FACTION_KEY));
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
        guard.getMemoryWithoutUpdate().set(GUARD_DEFEATED_KEY, true);
        guard.setNoAutoDespawn(false);
    }

    /**
     * Recovers unlocks for saves that defeated a blockade before faction-pool
     * propagation was introduced. Peacefully released guards remain in their
     * original system with their target key, so they are not mistaken for a
     * combat victory.
     */
    public static void restoreMkIVUnlocksForLegacySave() {
        if (Global.getSector() == null) return;
        for (SectorEntityToken tap : getQuestHypershunts()) {
            MemoryAPI memory = tap.getMemoryWithoutUpdate();
            if (!memory.getBoolean(TAP_GUARD_SPAWNED_KEY)
                    || !memory.getBoolean(TAP_GUARD_CLEARED_KEY)) {
                continue;
            }
            String factionId = memory.getString(TAP_GUARD_FACTION_KEY);
            if (!Factions.PIRATES.equals(factionId)
                    && !Factions.LUDDIC_PATH.equals(factionId)) {
                continue;
            }
            if (MkIVFleetIntegrationListener.isUnlocked(factionId)) continue;

            CampaignFleetAPI former = findFormerGuard(tap, factionId);
            if (former == null
                    || former.getMemoryWithoutUpdate().getBoolean(
                            GUARD_DEFEATED_KEY)
                    || former.getMemoryWithoutUpdate().getBoolean(
                            MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER)) {
                MkIVFleetIntegrationListener.unlockFaction(factionId);
            }
        }
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
                    || !guard.getMemoryWithoutUpdate().getBoolean(GUARD_KEY)
                    || !battle.isPlayerInvolved()
                    || !battle.isInvolved(guard)
                    || battle.onPlayerSide(guard)) {
                return;
            }
            if (primaryWinner == null
                    || !battle.onPlayerSide(primaryWinner)) return;
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

    /**
     * Opens the approach without deleting the peacefully resolved fleet.
     * The ships visibly leave under their own power and despawn only after
     * reaching a point well clear of the hypershunt.
     */
    private static void releaseGuard(
            CampaignFleetAPI guard, SectorEntityToken tap) {
        if (guard == null || tap == null
                || guard.getContainingLocation() == null) return;

        // Do not mutate guard.getBattle() here. The vanilla fleet-interaction
        // plugin owns that BattleAPI and calls cleanUpBattle() after the rule
        // conversation returns to its encounter menu. Leaving either side by
        // hand creates a half-detached battle that can be reused as a ghost
        // encounter on the next contact.
        for (FleetEventListener listener
                : new ArrayList<FleetEventListener>(
                        guard.getEventListeners())) {
            if (listener instanceof GuardBattleListener) {
                guard.removeEventListener(listener);
            }
        }

        MemoryAPI memory = guard.getMemoryWithoutUpdate();
        memory.unset(GUARD_KEY);
        memory.unset(GUARD_COMMITTED_KEY);
        memory.unset(MemFlags.STORY_CRITICAL);
        memory.unset(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        memory.unset(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
        memory.unset(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE);
        memory.set(MemFlags.NON_HOSTILE_OVERRIDES_MAKE_HOSTILE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE, true);
        memory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        memory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);

        guard.removeTag(Tags.STORY_CRITICAL);
        guard.removeTag(Tags.HAS_INTERACTION_DIALOG);
        // Keep the peacefully resolved ships alive. They move clear and hold
        // position as a neutral fleet rather than despawning through a path
        // that other mods may interpret as a combat defeat.
        guard.setNoAutoDespawn(true);

        float dx = guard.getLocation().x - tap.getLocation().x;
        float dy = guard.getLocation().y - tap.getLocation().y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 1f) {
            dx = Factions.LUDDIC_PATH.equals(
                    memory.getString(GUARD_FACTION_KEY)) ? -1f : 1f;
            dy = 0f;
            length = 1f;
        }
        float distance = 2400f;
        SectorEntityToken destination = guard.getContainingLocation()
                .createToken(
                        tap.getLocation().x + dx / length * distance,
                        tap.getLocation().y + dy / length * distance);

        guard.clearAssignments();
        guard.addAssignment(
                FleetAssignment.GO_TO_LOCATION,
                destination,
                30f,
                "leaving the approach corridor");
        guard.addAssignment(
                FleetAssignment.ORBIT_PASSIVE,
                destination,
                GUARD_DURATION,
                "standing down clear of the hypershunt");
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
