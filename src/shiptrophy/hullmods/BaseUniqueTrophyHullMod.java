package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseUniqueTrophyHullMod extends BaseHullMod {
    protected abstract String getHullModId();
    protected abstract String getRequiredShowcaseName();
    protected abstract boolean isUnlocked();
    protected abstract void syncDiscountForVariant(ShipVariantAPI variant, boolean unlocked);
    protected abstract int getCurrentDiscount(ShipVariantAPI variant);

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return isUnlocked() && !TrophyHullModUtil.hasOtherTrophyHullMod(ship, getHullModId());
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return isUnlocked();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!isUnlocked()) {
            return "Requires the " + getRequiredShowcaseName() + " in the Hall of Triumph network";
        }
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, getHullModId());
        if (other != null) return "Incompatible with " + other;
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        syncDiscountForVariant(stats == null ? null : stats.getVariant(), isUnlocked());
    }


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        tooltip.addPara("Trophy origin: %s.", opad, h, getRequiredShowcaseName());

        ShipVariantAPI variant = ship == null ? null : ship.getVariant();
        if (variant != null && variant.hasHullMod(getHullModId())) {
            tooltip.addPara("Current fitted-weapon OP discount: %s.",
                    opad, h, "" + getCurrentDiscount(variant));
        }
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.", opad, h, "one Hall of Triumph hullmod");
    }
}
