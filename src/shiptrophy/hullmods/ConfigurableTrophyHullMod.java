package shiptrophy.hullmods;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import shiptrophy.TrophySubtypeRegistry;
import shiptrophy.TrophySubtypeSpec;

public class ConfigurableTrophyHullMod extends BaseTrophyDoctrineHullMod {
    public static final String CONFIG_PATH = "data/config/ship_trophy_room/hullmod_effects.csv";

    private static final Map<String, EffectSpec> EFFECTS = new LinkedHashMap<String, EffectSpec>();
    private static boolean loaded = false;

    public static void reload() {
        loaded = false;
        EFFECTS.clear();
        loadIfNeeded();
    }

    @Override
    protected String getSubtypeId() {
        TrophySubtypeSpec subtype = TrophySubtypeRegistry.getSubtypeForHullMod(getHullModId());
        return subtype == null ? "" : subtype.id;
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        EffectSpec effect = getEffectSpec();
        if (effect == null) return;

        applyFlat(stats.getArmorBonus(), id, effect.armorFlat);
        applyFlat(stats.getHullBonus(), id, effect.hullFlat);
        applyPercent(stats.getHullBonus(), id, effect.hullPercent);
        applyPercent(stats.getHullDamageTakenMult(), id, effect.hullDamageTakenPercent);
        applyPercent(stats.getArmorDamageTakenMult(), id, effect.armorDamageTakenPercent);
        applyPercent(stats.getMaxSpeed(), id, effect.maxSpeedPercent);
        applyPercent(stats.getAcceleration(), id, effect.maneuverPercent);
        applyPercent(stats.getDeceleration(), id, effect.maneuverPercent);
        applyPercent(stats.getMaxTurnRate(), id, effect.maneuverPercent);
        applyPercent(stats.getTurnAcceleration(), id, effect.maneuverPercent);
        applyPercent(stats.getFluxCapacity(), id, effect.fluxCapacityPercent);
        applyPercent(stats.getFluxDissipation(), id, effect.fluxDissipationPercent);
        applyFlat(stats.getBallisticWeaponRangeBonus(), id, effect.ballisticRangeFlat);
        applyFlat(stats.getEnergyWeaponRangeBonus(), id, effect.energyRangeFlat);
        applyFlat(stats.getMissileWeaponRangeBonus(), id, effect.missileRangeFlat);
        applyPercent(stats.getBallisticWeaponDamageMult(), id, effect.ballisticDamagePercent);
        applyPercent(stats.getEnergyWeaponDamageMult(), id, effect.energyDamagePercent);
        applyPercent(stats.getMissileWeaponDamageMult(), id, effect.missileDamagePercent);
        applyPercent(stats.getBallisticWeaponFluxCostMod(), id, effect.ballisticFluxCostPercent);
        applyPercent(stats.getEnergyWeaponFluxCostMod(), id, effect.energyFluxCostPercent);
        applyPercent(stats.getMissileWeaponFluxCostMod(), id, effect.missileFluxCostPercent);
        applyPercent(stats.getShieldDamageTakenMult(), id, effect.shieldDamageTakenPercent);
        applyPercent(stats.getShieldUpkeepMult(), id, effect.shieldUpkeepPercent);
        applyPercent(stats.getFighterRefitTimeMult(), id, effect.fighterRefitTimePercent);
        applyPercent(stats.getCrewLossMult(), id, effect.crewLossPercent);
        applyFlat(stats.getSensorProfile(), id, effect.sensorProfileFlat);
        applyFlat(stats.getSensorStrength(), id, effect.sensorStrengthFlat);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        EffectSpec effect = getEffectSpec();
        if (effect == null || index < 0 || index >= effect.descriptionParams.length) return null;
        return effect.descriptionParams[index];
    }

    private String getHullModId() {
        return spec == null ? "" : spec.getId();
    }

    private EffectSpec getEffectSpec() {
        loadIfNeeded();
        return EFFECTS.get(getHullModId());
    }

    private static void applyFlat(com.fs.starfarer.api.combat.MutableStat stat, String id, float value) {
        if (value != 0f) stat.modifyFlat(id, value);
    }

    private static void applyFlat(com.fs.starfarer.api.combat.StatBonus stat, String id, float value) {
        if (value != 0f) stat.modifyFlat(id, value);
    }

    private static void applyPercent(com.fs.starfarer.api.combat.MutableStat stat, String id, float value) {
        if (value != 0f) stat.modifyPercent(id, value);
    }

    private static void applyPercent(com.fs.starfarer.api.combat.StatBonus stat, String id, float value) {
        if (value != 0f) stat.modifyPercent(id, value);
    }

    private static void loadIfNeeded() {
        if (loaded) return;
        loaded = true;
        EFFECTS.clear();

        try {
            JSONArray rows = Global.getSettings().getMergedSpreadsheetData("hullModId", CONFIG_PATH);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String hullModId = row.optString("hullModId", "").trim();
                if (hullModId.length() <= 0 || hullModId.startsWith("#")) continue;
                EFFECTS.put(hullModId, new EffectSpec(row));
            }
        } catch (IOException ex) {
            return;
        } catch (JSONException ex) {
            return;
        }
    }

    private static class EffectSpec {
        final String[] descriptionParams = new String[10];
        final float armorFlat;
        final float hullFlat;
        final float maxSpeedPercent;
        final float maneuverPercent;
        final float fluxCapacityPercent;
        final float fluxDissipationPercent;
        final float ballisticRangeFlat;
        final float energyRangeFlat;
        final float missileRangeFlat;
        final float ballisticDamagePercent;
        final float energyDamagePercent;
        final float missileDamagePercent;
        final float ballisticFluxCostPercent;
        final float energyFluxCostPercent;
        final float missileFluxCostPercent;
        final float shieldDamageTakenPercent;
        final float shieldUpkeepPercent;
        final float fighterRefitTimePercent;
        final float crewLossPercent;
        final float sensorProfileFlat;
        final float sensorStrengthFlat;
        final float hullPercent;
        final float hullDamageTakenPercent;
        final float armorDamageTakenPercent;

        EffectSpec(JSONObject row) {
            for (int i = 0; i < descriptionParams.length; i++) {
                descriptionParams[i] = row.optString("descParam" + i, "");
            }
            armorFlat = optFloat(row, "armorFlat");
            hullFlat = optFloat(row, "hullFlat");
            maxSpeedPercent = optFloat(row, "maxSpeedPercent");
            maneuverPercent = optFloat(row, "maneuverPercent");
            fluxCapacityPercent = optFloat(row, "fluxCapacityPercent");
            fluxDissipationPercent = optFloat(row, "fluxDissipationPercent");
            ballisticRangeFlat = optFloat(row, "ballisticRangeFlat");
            energyRangeFlat = optFloat(row, "energyRangeFlat");
            missileRangeFlat = optFloat(row, "missileRangeFlat");
            ballisticDamagePercent = optFloat(row, "ballisticDamagePercent");
            energyDamagePercent = optFloat(row, "energyDamagePercent");
            missileDamagePercent = optFloat(row, "missileDamagePercent");
            ballisticFluxCostPercent = optFloat(row, "ballisticFluxCostPercent");
            energyFluxCostPercent = optFloat(row, "energyFluxCostPercent");
            missileFluxCostPercent = optFloat(row, "missileFluxCostPercent");
            shieldDamageTakenPercent = optFloat(row, "shieldDamageTakenPercent");
            shieldUpkeepPercent = optFloat(row, "shieldUpkeepPercent");
            fighterRefitTimePercent = optFloat(row, "fighterRefitTimePercent");
            crewLossPercent = optFloat(row, "crewLossPercent");
            sensorProfileFlat = optFloat(row, "sensorProfileFlat");
            sensorStrengthFlat = optFloat(row, "sensorStrengthFlat");
            hullPercent = optFloat(row, "hullPercent");
            hullDamageTakenPercent = optFloat(row, "hullDamageTakenPercent");
            armorDamageTakenPercent = optFloat(row, "armorDamageTakenPercent");
        }

        private static float optFloat(JSONObject row, String key) {
            String value = row.optString(key, "").trim();
            if (value.length() <= 0) return 0f;
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException ex) {
                return 0f;
            }
        }
    }
}
