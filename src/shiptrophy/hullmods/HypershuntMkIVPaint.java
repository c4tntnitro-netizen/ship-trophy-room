package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;

/**
 * Legacy save-compatibility marker.
 *
 * <p>Mk IV ships now use pre-rendered hull skins. Keeping the old hullmod as
 * a no-op lets existing variants load long enough for the campaign migration
 * to replace or clean them.</p>
 */
public final class HypershuntMkIVPaint extends BaseHullMod {
    public static final String HULLMOD_ID =
            "ship_trophy_hypershunt_mk4_paint";
    public static final String PATH_TAG =
            "ship_trophy_hypershunt_mk4_path";
    public static final String PIRATE_TAG =
            "ship_trophy_hypershunt_mk4_pirate";
}
