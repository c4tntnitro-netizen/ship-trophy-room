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
        return isUnlocked() && matchesStyle(ship) && hasNoOtherTrophyHullMod(ship);
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
                    + showcaseName + " ships in the Hall of Triumph network";
        }
        if (!matchesStyle(ship)) {
            TrophySubtypeSpec subtype = getSubtype();
            String style = subtype == null ? "matching" : subtype.installStyle;
            return "Can only be installed on " + style + " ships";
        }
        if (!hasNoOtherTrophyHullMod(ship)) {
            String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, getCurrentHullModId());
            return other == null ? "Only one Hall of Triumph hullmod may be installed" : "Incompatible with " + other;
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
        String originName = subtype == null ? showcaseName : subtype.displayName;
        tooltip.addPara("Trophy origin: %s.", opad, h, originName);
        float current = TrophyNetwork.getSubtypeDp(getSubtypeId());
        tooltip.addPara("Trophy network showcase: %s / %s DP worth of %s ships.",
                opad, h, "" + Math.round(current), "" + Math.round(unlockDp), showcaseName);
        String dmodNote = getDModCalculationNote();
        if (dmodNote != null) {
            tooltip.addPara(dmodNote, opad, h, "counts as a D-mod");
        }
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.", opad, h, "one Hall of Triumph hullmod");
    }

    protected boolean isUnlocked() {
        return TrophyNetwork.isSubtypeUnlocked(getSubtypeId());
    }

    protected boolean matchesStyle(ShipAPI ship) {
        return TrophyNetwork.isMatchingInstallStyle(ship, getSubtypeId());
    }

    protected boolean hasNoOtherTrophyHullMod(ShipAPI ship) {
        return !TrophyHullModUtil.hasOtherTrophyHullMod(ship, getCurrentHullModId());
    }

    protected String getCurrentHullModId() {
        TrophySubtypeSpec subtype = getSubtype();
        return subtype == null ? "" : subtype.hullModId;
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
