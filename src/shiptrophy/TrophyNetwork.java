package shiptrophy;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import shiptrophy.hullmods.Contempt;
import shiptrophy.hullmods.Gaze;
import shiptrophy.hullmods.LionGuardPageantry;
import shiptrophy.hullmods.LuddicPathZeal;

public class TrophyNetwork {
    public static final float DOCTRINE_UNLOCK_DP = 60f;
    public static final int UNIQUE_HULLS_FOR_FULL_BONUS = 12;
    public static final int DP_FOR_FULL_BONUS = 240;

    public static NetworkStats computeNetworkStats() {
        NetworkStats network = new NetworkStats();
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) return network;

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market == null || !market.isPlayerOwned()) continue;
            Industry industry = market.getIndustry(ShipTrophyRoomIds.INDUSTRY);
            if (!TrophyRoomIndustry.isFunctionalTrophyRoom(industry)) continue;

            network.functionalRooms++;
            if (industry.isImproved()) network.improvedRooms++;
            network.merge(getMarketStats(market));
        }

        return network;
    }

    public static CollectionStats getMarketStats(MarketAPI market) {
        if (market == null) return new CollectionStats();
        if (market.hasSubmarket(ShipTrophyRoomIds.SUBMARKET)) {
            return getCargoStats(market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getCargo());
        }
        if (market.hasSubmarket(Submarkets.SUBMARKET_STORAGE)) {
            return getCargoStats(market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo());
        }
        return new CollectionStats();
    }

    public static CollectionStats getCargoStats(CargoAPI cargo) {
        CollectionStats stats = new CollectionStats();
        if (cargo == null || cargo.getMothballedShips() == null) return stats;

        List<FleetMemberAPI> members = cargo.getMothballedShips().getMembersListCopy();
        for (FleetMemberAPI member : members) {
            stats.add(member);
        }

        return stats;
    }

    public static boolean isDoctrineUnlocked(TrophyDoctrine doctrine) {
        TrophySubtypeSpec spec = TrophySubtypeRegistry.getSubtype(doctrine);
        return spec != null && getSubtypeDp(spec.id) >= spec.unlockDp;
    }

    public static float getDoctrineDp(TrophyDoctrine doctrine) {
        return doctrine == null ? 0f : getSubtypeDp(doctrine.id);
    }

    public static boolean isSubtypeUnlocked(String subtypeId) {
        TrophySubtypeSpec spec = TrophySubtypeRegistry.getSubtype(subtypeId);
        return spec != null && spec.isActive() && getSubtypeDp(spec.id) >= spec.unlockDp;
    }

    public static float getSubtypeDp(String subtypeId) {
        return computeNetworkStats().getSubtypeDp(subtypeId);
    }

    public static boolean isZigguratShowcased() {
        return hasShowcasedHull(computeNetworkStats(), Gaze.REQUIRED_BASE_HULL_ID);
    }

    public static boolean isOnslaughtMkIShowcased() {
        return hasShowcasedHull(computeNetworkStats(), Contempt.REQUIRED_BASE_HULL_ID);
    }

    public static void refreshPlayerHullmodUnlocks() {
        refreshPlayerHullmodUnlocks(computeNetworkStats());
    }

    public static void refreshPlayerHullmodUnlocks(NetworkStats stats) {
        if (Global.getSector() == null) return;
        FactionAPI player = Global.getSector().getPlayerFaction();
        if (player == null) return;

        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getAllSubtypes()) {
            if (!subtype.hasHullModUnlock() || !hullModExists(subtype.hullModId)) continue;
            boolean unlocked = subtype.isActive() && stats.getSubtypeDp(subtype.id) >= subtype.unlockDp;
            setKnownHullMod(player, subtype.hullModId, unlocked);
        }

        setKnownHullMod(player, Gaze.HULLMOD_ID, hasShowcasedHull(stats, Gaze.REQUIRED_BASE_HULL_ID));
        setKnownHullMod(player, Contempt.HULLMOD_ID, hasShowcasedHull(stats, Contempt.REQUIRED_BASE_HULL_ID));
        for (TrophyUniqueShowcases.ShowcaseSpec showcase : TrophyUniqueShowcases.getAllShowcases()) {
            if (hullModExists(showcase.hullModId)) {
                setKnownHullMod(player, showcase.hullModId, showcase.isActive() && hasShowcasedHull(stats, showcase.hullId));
            }
        }
    }

    private static boolean hullModExists(String hullModId) {
        try {
            return hullModId != null && hullModId.length() > 0 && Global.getSettings().getHullModSpec(hullModId) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void setKnownHullMod(FactionAPI player, String hullModId, boolean unlocked) {
        if (unlocked && !player.knowsHullMod(hullModId)) {
            player.addKnownHullMod(hullModId);
        } else if (!unlocked && player.knowsHullMod(hullModId)) {
            player.removeKnownHullMod(hullModId);
        }
    }

    public static void syncDmodMarkers(NetworkStats stats) {
        if (Global.getSector() == null) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null && fleet.getFleetData() != null) {
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                syncDmodMarkers(member, stats);
            }
        }

        if (Global.getSector().getEconomy() == null) return;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
                if (submarket.getCargo() == null || submarket.getCargo().getMothballedShips() == null) continue;
                for (FleetMemberAPI member : submarket.getCargo().getMothballedShips().getMembersListCopy()) {
                    syncDmodMarkers(member, stats);
                }
            }
        }
    }

    private static void syncDmodMarkers(FleetMemberAPI member, NetworkStats stats) {
        if (member == null || member.getVariant() == null) return;
        ShipVariantAPI variant = member.getVariant();

        TrophySubtypeSpec lp = TrophySubtypeRegistry.getSubtype(TrophyDoctrine.LP);
        boolean lpCounts = lp != null
                && stats.getSubtypeDp(lp.id) >= lp.unlockDp
                && variant.hasHullMod(TrophyDoctrine.LP.hullModId)
                && !variant.hasHullMod(HullMods.UNSTABLE_INJECTOR);
        setMarker(variant, LuddicPathZeal.DMOD_MARKER, lpCounts);

        TrophySubtypeSpec lg = TrophySubtypeRegistry.getSubtype(TrophyDoctrine.LG);
        boolean lgCounts = lg != null
                && stats.getSubtypeDp(lg.id) >= lg.unlockDp
                && variant.hasHullMod(TrophyDoctrine.LG.hullModId)
                && !variant.hasHullMod(LionGuardPageantry.ENERGY_BOLT_COHERER)
                && !variant.hasHullMod(LionGuardPageantry.MODULAR_BOLT_COHERER);
        setMarker(variant, LionGuardPageantry.DMOD_MARKER, lgCounts);
    }

    public static void syncUniqueDiscountMarkers(NetworkStats stats) {
        if (Global.getSector() == null) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null && fleet.getFleetData() != null) {
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                syncUniqueDiscountMarkers(member, stats);
            }
        }

        if (Global.getSector().getEconomy() == null) return;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
                if (submarket.getCargo() == null || submarket.getCargo().getMothballedShips() == null) continue;
                for (FleetMemberAPI member : submarket.getCargo().getMothballedShips().getMembersListCopy()) {
                    syncUniqueDiscountMarkers(member, stats);
                }
            }
        }
    }

    private static void syncUniqueDiscountMarkers(FleetMemberAPI member, NetworkStats stats) {
        if (member == null || member.getVariant() == null) return;
        Gaze.syncVariant(member.getVariant(), stats);
        Contempt.syncVariant(member.getVariant(), stats);
    }

    private static void setMarker(ShipVariantAPI variant, String markerId, boolean shouldHaveMarker) {
        if (shouldHaveMarker && !variant.hasHullMod(markerId)) {
            variant.addMod(markerId);
        } else if (!shouldHaveMarker && variant.hasHullMod(markerId)) {
            variant.removeMod(markerId);
            variant.removePermaMod(markerId);
        }
    }

    public static boolean isMatchingInstallStyle(ShipAPI ship, TrophyDoctrine doctrine) {
        return isMatchingInstallStyle(ship, TrophySubtypeRegistry.getSubtype(doctrine));
    }

    public static boolean isMatchingInstallStyle(ShipAPI ship, TrophySubtypeSpec subtype) {
        if (subtype == null) return true;
        return subtype.matchesInstallStyle(ship);
    }

    public static boolean isMatchingInstallStyle(ShipAPI ship, String subtypeId) {
        return isMatchingInstallStyle(ship, TrophySubtypeRegistry.getSubtype(subtypeId));
    }

    private static boolean isMatchingInstallStyleLegacy(ShipAPI ship, TrophyDoctrine doctrine) {
        if (ship == null) return true;
        String style = lower(ship.getHullStyleId());
        if ("low-tech".equals(doctrine.installStyle)) return "low_tech".equals(style) || "low-tech".equals(style);
        if ("midline".equals(doctrine.installStyle)) return "midline".equals(style);
        if ("high-tech".equals(doctrine.installStyle)) return "high_tech".equals(style) || "high-tech".equals(style);
        return true;
    }

    public static TrophyDoctrine getDoctrine(FleetMemberAPI member) {
        if (member == null || member.getHullSpec() == null) return null;

        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
            if (!subtype.matches(member)) continue;
            for (TrophyDoctrine doctrine : TrophyDoctrine.values()) {
                if (doctrine.id.equals(subtype.id)) return doctrine;
            }
        }
        return null;
    }

    public static String getBaseHullId(FleetMemberAPI member) {
        if (member == null) return "";
        if (member.getHullSpec() == null) return safe(member.getHullId());
        String baseHullId = member.getHullSpec().getBaseHullId();
        if (baseHullId != null && baseHullId.length() > 0) return baseHullId;
        return safe(member.getHullId());
    }

    public static boolean hasShowcasedHull(CollectionStats stats, String baseHullId) {
        if (stats == null || baseHullId == null) return false;
        for (String hullId : stats.uniqueHullIds) {
            if (baseHullId.equalsIgnoreCase(hullId)) return true;
        }
        return false;
    }

    private static String lower(String value) {
        return safe(value).toLowerCase();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public static class CollectionStats {
        public int storedShips = 0;
        public float uniqueDeploymentPoints = 0f;
        public Set<String> uniqueHullIds = new LinkedHashSet<String>();
        public Map<String, Float> uniqueHullDp = new LinkedHashMap<String, Float>();
        public Map<ShipAPI.HullSize, Integer> hullSizeCounts = new LinkedHashMap<ShipAPI.HullSize, Integer>();
        public Map<String, Float> subtypeDp = new LinkedHashMap<String, Float>();
        public Map<String, Float> doctrineDp = subtypeDp;
        public Map<String, Set<String>> subtypeHullIds = new LinkedHashMap<String, Set<String>>();
        public Map<String, Map<String, Float>> subtypeHullDp = new LinkedHashMap<String, Map<String, Float>>();

        public CollectionStats() {
            for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
                ensureSubtype(subtype.id);
            }
        }

        public void add(FleetMemberAPI member) {
            if (member == null || member.isFighterWing() || member.getHullSpec() == null) return;
            storedShips++;

            ShipAPI.HullSize size = member.getHullSpec().getHullSize();
            Integer sizeCount = hullSizeCounts.get(size);
            hullSizeCounts.put(size, sizeCount == null ? 1 : sizeCount + 1);

            String baseHullId = getBaseHullId(member);
            if (uniqueHullIds.add(baseHullId)) {
                float dp = Math.max(0f, member.getUnmodifiedDeploymentPointsCost());
                uniqueHullDp.put(baseHullId, dp);
                uniqueDeploymentPoints += dp;
            }

            String hullId = member.getHullId();
            if (TrophyUniqueShowcases.isOptionalUniqueHull(hullId) && uniqueHullIds.add(hullId)) {
                float dp = Math.max(0f, member.getUnmodifiedDeploymentPointsCost());
                uniqueHullDp.put(hullId, dp);
                uniqueDeploymentPoints += dp;
            }

            for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
                if (!subtype.matches(member)) continue;
                ensureSubtype(subtype.id);
                Set<String> subtypeHulls = subtypeHullIds.get(subtype.id);
                if (subtypeHulls.add(member.getHullId())) {
                    float dp = Math.max(0f, member.getUnmodifiedDeploymentPointsCost());
                    subtypeHullDp.get(subtype.id).put(member.getHullId(), dp);
                    subtypeDp.put(subtype.id, getSubtypeDp(subtype.id) + dp);
                }
            }
        }

        public void merge(CollectionStats other) {
            storedShips += other.storedShips;
            for (Map.Entry<ShipAPI.HullSize, Integer> entry : other.hullSizeCounts.entrySet()) {
                Integer count = hullSizeCounts.get(entry.getKey());
                hullSizeCounts.put(entry.getKey(), count == null ? entry.getValue() : count + entry.getValue());
            }

            for (String hullId : other.uniqueHullIds) {
                if (uniqueHullIds.add(hullId)) {
                    Float dp = other.uniqueHullDp.get(hullId);
                    if (dp == null) dp = 0f;
                    uniqueHullDp.put(hullId, dp);
                    uniqueDeploymentPoints += dp;
                }
            }

            for (Map.Entry<String, Set<String>> entry : other.subtypeHullIds.entrySet()) {
                String subtypeId = entry.getKey();
                ensureSubtype(subtypeId);
                for (String hullId : entry.getValue()) {
                    if (subtypeHullIds.get(subtypeId).add(hullId)) {
                        Float dp = other.subtypeHullDp.get(subtypeId).get(hullId);
                        if (dp == null) dp = 0f;
                        subtypeHullDp.get(subtypeId).put(hullId, dp);
                        subtypeDp.put(subtypeId, getSubtypeDp(subtypeId) + dp);
                    }
                }
            }
        }

        public float getDoctrineDp(TrophyDoctrine doctrine) {
            return doctrine == null ? 0f : getSubtypeDp(doctrine.id);
        }

        public float getSubtypeDp(String subtypeId) {
            Float value = subtypeDp.get(subtypeId);
            return value == null ? 0f : value;
        }

        private void ensureSubtype(String subtypeId) {
            if (subtypeId == null || subtypeId.length() <= 0) return;
            if (!subtypeDp.containsKey(subtypeId)) subtypeDp.put(subtypeId, 0f);
            if (!subtypeHullIds.containsKey(subtypeId)) subtypeHullIds.put(subtypeId, new LinkedHashSet<String>());
            if (!subtypeHullDp.containsKey(subtypeId)) subtypeHullDp.put(subtypeId, new LinkedHashMap<String, Float>());
        }

        public int getHullSizeCount(ShipAPI.HullSize size) {
            Integer count = hullSizeCounts.get(size);
            return count == null ? 0 : count;
        }

        public String getHullSizeSummary() {
            return getHullSizeCount(ShipAPI.HullSize.FRIGATE) + " frigate, "
                    + getHullSizeCount(ShipAPI.HullSize.DESTROYER) + " destroyer, "
                    + getHullSizeCount(ShipAPI.HullSize.CRUISER) + " cruiser, "
                    + getHullSizeCount(ShipAPI.HullSize.CAPITAL_SHIP) + " capital";
        }
    }

    public static class NetworkStats extends CollectionStats {
        public int functionalRooms = 0;
        public int improvedRooms = 0;
    }
}
