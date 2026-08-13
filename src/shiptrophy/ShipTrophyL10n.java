package shiptrophy;

import com.fs.starfarer.api.Global;

/** Central access to player-facing strings supplied by the active mod overlay. */
public final class ShipTrophyL10n {
    private static final String CATEGORY = "ship_trophy_room";

    private ShipTrophyL10n() {
    }

    public static String get(String key) {
        return Global.getSettings().getString(CATEGORY, key);
    }

    public static String format(String key, Object... arguments) {
        return String.format(get(key), arguments);
    }
}
