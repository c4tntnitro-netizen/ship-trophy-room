package shiptrophy.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.TrophyNetwork;

public class Contempt extends BaseUniqueTrophyHullMod {
    public static final String HULLMOD_ID = "ship_trophy_contempt";
    public static final String REQUIRED_BASE_HULL_ID = "onslaught_mk1";
    public static final String DISCOUNT_PREFIX = "ship_trophy_contempt_op_discount_";
    public static final float DAMAGE_BONUS = 25f;
    public static final float FLUX_REDUCTION = 0.10f;
    public static final float HULL_DAMAGE_BONUS = 5f;

    private static final String DAMAGE_MOD_ID = HULLMOD_ID + "_pd_damage";

    @Override
    protected String getHullModId() {
        return HULLMOD_ID;
    }

    @Override
    protected String getRequiredShowcaseName() {
        return "Onslaught Mk.I";
    }

    @Override
    protected boolean isUnlocked() {
        return TrophyNetwork.isOnslaughtMkIShowcased();
    }

    @Override
    protected void syncDiscountForVariant(ShipVariantAPI variant, boolean unlocked) {
        clearLegacyDiscountMarkers(variant);
    }

    public static void syncVariant(ShipVariantAPI variant, TrophyNetwork.CollectionStats stats) {
        clearLegacyDiscountMarkers(variant);
    }

    public static void syncVariant(ShipVariantAPI variant, boolean unlocked) {
        clearLegacyDiscountMarkers(variant);
    }

    private static void clearLegacyDiscountMarkers(ShipVariantAPI variant) {
        TrophyOpDiscounts.clearDiscount(variant, DISCOUNT_PREFIX);
    }

    @Override
    protected int getCurrentDiscount(ShipVariantAPI variant) {
        return 0;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        if (stats != null) stats.getDamageToTargetHullMult().modifyPercent(id, HULL_DAMAGE_BONUS);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.hasListenerOfClass(ContemptCombatListener.class)) return;
        ship.addListener(new ContemptCombatListener(ship));
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        tooltip.addPara("Trophy origin: %s.", opad, h, getRequiredShowcaseName());
        tooltip.addPara("Uses live point-defense classification, including weapons converted by S-modded Integrated Point Defense AI.",
                opad, h, "S-modded Integrated Point Defense AI");
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.", opad, h, "one Hall of Triumph hullmod");
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Math.round(DAMAGE_BONUS) + "%";
        if (index == 1) return Math.round(FLUX_REDUCTION * 100f) + "%";
        if (index == 2) return Math.round(HULL_DAMAGE_BONUS) + "%";
        return null;
    }

    public static boolean isPointDefense(WeaponAPI weapon) {
        return weapon != null && (weapon.hasAIHint(WeaponAPI.AIHints.PD)
                || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
                || weapon.hasAIHint(WeaponAPI.AIHints.PD_ALSO));
    }

    private static WeaponAPI getSourceWeapon(Object param) {
        if (param instanceof BeamAPI) return ((BeamAPI) param).getWeapon();
        if (param instanceof DamagingProjectileAPI) return ((DamagingProjectileAPI) param).getWeapon();
        return null;
    }

    private static boolean isFighterOrMissile(CombatEntityAPI target) {
        if (target instanceof MissileAPI) return true;
        return target instanceof ShipAPI && ((ShipAPI) target).isFighter();
    }

    public static class ContemptCombatListener implements DamageDealtModifier, AdvanceableListener {
        private final ShipAPI ship;
        private final Map<WeaponAPI, Float> previousCooldown = new HashMap<WeaponAPI, Float>();

        public ContemptCombatListener(ShipAPI ship) {
            this.ship = ship;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                previousCooldown.put(weapon, weapon.getCooldownRemaining());
            }
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage,
                Vector2f point, boolean shieldHit) {
            WeaponAPI weapon = getSourceWeapon(param);
            if (!isPointDefense(weapon) || !isFighterOrMissile(target) || damage == null) return null;
            damage.getModifier().modifyPercent(DAMAGE_MOD_ID, DAMAGE_BONUS);
            return DAMAGE_MOD_ID;
        }

        @Override
        public void advance(float amount) {
            if (ship == null || amount <= 0f || ship.isHulk()) return;

            float refund = 0f;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon == null || weapon.isDecorative() || !isPointDefense(weapon)) continue;
                float fluxCost = weapon.getFluxCostToFire();
                if (fluxCost <= 0f) continue;

                if (weapon.isBeam()) {
                    if (weapon.isFiring()) refund += fluxCost * FLUX_REDUCTION * amount;
                } else {
                    float currentCooldown = weapon.getCooldownRemaining();
                    Float previous = previousCooldown.get(weapon);
                    if (previous != null && currentCooldown > previous + 0.0001f) {
                        int burstSize = weapon.getSpec() == null ? 1 : Math.max(1, weapon.getSpec().getBurstSize());
                        refund += fluxCost * burstSize * FLUX_REDUCTION;
                    }
                    previousCooldown.put(weapon, currentCooldown);
                }
            }

            float softFlux = ship.getFluxTracker().getCurrFlux() - ship.getFluxTracker().getHardFlux();
            if (refund > 0f && softFlux > 0f) {
                ship.getFluxTracker().decreaseFlux(Math.min(refund, softFlux));
            }
        }
    }
}
