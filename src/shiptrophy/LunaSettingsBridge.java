package shiptrophy;

import lunalib.lunaSettings.LunaSettings;

/** Isolates the optional LunaLib dependency without using restricted reflection. */
final class LunaSettingsBridge {
    private LunaSettingsBridge() {
    }

    static Boolean getBoolean(String modId, String fieldId) {
        return LunaSettings.getBoolean(modId, fieldId);
    }

    static Float getFloat(String modId, String fieldId) {
        return LunaSettings.getFloat(modId, fieldId);
    }
}
