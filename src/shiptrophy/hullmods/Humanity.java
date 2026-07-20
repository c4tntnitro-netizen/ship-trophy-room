package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.NeuralInterface;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.TrophyNetwork;
import shiptrophy.TrophySubtypeRegistry;
import shiptrophy.TrophySubtypeSpec;

public class Humanity extends NeuralInterface {
    public static final String HULLMOD_ID = "ship_trophy_humanity";
    public static final String SUBTYPE_ID = "remnant";
    public static final float FIRE_RATE_REDUCTION = 80f;
    public static final float FIRE_RATE_MULT = 0.20f;
    public static final float SPEED_REDUCTION = 80f;
    public static final float SPEED_MULT = 0.20f;
    public static final float FIGHTER_RANGE_REDUCTION = 90f;
    public static final float FIGHTER_RANGE_MULT = 0.10f;

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return isUnlocked() && !hasNeuralInterface(ship)
                && !TrophyHullModUtil.hasOtherTrophyHullMod(ship, HULLMOD_ID);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return isUnlocked();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!isUnlocked()) {
            TrophySubtypeSpec subtype = getSubtype();
            String showcaseName = subtype == null ? "Remnant" : subtype.showcaseName;
            float unlockDp = subtype == null ? TrophyNetwork.DOCTRINE_UNLOCK_DP : subtype.unlockDp;
            return "Requires " + Math.round(unlockDp) + " DP worth of " + showcaseName
                    + " ships in the Hall of Triumph network";
        }
        if (hasNeuralInterface(ship)) return "Incompatible with Neural Interface";
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, HULLMOD_ID);
        if (other != null) return "Incompatible with " + other;
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (!isUnlocked() || stats == null) return;
        ShipVariantAPI variant = stats.getVariant();
        if (variant != null && variant.hasHullMod(HullMods.NEURAL_INTERFACE)) return;

        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id, 0f);
        stats.getBallisticRoFMult().modifyMult(id, FIRE_RATE_MULT);
        stats.getEnergyRoFMult().modifyMult(id, FIRE_RATE_MULT);
        stats.getMissileRoFMult().modifyMult(id, FIRE_RATE_MULT);
        stats.getMaxSpeed().modifyMult(id, SPEED_MULT);
        stats.getFighterWingRange().modifyMult(id, FIGHTER_RANGE_MULT);
    }

    @Override
    public void applyEffectsAfterShipAddedToCombatEngine(ShipAPI ship, String id) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine != null && !engine.hasPluginOfClass(HumanityTransferNotifier.class)) {
            engine.addPlugin(new HumanityTransferNotifier());
        }
    }
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize,
            ShipAPI ship, float width, boolean isForModSpec) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.",
                opad, h, "one Hall of Triumph hullmod");
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "0";
        if (index == 1) return Math.round(FIRE_RATE_REDUCTION) + "%";
        if (index == 2) return Math.round(SPEED_REDUCTION) + "%";
        if (index == 3) return Math.round(FIGHTER_RANGE_REDUCTION) + "%";
        return null;
    }

    private boolean isUnlocked() {
        return TrophyNetwork.isSubtypeUnlocked(SUBTYPE_ID);
    }

    private TrophySubtypeSpec getSubtype() {
        return TrophySubtypeRegistry.getSubtype(SUBTYPE_ID);
    }

    private boolean hasNeuralInterface(ShipAPI ship) {
        return ship != null && ship.getVariant() != null
                && ship.getVariant().hasHullMod(HullMods.NEURAL_INTERFACE);
    }
}
