package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class Memory extends BaseTrophyDoctrineHullMod {
    public static final String HULLMOD_ID = "ship_trophy_memory";
    public static final String SUBTYPE_ID = "domain_derelict";
    public static final float HULL_BONUS = 80f;
    public static final float ARMOR_BONUS = 300f;
    public static final float SPEED_BONUS_FRIGATE = 25f;
    public static final float SPEED_BONUS_DESTROYER = 20f;
    public static final float SPEED_BONUS_CRUISER = 15f;
    public static final float SPEED_BONUS_CAPITAL = 15f;

    @Override
    protected String getSubtypeId() {
        return SUBTYPE_ID;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship) && isDomainDerelict(ship) && !hasUnstableInjector(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        String baseReason = super.getUnapplicableReason(ship);
        if (baseReason != null) return baseReason;
        if (!isDomainDerelict(ship)) return "Can only be installed on Derelict or Explorarium ships";
        if (hasUnstableInjector(ship)) return "Incompatible with Unstable Injector";
        return null;
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;
        if (stats.getVariant() != null && stats.getVariant().hasHullMod(HullMods.UNSTABLE_INJECTOR)) return;
        stats.getHullBonus().modifyPercent(id, HULL_BONUS);
        stats.getArmorBonus().modifyFlat(id, ARMOR_BONUS);
        stats.getMaxSpeed().modifyFlat(id, getSpeedBonus(hullSize));
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize,
            ShipAPI ship, float width, boolean isForModSpec) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
        float opad = 10f;
        tooltip.addPara("Can only be installed on %s ships and is incompatible with %s.",
                opad, Misc.getHighlightColor(), "Derelict or Explorarium", "Unstable Injector");
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Math.round(HULL_BONUS) + "%";
        if (index == 1) return "" + Math.round(ARMOR_BONUS);
        if (index == 2) return "" + Math.round(SPEED_BONUS_FRIGATE);
        if (index == 3) return "" + Math.round(SPEED_BONUS_DESTROYER);
        if (index == 4) return "" + Math.round(SPEED_BONUS_CRUISER);
        if (index == 5) return "" + Math.round(SPEED_BONUS_CAPITAL);
        return null;
    }

    public static boolean isDomainDerelict(ShipAPI ship) {
        if (ship == null || ship.getHullSpec() == null) return false;
        String manufacturer = ship.getHullSpec().getManufacturer();
        if (manufacturer == null) return false;
        manufacturer = manufacturer.toLowerCase();
        return manufacturer.contains("explorarium") || manufacturer.contains("derelict");
    }

    private float getSpeedBonus(ShipAPI.HullSize hullSize) {
        if (hullSize == ShipAPI.HullSize.FRIGATE) return SPEED_BONUS_FRIGATE;
        if (hullSize == ShipAPI.HullSize.DESTROYER) return SPEED_BONUS_DESTROYER;
        if (hullSize == ShipAPI.HullSize.CRUISER) return SPEED_BONUS_CRUISER;
        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) return SPEED_BONUS_CAPITAL;
        return 0f;
    }

    private boolean hasUnstableInjector(ShipAPI ship) {
        return ship != null && ship.getVariant() != null
                && ship.getVariant().hasHullMod(HullMods.UNSTABLE_INJECTOR);
    }
}
