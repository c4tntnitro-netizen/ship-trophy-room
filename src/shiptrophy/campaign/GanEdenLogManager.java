package shiptrophy.campaign;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

/** Persistent recovery state and Intel filing for the Gan Eden archives. */
public final class GanEdenLogManager {
    private static final String RECOVERED_PREFIX =
            "$shipTrophyGanEdenLogRecovered_";

    private GanEdenLogManager() {
    }

    public static boolean isRecovered(GanEdenLogSpec spec) {
        return spec != null && Global.getSector() != null
                && memory().getBoolean(RECOVERED_PREFIX + spec.getId());
    }

    public static boolean recover(
            GanEdenLogSpec spec, TextPanelAPI textPanel) {
        if (spec == null || Global.getSector() == null) return false;
        boolean newlyRecovered = !isRecovered(spec);
        memory().set(RECOVERED_PREFIX + spec.getId(), true);
        ensureIntel(spec, textPanel, newlyRecovered);
        return newlyRecovered;
    }

    /** Files an archive without rendering its Intel description in dialogue. */
    public static boolean recoverSilently(GanEdenLogSpec spec) {
        if (spec == null || Global.getSector() == null) return false;
        boolean newlyRecovered = !isRecovered(spec);
        memory().set(RECOVERED_PREFIX + spec.getId(), true);
        ensureIntel(spec, null, false);
        return newlyRecovered;
    }

    /** Restores archives in old saves that already completed the Epitaph. */
    public static void ensureForCurrentSave() {
        if (Global.getSector() == null) return;
        GanEdenQuestManager.Stage stage = GanEdenQuestManager.getStage();
        int surveyed = GanEdenHypershuntManager.getSurveyedCount();
        for (GanEdenLogSpec spec : GanEdenLogSpec.ordered()) {
            boolean shouldMigrate = false;
            if (spec == GanEdenLogSpec.PART_ONE) {
                shouldMigrate = stage.ordinal()
                        >= GanEdenQuestManager.Stage
                                .INVESTIGATE_HYPERSHUNTS.ordinal();
            } else if (spec == GanEdenLogSpec.PART_TWO) {
                shouldMigrate = surveyed >= 1
                        || stage.ordinal() >= GanEdenQuestManager.Stage
                                .GAN_EDEN_REVEALED.ordinal();
            } else if (spec == GanEdenLogSpec.PART_THREE) {
                shouldMigrate = surveyed >= 2
                        || stage.ordinal() >= GanEdenQuestManager.Stage
                                .GAN_EDEN_REVEALED.ordinal();
            } else if (spec == GanEdenLogSpec.PART_FOUR) {
                shouldMigrate = stage.ordinal()
                        >= GanEdenQuestManager.Stage
                                .DEFEAT_GOLDEN_SHARDS.ordinal();
            } else if (spec == GanEdenLogSpec.FINAL) {
                shouldMigrate = GanEdenQuestManager.isEpitaphFound()
                        || GanEdenQuestManager.isCompleted();
            }
            if (shouldMigrate && !isRecovered(spec)) {
                memory().set(RECOVERED_PREFIX + spec.getId(), true);
            }
            if (isRecovered(spec)) ensureIntel(spec, null, false);
        }
    }

    private static GanEdenLogIntel ensureIntel(
            GanEdenLogSpec spec,
            TextPanelAPI textPanel,
            boolean announceNew) {
        List<IntelInfoPlugin> entries = Global.getSector().getIntelManager()
                .getIntel(GanEdenLogIntel.class);
        for (IntelInfoPlugin entry : entries) {
            if (entry instanceof GanEdenLogIntel) {
                GanEdenLogIntel intel = (GanEdenLogIntel) entry;
                if (spec.getId().equals(intel.getLogId())) return intel;
            }
        }

        GanEdenLogIntel intel = new GanEdenLogIntel(spec.getId());
        if (textPanel != null) {
            Global.getSector().getIntelManager().addIntel(
                    intel, !announceNew, textPanel);
        } else {
            Global.getSector().getIntelManager().addIntel(
                    intel, !announceNew);
        }
        return intel;
    }

    private static MemoryAPI memory() {
        return Global.getSector().getMemoryWithoutUpdate();
    }
}
