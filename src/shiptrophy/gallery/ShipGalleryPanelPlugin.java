package shiptrophy.gallery;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.gallery.ShipGalleryData.SizeFilter;
import shiptrophy.gallery.ShipGalleryData.SortKey;

final class ShipGalleryPanelPlugin implements CustomUIPanelPlugin {
    private static final String PRIMARY_ID = "ship_gallery_primary";
    private static final String SECONDARY_ID = "ship_gallery_secondary";
    private static final String SIZE_ID = "ship_gallery_size";
    private static final String FACTION_ID = "ship_gallery_faction";

    private static final String PRIMARY_MEMORY = "$ship_trophy_gallery_primary_sort";
    private static final String SECONDARY_MEMORY = "$ship_trophy_gallery_secondary_sort";
    private static final String SIZE_MEMORY = "$ship_trophy_gallery_size_filter";
    private static final String FACTION_MEMORY = "$ship_trophy_gallery_faction_filter";

    private final float width;
    private final float height;
    private CustomPanelAPI panel;
    private TooltipMakerAPI content;

    ShipGalleryPanelPlugin(float width, float height) {
        this.width = width;
        this.height = height;
    }

    void init(CustomPanelAPI panel) {
        this.panel = panel;
        rebuild();
    }

    private void rebuild() {
        if (panel == null) return;
        if (content != null) panel.removeComponent(content);

        float contentWidth = Math.max(600f, width - 14f);
        float contentHeight = Math.max(400f, height - 10f);
        content = panel.createUIElement(contentWidth, contentHeight, true);

        List<FleetMemberAPI> all = ShipGalleryData.getAllShips();
        List<String> manufacturers = ShipGalleryData.getManufacturers(all);
        SortKey primary = getPrimary();
        SortKey secondary = getSecondary();
        SizeFilter size = getSizeFilter();
        String manufacturer = validateManufacturer(getManufacturer(), manufacturers);
        if (!manufacturer.equals(getManufacturer())) setMemory(FACTION_MEMORY, manufacturer);

        List<FleetMemberAPI> visible = ShipGalleryData.filterAndSort(
                all, size, manufacturer, primary, secondary);

        content.addTitle("Ship Gallery", Misc.getBasePlayerColor());
        content.addPara("A read-only catalog of ships displayed across every functional Hall of Triumph. Gallery controls never move or modify stored ships.",
                6f, Misc.getGrayColor(), "read-only", "never move or modify stored ships");
        content.addSectionHeading("Sort and filter", Alignment.MID, 10f);
        content.addPara("Click a control to cycle its value. Sorting uses both categories in order.", 6f);

        addControl("Primary sort: " + primary.label, PRIMARY_ID);
        addControl("Secondary sort: " + secondary.label, SECONDARY_ID);
        addControl("Hull size: " + size.label, SIZE_ID);
        addControl("Faction/manufacturer: "
                + (manufacturer.isEmpty() ? "All" : manufacturer), FACTION_ID);

        content.addSectionHeading("Displayed ships", Alignment.MID, 12f);
        content.addPara("Showing %s of %s ships.", 6f, Misc.getHighlightColor(),
                Integer.toString(visible.size()), Integer.toString(all.size()));

        if (all.isEmpty()) {
            content.addPara("No ships are currently stored in a functional Hall of Triumph.", 12f,
                    Misc.getGrayColor(), "No ships");
        } else if (visible.isEmpty()) {
            content.addPara("No displayed ships match the active filters.", 12f,
                    Misc.getNegativeHighlightColor(), "No displayed ships");
        } else {
            int columns = Math.max(4, Math.min(10, (int) (contentWidth / 120f)));
            int rows = (visible.size() + columns - 1) / columns;
            float iconSize = Math.min(96f, (contentWidth - 30f) / columns);
            content.addShipList(columns, rows, iconSize,
                    Misc.getBasePlayerColor(), visible, 10f);
        }

        panel.addUIElement(content).inTL(5f, 5f);
    }

    private void addControl(String text, Object id) {
        ButtonAPI button = content.addButton(text, id,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                330f, 24f, 4f);
        button.setButtonPressedSound("ui_button_pressed");
    }

    @Override
    public void buttonPressed(Object buttonId) {
        if (PRIMARY_ID.equals(buttonId)) {
            setMemory(PRIMARY_MEMORY, getPrimary().next().label);
        } else if (SECONDARY_ID.equals(buttonId)) {
            setMemory(SECONDARY_MEMORY, getSecondary().next().label);
        } else if (SIZE_ID.equals(buttonId)) {
            setMemory(SIZE_MEMORY, getSizeFilter().next().label);
        } else if (FACTION_ID.equals(buttonId)) {
            cycleManufacturer();
        } else {
            return;
        }
        rebuild();
    }

    private void cycleManufacturer() {
        List<String> manufacturers = ShipGalleryData.getManufacturers(
                ShipGalleryData.getAllShips());
        String current = validateManufacturer(getManufacturer(), manufacturers);
        if (current.isEmpty()) {
            setMemory(FACTION_MEMORY, manufacturers.isEmpty() ? "" : manufacturers.get(0));
            return;
        }
        int index = manufacturers.indexOf(current);
        setMemory(FACTION_MEMORY,
                index < 0 || index + 1 >= manufacturers.size() ? "" : manufacturers.get(index + 1));
    }

    private SortKey getPrimary() {
        return SortKey.parse(getMemory(PRIMARY_MEMORY), SortKey.HULL);
    }

    private SortKey getSecondary() {
        return SortKey.parse(getMemory(SECONDARY_MEMORY), SortKey.FACTION);
    }

    private SizeFilter getSizeFilter() {
        return SizeFilter.parse(getMemory(SIZE_MEMORY));
    }

    private String getManufacturer() {
        return getMemory(FACTION_MEMORY);
    }

    private static String validateManufacturer(String value, List<String> manufacturers) {
        if (value == null || value.trim().isEmpty()) return "";
        for (String manufacturer : manufacturers) {
            if (manufacturer.equalsIgnoreCase(value.trim())) return manufacturer;
        }
        return "";
    }

    private static String getMemory(String key) {
        try {
            MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
            return memory.contains(key) ? memory.getString(key) : "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static void setMemory(String key, String value) {
        if (Global.getSector() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(key, value == null ? "" : value);
        }
    }

    @Override
    public void positionChanged(PositionAPI position) {
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
    }
}
