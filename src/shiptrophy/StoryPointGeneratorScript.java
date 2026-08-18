package shiptrophy;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.IntervalUtil;

import shiptrophy.hullmods.TrophyHullModUtil;

public class StoryPointGeneratorScript implements EveryFrameScript {
    private static final float NOTIFICATION_INTERVAL_DAYS = 30f;
    private static final String MEMORY_NOTIFICATION_DAYS =
            "$ship_trophy_room_story_point_notification_days";
    private static final String MEMORY_NOTIFICATION_POINTS =
            "$ship_trophy_room_story_point_notification_points";

    private final IntervalUtil interval = new IntervalUtil(1f, 1f);
    private final IntervalUtil settingInterval = new IntervalUtil(0.25f, 0.25f);

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

        settingInterval.advance(amount);
        if (settingInterval.intervalElapsed()) {
            TrophyHullModUtil.refreshPlayerFleetEffectsIfSettingChanged();
        }

        interval.advance(Global.getSector().getClock().convertToDays(amount));
        if (!interval.intervalElapsed()) return;
        float elapsedDays = interval.getIntervalDuration();

        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        float dailyProduction = getDailyStoryPointProgress(stats);
        rememberStats(stats);
        TrophyNetwork.syncDmodMarkers(stats);
        TrophyNetwork.syncUniqueDiscountMarkers(stats);
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);
        IsaTrophyManager.refreshIsaHullmod();

        if (!Float.isFinite(dailyProduction) || dailyProduction <= 0f) return;
        float progress = loadProgress();
        progress += dailyProduction * elapsedDays;

        if (!Float.isFinite(progress) || progress < 0f) {
            progress = 0f;
        }

        int points = (int) progress;
        if (points <= 0) {
            saveProgress(progress);
            advanceNotification(elapsedDays, 0);
            return;
        }

        progress -= points;
        saveProgress(progress);
        Global.getSector().getPlayerStats().addStoryPoints(points);
        advanceNotification(elapsedDays, points);
    }

    private float loadProgress() {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(
                ShipTrophyRoomIds.MEMORY_STORY_POINT_PROGRESS)) return 0f;
        Object raw = Global.getSector().getMemoryWithoutUpdate().get(
                ShipTrophyRoomIds.MEMORY_STORY_POINT_PROGRESS);
        if (!(raw instanceof Number)) return 0f;
        float stored = ((Number) raw).floatValue();
        return Float.isFinite(stored) && stored >= 0f && stored < 1f ? stored : 0f;
    }

    private void saveProgress(float value) {
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_STORY_POINT_PROGRESS, value);
    }

    private void advanceNotification(float elapsedDays, int generatedPoints) {
        Object rawDays = Global.getSector().getMemoryWithoutUpdate().get(MEMORY_NOTIFICATION_DAYS);
        float notificationDays = rawDays instanceof Number
                ? ((Number) rawDays).floatValue() : 0f;
        if (!Float.isFinite(notificationDays) || notificationDays < 0f) notificationDays = 0f;

        Object rawPoints = Global.getSector().getMemoryWithoutUpdate().get(MEMORY_NOTIFICATION_POINTS);
        int notificationPoints = rawPoints instanceof Number
                ? Math.max(0, ((Number) rawPoints).intValue()) : 0;
        notificationPoints += Math.max(0, generatedPoints);
        notificationDays += Math.max(0f, elapsedDays);

        if (notificationDays >= NOTIFICATION_INTERVAL_DAYS) {
            if (notificationPoints <= 0) {
                notificationDays %= NOTIFICATION_INTERVAL_DAYS;
            } else if (Global.getSector().getCampaignUI() != null) {
                Global.getSector().getCampaignUI().addMessage(
                        "Hall of Triumph generated " + notificationPoints + " story point"
                                + (notificationPoints == 1 ? " this month." : "s this month."));
                notificationPoints = 0;
                notificationDays %= NOTIFICATION_INTERVAL_DAYS;
            }
        }

        Global.getSector().getMemoryWithoutUpdate().set(
                MEMORY_NOTIFICATION_DAYS, notificationDays);
        Global.getSector().getMemoryWithoutUpdate().set(
                MEMORY_NOTIFICATION_POINTS, notificationPoints);
    }

    private float getDailyStoryPointProgress(TrophyNetwork.NetworkStats stats) {
        if (stats.functionalRooms <= 0) return 0f;

        float roomBonus = stats.functionalRooms + (stats.improvedRooms * 0.33333334f);
        float uniqueBonus = stats.uniqueHullIds.size() / (float) TrophyNetwork.UNIQUE_HULLS_FOR_FULL_BONUS;
        float dpBonus = stats.uniqueDeploymentPoints / TrophyNetwork.DP_FOR_FULL_BONUS;
        float productionMult = roomBonus + uniqueBonus + dpBonus;
        float isaMultiplier = 1f;
        if (IsaTrophyManager.wasOfficerGranted()) {
            isaMultiplier += HallOfTriumphFeatures
                    .getIsaStoryPointGenerationBonusPercent() / 100f;
        }

        return productionMult * isaMultiplier
                / TrophyRoomIndustry.BASE_DAYS_PER_STORY_POINT;
    }

    private void rememberStats(TrophyNetwork.NetworkStats stats) {
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_UNIQUE_HULLS, stats.uniqueHullIds);
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_UNIQUE_HULL_COUNT, stats.uniqueHullIds.size());
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_COLLECTION_DP, stats.uniqueDeploymentPoints);
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_ROOM_COUNT, stats.functionalRooms);
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_DOCTRINE_DP, stats.subtypeDp);
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_NETWORK_SUBTYPE_DP, stats.subtypeDp);
    }
}
