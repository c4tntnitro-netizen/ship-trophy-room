package shiptrophy.campaign;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.IsaTrophyManager;
import shiptrophy.ShipTrophyRoomIds;

/** Persistent state and world operations for Isa's Gan Eden quest. */
public final class GanEdenQuestManager {
    private static final String AT_THE_GATES_COMPLETED_KEY =
            "$gaATG_completed";
    private static final String AT_THE_GATES_MISSION_COMPLETED_KEY =
            "$gaATG_missionCompleted";

    public enum Stage {
        NOT_STARTED,
        ASK_AROUND_SHATTERED_RING,
        FIND_BLACK_MARKET_CLUE,
        INVESTIGATE_HYPERSHUNTS,
        GAN_EDEN_REVEALED,
        DEFEAT_GOLDEN_SHARDS,
        SPACE_ELEVATOR,
        COMPLETED
    }

    public static final String EXTERNAL_RING_ID =
            "ship_trophy_gan_eden_external_ring";
    public static final String EXTERNAL_RING_TYPE =
            "ship_trophy_gan_eden_external_ring";
    public static final String CORONAL_TAP_TYPE = "coronal_tap";
    public static final String CORONAL_TAP_USABLE_KEY = "$usable";
    private GanEdenQuestManager() {
    }

    public static Stage getStage() {
        if (Global.getSector() == null) return Stage.NOT_STARTED;
        String value = memory().getString(
                ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE);
        if (value == null || value.trim().isEmpty()) {
            return Stage.NOT_STARTED;
        }
        // These aliases preserve campaigns that began the shorter, pre-Epitaph
        // version of the quest.
        if ("CONTACT_GARGOYLE".equals(value)
                || "REACTIVATE_HYPERSHUNTS".equals(value)) {
            return Stage.INVESTIGATE_HYPERSHUNTS;
        }
        if ("GRAVE_FOUND".equals(value)
                || "EPITAPH_FOUND".equals(value)) {
            return GanEdenAmbushScript.isDefeated()
                    ? Stage.SPACE_ELEVATOR
                    : Stage.DEFEAT_GOLDEN_SHARDS;
        }
        try {
            return Stage.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return Stage.NOT_STARTED;
        }
    }

    public static boolean isAtLeast(Stage stage) {
        return getStage().ordinal() >= stage.ordinal();
    }

    public static boolean isStarted() {
        return getStage() != Stage.NOT_STARTED;
    }

    public static boolean isCompleted() {
        return getStage() == Stage.COMPLETED;
    }

    public static boolean isEpitaphFound() {
        return memory().getBoolean(
                ShipTrophyRoomIds.MEMORY_GAN_EDEN_EPITAPH_FOUND);
    }

    /**
     * Vanilla uses the mission-completed flag for its post-At-the-Gates
     * dialogue, while the final conversation also sets a second conclusion
     * flag. Accepting either keeps the prerequisite compatible with saves
     * produced by every legitimate ending path.
     */
    public static boolean isAtTheGatesCompleted() {
        if (Global.getSector() == null) return false;
        MemoryAPI memory = memory();
        return memory.getBoolean(AT_THE_GATES_COMPLETED_KEY)
                || memory.getBoolean(AT_THE_GATES_MISSION_COMPLETED_KEY);
    }

    /** Compatibility alias for the first implementation of the finale. */
    public static boolean isGraveFound() {
        return isEpitaphFound();
    }

    /** Starts the rewritten investigation at the end of Isa's homecoming. */
    public static void start(TextPanelAPI textPanel) {
        if (Global.getSector() == null
                || !isAtTheGatesCompleted()
                || isStarted()) {
            return;
        }
        GanEdenLogManager.recover(GanEdenLogSpec.PART_ONE, textPanel);
        setStage(Stage.INVESTIGATE_HYPERSHUNTS, textPanel, true);
        GanEdenHypershuntManager.ensureEncounters();
    }

    public static boolean canAskAroundShatteredRing() {
        return false;
    }

    public static void revealBlackMarketClue(TextPanelAPI textPanel) {
        if (getStage() == Stage.ASK_AROUND_SHATTERED_RING) {
            setStage(Stage.FIND_BLACK_MARKET_CLUE, textPanel, false);
        }
    }

    public static void beginHypershuntSearch(TextPanelAPI textPanel) {
        Stage stage = getStage();
        if (stage != Stage.ASK_AROUND_SHATTERED_RING
                && stage != Stage.FIND_BLACK_MARKET_CLUE) {
            return;
        }
        setStage(Stage.INVESTIGATE_HYPERSHUNTS, textPanel, false);
        GanEdenHypershuntManager.ensureEncounters();
    }

    /** Retrofits campaigns that played the homecoming before this quest existed. */
    public static void ensureForCurrentSave() {
        if (Global.getSector() == null) return;
        removeLegacyExternalRing();
        migrateLegacyStage();
        if (!isStarted()
                && IsaTrophyManager.wasShatteredRingHomecomingShown()) {
            start(null);
        }
        if (isStarted()) ensureIntel(null, false);
        // The pre-wafer version of the quest used a unique black-market
        // receiver. Retire it from both the market and player cargo without
        // breaking saves that had already acquired it.
        GanEdenClueManager.ensureStock();
        if (GanEdenClueManager.playerHasClue()) {
            GanEdenClueManager.consumePlayerClue();
        }
        if (getStage() == Stage.INVESTIGATE_HYPERSHUNTS) {
            GanEdenHypershuntManager.ensureEncounters();
        }
        if (isAtLeast(Stage.GAN_EDEN_REVEALED)) {
            ensureGateway();
            GanEdenAmbushScript.ensureFleet();
        }
        completeIfReady();
    }

    private static void migrateLegacyStage() {
        String raw = memory().getString(
                ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE);
        if (Stage.ASK_AROUND_SHATTERED_RING.name().equals(raw)
                || Stage.FIND_BLACK_MARKET_CLUE.name().equals(raw)) {
            memory().set(
                    ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE,
                    Stage.INVESTIGATE_HYPERSHUNTS.name());
            GanEdenLogManager.recover(GanEdenLogSpec.PART_ONE, null);
            return;
        }
        if ("CONTACT_GARGOYLE".equals(raw)) {
            memory().set(
                    ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE,
                    Stage.INVESTIGATE_HYPERSHUNTS.name());
            return;
        }
        if ("REACTIVATE_HYPERSHUNTS".equals(raw)) {
            memory().set(
                    ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE,
                    Stage.INVESTIGATE_HYPERSHUNTS.name());
            if (getRepairedHypershuntCount() >= 2) {
                GanEdenHypershuntManager.markAllSurveyedForLegacySave();
                checkHypershunts();
            }
            return;
        }
        if ("GRAVE_FOUND".equals(raw) || "EPITAPH_FOUND".equals(raw)) {
            memory().set(
                    ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE,
                    (GanEdenAmbushScript.isDefeated()
                            ? Stage.SPACE_ELEVATOR
                            : Stage.DEFEAT_GOLDEN_SHARDS).name());
        }
    }

    public static void checkHypershunts() {
        if (getStage() != Stage.INVESTIGATE_HYPERSHUNTS) return;
        int required = GanEdenHypershuntManager.getRequiredCount();
        if (required <= 0
                || GanEdenHypershuntManager.getSurveyedCount() < required) {
            return;
        }

        setStage(Stage.GAN_EDEN_REVEALED, null, false);
        ensureGateway();
        GanEdenAmbushScript.ensureFleet();
    }

    public static void markTreeLogFound(TextPanelAPI textPanel) {
        if (!isAtLeast(Stage.GAN_EDEN_REVEALED) || isCompleted()) return;
        if (GanEdenAmbushScript.isDefeated()) {
            setStage(Stage.SPACE_ELEVATOR, textPanel, false);
        } else {
            setStage(Stage.DEFEAT_GOLDEN_SHARDS, textPanel, false);
        }
        GanEdenGenerator.ensureGenerated();
    }

    public static void markEpitaphFound(TextPanelAPI textPanel) {
        if (getStage() != Stage.SPACE_ELEVATOR
                || !GanEdenAmbushScript.isDefeated()
                || isCompleted()) {
            return;
        }
        memory().set(ShipTrophyRoomIds.MEMORY_GAN_EDEN_EPITAPH_FOUND, true);
        complete(textPanel);
    }

    /** Compatibility alias retained for older rule packs and saves. */
    public static void markGraveFound(TextPanelAPI textPanel) {
        markEpitaphFound(textPanel);
    }

    public static void completeIfReady() {
        if (isCompleted()) return;
        if (getStage() == Stage.GAN_EDEN_REVEALED
                && GanEdenLogManager.isRecovered(GanEdenLogSpec.PART_FOUR)) {
            markTreeLogFound(null);
            return;
        }
        if (getStage() == Stage.DEFEAT_GOLDEN_SHARDS
                && GanEdenAmbushScript.isDefeated()) {
            setStage(Stage.SPACE_ELEVATOR, null, false);
            GanEdenGenerator.ensureGenerated();
            return;
        }
        // Old saves may already carry the former all-at-once Epitaph flag.
        if (getStage() == Stage.SPACE_ELEVATOR && isEpitaphFound()) {
            complete(null);
        }
    }

    private static void complete(TextPanelAPI textPanel) {
        setStage(Stage.COMPLETED, textPanel, false);
    }

    private static void setStage(
            Stage stage, TextPanelAPI textPanel, boolean newIntel) {
        memory().set(
                ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE,
                stage.name());
        GanEdenQuestIntel intel = ensureIntel(textPanel, newIntel);
        if (intel != null && !newIntel) {
            intel.sendUpdateIfPlayerHasIntel(stage, false);
        }
    }

    public static GanEdenQuestIntel ensureIntel(
            TextPanelAPI textPanel, boolean announceNew) {
        if (Global.getSector() == null || !isStarted()) return null;
        List<IntelInfoPlugin> entries = Global.getSector().getIntelManager()
                .getIntel(GanEdenQuestIntel.class);
        for (IntelInfoPlugin entry : entries) {
            if (entry instanceof GanEdenQuestIntel) {
                return (GanEdenQuestIntel) entry;
            }
        }

        GanEdenQuestIntel intel = new GanEdenQuestIntel();
        intel.setImportant(true);
        if (textPanel != null) {
            Global.getSector().getIntelManager().addIntel(
                    intel, !announceNew, textPanel);
        } else {
            Global.getSector().getIntelManager().addIntel(intel, !announceNew);
        }
        return intel;
    }

    public static int getRepairedHypershuntCount() {
        int count = 0;
        if (Global.getSector() == null) return count;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (SectorEntityToken entity : location.getAllEntities()) {
                if (isVanillaCoronalTap(entity)
                        && entity.getMemoryWithoutUpdate()
                                .getBoolean(CORONAL_TAP_USABLE_KEY)) {
                    count++;
                }
            }
        }
        return Math.min(2, count);
    }

    public static SectorEntityToken getFirstUnrepairedHypershunt() {
        if (Global.getSector() == null) return null;
        SectorEntityToken repaired = null;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (SectorEntityToken entity : location.getAllEntities()) {
                if (!isVanillaCoronalTap(entity)) continue;
                if (!entity.getMemoryWithoutUpdate()
                        .getBoolean(CORONAL_TAP_USABLE_KEY)) {
                    return entity;
                }
                repaired = entity;
            }
        }
        return repaired;
    }

    private static boolean isVanillaCoronalTap(SectorEntityToken entity) {
        return entity != null
                && CORONAL_TAP_TYPE.equals(entity.getCustomEntityType());
    }

    /** Unlocks the solitary power-transit Gate and the Eden-side ring. */
    public static SectorEntityToken ensureGateway() {
        if (Global.getSector() == null
                || !isAtLeast(Stage.GAN_EDEN_REVEALED)) {
            return null;
        }

        removeLegacyExternalRing();

        SectorEntityToken external =
                GanEdenTransitSystemGenerator.ensureGenerated();
        if (external != null) {
            external.setDiscoverable(null);
            external.setSensorProfile(null);
            external.getMemoryWithoutUpdate().set(
                    GateEntityPlugin.GATE_SCANNED, true);
        }

        StarSystemAPI system = GanEdenGenerator.findSystem();
        if (system != null) {
            SectorEntityToken internal = system.getEntityById(
                    GanEdenGenerator.ARRIVAL_RING_ID);
            if (internal != null) {
                // Match CoreDiscoverEntityPlugin's completed-discovery state
                // so the ring stays known without firing a discovery message.
                internal.setDiscoverable(null);
                internal.setSensorProfile(null);
                internal.addTag(Tags.GATE);
                internal.addTag(Tags.STORY_CRITICAL);
                internal.addTag(Tags.HAS_INTERACTION_DIALOG);
                internal.removeTag(Tags.NON_CLICKABLE);
                internal.removeTag(Tags.NO_ENTITY_TOOLTIP);
                internal.getMemoryWithoutUpdate().set(
                        GateEntityPlugin.GATE_SCANNED, true);
            }
        }
        return external;
    }

    private static void removeLegacyExternalRing() {
        if (Global.getSector() == null) return;
        LocationAPI hyperspace = Global.getSector().getHyperspace();
        SectorEntityToken external = hyperspace.getEntityById(EXTERNAL_RING_ID);
        if (external == null) return;

        GateEntityPlugin.getGateData().scanned.remove(external);
        hyperspace.removeEntity(external);
    }

    public static SectorEntityToken getExternalRing() {
        if (Global.getSector() == null) return null;
        StarSystemAPI system = GanEdenTransitSystemGenerator.findSystem();
        return system == null ? null : system.getEntityById(EXTERNAL_RING_ID);
    }

    public static boolean canTransitFromGate(SectorEntityToken source) {
        return false;
    }

    public static SectorEntityToken getNearestKnownGate() {
        if (Global.getSector() == null) return null;
        SectorEntityToken nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        for (SectorEntityToken gate
                : GateEntityPlugin.getGateData().scanned) {
            if (gate == null
                    || gate.getContainingLocation() == null
                    || GanEdenGenerator.ARRIVAL_RING_ID.equals(gate.getId())
                    || EXTERNAL_RING_ID.equals(gate.getId())) {
                continue;
            }
            float distance = Misc.getDistanceToPlayerLY(gate);
            if (nearest == null || distance < nearestDistance) {
                nearest = gate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    /**
     * Safety destination for legacy/console entries that have no recorded
     * origin and no scanned Gate. Prefer an ordinary vanilla Gate near the
     * Shattered Ring rather than leaving the player trapped in Gan Eden.
     */
    private static SectorEntityToken getFallbackGate() {
        if (Global.getSector() == null) return null;
        SectorEntityToken shatteredRing = getShatteredRing();
        SectorEntityToken nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (SectorEntityToken entity : location.getAllEntities()) {
                if (entity == null
                        || entity.getContainingLocation() == null
                        || !Entities.INACTIVE_GATE.equals(
                                entity.getCustomEntityType())
                        || GanEdenGenerator.ARRIVAL_RING_ID.equals(
                                entity.getId())
                        || EXTERNAL_RING_ID.equals(entity.getId())) {
                    continue;
                }
                float distance = shatteredRing == null
                        ? 0f
                        : Misc.getDistanceLY(entity, shatteredRing);
                if (nearest == null || distance < nearestDistance) {
                    nearest = entity;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    public static SectorEntityToken getInternalRing() {
        StarSystemAPI system = GanEdenGenerator.findSystem();
        return system == null ? null
                : system.getEntityById(GanEdenGenerator.ARRIVAL_RING_ID);
    }

    public static SectorEntityToken getShatteredRing() {
        if (Global.getSector() == null) return null;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            SectorEntityToken ring = location.getEntityById(
                    ShatteredRingGenerator.ENTITY_ID);
            if (ring != null) return ring;
        }
        return null;
    }

    public static void transitIntoGanEden(SectorEntityToken source) {
        SectorEntityToken destination = getInternalRing();
        transition(source, destination, "Gan Eden");
    }

    public static void transitOutOfGanEden(SectorEntityToken source) {
        SectorEntityToken destination = getExternalRing();
        if (destination == null) destination = getNearestKnownGate();
        if (destination == null) destination = getFallbackGate();
        transition(source, destination, "Power Transit Gate");
    }

    private static void transition(
            SectorEntityToken source,
            SectorEntityToken destination,
            String label) {
        if (Global.getSector() == null || destination == null) return;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null) return;
        Global.getSector().doHyperspaceTransition(
                player,
                source == null ? player : source,
                new JumpPointAPI.JumpDestination(destination, label),
                5f);
    }

    private static MemoryAPI memory() {
        return Global.getSector().getMemoryWithoutUpdate();
    }
}
