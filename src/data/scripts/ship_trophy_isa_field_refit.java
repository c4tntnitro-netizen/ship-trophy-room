package data.scripts;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ship_trophy_isa_field_refit {
    public static final float DAMAGE_TAKEN_MULT = 0.9f;

    public static class Level1 implements ShipSkillEffect {
        @Override
        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            stats.getShieldDamageTakenMult().modifyMult(id, DAMAGE_TAKEN_MULT);
            stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_TAKEN_MULT);
            stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_TAKEN_MULT);
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getShieldDamageTakenMult().unmodify(id);
            stats.getArmorDamageTakenMult().unmodify(id);
            stats.getHullDamageTakenMult().unmodify(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "10% less damage taken by shields, armor, and hull";
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
