package shiptrophy.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class IronShellDiscipline extends BaseTrophyDoctrineHullMod {
    public static final String SUBTYPE_ID = "iron_shell";
    public static final String HULLMOD_ID = "ship_trophy_iron_shell_drill";
    public static final String HANDLER_HULLMOD_ID = "ship_trophy_iaido_system_handler";
    public static final String IAIDO_SYSTEM_ID = "ship_trophy_iaido";
    public static final String IRON_SHELL_MOD_ID = "timid_xiv";

    public static final float TORPEDO_SPEED_MULT = 3f;
    public static final float KINETIC_DAMAGE_FRACTION = 0.50f;

    private static final String ORIGINAL_SYSTEM_TAG_PREFIX = "ship_trophy_iaido_original_system:";
    private static final String NO_SYSTEM_SENTINEL = "__none__";
    private static final String SEEN_KEY = "ship_trophy_iaido_seen";
    private static final String BOOSTED_KEY = "ship_trophy_iaido_boosted";
    private static final String KINETIC_DAMAGE_KEY = "ship_trophy_iaido_kinetic_damage";
    private static final String MISSILE_SPEED_MOD_ID = "ship_trophy_iaido_missile_speed";
    private static final String TORPEDO_LAUNCH_SOUND_ID = "ship_trophy_iaido_torpedo_launch";
    private static final float LAUNCH_WINDOW_GRACE = 0.15f;
    private static final Color LAUNCH_COLOR = new Color(255, 190, 80, 210);
    private static final Color MISSILE_JITTER_COLOR = new Color(255, 150, 50, 120);

    @Override
    protected String getSubtypeId() {
        return SUBTYPE_ID;
    }

    @Override
    protected boolean matchesStyle(ShipAPI ship) {
        return isIronShellShip(ship);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return isValidIronShellCombatShip(ship) && super.isApplicableToShip(ship);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return isUnlocked() && isValidIronShellCombatShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!isIronShellShip(ship)) return "Can only be installed on Iron Shell ships";
        if (ship != null && (ship.isFighter() || ship.isStation() || ship.isStationModule())) {
            return "Cannot be installed on fighters, stations, or station modules";
        }
        return super.getUnapplicableReason(ship);
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        installIaido(stats);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (!isUnlocked() || !isValidIronShellCombatShip(ship) || ship.getSystem() == null
                || !IAIDO_SYSTEM_ID.equals(ship.getSystem().getId())
                || ship.hasListenerOfClass(IaidoTorpedoListener.class)) {
            return;
        }
        ship.addListener(new IaidoTorpedoListener(ship));
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "Iaido";
        if (index == 1) return "50%";
        if (index == 2) return "50%";
        if (index == 3) return Math.round((TORPEDO_SPEED_MULT - 1f) * 100f) + "%";
        if (index == 4) return Math.round(KINETIC_DAMAGE_FRACTION * 100f) + "%";
        return null;
    }

    public static boolean isIronShellShip(ShipAPI ship) {
        return ship != null && isIronShellHull(ship.getHullSpec());
    }

    public static boolean isIronShellHull(ShipHullSpecAPI spec) {
        if (spec == null) return false;

        ModSpecAPI source = spec.getSourceMod();
        if (source != null && IRON_SHELL_MOD_ID.equalsIgnoreCase(source.getId())) return true;

        String hullId = lower(spec.getHullId());
        String baseHullId = lower(spec.getBaseHullId());
        String manufacturer = lower(spec.getManufacturer());
        return hullId.startsWith("eis_")
                || baseHullId.startsWith("eis_")
                || manufacturer.contains("iron shell");
    }

    public static boolean isTorpedoWeapon(WeaponAPI weapon) {
        if (weapon == null || weapon.isDecorative() || weapon.getType() != WeaponAPI.WeaponType.MISSILE) {
            return false;
        }
        if (weapon.hasAIHint(WeaponAPI.AIHints.PD)
                || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
                || weapon.hasAIHint(WeaponAPI.AIHints.PD_ALSO)) {
            return false;
        }

        WeaponSpecAPI spec = weapon.getSpec();
        if (spec == null) return false;
        if (containsTorpedo(spec.getPrimaryRoleStr())
                || containsTorpedo(spec.getWeaponName())
                || containsTorpedo(spec.getCustomPrimary())
                || containsTorpedo(spec.getCustomAncillary())) {
            return true;
        }

        Set<String> tags = spec.getTags();
        if (tags != null) {
            for (String tag : tags) {
                if (containsTorpedo(tag)) return true;
            }
        }
        return false;
    }

    public static void installIaido(MutableShipStatsAPI stats) {
        if (stats == null || stats.getVariant() == null) return;
        ShipVariantAPI variant = stats.getVariant();
        ShipHullSpecAPI hullSpec = variant.getHullSpec();
        if (!isIronShellHull(hullSpec)) return;

        String currentSystem = hullSpec.getShipSystemId();
        if (!IAIDO_SYSTEM_ID.equals(currentSystem)) {
            rememberOriginalSystem(variant, currentSystem);
        }
        if (!variant.hasHullMod(HANDLER_HULLMOD_ID)) {
            variant.addPermaMod(HANDLER_HULLMOD_ID);
        }
        hullSpec.setShipSystemId(IAIDO_SYSTEM_ID);
    }

    public static void restoreOriginalSystemIfNeeded(MutableShipStatsAPI stats) {
        if (stats == null || stats.getVariant() == null) return;
        ShipVariantAPI variant = stats.getVariant();
        ShipHullSpecAPI hullSpec = variant.getHullSpec();
        if (hullSpec == null || !IAIDO_SYSTEM_ID.equals(hullSpec.getShipSystemId())) return;

        String original = getRememberedOriginalSystem(variant);
        if (original == null) return;
        hullSpec.setShipSystemId(NO_SYSTEM_SENTINEL.equals(original) ? null : original);
    }

    private static boolean isValidIronShellCombatShip(ShipAPI ship) {
        return isIronShellShip(ship)
                && !ship.isFighter()
                && !ship.isStation()
                && !ship.isStationModule();
    }

    private static void rememberOriginalSystem(ShipVariantAPI variant, String systemId) {
        for (String tag : new ArrayList<String>(variant.getTags())) {
            if (tag != null && tag.startsWith(ORIGINAL_SYSTEM_TAG_PREFIX)) {
                variant.removeTag(tag);
            }
        }
        String value = systemId == null || systemId.length() <= 0 ? NO_SYSTEM_SENTINEL : systemId;
        variant.addTag(ORIGINAL_SYSTEM_TAG_PREFIX + value);
    }

    private static String getRememberedOriginalSystem(ShipVariantAPI variant) {
        for (String tag : variant.getTags()) {
            if (tag != null && tag.startsWith(ORIGINAL_SYSTEM_TAG_PREFIX)) {
                return tag.substring(ORIGINAL_SYSTEM_TAG_PREFIX.length());
            }
        }
        return null;
    }

    private static boolean containsTorpedo(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains("torpedo");
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static boolean isLaunchWindow(ShipSystemAPI system) {
        if (system == null || !IAIDO_SYSTEM_ID.equals(system.getId())) return false;
        ShipSystemAPI.SystemState state = system.getState();
        return state == ShipSystemAPI.SystemState.IN || state == ShipSystemAPI.SystemState.ACTIVE;
    }

    public static class IaidoTorpedoListener implements AdvanceableListener, DamageDealtModifier {
        private final ShipAPI ship;
        private float launchWindowUntil = -1f;

        public IaidoTorpedoListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (ship == null || ship.isHulk() || amount <= 0f) return;

            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null || engine.isPaused()) return;

            float now = engine.getTotalElapsedTime(false);
            if (isLaunchWindow(ship.getSystem())) {
                launchWindowUntil = now + LAUNCH_WINDOW_GRACE;
            }

            for (MissileAPI missile : engine.getMissiles()) {
                if (missile == null || missile.getSource() != ship) continue;

                if (missile.getCustomData().containsKey(BOOSTED_KEY)) {
                    missile.setJitter(this, MISSILE_JITTER_COLOR, 0.7f, 3, 2f, 7f);
                }
                if (missile.getCustomData().containsKey(SEEN_KEY)) continue;
                missile.setCustomData(SEEN_KEY, Boolean.TRUE);

                if (now > launchWindowUntil || !isDirectTorpedo(missile)) continue;
                empowerTorpedo(engine, missile);
            }
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage,
                Vector2f point, boolean shieldHit) {
            if (!(param instanceof MissileAPI) || target == null) return null;
            MissileAPI missile = (MissileAPI) param;
            Object stored = missile.getCustomData().remove(KINETIC_DAMAGE_KEY);
            if (!(stored instanceof Number)) return null;

            float bonusDamage = ((Number) stored).floatValue();
            if (bonusDamage <= 0f) return null;

            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null) return null;
            Vector2f hitPoint = point == null ? new Vector2f(target.getLocation()) : new Vector2f(point);
            engine.applyDamage(target, hitPoint, bonusDamage, DamageType.KINETIC,
                    0f, false, false, ship);
            engine.addHitParticle(hitPoint, target.getVelocity(), 45f, 1f, 0.15f, LAUNCH_COLOR);
            return null;
        }

        private boolean isDirectTorpedo(MissileAPI missile) {
            if (missile.isFlare() || missile.isMine() || missile.isFromMissile()) return false;
            return isTorpedoWeapon(missile.getWeapon());
        }

        private void empowerTorpedo(CombatEngineAPI engine, MissileAPI missile) {
            missile.setCustomData(BOOSTED_KEY, Boolean.TRUE);

            float originalMaxSpeed = Math.max(1f, missile.getMaxSpeed());
            Vector2f boostedVelocity = getBoostedLaunchVelocity(missile, originalMaxSpeed);
            float boostedMaxSpeed = Math.max(
                    originalMaxSpeed * TORPEDO_SPEED_MULT,
                    boostedVelocity.length());

            MutableShipStatsAPI engineStats = missile.getEngineStats();
            if (engineStats != null) {
                engineStats.getMaxSpeed().modifyFlat(
                        MISSILE_SPEED_MOD_ID,
                        boostedMaxSpeed - originalMaxSpeed);
                engineStats.getAcceleration().modifyMult(MISSILE_SPEED_MOD_ID, TORPEDO_SPEED_MULT);
                missile.updateMaxSpeed();
            }
            if (missile.getVelocity() != null) {
                missile.getVelocity().set(boostedVelocity);
            }

            float kineticDamage = missile.getDamageAmount() * KINETIC_DAMAGE_FRACTION;
            missile.setCustomData(KINETIC_DAMAGE_KEY, Float.valueOf(Math.max(0f, kineticDamage)));

            engine.addSmoothParticle(
                    new Vector2f(missile.getLocation()),
                    new Vector2f(boostedVelocity),
                    80f,
                    1f,
                    0.35f,
                    LAUNCH_COLOR);

            Vector2f soundVelocity = ship.getVelocity() == null
                    ? new Vector2f()
                    : new Vector2f(ship.getVelocity());
            Global.getSoundPlayer().playSound(
                    TORPEDO_LAUNCH_SOUND_ID,
                    1f,
                    1f,
                    missile.getLocation(),
                    soundVelocity);
        }

        private Vector2f getBoostedLaunchVelocity(MissileAPI missile, float originalMaxSpeed) {
            Vector2f shipVelocity = ship.getVelocity() == null
                    ? new Vector2f()
                    : new Vector2f(ship.getVelocity());
            Vector2f relativeVelocity = missile.getVelocity() == null
                    ? new Vector2f()
                    : Vector2f.sub(missile.getVelocity(), shipVelocity, null);

            if (relativeVelocity.x * relativeVelocity.x + relativeVelocity.y * relativeVelocity.y < 1f) {
                double radians = Math.toRadians(missile.getFacing());
                relativeVelocity.set(
                        (float) Math.cos(radians) * originalMaxSpeed,
                        (float) Math.sin(radians) * originalMaxSpeed);
            }

            relativeVelocity.scale(TORPEDO_SPEED_MULT);
            return Vector2f.add(shipVelocity, relativeVelocity, relativeVelocity);
        }
    }
}
