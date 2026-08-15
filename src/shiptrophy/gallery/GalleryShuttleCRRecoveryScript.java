package shiptrophy.gallery;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import shiptrophy.ShipTrophyL10n;

/** Keeps the selected Hall tour shuttle's CR recovery benefit on the fleet. */
public final class GalleryShuttleCRRecoveryScript implements EveryFrameScript {
    public static final String SHUTTLE_MEMORY =
            "$ship_trophy_gallery_tour_shuttle";

    private static final String MODIFIER_PREFIX =
            "ship_trophy_gallery_shuttle_cr_recovery_";
    private static final float REFRESH_SECONDS = 0.5f;
    private static final float TEMPORARY_DURATION = 1f;

    private final IntervalUtil refresh = new IntervalUtil(
            REFRESH_SECONDS, REFRESH_SECONDS);
    private boolean initialized;

    /** Records the Gallery selection immediately, including while paused. */
    public static void rememberSelection(FleetMemberAPI shuttle) {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                SHUTTLE_MEMORY,
                ShipGalleryData.isTourShuttleEligible(shuttle)
                        ? safe(shuttle.getId()) : "");
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null) return;

        if (!initialized) {
            initialized = true;
            refreshFleetBonus();
            return;
        }

        refresh.advance(Math.max(0f, amount));
        if (refresh.intervalElapsed()) refreshFleetBonus();
    }

    private void refreshFleetBonus() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getFleetData() == null) return;

        FleetMemberAPI shuttle = findSelectedShuttle();
        float bonus = ShipGalleryData.getTourCRRecoveryBonus(shuttle);
        String description = ShipTrophyL10n.get("gallery_tour_shuttle");

        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member == null || member.getStats() == null) continue;
            String modifierId = MODIFIER_PREFIX + memberKey(member);
            MutableStat recovery = member.getStats()
                    .getBaseCRRecoveryRatePercentPerDay();
            if (bonus > 0f) {
                fleet.getStats().addTemporaryModPercent(
                        TEMPORARY_DURATION, modifierId, description,
                        bonus, recovery);
            } else {
                fleet.getStats().removeTemporaryMod(modifierId);
                recovery.unmodify(modifierId);
            }
        }
    }

    private FleetMemberAPI findSelectedShuttle() {
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        String selectedId = memory.contains(SHUTTLE_MEMORY)
                ? safe(memory.getString(SHUTTLE_MEMORY)) : "";
        if (selectedId.isEmpty()) return null;

        for (FleetMemberAPI member : ShipGalleryData.getAllShips()) {
            if (member != null
                    && selectedId.equals(safe(member.getId()))
                    && ShipGalleryData.isTourShuttleEligible(member)) {
                return member;
            }
        }

        memory.set(SHUTTLE_MEMORY, "");
        return null;
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
