package shiptrophy;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;

import shiptrophy.hullmods.Contempt;
import shiptrophy.hullmods.Gaze;

/** Permanent +5% replacements for trophy-hullmod unlocks in that edition. */
public final class PermanentStoryPointBoosts {
    public static final float BONUS_PER_REWARD = 0.05f;

    private PermanentStoryPointBoosts() {
    }

    public static float getMultiplier(TrophyNetwork.NetworkStats stats) {
        float isaBonus = IsaTrophyManager.wasOfficerGranted()
                ? HallOfTriumphFeatures.getIsaStoryPointGenerationBonusPercent() / 100f
                : 0f;
        if (HallOfTriumphFeatures.areTrophyHullmodsEnabled()) return 1f + isaBonus;
        Set<String> rewards = loadRewards();

        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
            if (subtype.hasHullModUnlock() && stats.getSubtypeDp(subtype.id) >= subtype.unlockDp) {
                rewards.add("faction:" + subtype.id);
            }
        }

        if (TrophyNetwork.hasShowcasedHull(stats, Gaze.REQUIRED_BASE_HULL_ID)) {
            rewards.add("unique:gaze");
        }
        if (TrophyNetwork.hasShowcasedHull(stats, Contempt.REQUIRED_BASE_HULL_ID)) {
            rewards.add("unique:contempt");
        }
        for (TrophyUniqueShowcases.ShowcaseSpec showcase : TrophyUniqueShowcases.getActiveShowcases()) {
            if (TrophyNetwork.hasShowcasedHull(stats, showcase.hullId)) {
                rewards.add("unique:" + showcase.id);
            }
        }
        if (IsaTrophyManager.isMasterworkComplete()) {
            rewards.add("unique:isa_masterwork");
        }

        saveRewards(rewards);
        return 1f + rewards.size() * BONUS_PER_REWARD + isaBonus;
    }

    private static Set<String> loadRewards() {
        Set<String> result = new LinkedHashSet<String>();
        if (Global.getSector() == null) return result;
        Object stored = Global.getSector().getMemoryWithoutUpdate().get(
                ShipTrophyRoomIds.MEMORY_PERMANENT_STORY_REWARDS);
        if (!(stored instanceof Collection<?>)) return result;
        for (Object item : (Collection<?>) stored) {
            if (item instanceof String && ((String) item).length() > 0) {
                result.add((String) item);
            }
        }
        return result;
    }

    private static void saveRewards(Set<String> rewards) {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_PERMANENT_STORY_REWARDS,
                new LinkedHashSet<String>(rewards));
    }
}
