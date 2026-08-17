package shiptrophy.gallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import shiptrophy.ShipTrophyRoomIds;
import shiptrophy.ShipTrophyL10n;

final class ShipGalleryData {
    enum SortKey {
        HULL("Hull Name"),
        FACTION("Faction"),
        SIZE("Size"),
        DP("DP Cost");

        final String label;

        SortKey(String label) {
            this.label = label;
        }

        static SortKey parse(String value, SortKey fallback) {
            if (value != null) {
                for (SortKey key : values()) {
                    if (key.label.equalsIgnoreCase(value.trim())) return key;
                }
            }
            return fallback;
        }

        SortKey next() {
            SortKey[] all = values();
            return all[(ordinal() + 1) % all.length];
        }

        String displayLabel() {
            if (this == FACTION) return ShipTrophyL10n.get("gallery_sort_faction");
            if (this == SIZE) return ShipTrophyL10n.get("gallery_sort_size");
            if (this == DP) return ShipTrophyL10n.get("gallery_sort_dp");
            return ShipTrophyL10n.get("gallery_sort_hull");
        }
    }

    enum SizeFilter {
        ALL("All sizes", null),
        FRIGATE("Frigates", ShipAPI.HullSize.FRIGATE),
        DESTROYER("Destroyers", ShipAPI.HullSize.DESTROYER),
        CRUISER("Cruisers", ShipAPI.HullSize.CRUISER),
        CAPITAL("Capital ships", ShipAPI.HullSize.CAPITAL_SHIP);

        final String label;
        final ShipAPI.HullSize size;

        SizeFilter(String label, ShipAPI.HullSize size) {
            this.label = label;
            this.size = size;
        }

        static SizeFilter parse(String value) {
            if (value != null) {
                for (SizeFilter filter : values()) {
                    if (filter.label.equalsIgnoreCase(value.trim())) return filter;
                }
            }
            return ALL;
        }

        SizeFilter next() {
            SizeFilter[] all = values();
            return all[(ordinal() + 1) % all.length];
        }

        String displayLabel() {
            if (this == FRIGATE) return ShipTrophyL10n.get("gallery_size_frigates");
            if (this == DESTROYER) return ShipTrophyL10n.get("gallery_size_destroyers");
            if (this == CRUISER) return ShipTrophyL10n.get("gallery_size_cruisers");
            if (this == CAPITAL) return ShipTrophyL10n.get("gallery_size_capitals");
            return ShipTrophyL10n.get("gallery_size_all");
        }
    }

    private ShipGalleryData() {
    }

    static List<FleetMemberAPI> getAllShips() {
        Map<String, FleetMemberAPI> unique =
                new LinkedHashMap<String, FleetMemberAPI>();
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) {
            return new ArrayList<FleetMemberAPI>();
        }
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market == null || !market.isPlayerOwned()) continue;
            if (market.getIndustry(ShipTrophyRoomIds.INDUSTRY) == null) continue;
            if (!market.hasSubmarket(ShipTrophyRoomIds.SUBMARKET)) continue;
            if (market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getCargo() == null
                    || market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getCargo()
                    .getMothballedShips() == null) continue;
            for (FleetMemberAPI member : market.getSubmarket(ShipTrophyRoomIds.SUBMARKET)
                    .getCargo().getMothballedShips().getMembersListCopy()) {
                if (member != null && !member.isFighterWing() && member.getHullSpec() != null) {
                    String key = galleryHullKey(member);
                    if (!key.isEmpty() && !unique.containsKey(key)) {
                        unique.put(key, member);
                    }
                }
            }
        }
        return new ArrayList<FleetMemberAPI>(unique.values());
    }

    /** Collapses literal repeats while preserving distinct skins and hull variants. */
    static String galleryHullKey(FleetMemberAPI member) {
        if (member == null || member.getHullSpec() == null) return "";
        String hullId = safe(member.getHullSpec().getHullId());
        if (hullId.isEmpty()) hullId = safe(member.getHullId());
        return hullId.toLowerCase(Locale.ROOT);
    }

    static List<String> getManufacturers(List<FleetMemberAPI> ships) {
        Set<String> unique = new LinkedHashSet<String>();
        for (FleetMemberAPI ship : ships) {
            String value = faction(ship);
            if (!value.isEmpty()) unique.add(value);
        }
        List<String> result = new ArrayList<String>(unique);
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    static boolean isTourShuttleEligible(FleetMemberAPI member) {
        return member != null
                && member.isFrigate()
                && member.getVariant() != null
                && member.getVariant().hasHullMod(HullMods.CIVGRADE);
    }

    static List<FleetMemberAPI> filterAndSort(List<FleetMemberAPI> source,
                                               SizeFilter sizeFilter,
                                               String manufacturer,
                                               final SortKey primary,
                                               final SortKey secondary) {
        List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
        for (FleetMemberAPI ship : source) {
            if (sizeFilter != SizeFilter.ALL
                    && ship.getHullSpec().getHullSize() != sizeFilter.size) continue;
            if (manufacturer != null && !manufacturer.isEmpty()
                    && !manufacturer.equalsIgnoreCase(faction(ship))) continue;
            result.add(ship);
        }
        Collections.sort(result, new Comparator<FleetMemberAPI>() {
            @Override
            public int compare(FleetMemberAPI left, FleetMemberAPI right) {
                int value = compareBy(left, right, primary);
                if (value == 0 && secondary != primary) {
                    value = compareBy(left, right, secondary);
                }
                if (value == 0) value = compareText(hullName(left), hullName(right));
                if (value == 0) value = compareText(faction(left), faction(right));
                if (value == 0) value = compareText(shipName(left), shipName(right));
                return value;
            }
        });
        return result;
    }

    private static int compareBy(FleetMemberAPI left, FleetMemberAPI right, SortKey key) {
        if (key == SortKey.FACTION) return compareText(faction(left), faction(right));
        if (key == SortKey.SIZE) return Integer.compare(sizeRank(left), sizeRank(right));
        if (key == SortKey.DP) return Float.compare(dp(left), dp(right));
        return compareText(hullName(left), hullName(right));
    }

    private static String hullName(FleetMemberAPI member) {
        return member == null || member.getHullSpec() == null
                ? "" : safe(member.getHullSpec().getHullName());
    }

    static String faction(FleetMemberAPI member) {
        return member == null || member.getHullSpec() == null
                ? "" : safe(member.getHullSpec().getManufacturer());
    }

    private static String shipName(FleetMemberAPI member) {
        return member == null ? "" : safe(member.getShipName());
    }

    private static float dp(FleetMemberAPI member) {
        return member == null ? Float.MAX_VALUE
                : Math.max(0f, member.getUnmodifiedDeploymentPointsCost());
    }

    private static int sizeRank(FleetMemberAPI member) {
        if (member == null || member.getHullSpec() == null) return Integer.MAX_VALUE;
        ShipAPI.HullSize size = member.getHullSpec().getHullSize();
        if (size == ShipAPI.HullSize.FRIGATE) return 0;
        if (size == ShipAPI.HullSize.DESTROYER) return 1;
        if (size == ShipAPI.HullSize.CRUISER) return 2;
        if (size == ShipAPI.HullSize.CAPITAL_SHIP) return 3;
        return 4;
    }

    private static int compareText(String left, String right) {
        return normalize(left).compareTo(normalize(right));
    }

    private static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) return "\uffff";
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
