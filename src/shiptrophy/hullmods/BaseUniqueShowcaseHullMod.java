package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.TrophyNetwork;

public abstract class BaseUniqueShowcaseHullMod extends BaseHullMod {
    protected abstract String getHullModId();
    protected abstract String getRequiredHullId();
    protected abstract String getRequiredShowcaseName();

    protected boolean isUnlocked() {
        return TrophyNetwork.hasShowcasedHull(TrophyNetwork.computeNetworkStats(), getRequiredHullId());
    }

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
        if (!isUnlocked()) return "Requires " + getRequiredShowcaseName() + " in the Hall of Triumph network";
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, getHullModId());
        if (other != null) return "Incompatible with " + other;
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        tooltip.addPara("Trophy origin: %s.", opad, h, getRequiredShowcaseName());
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.", opad, h, "one Hall of Triumph hullmod");
    }
}