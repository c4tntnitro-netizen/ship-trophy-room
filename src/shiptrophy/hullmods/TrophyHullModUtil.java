package shiptrophy.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import shiptrophy.IsaTrophyManager;
import shiptrophy.TrophySubtypeRegistry;
import shiptrophy.TrophySubtypeSpec;

public class TrophyHullModUtil {
    public static boolean hasOtherTrophyHullMod(ShipAPI ship, String currentHullModId) {
        return getOtherTrophyHullModId(ship, currentHullModId) != null;
    }

    public static String getOtherTrophyHullModName(ShipAPI ship, String currentHullModId) {
        String other = getOtherTrophyHullModId(ship, currentHullModId);
        if (other == null) return null;
        HullModSpecAPI spec = getHullModSpec(other);
        return spec == null ? other : spec.getDisplayName();
    }

    private static String getOtherTrophyHullModId(ShipAPI ship, String currentHullModId) {
        if (ship == null || ship.getVariant() == null) return null;
        ShipVariantAPI variant = ship.getVariant();
        for (String hullModId : variant.getHullMods()) {
            if (hullModId == null || hullModId.equals(currentHullModId)) continue;
            if (isVisibleTrophyHullMod(hullModId)) return hullModId;
        }
        return null;
    }

    public static boolean isVisibleTrophyHullMod(String hullModId) {
        if (hullModId == null || hullModId.length() <= 0) return false;
        if (Gaze.HULLMOD_ID.equals(hullModId) || Contempt.HULLMOD_ID.equals(hullModId)
                || BlackLionInheritance.HULLMOD_ID.equals(hullModId)
                || AbundantMercyVow.HULLMOD_ID.equals(hullModId)
                || IsaTrophyManager.PROVENANCE_HULLMOD_ID.equals(hullModId)) {
            return true;
        }
        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getAllSubtypes()) {
            if (hullModId.equals(subtype.hullModId)) return true;
        }
        return false;
    }

    private static HullModSpecAPI getHullModSpec(String hullModId) {
        if (Global.getSettings() == null) return null;
        try {
            return Global.getSettings().getHullModSpec(hullModId);
        } catch (Exception ex) {
            return null;
        }
    }
}
