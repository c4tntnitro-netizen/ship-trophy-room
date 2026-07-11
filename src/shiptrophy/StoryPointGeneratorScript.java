package shiptrophy;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.IntervalUtil;

public class StoryPointGeneratorScript implements EveryFrameScript {
    private final IntervalUtil interval = new IntervalUtil(1f, 1f);
    private float progress;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) return;

        interval.advance(Global.getSector().getClock().convertToDays(amount));
        if (!interval.intervalElapsed()) return;

        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        float dailyProduction = getDailyStoryPointProgress(stats);
        rememberStats(stats);
        TrophyNetwork.syncDmodMarkers(stats);
        TrophyNetwork.syncUniqueDiscountMarkers(stats);
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);

        if (dailyProduction <= 0f) return;
        progress += dailyProduction * interval.getIntervalDuration();

        int points = (int) progress;
        if (points <= 0) return;

        progress -= points;
        Global.getSector().getPlayerStats().addStoryPoints(points);
    }

    private float getDailyStoryPointProgress(TrophyNetwork.NetworkStats stats) {
        if (stats.functionalRooms <= 0) return 0f;

        float roomBonus = stats.functionalRooms + (stats.improvedRooms * 0.33333334f);
        float uniqueBonus = stats.uniqueHullIds.size() / (float) TrophyNetwork.UNIQUE_HULLS_FOR_FULL_BONUS;
        float dpBonus = stats.uniqueDeploymentPoints / TrophyNetwork.DP_FOR_FULL_BONUS;
        float productionMult = roomBonus + uniqueBonus + dpBonus;

        return productionMult / TrophyRoomIndustry.BASE_DAYS_PER_STORY_POINT;
    }

    private void rememberStats(TrophyNetwork.NetworkStats stats) {
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_UNIQUE_HULLS, stats.uniqueHullIds);
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_UNIQUE_HULL_COUNT, stats.uniqueHullIds.size());
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_COLLECTION_DP, stats.uniqueDeploymentPoints);
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_ROOM_COUNT, stats.functionalRooms);
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_DOCTRINE_DP, stats.doctrineDp);
    }
}
