package shiptrophy.achievements;

import com.fs.starfarer.api.Global;

import org.magiclib.achievements.MagicAchievement;

import shiptrophy.HallOfTriumphFeatures;
import shiptrophy.IsaTrophyManager;
import shiptrophy.TrophyNetwork;
import shiptrophy.campaign.GanEdenQuestManager;

/** Optional MagicLib achievements, shown only alongside Nexerelin. */
public final class HallOfTriumphAchievement extends MagicAchievement {
    private static final String FIRST_EXHIBIT =
            "ship_trophy_room_first_exhibit";
    private static final String DOCTRINE_ARCHIVE =
            "ship_trophy_room_doctrine_archive";
    private static final String ISA_MASTERWORK =
            "ship_trophy_room_isa_masterwork";
    private static final String GAN_EDEN = "ship_trophy_room_gan_eden";

    @Override
    public boolean shouldShowInIntel() {
        return isComplete()
                || (HallOfTriumphFeatures
                        .isNexAchievementIntegrationActive()
                        && super.shouldShowInIntel());
    }

    @Override
    public void advanceAfterInterval(float amount) {
        if (isComplete()
                || Global.getSector() == null
                || !HallOfTriumphFeatures
                        .isNexAchievementIntegrationActive()) {
            return;
        }

        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        String id = getSpecId();
        boolean complete = FIRST_EXHIBIT.equals(id)
                && !stats.uniqueHullIds.isEmpty();
        if (DOCTRINE_ARCHIVE.equals(id)) {
            complete = hasCompletedVanillaPrograms(stats);
        } else if (ISA_MASTERWORK.equals(id)) {
            complete = IsaTrophyManager.isMasterworkComplete();
        } else if (GAN_EDEN.equals(id)) {
            complete = GanEdenQuestManager.isCompleted();
        }

        if (complete) completeAchievement();
    }

    private static boolean hasCompletedVanillaPrograms(
            TrophyNetwork.NetworkStats stats) {
        return stats.getSubtypeDp("xiv") >= 40f
                && stats.getSubtypeDp("lp") >= 40f
                && stats.getSubtypeDp("lg") >= 40f
                && stats.getSubtypeDp("tt") >= 40f
                && stats.getSubtypeDp("remnant") >= 40f
                && stats.getSubtypeDp("domain_derelict") >= 20f;
    }
}
