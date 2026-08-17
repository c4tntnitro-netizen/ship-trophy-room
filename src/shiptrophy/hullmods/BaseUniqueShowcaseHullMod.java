package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.TrophyNetwork;
import shiptrophy.ShipTrophyL10n;

public abstract class BaseUniqueShowcaseHullMod extends BaseHullMod {
    protected abstract String getHullModId();
    protected abstract String getRequiredHullId();
    protected abstract String getRequiredShowcaseName();

    protected boolean isUnlocked() {
        return TrophyNetwork.hasShowcasedHull(TrophyNetwork.computeNetworkStats(), getRequiredHullId());
    }

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
        if (!isUnlocked()) return ShipTrophyL10n.format(
                "hullmod_requires_showcase", getRequiredShowcaseName());
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, getHullModId());
        if (other != null) return ShipTrophyL10n.format("hullmod_incompatible", other);
        return null;
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
        tooltip.addPara(ShipTrophyL10n.get("hullmod_only_one"), opad, h,
                ShipTrophyL10n.get("hullmod_only_one_highlight"));
    }
}
