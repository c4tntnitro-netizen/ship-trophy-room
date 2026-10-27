package shiptrophy;

import com.fs.starfarer.api.Global;

/** Runtime switches used by official Hall of Triumph replacement builds. */
public final class HallOfTriumphFeatures {
    private static final String MOD_ID = "ship_trophy_room";
    private static final String LUNALIB_ID = "lunalib";
    private static final String MAGICLIB_ID = "MagicLib";
    private static final String NEXERELIN_ID = "nexerelin";
    private static final String TROPHY_HULLMODS_ENABLED = "shipTrophyHullmodsEnabled";
    private static final String NEX_ACHIEVEMENTS_ENABLED = "shipTrophyNexAchievementsEnabled";
    private static final String SHIP_GALLERY_ENABLED = "shipTrophyShipGalleryEnabled";
    private static final String ISA_QOL_ENABLED = "shipTrophyIsaQualityOfLifeBonusesEnabled";
    private static final String ISA_CREW_PAY_REDUCTION = "shipTrophyIsaCrewPayReductionPercent";
    private static final String ISA_SENSOR_PROFILE_REDUCTION = "shipTrophyIsaSensorProfileReductionPercent";
    private static final String ISA_BURN_LEVEL_BONUS = "shipTrophyIsaBurnLevelBonus";
    private static final String ISA_STORY_POINT_BONUS = "shipTrophyIsaStoryPointGenerationBonusPercent";

    private HallOfTriumphFeatures() {
    }

    public static boolean areTrophyHullmodsEnabled() {
        return getBoolean(TROPHY_HULLMODS_ENABLED, true);
    }

    public static boolean areNexAchievementsEnabled() {
        return getBoolean(NEX_ACHIEVEMENTS_ENABLED, true);
    }

    public static boolean isShipGalleryEnabled() {
        return getBoolean(SHIP_GALLERY_ENABLED, false);
    }

    public static boolean isNexAchievementIntegrationActive() {
        return areNexAchievementsEnabled()
                && isModEnabled(NEXERELIN_ID)
                && isModEnabled(MAGICLIB_ID);
    }

    public static boolean areIsaQualityOfLifeBonusesEnabled() {
        return getBoolean(ISA_QOL_ENABLED, true);
    }

    public static float getIsaCrewPayReductionPercent() {
        return getNonNegativeFloat(ISA_CREW_PAY_REDUCTION, 20f, 100f);
    }

    public static float getIsaSensorProfileReductionPercent() {
        return getNonNegativeFloat(ISA_SENSOR_PROFILE_REDUCTION, 25f, 100f);
    }

    public static float getIsaBurnLevelBonus() {
        return getNonNegativeFloat(ISA_BURN_LEVEL_BONUS, 1f, 20f);
    }

    public static float getIsaStoryPointGenerationBonusPercent() {
        return getNonNegativeFloat(ISA_STORY_POINT_BONUS, 0f, 10000f);
    }

    private static boolean getBoolean(String id, boolean fallback) {
        Object lunaValue = getLunaValue("getBoolean", id);
        if (lunaValue instanceof Boolean) return (Boolean) lunaValue;
        try {
            return Global.getSettings().getBoolean(id);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static float getNonNegativeFloat(String id, float fallback, float maximum) {
        Object lunaValue = getLunaValue("getFloat", id);
        if (lunaValue instanceof Number) {
            float value = ((Number) lunaValue).floatValue();
            if (Float.isFinite(value)) {
                return Math.max(0f, Math.min(maximum, value));
            }
        }
        try {
            float value = Global.getSettings().getFloat(id);
            if (!Float.isFinite(value)) return fallback;
            return Math.max(0f, Math.min(maximum, value));
        } catch (Exception ex) {
            return fallback;
        }
    }

    /** Only loads the optional bridge when LunaLib is enabled. */
    private static Object getLunaValue(String method, String id) {
        // Only these fields are exposed in LunaSettings.csv. Isa's JSON-only
        // settings must not trigger LunaLib missing-field errors every frame.
        if (!TROPHY_HULLMODS_ENABLED.equals(id)
                && !NEX_ACHIEVEMENTS_ENABLED.equals(id)
                && !SHIP_GALLERY_ENABLED.equals(id)) return null;
        if (!isModEnabled(LUNALIB_ID)) return null;
        try {
            if ("getBoolean".equals(method)) {
                return LunaSettingsBridge.getBoolean(MOD_ID, id);
            } else if ("getFloat".equals(method)) {
                return LunaSettingsBridge.getFloat(MOD_ID, id);
            }
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isModEnabled(String id) {
        try {
            return Global.getSettings() != null
                    && Global.getSettings().getModManager() != null
                    && Global.getSettings().getModManager().isModEnabled(id);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
