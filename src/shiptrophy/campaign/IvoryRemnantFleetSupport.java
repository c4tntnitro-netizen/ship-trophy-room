package shiptrophy.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;

import shiptrophy.hullmods.WhiteRemnantEscort;

/** Shared, non-destructive conversion of generated Remnants into Ivory hulls. */
public final class IvoryRemnantFleetSupport {
    private IvoryRemnantFleetSupport() {
    }

    public static void refitFleet(CampaignFleetAPI fleet) {
        if (fleet == null) return;
        boolean changed = false;
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member == null || member.isFighterWing()) continue;
            changed |= refitMember(member);
            readyMember(member);
        }
        if (changed) fleet.forceSync();
    }

    public static boolean refitMember(FleetMemberAPI member) {
        if (member == null || member.getVariant() == null) return false;
        ShipVariantAPI current = member.getVariant();
        String whiteHullId = WhiteRemnantEscort.getWhiteHullId(
                current.getHullSpec().getBaseHullId());
        if (whiteHullId == null
                || Global.getSettings().getHullSpec(whiteHullId) == null) {
            return false;
        }
        if (whiteHullId.equals(current.getHullSpec().getHullId())
                && current.hasHullMod(WhiteRemnantEscort.HULLMOD_ID)
                && !current.hasTag(Tags.UNRECOVERABLE)) {
            return false;
        }

        // FleetFactoryV3 commonly returns shared stock variants. Clone before
        // replacing the hull spec so unrelated Remnant fleets stay untouched.
        ShipVariantAPI variant = current.clone();
        variant.setSource(VariantSource.REFIT);
        variant.setHullSpecAPI(Global.getSettings().getHullSpec(whiteHullId));
        variant.removeTag(Tags.UNRECOVERABLE);
        variant.addPermaMod(WhiteRemnantEscort.HULLMOD_ID);
        member.setVariant(variant, false, false);
        return true;
    }

    public static void readyMember(FleetMemberAPI member) {
        if (member == null) return;
        member.getRepairTracker().setMothballed(false);
        member.getRepairTracker().setCR(
                member.getRepairTracker().getMaxCR());
        member.updateStats();
    }
}
