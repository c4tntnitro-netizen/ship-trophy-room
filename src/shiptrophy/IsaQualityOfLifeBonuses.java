package shiptrophy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

/** Fleet-wide conveniences supplied by Isa after she permanently joins. */
public final class IsaQualityOfLifeBonuses {
    private static final String SENSOR_MOD = "ship_trophy_isa_sensor_baffling";
    private static final String BURN_MOD = "ship_trophy_isa_drive_tuning";
    private static final String CREW_PAY_PROGRESS = "$shipTrophyIsaCrewPayReductionProgress";
    private static final float BASE_CREW_PAY_PER_MONTH = 10f;
    private static final float DAYS_PER_MONTH = 30f;

    private IsaQualityOfLifeBonuses() {
    }

    public static void advance(float elapsedDays) {
        CampaignFleetAPI fleet = Global.getSector() == null
                ? null : Global.getSector().getPlayerFleet();
        if (fleet == null) return;

        boolean active = IsaTrophyManager.wasOfficerGranted()
                && HallOfTriumphFeatures.areIsaQualityOfLifeBonusesEnabled();
        if (!active) {
            fleet.getStats().getSensorProfileMod().unmodify(SENSOR_MOD);
            fleet.getStats().getFleetwideMaxBurnMod().unmodify(BURN_MOD);
            return;
        }

        float sensorReduction = HallOfTriumphFeatures
                .getIsaSensorProfileReductionPercent();
        float burnBonus = HallOfTriumphFeatures.getIsaBurnLevelBonus();
        fleet.getStats().getSensorProfileMod().modifyPercent(
                SENSOR_MOD, -sensorReduction, "Isa: fleetwide sensor baffling");
        fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(
                BURN_MOD, burnBonus, "Isa: synchronized drive tuning");

        reimburseCrewPay(fleet, elapsedDays);
    }

    private static void reimburseCrewPay(CampaignFleetAPI fleet, float elapsedDays) {
        if (fleet.getCargo() == null || elapsedDays <= 0f) return;
        float reduction = HallOfTriumphFeatures.getIsaCrewPayReductionPercent() / 100f;
        if (reduction <= 0f) return;

        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        float progress = memory.contains(CREW_PAY_PROGRESS)
                ? memory.getFloat(CREW_PAY_PROGRESS) : 0f;
        progress += fleet.getCargo().getCrew() * BASE_CREW_PAY_PER_MONTH
                * reduction * elapsedDays / DAYS_PER_MONTH;
        if (!Float.isFinite(progress) || progress < 0f) progress = 0f;

        int credits = (int) progress;
        if (credits > 0) {
            fleet.getCargo().getCredits().add(credits);
            progress -= credits;
        }
        memory.set(CREW_PAY_PROGRESS, progress);
    }
}
