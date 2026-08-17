package shiptrophy.gallery;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;

/**
 * Save-compatibility shim for builds that serialized the former Gallery CR
 * recovery reward script. New saves never add this script.
 */
@Deprecated
public final class GalleryShuttleCRRecoveryScript implements EveryFrameScript {
    private static final String MODIFIER_PREFIX =
            "ship_trophy_gallery_shuttle_cr_recovery_";
    private static final float REFRESH_SECONDS = 0.5f;

    // Retained solely so older XStream saves see the fields they serialized.
    @SuppressWarnings("unused")
    private final IntervalUtil refresh = new IntervalUtil(
            REFRESH_SECONDS, REFRESH_SECONDS);
    @SuppressWarnings("unused")
    private boolean initialized;
    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        clearCurrentFleetBonus();
        done = true;
    }

    /** Removes modifiers left behind by the discontinued Gallery reward. */
    public static void clearCurrentFleetBonus() {
        if (Global.getSector() == null) return;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getFleetData() == null) return;

        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member == null || member.getStats() == null) continue;
            String modifierId = MODIFIER_PREFIX + memberKey(member);
            MutableStat recovery = member.getStats()
                    .getBaseCRRecoveryRatePercentPerDay();
            fleet.getStats().removeTemporaryMod(modifierId);
            recovery.unmodify(modifierId);
        }
    }

    private static String memberKey(FleetMemberAPI member) {
        String id = safe(member.getId());
        if (!id.isEmpty()) return id;
        return Integer.toHexString(System.identityHashCode(member));
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
