package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.TrophyDoctrine;
import shiptrophy.TrophyNetwork;

public abstract class BaseTrophyDoctrineHullMod extends BaseHullMod {
    protected abstract TrophyDoctrine getDoctrine();
    protected String getDModCalculationNote() { return null; }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return isUnlocked() && matchesStyle(ship) && hasNoOtherDoctrine(ship);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return isUnlocked();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!isUnlocked()) {
            return "Requires " + Math.round(TrophyNetwork.DOCTRINE_UNLOCK_DP) + " DP worth of "
                    + getDoctrine().showcaseName + " ships in the Trophy Room network";
        }
        if (!matchesStyle(ship)) {
            return "Can only be installed on " + getDoctrine().installStyle + " ships";
        }
        if (!hasNoOtherDoctrine(ship)) {
            return "Only one Trophy doctrine hullmod may be installed";
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (!isUnlocked()) return;
        applyDoctrineEffects(hullSize, stats, id);
    }

    protected abstract void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id);

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        float current = TrophyNetwork.getDoctrineDp(getDoctrine());
        tooltip.addPara("Trophy network showcase: %s / %s DP worth of %s ships.",
                opad, h, "" + Math.round(current), "" + Math.round(TrophyNetwork.DOCTRINE_UNLOCK_DP), getDoctrine().showcaseName);
        String dmodNote = getDModCalculationNote();
        if (dmodNote != null) {
            tooltip.addPara(dmodNote, opad, h, "counts as a D-mod");
        }
    }

    protected boolean isUnlocked() {
        return TrophyNetwork.isDoctrineUnlocked(getDoctrine());
    }

    protected boolean matchesStyle(ShipAPI ship) {
        return TrophyNetwork.isMatchingInstallStyle(ship, getDoctrine());
    }

    protected boolean hasNoOtherDoctrine(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return true;
        for (TrophyDoctrine doctrine : TrophyDoctrine.values()) {
            if (doctrine != getDoctrine() && ship.getVariant().hasHullMod(doctrine.hullModId)) return false;
        }
        return true;
    }

    protected void addHiddenMarker(MutableShipStatsAPI stats, String markerId) {
        ShipVariantAPI variant = stats.getVariant();
        if (variant != null && !variant.hasHullMod(markerId)) {
            variant.addMod(markerId);
        }
    }

    protected void removeHiddenMarker(MutableShipStatsAPI stats, String markerId) {
        ShipVariantAPI variant = stats.getVariant();
        if (variant != null && variant.hasHullMod(markerId)) {
            variant.removeMod(markerId);
            variant.removePermaMod(markerId);
        }
    }
}
