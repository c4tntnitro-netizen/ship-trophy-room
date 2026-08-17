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
import shiptrophy.ShipTrophyL10n;

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
        return TrophyHullModUtil.areUnlocksEnabled()
                && isUnlocked() && !hasNeuralInterface(ship)
                && !TrophyHullModUtil.hasOtherTrophyHullMod(ship, HULLMOD_ID);
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
            TrophySubtypeSpec subtype = getSubtype();
            String showcaseName = subtype == null
                    ? ShipTrophyL10n.get("hullmod_remnant") : subtype.showcaseName;
            float unlockDp = subtype == null ? TrophyNetwork.DOCTRINE_UNLOCK_DP : subtype.unlockDp;
            return ShipTrophyL10n.format(
                    "hullmod_requires_dp", Math.round(unlockDp), showcaseName);
        }
        if (hasNeuralInterface(ship)) return ShipTrophyL10n.format(
                "hullmod_incompatible",
                ShipTrophyL10n.get("hullmod_neural_interface"));
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, HULLMOD_ID);
        if (other != null) return ShipTrophyL10n.format("hullmod_incompatible", other);
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (!TrophyHullModUtil.areEffectsEnabled() || !isUnlocked() || stats == null) return;
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
        if (!TrophyHullModUtil.areEffectsEnabled()) return;
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
        if (!TrophyHullModUtil.areUnlocksEnabled()) {
            tooltip.addPara(ShipTrophyL10n.get("hullmod_effects_disabled"),
                    opad, Misc.getNegativeHighlightColor(),
                    ShipTrophyL10n.get("hullmod_disabled_highlight"));
        }
        tooltip.addPara(ShipTrophyL10n.get("hullmod_only_one"),
                opad, h, ShipTrophyL10n.get("hullmod_only_one_highlight"));
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
