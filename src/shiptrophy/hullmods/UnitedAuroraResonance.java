package shiptrophy.hullmods;

import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class UnitedAuroraResonance extends BaseTrophyDoctrineHullMod {
    public static final String SUBTYPE_ID = "uaf";
    public static final String ECCM_PACKAGE = "eccm";

    public static final float GUIDED_MISSILE_SPEED_MULT = 1.33f;
    public static final float DECOY_SPEED_MULT = 1.10f;
    public static final float SMALL_DECOY_LIFETIME = 2f;
    public static final float MEDIUM_DECOY_LIFETIME = 4f;
    public static final float LARGE_DECOY_LIFETIME = 10f;
    public static final int STANDARD_DECOY_COUNT = 2;
    public static final int SEMIBREVE_DECOY_COUNT = 5;

    private static final String SEMIBREVE_ID = "uaf_semibreve_base";
    private static final String GUIDED_SEMIBREVE_ID = "uaf_semibreve_guided";
    private static final String DECOY_WEAPON_ID = "flarelauncher3";
    private static final String PROCESSED_KEY = "ship_trophy_uaf_resonance_processed";
    private static final String SPEED_MOD_ID = "ship_trophy_uaf_resonance_speed";
    private static final String DECOY_SPEED_MOD_ID = "ship_trophy_uaf_resonance_decoy_speed";
    private static final float MIN_BATCH_WINDOW = 0.15f;
    private static final float BATCH_WINDOW_PADDING = 0.10f;
    private static final float DECOY_SPREAD_DEGREES = 6f;

    @Override
    protected String getSubtypeId() {
        return SUBTYPE_ID;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship) && !hasEccmPackage(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (hasEccmPackage(ship)) return "Incompatible with ECCM Package";
        return super.getUnapplicableReason(ship);
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // The effect is applied to individual missiles at runtime so guided missiles,
        // torpedoes, rockets, and firing batches can be handled independently.
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (!isUnlocked() || ship == null || ship.isFighter()
                || ship.hasListenerOfClass(ResonanceCombatListener.class)) {
            return;
        }
        ship.addListener(new ResonanceCombatListener(ship));
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Math.round((GUIDED_MISSILE_SPEED_MULT - 1f) * 100f) + "%";
        if (index == 1) return String.valueOf(STANDARD_DECOY_COUNT);
        if (index == 2) return Math.round(DECOY_SPEED_MULT * 100f) + "%";
        if (index == 3) return String.valueOf(SEMIBREVE_DECOY_COUNT);
        if (index == 4) return String.valueOf(Math.round(SMALL_DECOY_LIFETIME));
        if (index == 5) return String.valueOf(Math.round(MEDIUM_DECOY_LIFETIME));
        if (index == 6) return String.valueOf(Math.round(LARGE_DECOY_LIFETIME));
        return null;
    }

    private boolean hasEccmPackage(ShipAPI ship) {
        return ship != null && ship.getVariant() != null && ship.getVariant().hasHullMod(ECCM_PACKAGE);
    }

    private static boolean isPointDefense(WeaponAPI weapon) {
        return weapon != null && (weapon.hasAIHint(WeaponAPI.AIHints.PD)
                || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
                || weapon.hasAIHint(WeaponAPI.AIHints.PD_ALSO));
    }

    private static String getWeaponId(WeaponAPI weapon) {
        if (weapon == null) return "";
        WeaponSpecAPI spec = weapon.getSpec();
        if (spec != null && spec.getWeaponId() != null) return spec.getWeaponId();
        return weapon.getId() == null ? "" : weapon.getId();
    }

    private static boolean isSemibreve(WeaponAPI weapon) {
        String id = getWeaponId(weapon);
        return SEMIBREVE_ID.equals(id) || GUIDED_SEMIBREVE_ID.equals(id);
    }

    private static boolean isTorpedoOrRocket(WeaponAPI weapon) {
        if (weapon == null || weapon.getSpec() == null) return false;

        WeaponSpecAPI spec = weapon.getSpec();
        String role = spec.getPrimaryRoleStr();
        if (role != null) {
            String normalized = role.toLowerCase(Locale.ROOT);
            if (normalized.contains("torpedo") || normalized.contains("rocket")) return true;
        }

        Set<String> tags = spec.getTags();
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null && tag.toLowerCase(Locale.ROOT).startsWith("rocket")) return true;
            }
        }
        return false;
    }

    private static float getBatchWindow(WeaponAPI weapon) {
        if (weapon == null || weapon.getSpec() == null) return MIN_BATCH_WINDOW;
        WeaponSpecAPI spec = weapon.getSpec();
        if (spec.getBurstSize() <= 1) return MIN_BATCH_WINDOW;
        return Math.max(MIN_BATCH_WINDOW, spec.getBurstDuration() + BATCH_WINDOW_PADDING);
    }

    private static float getDecoyLifetime(WeaponAPI weapon) {
        if (weapon == null || weapon.getSize() == null) return SMALL_DECOY_LIFETIME;
        if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) return LARGE_DECOY_LIFETIME;
        if (weapon.getSize() == WeaponAPI.WeaponSize.MEDIUM) return MEDIUM_DECOY_LIFETIME;
        return SMALL_DECOY_LIFETIME;
    }

    public static class ResonanceCombatListener implements AdvanceableListener {
        private final ShipAPI ship;
        private final Map<WeaponAPI, Float> decoyBatchEnds = new IdentityHashMap<WeaponAPI, Float>();

        public ResonanceCombatListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (ship == null || ship.isHulk() || amount <= 0f) return;

            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null || engine.isPaused()) return;

            float now = engine.getTotalElapsedTime(false);
            for (MissileAPI missile : engine.getMissiles()) {
                if (!shouldProcess(missile)) continue;
                missile.setCustomData(PROCESSED_KEY, Boolean.TRUE);

                WeaponAPI weapon = missile.getWeapon();
                if (missile.isGuided() && !isPointDefense(weapon)) {
                    applyGuidedSpeedBonus(missile);
                }

                if (!isSemibreve(weapon) && !isTorpedoOrRocket(weapon)) continue;
                if (!beginDecoyBatch(weapon, now)) continue;

                int count = isSemibreve(weapon) ? SEMIBREVE_DECOY_COUNT : STANDARD_DECOY_COUNT;
                spawnDecoys(engine, missile, weapon, count);
            }
        }

        private boolean shouldProcess(MissileAPI missile) {
            if (missile == null || missile.getSource() != ship || missile.isFlare()) return false;
            if (missile.getCustomData().containsKey(PROCESSED_KEY)) return false;

            WeaponAPI weapon = missile.getWeapon();
            if (weapon == null || missile.isFromMissile()) return false;
            return !isPointDefense(weapon) || isSemibreve(weapon);
        }

        private boolean beginDecoyBatch(WeaponAPI weapon, float now) {
            if (weapon == null) return false;
            Float batchEnd = decoyBatchEnds.get(weapon);
            if (batchEnd != null && now < batchEnd) return false;
            decoyBatchEnds.put(weapon, now + getBatchWindow(weapon));
            return true;
        }

        private void applyGuidedSpeedBonus(MissileAPI missile) {
            MutableShipStatsAPI engineStats = missile.getEngineStats();
            if (engineStats == null) return;
            engineStats.getMaxSpeed().modifyMult(SPEED_MOD_ID, GUIDED_MISSILE_SPEED_MULT);
            missile.updateMaxSpeed();
        }

        private void spawnDecoys(CombatEngineAPI engine, MissileAPI parent, WeaponAPI weapon, int count) {
            float parentSpeed = parent.getMaxSpeed();
            if (parentSpeed <= 0f && parent.getVelocity() != null) {
                parentSpeed = parent.getVelocity().length();
            }
            float decoySpeed = Math.max(1f, parentSpeed * DECOY_SPEED_MULT);
            float center = (count - 1) * 0.5f;

            for (int i = 0; i < count; i++) {
                float angle = parent.getFacing() + (i - center) * DECOY_SPREAD_DEGREES;
                double radians = Math.toRadians(angle);
                Vector2f velocity = new Vector2f(
                        (float) Math.cos(radians) * decoySpeed,
                        (float) Math.sin(radians) * decoySpeed);

                CombatEntityAPI entity = engine.spawnProjectile(
                        ship,
                        weapon,
                        DECOY_WEAPON_ID,
                        new Vector2f(parent.getLocation()),
                        angle,
                        velocity);
                if (!(entity instanceof MissileAPI)) continue;

                MissileAPI decoy = (MissileAPI) entity;
                decoy.setCustomData(PROCESSED_KEY, Boolean.TRUE);
                decoy.setDamageAmount(0f);
                decoy.setMaxFlightTime(getDecoyLifetime(weapon));

                MutableShipStatsAPI engineStats = decoy.getEngineStats();
                if (engineStats != null) {
                    float currentMaxSpeed = decoy.getMaxSpeed();
                    engineStats.getMaxSpeed().modifyFlat(
                            DECOY_SPEED_MOD_ID,
                            decoySpeed - currentMaxSpeed);
                    decoy.updateMaxSpeed();
                }
                decoy.getVelocity().set(velocity);
            }
        }
    }
}
