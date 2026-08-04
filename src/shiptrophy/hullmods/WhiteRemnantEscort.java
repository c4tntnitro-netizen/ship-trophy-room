package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/** Gives the Remnant escorts rebuilt in Gan Eden a pale ceramic finish. */
public final class WhiteRemnantEscort extends BaseHullMod {
    public static final String HULLMOD_ID =
            "ship_trophy_white_remnant_escort";

    private static final Color WEAPON_TINT = new Color(232, 237, 241);
    private static final Color VENT_CORE = new Color(245, 250, 255);
    private static final Color VENT_FRINGE = new Color(174, 205, 226);
    private static final Color EXPLOSION = new Color(216, 232, 244);

    public static String getWhiteHullId(String baseHullId) {
        if (baseHullId == null) return null;
        if ("glimmer".equals(baseHullId)
                || "lumen".equals(baseHullId)
                || "fulgent".equals(baseHullId)
                || "scintilla".equals(baseHullId)
                || "brilliant".equals(baseHullId)
                || "apex".equals(baseHullId)
                || "nova".equals(baseHullId)
                || "radiant".equals(baseHullId)) {
            return "ship_trophy_white_" + baseHullId;
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(
            HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Ivory Custodians are prizes as well as gatekeepers. A large flat
        // bonus guarantees that a disabled hull can enter the normal recovery
        // list; Automated Ships still governs whether the player can field it.
        stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD)
                .modifyFlat(id, 1000f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) return;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            setColor(weapon.getSprite());
            setColor(weapon.getUnderSpriteAPI());
            setColor(weapon.getBarrelSpriteAPI());
        }
        ship.setVentCoreColor(VENT_CORE);
        ship.setVentFringeColor(VENT_FRINGE);
        ship.setExplosionFlashColorOverride(EXPLOSION);
    }

    private static void setColor(SpriteAPI sprite) {
        if (sprite != null) sprite.setColor(WEAPON_TINT);
    }
}
