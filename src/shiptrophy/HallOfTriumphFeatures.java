package shiptrophy;

import java.lang.reflect.Method;

import com.fs.starfarer.api.Global;

/** Runtime switches used by official Hall of Triumph replacement builds. */
public final class HallOfTriumphFeatures {
    private static final String MOD_ID = "ship_trophy_room";
    private static final String LUNALIB_ID = "lunalib";
    private static final String MAGICLIB_ID = "MagicLib";
    private static final String NEXERELIN_ID = "nexerelin";
    private static final String TROPHY_HULLMODS_ENABLED = "shipTrophyHullmodsEnabled";
    private static final String NEX_ACHIEVEMENTS_ENABLED = "shipTrophyNexAchievementsEnabled";
    private static final String PRIMARY_STORAGE_SORT = "shipTrophyPrimaryStorageSort";
    private static final String SECONDARY_STORAGE_SORT = "shipTrophySecondaryStorageSort";
    private static final String ISA_QOL_ENABLED = "shipTrophyIsaQualityOfLifeBonusesEnabled";
    private static final String ISA_CREW_PAY_REDUCTION = "shipTrophyIsaCrewPayReductionPercent";
    private static final String ISA_SENSOR_PROFILE_REDUCTION = "shipTrophyIsaSensorProfileReductionPercent";
    private static final String ISA_BURN_LEVEL_BONUS = "shipTrophyIsaBurnLevelBonus";
    private static final String ISA_STORY_POINT_BONUS = "shipTrophyIsaStoryPointGenerationBonusPercent";

    private static volatile boolean lunaMethodsResolved;
    private static Method lunaBooleanGetter;
    private static Method lunaFloatGetter;
    private static Method lunaStringGetter;

    private HallOfTriumphFeatures() {
    }

    public static boolean areTrophyHullmodsEnabled() {
        return getBoolean(TROPHY_HULLMODS_ENABLED, true);
    }

    public static boolean areNexAchievementsEnabled() {
        return getBoolean(NEX_ACHIEVEMENTS_ENABLED, true);
    }

    public static boolean isNexAchievementIntegrationActive() {
        return areNexAchievementsEnabled()
                && isModEnabled(NEXERELIN_ID)
                && isModEnabled(MAGICLIB_ID);
    }

    public static String getPrimaryStorageSort() {
        return getString(PRIMARY_STORAGE_SORT, "Hull Name");
    }

    public static String getSecondaryStorageSort() {
        return getString(SECONDARY_STORAGE_SORT, "Faction");
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

    private static String getString(String id, String fallback) {
        Object lunaValue = getLunaValue("getString", id);
        if (lunaValue instanceof String
                && !((String) lunaValue).trim().isEmpty()) {
            return ((String) lunaValue).trim();
        }
        try {
            String value = Global.getSettings().getString(id);
            return value == null || value.trim().isEmpty()
                    ? fallback : value.trim();
        } catch (Exception ex) {
            return fallback;
        }
    }

    /** Uses reflection so LunaLib remains an optional dependency. */
    private static Object getLunaValue(String method, String id) {
        if (!isModEnabled(LUNALIB_ID)) return null;
        try {
            resolveLunaMethods();
            Method getter;
            if ("getBoolean".equals(method)) {
                getter = lunaBooleanGetter;
            } else if ("getFloat".equals(method)) {
                getter = lunaFloatGetter;
            } else {
                getter = lunaStringGetter;
            }
            return getter == null ? null : getter.invoke(null, MOD_ID, id);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static synchronized void resolveLunaMethods() throws Exception {
        if (lunaMethodsResolved) return;

        ClassLoader loader = Global.getSettings().getScriptClassLoader();
        Class<?> settings = Class.forName(
                "lunalib.lunaSettings.LunaSettings", true, loader);
        lunaBooleanGetter = settings.getMethod(
                "getBoolean", String.class, String.class);
        lunaFloatGetter = settings.getMethod(
                "getFloat", String.class, String.class);
        lunaStringGetter = settings.getMethod(
                "getString", String.class, String.class);
        lunaMethodsResolved = true;
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
