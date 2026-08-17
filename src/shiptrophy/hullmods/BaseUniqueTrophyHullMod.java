package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.ShipTrophyL10n;

public abstract class BaseUniqueTrophyHullMod extends BaseHullMod {
    protected abstract String getHullModId();
    protected abstract String getRequiredShowcaseName();
    protected abstract boolean isUnlocked();
    protected abstract void syncDiscountForVariant(ShipVariantAPI variant, boolean unlocked);
    protected abstract int getCurrentDiscount(ShipVariantAPI variant);

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return TrophyHullModUtil.areUnlocksEnabled() && isUnlocked()
                && !TrophyHullModUtil.hasOtherTrophyHullMod(ship, getHullModId());
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return TrophyHullModUtil.areUnlocksEnabled() && isUnlocked();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!TrophyHullModUtil.areUnlocksEnabled()) {
            return ShipTrophyL10n.get("hullmod_disabled_reason");
        }
        if (!isUnlocked()) {
            return ShipTrophyL10n.format(
                    "hullmod_requires_the_showcase", getRequiredShowcaseName());
        }
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, getHullModId());
        if (other != null) return ShipTrophyL10n.format("hullmod_incompatible", other);
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        syncDiscountForVariant(stats == null ? null : stats.getVariant(),
                TrophyHullModUtil.areEffectsEnabled() && isUnlocked());
    }


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();

        if (!TrophyHullModUtil.areUnlocksEnabled()) {
            tooltip.addPara(ShipTrophyL10n.get("hullmod_effects_disabled"),
                    opad, Misc.getNegativeHighlightColor(),
                    ShipTrophyL10n.get("hullmod_disabled_highlight"));
        }

        ShipVariantAPI variant = ship == null ? null : ship.getVariant();
        if (variant != null && variant.hasHullMod(getHullModId())) {
            tooltip.addPara(ShipTrophyL10n.get("hullmod_op_discount"),
                    opad, h, "" + getCurrentDiscount(variant));
        }
        tooltip.addPara(ShipTrophyL10n.get("hullmod_only_one"), opad, h,
                ShipTrophyL10n.get("hullmod_only_one_highlight"));
    }
}
