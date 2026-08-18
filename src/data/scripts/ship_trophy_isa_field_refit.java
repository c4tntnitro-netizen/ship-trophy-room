package data.scripts;

import java.awt.Color;

import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.Misc;

public class ship_trophy_isa_field_refit {
    public static final float HULL_DAMAGE_TAKEN_MULT = 0.6f;
    public static final float CREW_LOSS_MULT = 0.25f;
    public static final float ARMOR_REPAIR_RATE = 0.001f;
    public static final float ARMOR_REPAIR_LIMIT = 1000f;
    public static final float ARMOR_REPAIR_LIMIT_FRACTION = 0.15f;

    public static class UniqueSkillDescription implements DescriptionSkillEffect {
        private static final String LABEL = "Isa's Unique Skill:";

        @Override
        public String getString() {
            return LABEL;
        }

        @Override
        public Color[] getHighlightColors() {
            return new Color[] { Misc.getHighlightColor() };
        }

        @Override
        public String[] getHighlights() {
            return new String[] { LABEL };
        }

        @Override
        public Color getTextColor() {
            return null;
        }
    }

    public static class Level1 implements ShipSkillEffect {
        @Override
        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            stats.getHullDamageTakenMult().modifyMult(id, HULL_DAMAGE_TAKEN_MULT);
            stats.getCrewLossMult().modifyMult(id, CREW_LOSS_MULT);
            stats.getWeaponDamageTakenMult().modifyMult(id, 0f);
            stats.getEngineDamageTakenMult().modifyMult(id, 0f);
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getHullDamageTakenMult().unmodify(id);
            stats.getCrewLossMult().unmodify(id);
            stats.getWeaponDamageTakenMult().unmodify(id);
            stats.getEngineDamageTakenMult().unmodify(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "40% less hull damage taken; 75% fewer crew casualties; "
                    + "weapons and engines cannot be disabled";
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

    public static class Elite implements AfterShipCreationSkillEffect {
        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.removeListenerOfClass(ArmorRepairListener.class);
            ship.addListener(new ArmorRepairListener(ship));
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.removeListenerOfClass(ArmorRepairListener.class);
        }

        @Override
        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
        }

        @Override
        public String getEffectDescription(float level) {
            return "Regenerates 0.1% of total armor per second, up to the lesser of "
                    + "1,000 armor or 15% of total armor";
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

    public static class ArmorRepairListener implements AdvanceableListener {
        private final ShipAPI ship;
        private final float repairRate;
        private final float repairLimit;
        private float repaired;

        public ArmorRepairListener(ShipAPI ship) {
            this.ship = ship;
            float totalArmor = Math.max(0f, ship.getArmorGrid().getArmorRating());
            repairRate = totalArmor * ARMOR_REPAIR_RATE;
            repairLimit = Math.min(ARMOR_REPAIR_LIMIT, totalArmor * ARMOR_REPAIR_LIMIT_FRACTION);
        }

        @Override
        public void advance(float amount) {
            if (amount <= 0f || repaired >= repairLimit || !ship.isAlive() || ship.isHulk()) return;

            ArmorGridAPI armor = ship.getArmorGrid();
            float[][] grid = armor.getGrid();
            if (grid == null || grid.length == 0 || grid[0].length == 0) return;

            float maxInCell = armor.getMaxArmorInCell();
            float totalMissing = 0f;
            for (int x = 0; x < grid.length; x++) {
                for (int y = 0; y < grid[x].length; y++) {
                    totalMissing += Math.max(0f, maxInCell - grid[x][y]);
                }
            }
            if (totalMissing <= 0f) return;

            float repairAmount = Math.min(repairRate * amount, repairLimit - repaired);
            repairAmount = Math.min(repairAmount, totalMissing);
            if (repairAmount <= 0f) return;

            for (int x = 0; x < grid.length; x++) {
                for (int y = 0; y < grid[x].length; y++) {
                    float missing = Math.max(0f, maxInCell - grid[x][y]);
                    if (missing <= 0f) continue;
                    float cellRepair = repairAmount * (missing / totalMissing);
                    armor.setArmorValue(x, y, Math.min(maxInCell, grid[x][y] + cellRepair));
                }
            }

            repaired += repairAmount;
            ship.syncWithArmorGridState();
            ship.syncWeaponDecalsWithArmorDamage();
        }
    }
}
