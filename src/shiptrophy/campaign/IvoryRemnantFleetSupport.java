package shiptrophy.campaign;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;

/** Shared, non-destructive conversion of generated Remnants into Ivory hulls. */
public final class IvoryRemnantFleetSupport {
    public static final String LEGACY_HULLMOD_ID =
            "ship_trophy_white_remnant_escort";
    private static final String LEGACY_MIGRATION_KEY =
            "$shipTrophyIvoryLegacyHullmodMigrationV2";
    private static final String[] IVORY_BUILT_INS = {
        "insulatedengine",
        "fluxbreakers",
        "solar_shielding",
        "stabilizedshieldemitter",
    };

    private IvoryRemnantFleetSupport() {
    }

    public static boolean refitFleet(CampaignFleetAPI fleet) {
        if (fleet == null) return false;
        boolean changed = false;
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member == null || member.isFighterWing()) continue;
            if (member.getVariant() == null
                    || getWhiteHullId(member.getHullSpec().getBaseHullId())
                            == null) {
                // FleetFactory draws from the globally modifiable Remnant
                // pool. Unsupported mod-added hulls have no baked Ivory art
                // and therefore do not belong in this authored fleet.
                if (member.getCaptain() != null
                        && fleet.getFleetData().getOfficerData(
                                member.getCaptain()) != null) {
                    fleet.getFleetData().removeOfficer(member.getCaptain());
                }
                fleet.getFleetData().removeFleetMember(member);
                changed = true;
                continue;
            }
            boolean memberChanged = refitMember(member);
            changed |= memberChanged;
            if (memberChanged) readyMember(member);
        }
        if (changed && !fleet.isEmpty()) {
            fleet.getFleetData().ensureHasFlagship();
            FleetMemberAPI flagship = fleet.getFlagship();
            if (flagship != null && flagship.getCaptain() != null) {
                fleet.setCommander(flagship.getCaptain());
            }
            fleet.forceSync();
        }
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member == null || member.isFighterWing()) continue;
            if (!isIvoryMemberComplete(member)) return false;
        }
        return !fleet.isEmpty();
    }

    /** True for a fully converted member even when no mutation was needed. */
    public static boolean isIvoryMemberComplete(FleetMemberAPI member) {
        if (member == null
                || member.getVariant() == null
                || member.getHullSpec() == null) {
            return false;
        }
        ShipVariantAPI variant = member.getVariant();
        String whiteHullId = getWhiteHullId(
                member.getHullSpec().getBaseHullId());
        return whiteHullId != null
                && whiteHullId.equals(member.getHullSpec().getHullId())
                && !variant.hasHullMod(LEGACY_HULLMOD_ID)
                && !variant.hasTag(Tags.UNRECOVERABLE)
                && !variant.hasTag(Tags.VARIANT_UNBOARDABLE)
                && !variant.hasTag(Tags.AUTOMATED_RECOVERABLE)
                && variant.hasTag(Tags.VARIANT_ALWAYS_RECOVERABLE)
                && !variant.hasTag(Tags.SHIP_RECOVERABLE)
                && !hasDuplicateIvoryBuiltInBookkeeping(variant);
    }

    public static boolean refitMember(FleetMemberAPI member) {
        if (member == null || member.getVariant() == null) return false;
        ShipVariantAPI current = member.getVariant();
        String whiteHullId = getWhiteHullId(
                current.getHullSpec().getBaseHullId());
        if (whiteHullId == null
                || Global.getSettings().getHullSpec(whiteHullId) == null) {
            return false;
        }
        if (isIvoryMemberComplete(member)) return false;

        // FleetFactoryV3 commonly returns shared stock variants. Clone before
        // replacing the hull spec so unrelated Remnant fleets stay untouched.
        ShipVariantAPI variant = current.clone();
        variant.setSource(VariantSource.REFIT);
        Set<String> preservedBuiltInSMods = new HashSet<String>();
        // These are provided by every Ivory skin. Remove loadout-level copies
        // before swapping hull specs so stock variants do not carry duplicate
        // normal/permanent/S-mod bookkeeping for the same built-in effect.
        for (String hullmodId : IVORY_BUILT_INS) {
            if (current.getSMods().contains(hullmodId)
                    || current.getSModdedBuiltIns().contains(hullmodId)) {
                preservedBuiltInSMods.add(hullmodId);
            }
            variant.removePermaMod(hullmodId);
            variant.removeMod(hullmodId);
            variant.removeSuppressedMod(hullmodId);
            variant.getSMods().remove(hullmodId);
            variant.getSModdedBuiltIns().remove(hullmodId);
        }
        variant.setHullSpecAPI(Global.getSettings().getHullSpec(whiteHullId));
        for (String hullmodId : preservedBuiltInSMods) {
            // This effect is intrinsic to the Ivory skin now. Preserve any
            // S-mod bonus in the dedicated built-in set without recreating a
            // duplicate fitted/permanent hullmod entry.
            variant.getSModdedBuiltIns().add(hullmodId);
        }
        variant.removeTag(Tags.UNRECOVERABLE);
        variant.removeTag(Tags.VARIANT_UNBOARDABLE);
        // The Ivory skin itself carries auto_rec; keeping the same marker on
        // every generated variant is redundant and complicates migrations.
        variant.removeTag(Tags.AUTOMATED_RECOVERABLE);
        variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
        // Vanilla owns this transient marker and adds it only after preparing
        // a specific casualty for post-battle recovery.
        variant.removeTag(Tags.SHIP_RECOVERABLE);
        variant.removePermaMod(LEGACY_HULLMOD_ID);
        variant.removeMod(LEGACY_HULLMOD_ID);
        variant.removeSuppressedMod(LEGACY_HULLMOD_ID);
        member.setVariant(variant, false, false);
        return true;
    }

    private static boolean hasDuplicateIvoryBuiltInBookkeeping(
            ShipVariantAPI variant) {
        if (variant == null) return true;
        for (String hullmodId : IVORY_BUILT_INS) {
            if (variant.getPermaMods().contains(hullmodId)
                    || variant.getSMods().contains(hullmodId)
                    || variant.getNonBuiltInHullmods().contains(hullmodId)
                    || variant.getSuppressedMods().contains(hullmodId)) {
                return true;
            }
        }
        return false;
    }

    /** Removes the retired runtime Ivory marker from active and stored ships. */
    public static void migrateLegacyVariants() {
        if (Global.getSector() == null) return;
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(
                LEGACY_MIGRATION_KEY)) {
            return;
        }
        // Authored NPC fleets migrate through their own bounded configure
        // paths. Only the player's fleet and storage require a load-time
        // sweep; never scan every fleet in every campaign location here.
        migrateLegacyFleet(Global.getSector().getPlayerFleet());
        if (Global.getSector().getEconomy() == null) return;
        for (MarketAPI market
                : Global.getSector().getEconomy().getMarketsCopy()) {
            for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
                if (submarket == null || submarket.getCargoNullOk() == null) {
                    continue;
                }
                for (FleetMemberAPI member : submarket.getCargoNullOk()
                        .getMothballedShips().getMembersListCopy()) {
                    migrateLegacyMember(member);
                }
            }
        }
        Global.getSector().getMemoryWithoutUpdate().set(
                LEGACY_MIGRATION_KEY, true);
    }

    private static void migrateLegacyFleet(CampaignFleetAPI fleet) {
        if (fleet == null) return;
        boolean changed = false;
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            changed |= migrateLegacyMember(member);
        }
        if (changed) fleet.forceSync();
    }

    private static boolean migrateLegacyMember(FleetMemberAPI member) {
        if (member == null || member.getVariant() == null) {
            return false;
        }
        String whiteHullId = getWhiteHullId(
                member.getHullSpec().getBaseHullId());
        boolean alreadyIvory = whiteHullId != null
                && whiteHullId.equals(member.getHullSpec().getHullId());
        if (!alreadyIvory
                && !member.getVariant().hasHullMod(LEGACY_HULLMOD_ID)) {
            return false;
        }
        if (!refitMember(member)) return false;
        member.updateStats();
        return true;
    }

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

    public static void readyMember(FleetMemberAPI member) {
        if (member == null) return;
        member.getRepairTracker().setMothballed(false);
        member.getRepairTracker().setCR(
                member.getRepairTracker().getMaxCR());
        member.updateStats();
    }
}
