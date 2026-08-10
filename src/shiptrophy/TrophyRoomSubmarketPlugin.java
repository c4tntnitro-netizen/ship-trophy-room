package shiptrophy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;

public class TrophyRoomSubmarketPlugin extends StoragePlugin {
    private enum SortKey {
        HULL_NAME("Hull Name"),
        FACTION("Faction"),
        SIZE("Size"),
        DP_COST("DP Cost");

        private final String label;

        SortKey(String label) {
            this.label = label;
        }

        private static SortKey fromSetting(String value, SortKey fallback) {
            if (value != null) {
                for (SortKey key : values()) {
                    if (key.label.equalsIgnoreCase(value.trim())) return key;
                }
            }
            return fallback;
        }
    }

    @Override
    public void init(com.fs.starfarer.api.campaign.econ.SubmarketAPI submarket) {
        super.init(submarket);
        setPlayerPaidToUnlock(true);
    }


    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        return hasFunctionalTrophyRoom();
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        super.updateCargoPrePlayerInteraction();
        sortStoredShips();
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
        super.reportPlayerMarketTransaction(transaction);
        sortStoredShips();
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        if (hasFunctionalTrophyRoom()) {
            return "Ships displayed here contribute to the colony's Hall of Triumph story point generation.";
        }
        return "Requires a functional Hall of Triumph on this colony.";
    }

    @Override
    public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
        Highlights highlights = new Highlights();
        highlights.setText("Hall of Triumph", "story point generation", "Requires a functional Hall of Triumph");
        return highlights;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        SortKey primary = getPrimarySort();
        SortKey secondary = getSecondarySort();
        tooltip.addPara("Ship display order: %s, then %s. Configure both categories in Hall of Triumph's LunaLib settings.",
                10f, com.fs.starfarer.api.util.Misc.getHighlightColor(),
                primary.label, secondary.label);
        tooltip.addPara("Stored ships are preserved even if the Hall of Triumph is disrupted, but this tab can only be opened while the structure is functional.", 10f);
    }

    private void sortStoredShips() {
        if (getCargo() == null || getCargo().getMothballedShips() == null) return;
        FleetDataAPI ships = getCargo().getMothballedShips();
        List<FleetMemberAPI> ordered = ships.getMembersListCopy();
        if (ordered.size() < 2) return;

        final SortKey primary = getPrimarySort();
        final SortKey secondary = getSecondarySort();
        Collections.sort(ordered, new Comparator<FleetMemberAPI>() {
            @Override
            public int compare(FleetMemberAPI left, FleetMemberAPI right) {
                int result = compareBy(left, right, primary);
                if (result == 0 && secondary != primary) {
                    result = compareBy(left, right, secondary);
                }
                if (result == 0) result = compareText(hullName(left), hullName(right));
                if (result == 0) result = compareText(faction(left), faction(right));
                if (result == 0) result = compareText(shipName(left), shipName(right));
                return result;
            }
        });
        ships.sortToMatchOrder(ordered);
    }

    private SortKey getPrimarySort() {
        return SortKey.fromSetting(
                HallOfTriumphFeatures.getPrimaryStorageSort(), SortKey.HULL_NAME);
    }

    private SortKey getSecondarySort() {
        return SortKey.fromSetting(
                HallOfTriumphFeatures.getSecondaryStorageSort(), SortKey.FACTION);
    }

    private static int compareBy(FleetMemberAPI left, FleetMemberAPI right, SortKey key) {
        if (key == SortKey.FACTION) return compareText(faction(left), faction(right));
        if (key == SortKey.SIZE) return Integer.compare(sizeRank(left), sizeRank(right));
        if (key == SortKey.DP_COST) {
            return Float.compare(dpCost(left), dpCost(right));
        }
        return compareText(hullName(left), hullName(right));
    }

    private static String hullName(FleetMemberAPI member) {
        return member == null || member.getHullSpec() == null
                ? "" : member.getHullSpec().getHullName();
    }

    private static String faction(FleetMemberAPI member) {
        return member == null || member.getHullSpec() == null
                ? "" : member.getHullSpec().getManufacturer();
    }

    private static String shipName(FleetMemberAPI member) {
        return member == null ? "" : member.getShipName();
    }

    private static float dpCost(FleetMemberAPI member) {
        return member == null ? Float.MAX_VALUE : member.getDeploymentPointsCost();
    }

    private static int sizeRank(FleetMemberAPI member) {
        if (member == null || member.getHullSpec() == null) return Integer.MAX_VALUE;
        ShipAPI.HullSize size = member.getHullSpec().getHullSize();
        if (size == ShipAPI.HullSize.FIGHTER) return 0;
        if (size == ShipAPI.HullSize.FRIGATE) return 1;
        if (size == ShipAPI.HullSize.DESTROYER) return 2;
        if (size == ShipAPI.HullSize.CRUISER) return 3;
        if (size == ShipAPI.HullSize.CAPITAL_SHIP) return 4;
        return 5;
    }

    private static int compareText(String left, String right) {
        String a = normalize(left);
        String b = normalize(right);
        return a.compareTo(b);
    }

    private static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) return "\uffff";
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasFunctionalTrophyRoom() {
        return market != null && TrophyRoomIndustry.isFunctionalTrophyRoom(market.getIndustry(ShipTrophyRoomIds.INDUSTRY));
    }
}
