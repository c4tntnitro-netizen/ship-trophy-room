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
import shiptrophy.TrophySubtypeRegistry;
import shiptrophy.TrophySubtypeSpec;

public abstract class BaseTrophyDoctrineHullMod extends BaseHullMod {
    protected TrophyDoctrine getDoctrine() { return null; }
    protected String getSubtypeId() {
        TrophyDoctrine doctrine = getDoctrine();
        return doctrine == null ? "" : doctrine.id;
    }
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
            TrophySubtypeSpec subtype = getSubtype();
            String showcaseName = subtype == null ? "matching" : subtype.showcaseName;
            float unlockDp = subtype == null ? TrophyNetwork.DOCTRINE_UNLOCK_DP : subtype.unlockDp;
            return "Requires " + Math.round(unlockDp) + " DP worth of "
                    + showcaseName + " ships in the Trophy Room network";
        }
        if (!matchesStyle(ship)) {
            TrophySubtypeSpec subtype = getSubtype();
            String style = subtype == null ? "matching" : subtype.installStyle;
            return "Can only be installed on " + style + " ships";
        }
        if (!hasNoOtherDoctrine(ship)) {
            return "Only one Trophy subtype hullmod may be installed";
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
        TrophySubtypeSpec subtype = getSubtype();
        String showcaseName = subtype == null ? "matching" : subtype.showcaseName;
        float unlockDp = subtype == null ? TrophyNetwork.DOCTRINE_UNLOCK_DP : subtype.unlockDp;
        float current = TrophyNetwork.getSubtypeDp(getSubtypeId());
        tooltip.addPara("Trophy network showcase: %s / %s DP worth of %s ships.",
                opad, h, "" + Math.round(current), "" + Math.round(unlockDp), showcaseName);
        String dmodNote = getDModCalculationNote();
        if (dmodNote != null) {
            tooltip.addPara(dmodNote, opad, h, "counts as a D-mod");
        }
    }

    protected boolean isUnlocked() {
        return TrophyNetwork.isSubtypeUnlocked(getSubtypeId());
    }

    protected boolean matchesStyle(ShipAPI ship) {
        return TrophyNetwork.isMatchingInstallStyle(ship, getSubtypeId());
    }

    protected boolean hasNoOtherDoctrine(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return true;
        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getAllSubtypes()) {
            if (subtype.id.equals(getSubtypeId())) continue;
            if (subtype.hasHullModUnlock() && ship.getVariant().hasHullMod(subtype.hullModId)) return false;
        }
        return true;
    }

    protected TrophySubtypeSpec getSubtype() {
        return TrophySubtypeRegistry.getSubtype(getSubtypeId());
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
