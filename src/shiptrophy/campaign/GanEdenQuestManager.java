package shiptrophy.campaign;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import shiptrophy.IsaTrophyManager;
import shiptrophy.ShipTrophyRoomIds;

/** Persistent state and world operations for Isa's Gan Eden quest. */
public final class GanEdenQuestManager {
    public enum Stage {
        NOT_STARTED,
        CONTACT_GARGOYLE,
        REACTIVATE_HYPERSHUNTS,
        GAN_EDEN_REVEALED,
        GRAVE_FOUND,
        COMPLETED
    }

    public static final String EXTERNAL_RING_ID =
            "ship_trophy_gan_eden_external_ring";
    public static final String EXTERNAL_RING_TYPE =
            "ship_trophy_gan_eden_external_ring";
    public static final String CORONAL_TAP_TYPE = "coronal_tap";
    public static final String CORONAL_TAP_USABLE_KEY = "$usable";
    public static final String AT_THE_GATES_COMPLETED_KEY =
            "$gaATG_completed";

    private GanEdenQuestManager() {
    }

    public static Stage getStage() {
        if (Global.getSector() == null) return Stage.NOT_STARTED;
        String value = memory().getString(
                ShipTrophyRoomIds.MEMORY_GAN_EDEN_QUEST_STAGE);
        if (value == null || value.trim().isEmpty()) {
            return Stage.NOT_STARTED;
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

    public static boolean isGraveFound() {
        return memory().getBoolean(
                ShipTrophyRoomIds.MEMORY_GAN_EDEN_GRAVE_FOUND);
    }

    /** Vanilla sets this at the final conclusion of "At the Gates". */
    public static boolean isAtTheGatesCompleted() {
        return Global.getSector() != null
                && memory().getBoolean(AT_THE_GATES_COMPLETED_KEY);
    }

    /** Starts the quest at the end of Isa's Shattered Ring homecoming. */
    public static void start(TextPanelAPI textPanel) {
        if (Global.getSector() == null
                || isStarted()
                || !isAtTheGatesCompleted()) {
            return;
        }
        setStage(Stage.CONTACT_GARGOYLE, textPanel, true);
    }

    /** Retrofits campaigns that played the homecoming before this quest existed. */
    public static void ensureForCurrentSave() {
        if (Global.getSector() == null) return;
        if (!isStarted()
                && IsaTrophyManager.wasShatteredRingHomecomingShown()) {
            start(null);
        }
        if (isStarted()) ensureIntel(null, false);
        if (isAtLeast(Stage.GAN_EDEN_REVEALED)) ensureGateway();
    }

    public static void finishGargoyleInvestigation(TextPanelAPI textPanel) {
        if (getStage() != Stage.CONTACT_GARGOYLE) return;
        setStage(Stage.REACTIVATE_HYPERSHUNTS, textPanel, false);
        checkHypershunts();
    }

    public static void checkHypershunts() {
        if (getStage() != Stage.REACTIVATE_HYPERSHUNTS) return;
        if (getRepairedHypershuntCount() < 2) return;

        setStage(Stage.GAN_EDEN_REVEALED, null, false);
        ensureGateway();
        GanEdenAmbushScript.ensureFleet();
    }

    public static void markGraveFound(TextPanelAPI textPanel) {
        if (!isAtLeast(Stage.GAN_EDEN_REVEALED) || isCompleted()) return;
        memory().set(ShipTrophyRoomIds.MEMORY_GAN_EDEN_GRAVE_FOUND, true);
        // The Golden Shards remain dormant until Isaac's memorial answers
        // Isa. Reconfiguring the existing fleet here makes the battle the
        // normal final beat of the quest.
        GanEdenAmbushScript.ensureFleet();
        if (GanEdenAmbushScript.isDefeated()) {
            complete(textPanel);
        } else {
            setStage(Stage.GRAVE_FOUND, textPanel, false);
        }
    }

    public static void completeIfReady() {
        if (isCompleted() || !isGraveFound()) return;
        if (GanEdenAmbushScript.isDefeated()) complete(null);
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

    /** Creates and reveals the remote transit ring once triangulation succeeds. */
    public static SectorEntityToken ensureGateway() {
        if (Global.getSector() == null
                || !isAtLeast(Stage.GAN_EDEN_REVEALED)) {
            return null;
        }

        LocationAPI hyperspace = Global.getSector().getHyperspace();
        SectorEntityToken external = hyperspace.getEntityById(EXTERNAL_RING_ID);
        if (external == null) {
            external = hyperspace.addCustomEntity(
                    EXTERNAL_RING_ID,
                    "Gan Eden Transit Ring",
                    EXTERNAL_RING_TYPE,
                    Factions.NEUTRAL);
        }
        if (external != null) {
            float[] location = getOrCreateRingLocation();
            external.setFixedLocation(location[0], location[1]);
            external.setDiscoverable(true);
            external.setSensorProfile(250f);
            external.addTag(Tags.GATE);
            external.addTag(Tags.STORY_CRITICAL);
            external.addTag(Tags.HAS_INTERACTION_DIALOG);
            external.removeTag(Tags.NON_CLICKABLE);
            external.removeTag(Tags.NO_ENTITY_TOOLTIP);
        }

        StarSystemAPI system = GanEdenGenerator.findSystem();
        if (system != null) {
            SectorEntityToken internal = system.getEntityById(
                    GanEdenGenerator.ARRIVAL_RING_ID);
            if (internal != null) {
                internal.setDiscoverable(false);
                internal.setSensorProfile(1f);
                internal.addTag(Tags.GATE);
                internal.addTag(Tags.STORY_CRITICAL);
                internal.addTag(Tags.HAS_INTERACTION_DIALOG);
                internal.removeTag(Tags.NON_CLICKABLE);
                internal.removeTag(Tags.NO_ENTITY_TOOLTIP);
            }
        }
        return external;
    }

    private static float[] getOrCreateRingLocation() {
        MemoryAPI memory = memory();
        if (memory.contains(ShipTrophyRoomIds.MEMORY_GAN_EDEN_RING_X)
                && memory.contains(ShipTrophyRoomIds.MEMORY_GAN_EDEN_RING_Y)) {
            return new float[] {
                    memory.getFloat(ShipTrophyRoomIds.MEMORY_GAN_EDEN_RING_X),
                    memory.getFloat(ShipTrophyRoomIds.MEMORY_GAN_EDEN_RING_Y)
            };
        }

        Random random = new Random(
                0x67616e6564656eL
                        ^ Global.getSector().getSeedString().hashCode());
        float bestX = 32000f;
        float bestY = 0f;
        float bestClearance = -1f;
        for (int i = 0; i < 48; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            float radius = 29000f + random.nextFloat() * 8000f;
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            float clearance = minimumSystemDistanceSquared(x, y);
            if (clearance > bestClearance) {
                bestClearance = clearance;
                bestX = x;
                bestY = y;
            }
        }
        memory.set(ShipTrophyRoomIds.MEMORY_GAN_EDEN_RING_X, bestX);
        memory.set(ShipTrophyRoomIds.MEMORY_GAN_EDEN_RING_Y, bestY);
        return new float[] { bestX, bestY };
    }

    private static float minimumSystemDistanceSquared(float x, float y) {
        float result = Float.MAX_VALUE;
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (GanEdenGenerator.SYSTEM_ID.equals(system.getId())) continue;
            float dx = system.getLocation().x - x;
            float dy = system.getLocation().y - y;
            result = Math.min(result, dx * dx + dy * dy);
        }
        return result;
    }

    public static SectorEntityToken getExternalRing() {
        if (Global.getSector() == null) return null;
        return Global.getSector().getHyperspace().getEntityById(EXTERNAL_RING_ID);
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
        SectorEntityToken destination = ensureGateway();
        transition(source, destination, "Hyperspace");
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
