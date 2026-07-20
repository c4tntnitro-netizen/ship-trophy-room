package data.scripts;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ship_trophy_isa_redline {
    public static final float TOP_SPEED_BONUS = 10f;
    public static final float MANEUVERABILITY_BONUS = 20f;

    public static class Level1 implements ShipSkillEffect {
        @Override
        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            stats.getMaxSpeed().modifyPercent(id, TOP_SPEED_BONUS);
            stats.getAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
            stats.getDeceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
            stats.getMaxTurnRate().modifyPercent(id, MANEUVERABILITY_BONUS);
            stats.getTurnAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
            stats.getTurnAcceleration().unmodify(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "+10% top speed; +20% acceleration, deceleration, and maneuverability";
        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
