package shiptrophy;

/** Applies Isa's optional flat bonus to Hall story-point generation. */
public final class PermanentStoryPointBoosts {
    private PermanentStoryPointBoosts() {
    }

    public static float getMultiplier(TrophyNetwork.NetworkStats stats) {
        float isaBonus = IsaTrophyManager.wasOfficerGranted()
                ? HallOfTriumphFeatures.getIsaStoryPointGenerationBonusPercent() / 100f
                : 0f;
        return 1f + isaBonus;
    }
}
