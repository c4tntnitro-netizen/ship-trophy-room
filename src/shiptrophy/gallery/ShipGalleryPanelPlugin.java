package shiptrophy.gallery;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TextFieldAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.gallery.ShipGalleryData.SizeFilter;
import shiptrophy.gallery.ShipGalleryData.SortKey;

/** Cover Flow-style, read-only browser for ships displayed in every Hall. */
final class ShipGalleryPanelPlugin implements CustomUIPanelPlugin {
    private static final String PRIMARY_ID = "ship_gallery_primary";
    private static final String SECONDARY_ID = "ship_gallery_secondary";
    private static final String SIZE_ID = "ship_gallery_size";
    private static final String FACTION_ID = "ship_gallery_faction";
    private static final String PREVIOUS_ID = "ship_gallery_previous";
    private static final String NEXT_ID = "ship_gallery_next";

    private static final String PRIMARY_MEMORY = "$ship_trophy_gallery_primary_sort";
    private static final String SECONDARY_MEMORY = "$ship_trophy_gallery_secondary_sort";
    private static final String SIZE_MEMORY = "$ship_trophy_gallery_size_filter";
    private static final String FACTION_MEMORY = "$ship_trophy_gallery_faction_filter";
    private static final String SELECTED_MEMORY = "$ship_trophy_gallery_selected_ship";
    private static final String TOUR_SHUTTLE_MEMORY =
            "$ship_trophy_gallery_tour_shuttle";
    private static final String TOUR_MANIFEST_MEMORY =
            "$ship_trophy_gallery_tour_manifest";

    private static final float OUTER_PAD = 15f;
    private static final float HEADER_HEIGHT = 62f;
    private static final float SIDEBAR_TOP = 76f;
    private static final float CONTROLS_HEIGHT = 168f;
    private static final float SIDEBAR_GAP = 8f;
    private static final float SEARCH_HEIGHT = 52f;
    private static final float SEARCH_RESULTS_GAP = 4f;
    private static final float RAIL_BOTTOM = 16f;
    private static final float RAIL_HEIGHT = 96f;
    private static final float RAIL_SIDE_PAD = 58f;
    private static final float ICON_SLOT = 82f;
    private static final float HERO_MAX_NATIVE_SCALE = 1.35f;
    private static final int MANIFEST_COLUMNS = 2;
    private static final float MANIFEST_SLOT = 62f;
    private static final float MANIFEST_GAP = 6f;
    private static final float MANIFEST_PAD = 9f;
    private static final float HERO_GAP = 12f;

    private static final Color VOID = new Color(5, 11, 15);
    private static final Color STAGE = new Color(9, 20, 26);
    private static final Color RAIL = new Color(12, 26, 33);
    private static final Color SOFT_WHITE = new Color(220, 232, 235);
    private static final Color BERTH_CYAN = new Color(54, 164, 180);
    private static final Color BERTH_AMBER = new Color(230, 164, 55);

    private final float width;
    private final float height;
    private CustomPanelAPI panel;
    private CustomPanelAPI contentPanel;
    private PositionAPI position;

    private TooltipMakerAPI header;
    private TooltipMakerAPI controls;
    private TooltipMakerAPI details;
    private TooltipMakerAPI factionSearchPanel;
    private TooltipMakerAPI factionResults;
    private TextFieldAPI factionSearch;
    private TooltipMakerAPI emptyState;
    private TooltipMakerAPI previousControl;
    private TooltipMakerAPI nextControl;

    private List<FleetMemberAPI> allShips = Collections.emptyList();
    private List<FleetMemberAPI> visibleShips = Collections.emptyList();
    private List<FleetMemberAPI> tourManifest = new ArrayList<FleetMemberAPI>();
    private FleetMemberAPI tourShuttle;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private boolean factionDropdownOpen;
    private boolean rebuildPending;
    private String factionQuery = "";
    private String renderedFactionQuery = "";

    ShipGalleryPanelPlugin(float width, float height) {
        this.width = width;
        this.height = height;
    }

    void init(CustomPanelAPI panel) {
        this.panel = panel;
        rebuild();
    }

    /** Returns the ordered exhibits explicitly loaded into the tour hall. */
    List<FleetMemberAPI> getTourManifestSnapshot() {
        return Collections.unmodifiableList(
                new ArrayList<FleetMemberAPI>(tourManifest));
    }

    /** Returns the preserved civilian frigate selected inside the Gallery. */
    FleetMemberAPI getTourShuttleSnapshot() {
        return tourShuttle;
    }

    private void rebuild() {
        if (panel == null) return;
        removeUi();
        refreshModel();
        // Buttons report to the plugin of the panel that directly owns their
        // UI elements. The replaceable child panel prevents details from
        // stacking, so give it a relay instead of leaving its plugin null.
        contentPanel = panel.createCustomPanel(width, height,
                new ContentButtonRelay());
        panel.addComponent(contentPanel).inTL(0f, 0f);

        header = contentPanel.createUIElement(Math.max(560f, width - OUTER_PAD * 2f),
                HEADER_HEIGHT, false);
        header.addTitle("Ship Gallery", Misc.getBasePlayerColor());
        if (allShips.isEmpty()) {
            header.addPara("A read-only catalog of the vessels preserved across your Halls of Triumph.",
                    4f, Misc.getGrayColor(), "read-only");
        } else if (tourShuttle == null) {
            header.addPara("Select a hull or scroll over the filmstrip. Tours require a stored "
                            + "%s with %s; none is currently preserved.",
                    4f, Misc.getNegativeHighlightColor(),
                    "frigate", "Civilian-grade Hull");
        } else {
            header.addPara("Showing %s of %s unique hulls. Tour shuttle: %s. Left-click the conveyor "
                            + "to add exhibits (%s/%s); right-click the left rack to remove them.",
                    4f, Misc.getHighlightColor(), Integer.toString(visibleShips.size()),
                    Integer.toString(allShips.size()), displayShipName(tourShuttle),
                    Integer.toString(tourManifest.size()),
                    Integer.toString(getManifestCapacity()));
        }
        contentPanel.addUIElement(header).inTL(OUTER_PAD, 8f);

        buildControls();

        if (factionDropdownOpen) {
            buildFactionPicker();
        } else if (allShips.isEmpty()) {
            buildEmptyState("No ships are currently stored in a Hall of Triumph.", false);
        } else if (visibleShips.isEmpty()) {
            buildEmptyState("No displayed ships match the active filters.", true);
        } else {
            buildDetails();
            buildNavigation();
        }
    }

    private void refreshModel() {
        allShips = ShipGalleryData.getAllShips();
        restoreTourManifest();
        FleetMemberAPI rememberedShuttle = findShipById(
                allShips, getMemory(TOUR_SHUTTLE_MEMORY));
        if (!ShipGalleryData.isTourShuttleEligible(rememberedShuttle)) {
            rememberedShuttle = findFirstEligibleShuttle(allShips);
        }
        List<String> manufacturers = ShipGalleryData.getManufacturers(allShips);
        String manufacturer = validateManufacturer(getManufacturer(), manufacturers);
        if (!manufacturer.equals(getManufacturer())) setMemory(FACTION_MEMORY, manufacturer);

        visibleShips = ShipGalleryData.filterAndSort(allShips, getSizeFilter(), manufacturer,
                getPrimary(), getSecondary());
        selectedIndex = findShipIndexById(
                visibleShips, getMemory(SELECTED_MEMORY));
        if (selectedIndex < 0 && !visibleShips.isEmpty()) selectedIndex = 0;
        if (selectedIndex >= 0) {
            FleetMemberAPI selected = visibleShips.get(selectedIndex);
            setMemory(SELECTED_MEMORY, safe(selected.getId()));
            if (ShipGalleryData.isTourShuttleEligible(selected)) {
                rememberedShuttle = selected;
            }
        }
        tourShuttle = rememberedShuttle;
        setMemory(TOUR_SHUTTLE_MEMORY,
                tourShuttle == null ? "" : safe(tourShuttle.getId()));
        hoveredIndex = -1;
    }

    private void buildControls() {
        float sidebarWidth = getSidebarWidth();
        controls = contentPanel.createUIElement(sidebarWidth, CONTROLS_HEIGHT, false);
        controls.addSectionHeading("Catalog order", Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(), Alignment.MID, 0f);
        controls.addPara("Sort and size cycle; faction opens a search menu.", 4f,
                Misc.getGrayColor(), "search");
        addControl("Primary: " + getPrimary().label, PRIMARY_ID, sidebarWidth);
        addControl("Then by: " + getSecondary().label, SECONDARY_ID, sidebarWidth);
        addControl("Hull size: " + getSizeFilter().label, SIZE_ID, sidebarWidth);
        String manufacturer = getManufacturer();
        String factionLabel = "Faction [search]: "
                + (manufacturer.isEmpty() ? "All" : manufacturer);
        addControl(controls.shortenString(factionLabel, Math.max(210f, sidebarWidth - 18f)),
                FACTION_ID, sidebarWidth);
        contentPanel.addUIElement(controls).inTR(OUTER_PAD, SIDEBAR_TOP);
    }

    private void buildFactionPicker() {
        float sidebarWidth = getSidebarWidth();
        float pickerTop = getSidebarContentTop();

        factionSearchPanel = contentPanel.createUIElement(sidebarWidth, SEARCH_HEIGHT, false);
        factionSearchPanel.setForceProcessInput(true);
        factionSearchPanel.addSectionHeading("Search factions", Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(), Alignment.MID, 0f);
        factionSearch = factionSearchPanel.addTextField(Math.max(210f, sidebarWidth - 4f), 3f);
        factionSearch.setText(factionQuery);
        factionSearch.setMaxChars(80);
        factionSearch.setLimitByStringWidth(true);
        factionSearch.setHandleCtrlV(true);
        factionSearch.setUndoOnEscape(false);
        factionSearch.setColor(Misc.getTextColor());
        factionSearch.setBgColor(VOID);
        factionSearch.setBorderColor(Misc.getBasePlayerColor());
        contentPanel.addUIElement(factionSearchPanel).inTR(OUTER_PAD, pickerTop);
        contentPanel.bringComponentToTop(factionSearchPanel);
        factionSearch.grabFocus(false);

        renderedFactionQuery = normalizeQuery(factionQuery);
        rebuildFactionResults();
    }

    private void rebuildFactionResults() {
        remove(factionResults);
        factionResults = null;
        if (!factionDropdownOpen || panel == null) return;

        float sidebarWidth = getSidebarWidth();
        float resultsHeight = Math.max(64f,
                getSidebarContentHeight() - SEARCH_HEIGHT - SEARCH_RESULTS_GAP);
        factionResults = contentPanel.createUIElement(sidebarWidth, resultsHeight, true);
        factionResults.setForceProcessInput(true);
        factionResults.setBgAlpha(0.98f);

        addFactionChoice("All factions", "", sidebarWidth);
        int matches = 0;
        String query = normalizeQuery(factionQuery);
        for (String manufacturer : ShipGalleryData.getManufacturers(allShips)) {
            if (!query.isEmpty()
                    && !manufacturer.toLowerCase(Locale.ROOT).contains(query)) continue;
            addFactionChoice(manufacturer, manufacturer, sidebarWidth);
            matches++;
        }
        if (matches == 0 && !query.isEmpty()) {
            factionResults.addPara("No matching factions.", 6f,
                    Misc.getNegativeHighlightColor(), "No matching factions");
        }

        contentPanel.addUIElement(factionResults).inTR(OUTER_PAD,
                getSidebarContentTop() + SEARCH_HEIGHT + SEARCH_RESULTS_GAP);
        contentPanel.bringComponentToTop(factionResults);
    }

    private void addFactionChoice(String label, String manufacturer, float sidebarWidth) {
        ButtonAPI button = factionResults.addButton(label, new FactionChoice(manufacturer),
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                Math.max(205f, sidebarWidth - 16f), 24f, 2f);
        button.setButtonPressedSound("ui_button_pressed");
        button.setChecked(manufacturer.equalsIgnoreCase(getManufacturer()));
    }

    private void buildDetails() {
        FleetMemberAPI selected = getSelected();
        if (selected == null || selected.getHullSpec() == null) return;

        float sidebarWidth = getSidebarWidth();
        details = contentPanel.createUIElement(
                sidebarWidth, getSidebarContentHeight(), true);
        details.setBgAlpha(0.92f);
        details.addTitle(displayShipName(selected), Misc.getBasePlayerColor());
        details.addPara(selected.getHullSpec().getHullNameWithDashClass(), 2f,
                Misc.getGrayColor(), selected.getHullSpec().getHullName());
        details.beginGrid(Math.max(110f, sidebarWidth * 0.46f), 1);
        details.addToGrid(0, 0, "Manufacturer", displayManufacturer(selected));
        details.addToGrid(0, 1, "Hull size", displayHullSize(selected));
        details.addToGrid(0, 2, "Deployment points", displayDp(selected));
        details.addToGrid(0, 3, "Gallery status", "Preserved", Misc.getHighlightColor());
        if (ShipGalleryData.isTourShuttleEligible(selected)) {
            details.addToGrid(0, 4, "Tour shuttle", "Selected",
                    Misc.getPositiveHighlightColor());
        } else if (tourShuttle != null) {
            details.addToGrid(0, 4, "Tour shuttle", displayShipName(tourShuttle));
        } else {
            details.addToGrid(0, 4, "Tour shuttle", "No eligible hull",
                    Misc.getNegativeHighlightColor());
        }
        details.addGrid(4f);
        addCodexDescription(details, selected.getHullSpec());
        contentPanel.addUIElement(details).inTR(OUTER_PAD,
                getSidebarContentTop());
    }

    private void buildNavigation() {
        if (visibleShips.size() <= 1) return;

        previousControl = contentPanel.createUIElement(42f, 32f, false);
        ButtonAPI previous = previousControl.addButton("<", PREVIOUS_ID,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), 38f, 26f, 0f);
        previous.setButtonPressedSound("ui_button_pressed");
        previous.setEnabled(selectedIndex > 0);
        contentPanel.addUIElement(previousControl).inBL(
                OUTER_PAD, RAIL_BOTTOM + 31f);

        nextControl = contentPanel.createUIElement(42f, 32f, false);
        ButtonAPI next = nextControl.addButton(">", NEXT_ID,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), 38f, 26f, 0f);
        next.setButtonPressedSound("ui_button_pressed");
        next.setEnabled(selectedIndex + 1 < visibleShips.size());
        contentPanel.addUIElement(nextControl).inBR(
                OUTER_PAD, RAIL_BOTTOM + 31f);
    }

    private void buildEmptyState(String message, boolean filtered) {
        float stageWidth = getStageWidth();
        emptyState = contentPanel.createUIElement(
                Math.max(280f, stageWidth - 60f), 90f, false);
        emptyState.setParaOrbitronLarge();
        emptyState.addPara(message, 0f,
                filtered ? Misc.getNegativeHighlightColor() : Misc.getGrayColor(),
                filtered ? "No displayed ships" : "No ships");
        emptyState.setParaFontDefault();
        emptyState.addPara(filtered
                ? "Cycle the size or faction controls to widen the collection."
                : "Store a ship in a Hall's dedicated storage to add it here.", 8f,
                Misc.getGrayColor());
        contentPanel.addUIElement(emptyState).inTL(
                OUTER_PAD + 30f, 150f);
    }

    private void removeUi() {
        if (panel != null && contentPanel != null) {
            panel.removeComponent(contentPanel);
        }
        contentPanel = null;
        header = null;
        controls = null;
        details = null;
        factionSearchPanel = null;
        factionResults = null;
        factionSearch = null;
        emptyState = null;
        previousControl = null;
        nextControl = null;
    }

    private void remove(TooltipMakerAPI component) {
        if (component != null && contentPanel != null) {
            contentPanel.removeComponent(component);
        }
    }

    private void requestRebuild() {
        rebuildPending = true;
    }

    private void addControl(String text, Object id, float sidebarWidth) {
        ButtonAPI button = controls.addButton(text, id,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                Math.max(230f, sidebarWidth - 2f), 24f, 4f);
        button.setButtonPressedSound("ui_button_pressed");
    }

    @Override
    public void buttonPressed(Object buttonId) {
        if (buttonId instanceof FactionChoice) {
            setMemory(FACTION_MEMORY, ((FactionChoice) buttonId).manufacturer);
            factionDropdownOpen = false;
            factionQuery = "";
            requestRebuild();
            return;
        } else if (PRIMARY_ID.equals(buttonId)) {
            setMemory(PRIMARY_MEMORY, getPrimary().next().label);
            factionDropdownOpen = false;
        } else if (SECONDARY_ID.equals(buttonId)) {
            setMemory(SECONDARY_MEMORY, getSecondary().next().label);
            factionDropdownOpen = false;
        } else if (SIZE_ID.equals(buttonId)) {
            setMemory(SIZE_MEMORY, getSizeFilter().next().label);
            factionDropdownOpen = false;
        } else if (FACTION_ID.equals(buttonId)) {
            factionDropdownOpen = !factionDropdownOpen;
            factionQuery = "";
        } else if (PREVIOUS_ID.equals(buttonId)) {
            selectRelative(-1);
            return;
        } else if (NEXT_ID.equals(buttonId)) {
            selectRelative(1);
            return;
        } else {
            return;
        }
        requestRebuild();
    }

    private void selectRelative(int amount) {
        if (visibleShips.isEmpty()) return;
        select(Math.max(0, Math.min(visibleShips.size() - 1, selectedIndex + amount)));
    }

    private void select(int index) {
        if (index < 0 || index >= visibleShips.size() || index == selectedIndex) return;
        selectedIndex = index;
        setMemory(SELECTED_MEMORY, safe(visibleShips.get(index).getId()));
        requestRebuild();
    }

    private void addToTourManifest(FleetMemberAPI member) {
        if (member == null || tourManifest.size() >= getManifestCapacity()) return;
        String key = ShipGalleryData.galleryHullKey(member);
        if (key.isEmpty()) return;
        for (FleetMemberAPI existing : tourManifest) {
            if (key.equals(ShipGalleryData.galleryHullKey(existing))) return;
        }
        tourManifest.add(member);
        persistTourManifest();
        requestRebuild();
    }

    private void removeFromTourManifest(int index) {
        if (index < 0 || index >= tourManifest.size()) return;
        tourManifest.remove(index);
        persistTourManifest();
        requestRebuild();
    }

    private void restoreTourManifest() {
        List<FleetMemberAPI> restored = new ArrayList<FleetMemberAPI>();
        String encoded = getMemory(TOUR_MANIFEST_MEMORY);
        if (!encoded.isEmpty()) {
            String[] keys = encoded.split("\\|");
            for (String rawKey : keys) {
                String key = safe(rawKey).toLowerCase(Locale.ROOT);
                if (key.isEmpty()) continue;
                FleetMemberAPI match = findShipByHullKey(allShips, key);
                if (match != null && restored.size() < getManifestCapacity()) {
                    restored.add(match);
                }
            }
        }
        tourManifest = restored;
        persistTourManifest();
    }

    private void persistTourManifest() {
        StringBuilder encoded = new StringBuilder();
        for (FleetMemberAPI member : tourManifest) {
            String key = ShipGalleryData.galleryHullKey(member);
            if (key.isEmpty()) continue;
            if (encoded.length() > 0) encoded.append('|');
            encoded.append(key);
        }
        setMemory(TOUR_MANIFEST_MEMORY, encoded.toString());
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

    private FleetMemberAPI getSelected() {
        if (selectedIndex < 0 || selectedIndex >= visibleShips.size()) return null;
        return visibleShips.get(selectedIndex);
    }

    private float getSidebarWidth() {
        return Math.min(330f, Math.max(260f, width * 0.28f));
    }

    private float getStageWidth() {
        return Math.max(260f, width - getSidebarWidth() - OUTER_PAD * 3f);
    }

    private float getStageHeight() {
        return Math.max(140f,
                height - SIDEBAR_TOP - RAIL_BOTTOM - RAIL_HEIGHT - 14f);
    }

    private float getManifestWidth() {
        return MANIFEST_PAD * 2f
                + MANIFEST_COLUMNS * MANIFEST_SLOT
                + (MANIFEST_COLUMNS - 1) * MANIFEST_GAP;
    }

    private int getManifestRows() {
        float usable = getStageHeight() - MANIFEST_PAD * 2f;
        return Math.max(1, (int) Math.floor(
                (usable + MANIFEST_GAP) / (MANIFEST_SLOT + MANIFEST_GAP)));
    }

    private int getManifestCapacity() {
        return Math.min(GalleryTourLauncher.MAX_EXHIBITS,
                getManifestRows() * MANIFEST_COLUMNS);
    }

    private float manifestSlotX(float stageX, int index) {
        int column = index % MANIFEST_COLUMNS;
        return stageX + MANIFEST_PAD
                + column * (MANIFEST_SLOT + MANIFEST_GAP);
    }

    private float manifestSlotY(float stageY, float stageHeight, int index) {
        int row = index / MANIFEST_COLUMNS;
        return stageY + stageHeight - MANIFEST_PAD - MANIFEST_SLOT
                - row * (MANIFEST_SLOT + MANIFEST_GAP);
    }

    private float getSidebarContentTop() {
        return SIDEBAR_TOP + CONTROLS_HEIGHT + SIDEBAR_GAP;
    }

    private float getSidebarContentHeight() {
        float railClearance = RAIL_BOTTOM + RAIL_HEIGHT + 14f;
        return Math.max(116f, height - getSidebarContentTop() - railClearance);
    }

    private int getVisibleSlotCount() {
        int count = (int) Math.floor((width - RAIL_SIDE_PAD * 2f) / ICON_SLOT);
        count = Math.max(5, Math.min(13, count));
        if (count % 2 == 0) count--;
        return count;
    }

    private int getWindowStart() {
        int shown = Math.min(getVisibleSlotCount(), visibleShips.size());
        return Math.max(0, Math.min(visibleShips.size() - shown, selectedIndex - shown / 2));
    }

    private int getWindowCount() {
        return Math.min(getVisibleSlotCount(), visibleShips.size());
    }

    private float getWindowLeft() {
        return position.getX() + (width - getWindowCount() * ICON_SLOT) * 0.5f;
    }

    private static String validateManufacturer(String value, List<String> manufacturers) {
        if (value == null || value.trim().isEmpty()) return "";
        for (String manufacturer : manufacturers) {
            if (manufacturer.equalsIgnoreCase(value.trim())) return manufacturer;
        }
        return "";
    }

    private static int findShipIndexById(List<FleetMemberAPI> ships, String id) {
        if (id == null || id.isEmpty()) return -1;
        for (int i = 0; i < ships.size(); i++) {
            if (id.equals(safe(ships.get(i).getId()))) return i;
        }
        return -1;
    }

    private static FleetMemberAPI findShipById(
            List<FleetMemberAPI> ships, String id) {
        int index = findShipIndexById(ships, id);
        return index < 0 ? null : ships.get(index);
    }

    private static FleetMemberAPI findFirstEligibleShuttle(
            List<FleetMemberAPI> ships) {
        for (FleetMemberAPI member : ships) {
            if (ShipGalleryData.isTourShuttleEligible(member)) return member;
        }
        return null;
    }

    private static FleetMemberAPI findShipByHullKey(
            List<FleetMemberAPI> ships, String key) {
        if (key == null || key.isEmpty()) return null;
        for (FleetMemberAPI member : ships) {
            if (key.equals(ShipGalleryData.galleryHullKey(member))) return member;
        }
        return null;
    }

    private static String displayShipName(FleetMemberAPI member) {
        String value = member == null ? "" : safe(member.getShipName());
        if (!value.isEmpty()) return value;
        return member == null || member.getHullSpec() == null
                ? "Unknown vessel" : safe(member.getHullSpec().getHullName());
    }

    private static String displayManufacturer(FleetMemberAPI member) {
        String value = ShipGalleryData.faction(member);
        return value.isEmpty() ? "Unknown" : value;
    }

    private static String displayHullSize(FleetMemberAPI member) {
        if (member == null || member.getHullSpec() == null) return "Unknown";
        ShipAPI.HullSize size = member.getHullSpec().getHullSize();
        if (size == ShipAPI.HullSize.FRIGATE) return "Frigate";
        if (size == ShipAPI.HullSize.DESTROYER) return "Destroyer";
        if (size == ShipAPI.HullSize.CRUISER) return "Cruiser";
        if (size == ShipAPI.HullSize.CAPITAL_SHIP) return "Capital ship";
        return "Unknown";
    }

    private static String displayDp(FleetMemberAPI member) {
        if (member == null) return "0";
        float value = Math.max(0f, member.getUnmodifiedDeploymentPointsCost());
        if (Math.abs(value - Math.round(value)) < 0.01f) {
            return Integer.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static void addCodexDescription(TooltipMakerAPI tooltip, ShipHullSpecAPI spec) {
        tooltip.addSectionHeading("Codex entry", Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(), Alignment.MID, 8f);
        if (spec == null) {
            tooltip.addPara("No Codex entry is available for this hull.",
                    Misc.getGrayColor(), 6f);
            return;
        }

        String prefix = safe(spec.getDescriptionPrefix());
        if (!prefix.isEmpty()) tooltip.addPara(prefix, 6f);

        Description description = findShipDescription(spec);
        if (description == null) {
            tooltip.addPara("No Codex entry is available for this hull.",
                    Misc.getGrayColor(), 6f);
            return;
        }
        boolean added = false;
        for (String paragraph : description.getText1Paras()) {
            String text = safe(paragraph);
            if (text.isEmpty()) continue;
            tooltip.addPara(text, added || !prefix.isEmpty() ? 8f : 6f);
            added = true;
        }
        if (!added && prefix.isEmpty()) {
            tooltip.addPara("No Codex entry is available for this hull.",
                    Misc.getGrayColor(), 6f);
        }
    }

    private static Description findShipDescription(ShipHullSpecAPI spec) {
        if (spec == null) return null;
        Description description = getShipDescription(spec.getDescriptionId());
        if (description != null) return description;

        try {
            description = getShipDescription(spec.getDParentHull() == null
                    ? null : spec.getDParentHull().getDescriptionId());
            if (description != null) return description;
        } catch (Throwable ignored) {
        }
        try {
            description = getShipDescription(spec.getBaseHull() == null
                    ? null : spec.getBaseHull().getDescriptionId());
            if (description != null) return description;
        } catch (Throwable ignored) {
        }
        description = getHullSpecDescription(spec.getRestoredToHullId());
        if (description != null) return description;
        description = getHullSpecDescription(spec.getBaseHullId());
        if (description != null) return description;
        return getShipDescription(spec.getHullId());
    }

    private static Description getHullSpecDescription(String hullId) {
        if (hullId == null || hullId.trim().isEmpty()) return null;
        try {
            ShipHullSpecAPI hull = Global.getSettings().getHullSpec(hullId.trim());
            return hull == null ? null : getShipDescription(hull.getDescriptionId());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Description getShipDescription(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        try {
            Description description = Global.getSettings().getDescription(
                    id.trim(), Description.Type.SHIP);
            return description != null && description.hasText1() ? description : null;
        } catch (Throwable ignored) {
            return null;
        }
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

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void renderBelow(float alphaMult) {
        if (position == null) return;

        float stageX = position.getX() + OUTER_PAD;
        float stageY = position.getY() + RAIL_BOTTOM + RAIL_HEIGHT + 14f;
        float stageWidth = getStageWidth();
        float stageHeight = getStageHeight();
        drawRect(stageX, stageY, stageWidth, stageHeight, VOID, 0.88f * alphaMult);
        drawRect(stageX + 1f, stageY + 1f, stageWidth - 2f, stageHeight - 2f,
                STAGE, 0.68f * alphaMult);
        drawBorder(stageX, stageY, stageWidth, stageHeight,
                Misc.getDarkPlayerColor(), 0.55f * alphaMult, 1f);

        renderTourManifestFrames(stageX, stageY, stageHeight, alphaMult);

        float heroX = stageX + getManifestWidth() + HERO_GAP;
        float heroWidth = Math.max(100f,
                stageWidth - getManifestWidth() - HERO_GAP);
        if (selectedIndex >= 0 && !visibleShips.isEmpty()) {
            renderHeroBerth(getSelected(), heroX, stageY,
                    heroWidth, stageHeight, alphaMult);
        }

        float railX = position.getX() + RAIL_SIDE_PAD;
        float railY = position.getY() + RAIL_BOTTOM;
        float railWidth = width - RAIL_SIDE_PAD * 2f;
        drawRect(railX, railY, railWidth, RAIL_HEIGHT, RAIL, 0.92f * alphaMult);
        drawBorder(railX, railY, railWidth, RAIL_HEIGHT,
                Misc.getDarkPlayerColor(), 0.75f * alphaMult, 1f);
        renderConveyorBelt(railX, railY, railWidth, alphaMult);

        if (selectedIndex >= 0 && !visibleShips.isEmpty()) {
            renderRailFrames(alphaMult);
        }
    }

    private void renderTourManifestFrames(
            float stageX, float stageY, float stageHeight, float alphaMult) {
        float manifestWidth = getManifestWidth();
        drawRect(stageX + 2f, stageY + 2f,
                manifestWidth - 4f, stageHeight - 4f,
                VOID, 0.72f * alphaMult);
        drawBorder(stageX + 2f, stageY + 2f,
                manifestWidth - 4f, stageHeight - 4f,
                BERTH_CYAN, 0.32f * alphaMult, 1f);

        int capacity = getManifestCapacity();
        for (int index = 0; index < capacity; index++) {
            float x = manifestSlotX(stageX, index);
            float y = manifestSlotY(stageY, stageHeight, index);
            boolean occupied = index < tourManifest.size();
            drawRect(x, y, MANIFEST_SLOT, MANIFEST_SLOT,
                    occupied ? STAGE : VOID,
                    (occupied ? 0.66f : 0.34f) * alphaMult);
            drawBorder(x, y, MANIFEST_SLOT, MANIFEST_SLOT,
                    occupied ? BERTH_AMBER : Misc.getDarkPlayerColor(),
                    (occupied ? 0.72f : 0.42f) * alphaMult,
                    occupied ? 1.5f : 1f);
            if (!occupied) {
                float centerX = x + MANIFEST_SLOT * 0.5f;
                float centerY = y + MANIFEST_SLOT * 0.5f;
                drawLine(centerX - 8f, centerY, centerX + 8f, centerY,
                        BERTH_CYAN, 0.18f * alphaMult, 1f);
            }
        }
    }

    private static void renderConveyorBelt(
            float x, float y, float beltWidth, float alphaMult) {
        float innerX = x + 7f;
        float innerY = y + 8f;
        float innerWidth = beltWidth - 14f;
        float innerHeight = RAIL_HEIGHT - 16f;
        drawRect(innerX, innerY, innerWidth, innerHeight,
                new Color(8, 18, 23), 0.78f * alphaMult);
        drawLine(innerX, innerY + 7f, innerX + innerWidth, innerY + 7f,
                BERTH_CYAN, 0.34f * alphaMult, 2f);
        drawLine(innerX, innerY + innerHeight - 7f,
                innerX + innerWidth, innerY + innerHeight - 7f,
                BERTH_CYAN, 0.34f * alphaMult, 2f);

        for (float slatX = innerX + 14f; slatX < innerX + innerWidth; slatX += 26f) {
            drawLine(slatX, innerY + 10f, slatX, innerY + innerHeight - 10f,
                    SOFT_WHITE, 0.08f * alphaMult, 1f);
        }
        for (float arrowX = innerX + 28f;
                arrowX < innerX + innerWidth - 14f; arrowX += 104f) {
            float arrowY = innerY + 7f;
            drawLine(arrowX - 6f, arrowY - 3f, arrowX, arrowY,
                    BERTH_AMBER, 0.48f * alphaMult, 1.4f);
            drawLine(arrowX, arrowY, arrowX - 6f, arrowY + 3f,
                    BERTH_AMBER, 0.48f * alphaMult, 1.4f);
        }
    }

    /** Draws a recessed museum berth sized to the selected hull class. */
    private static void renderHeroBerth(
            FleetMemberAPI member,
            float stageX,
            float stageY,
            float stageWidth,
            float stageHeight,
            float alphaMult) {
        if (member == null || member.getHullSpec() == null) return;

        int scaleClass = berthScaleClass(member.getHullSpec().getHullSize());
        float[] widthFractions = {0.30f, 0.43f, 0.60f, 0.78f};
        float[] heightFractions = {0.34f, 0.43f, 0.54f, 0.68f};
        float berthWidth = Math.min(stageWidth - 44f,
                Math.max(150f, stageWidth * widthFractions[scaleClass - 1]));
        float berthHeight = Math.min(stageHeight - 34f,
                Math.max(132f, stageHeight * heightFractions[scaleClass - 1]));
        float centerX = stageX + stageWidth * 0.5f;
        float centerY = stageY + stageHeight * 0.52f;
        float left = centerX - berthWidth * 0.5f;
        float bottom = centerY - berthHeight * 0.5f;
        float chamfer = Math.max(12f,
                Math.min(30f, Math.min(berthWidth, berthHeight) * 0.08f));

        drawChamferedFill(left, bottom, berthWidth, berthHeight, chamfer,
                new Color(8, 23, 29), 0.58f * alphaMult);
        drawChamferedFrame(left, bottom, berthWidth, berthHeight, chamfer,
                BERTH_CYAN, 0.48f * alphaMult, 1.4f);
        drawChamferedFrame(left + 10f, bottom + 10f,
                berthWidth - 20f, berthHeight - 20f,
                Math.max(6f, chamfer - 6f),
                Misc.getDarkPlayerColor(), 0.62f * alphaMult, 1f);

        // Recessed deck centerlines keep the berth legible without placing a
        // solid panel directly beneath the ship sprite.
        drawLine(centerX, bottom + 14f, centerX, bottom + berthHeight - 14f,
                BERTH_CYAN, 0.13f * alphaMult, 1f);
        drawLine(left + 14f, centerY, left + berthWidth - 14f, centerY,
                BERTH_CYAN, 0.10f * alphaMult, 1f);

        drawDockingClamp(left - 3f, centerY, true, alphaMult);
        drawDockingClamp(left + berthWidth + 3f, centerY, false, alphaMult);
        drawDockingClamp(centerX, bottom - 3f, true, alphaMult);
        drawDockingClamp(centerX, bottom + berthHeight + 3f, false, alphaMult);

        // One through four registration marks make hull-size changes legible.
        float tickGap = 12f;
        float ticksWidth = (scaleClass - 1) * tickGap;
        for (int i = 0; i < scaleClass; i++) {
            float tickX = centerX - ticksWidth * 0.5f + i * tickGap;
            drawRect(tickX - 3.5f, bottom + 5f,
                    7f, 2.5f, BERTH_AMBER, 0.88f * alphaMult);
        }
    }

    private static int berthScaleClass(ShipAPI.HullSize hullSize) {
        if (hullSize == ShipAPI.HullSize.DESTROYER) return 2;
        if (hullSize == ShipAPI.HullSize.CRUISER) return 3;
        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) return 4;
        return 1;
    }

    private static void drawDockingClamp(
            float x, float y, boolean pointsPositive, float alphaMult) {
        float direction = pointsPositive ? 1f : -1f;
        float innerX = x + direction * 13f;
        drawLine(x, y - 13f, x, y + 13f,
                BERTH_AMBER, 0.82f * alphaMult, 2.4f);
        drawLine(x, y - 13f, innerX, y - 7f,
                BERTH_AMBER, 0.64f * alphaMult, 1.5f);
        drawLine(x, y + 13f, innerX, y + 7f,
                BERTH_AMBER, 0.64f * alphaMult, 1.5f);
    }

    private static void drawChamferedFill(
            float x, float y, float width, float height, float chamfer,
            Color color, float alpha) {
        if (width <= 0f || height <= 0f || alpha <= 0f) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, Math.max(0f, Math.min(1f, alpha)));
        GL11.glBegin(GL11.GL_POLYGON);
        addChamferedVertices(x, y, width, height, chamfer);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private static void drawChamferedFrame(
            float x, float y, float width, float height, float chamfer,
            Color color, float alpha, float lineWidth) {
        if (width <= 0f || height <= 0f || alpha <= 0f) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, Math.max(0f, Math.min(1f, alpha)));
        GL11.glBegin(GL11.GL_LINE_LOOP);
        addChamferedVertices(x, y, width, height, chamfer);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private static void addChamferedVertices(
            float x, float y, float width, float height, float chamfer) {
        GL11.glVertex2f(x + chamfer, y);
        GL11.glVertex2f(x + width - chamfer, y);
        GL11.glVertex2f(x + width, y + chamfer);
        GL11.glVertex2f(x + width, y + height - chamfer);
        GL11.glVertex2f(x + width - chamfer, y + height);
        GL11.glVertex2f(x + chamfer, y + height);
        GL11.glVertex2f(x, y + height - chamfer);
        GL11.glVertex2f(x, y + chamfer);
    }

    private static void drawLine(
            float x1, float y1, float x2, float y2,
            Color color, float alpha, float lineWidth) {
        if (alpha <= 0f) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, Math.max(0f, Math.min(1f, alpha)));
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    @Override
    public void render(float alphaMult) {
        if (position == null) return;

        FleetMemberAPI selected = getSelected();
        float stageX = position.getX() + OUTER_PAD;
        float stageY = position.getY() + RAIL_BOTTOM + RAIL_HEIGHT + 14f;
        float stageWidth = getStageWidth();
        float stageHeight = getStageHeight();
        float heroX = stageX + getManifestWidth() + HERO_GAP;
        float heroWidth = Math.max(100f,
                stageWidth - getManifestWidth() - HERO_GAP);
        if (selected != null) {
            renderShip(selected, heroX + heroWidth * 0.5f,
                    stageY + stageHeight * 0.52f,
                    Math.max(80f, heroWidth - 54f),
                    Math.max(80f, stageHeight - 42f), alphaMult, true);
        }

        for (int index = 0; index < tourManifest.size(); index++) {
            float x = manifestSlotX(stageX, index) + MANIFEST_SLOT * 0.5f;
            float y = manifestSlotY(stageY, stageHeight, index)
                    + MANIFEST_SLOT * 0.5f;
            renderShip(tourManifest.get(index), x, y,
                    MANIFEST_SLOT - 12f, MANIFEST_SLOT - 12f,
                    alphaMult * 0.88f, false);
        }

        int start = getWindowStart();
        int count = getWindowCount();
        float left = getWindowLeft();
        float centerY = position.getY() + RAIL_BOTTOM + RAIL_HEIGHT * 0.5f;
        for (int slot = 0; slot < count; slot++) {
            int index = start + slot;
            boolean selectedIcon = index == selectedIndex;
            float iconSize = selectedIcon ? 68f : 56f;
            float centerX = left + slot * ICON_SLOT + ICON_SLOT * 0.5f;
            renderShip(visibleShips.get(index), centerX,
                    centerY + (selectedIcon ? 4f : 0f), iconSize, iconSize,
                    alphaMult * (selectedIcon ? 1f : 0.64f), false);
        }
    }

    private void renderRailFrames(float alphaMult) {
        int start = getWindowStart();
        int count = getWindowCount();
        float left = getWindowLeft();
        float bottom = position.getY() + RAIL_BOTTOM + 7f;
        for (int slot = 0; slot < count; slot++) {
            int index = start + slot;
            float x = left + slot * ICON_SLOT + 5f;
            float frameWidth = ICON_SLOT - 10f;
            if (index == selectedIndex) {
                drawRect(x, bottom, frameWidth, RAIL_HEIGHT - 14f,
                        Misc.getDarkPlayerColor(), 0.72f * alphaMult);
                drawBorder(x, bottom, frameWidth, RAIL_HEIGHT - 14f,
                        Misc.getBasePlayerColor(), alphaMult, 2f);
            } else if (index == hoveredIndex) {
                drawRect(x, bottom, frameWidth, RAIL_HEIGHT - 14f,
                        SOFT_WHITE, 0.09f * alphaMult);
                drawBorder(x, bottom, frameWidth, RAIL_HEIGHT - 14f,
                        Misc.getGrayColor(), 0.7f * alphaMult, 1f);
            }
        }
    }

    private static void renderShip(FleetMemberAPI member, float centerX, float centerY,
                                   float maxWidth, float maxHeight, float alpha,
                                   boolean preserveRelativeScale) {
        if (member == null || member.getHullSpec() == null || alpha <= 0f) return;
        String spriteName = safe(member.getSpriteOverride());
        if (spriteName.isEmpty()) spriteName = safe(member.getHullSpec().getSpriteName());
        if (spriteName.isEmpty()) return;

        SpriteAPI sprite = null;
        float oldWidth = 0f;
        float oldHeight = 0f;
        float oldAngle = 0f;
        float oldAlpha = 1f;
        Color oldColor = Color.WHITE;
        int oldBlendSource = GL11.GL_SRC_ALPHA;
        int oldBlendDestination = GL11.GL_ONE_MINUS_SRC_ALPHA;
        boolean stateCaptured = false;
        try {
            sprite = Global.getSettings().getSprite(spriteName);
            oldWidth = sprite.getWidth();
            oldHeight = sprite.getHeight();
            oldAngle = sprite.getAngle();
            oldAlpha = sprite.getAlphaMult();
            oldColor = sprite.getColor();
            oldBlendSource = sprite.getBlendSrc();
            oldBlendDestination = sprite.getBlendDest();
            stateCaptured = true;

            float sourceWidth = oldWidth;
            float sourceHeight = oldHeight;
            Vector2f override = member.getOverrideSpriteSize();
            if (override != null && override.x > 0f && override.y > 0f) {
                sourceWidth = override.x;
                sourceHeight = override.y;
            }
            if (sourceWidth <= 0f || sourceHeight <= 0f) return;

            float scale = Math.min(maxWidth / sourceWidth, maxHeight / sourceHeight);
            if (preserveRelativeScale) {
                scale = Math.min(scale, HERO_MAX_NATIVE_SCALE);
            }
            sprite.setSize(sourceWidth * scale, sourceHeight * scale);
            sprite.setAngle(0f);
            sprite.setColor(Color.WHITE);
            sprite.setAlphaMult(Math.max(0f, Math.min(1f, alpha)));
            sprite.setNormalBlend();
            sprite.renderAtCenter(centerX, centerY);
        } catch (Throwable ignored) {
            // A bad third-party sprite should not prevent the rest of the gallery from rendering.
        } finally {
            if (sprite != null && stateCaptured) {
                try {
                    sprite.setSize(oldWidth, oldHeight);
                    sprite.setAngle(oldAngle);
                    sprite.setColor(oldColor == null ? Color.WHITE : oldColor);
                    sprite.setAlphaMult(oldAlpha);
                    sprite.setBlendFunc(oldBlendSource, oldBlendDestination);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static void drawRect(float x, float y, float width, float height,
                                 Color color, float alpha) {
        if (width <= 0f || height <= 0f || alpha <= 0f) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, Math.max(0f, Math.min(1f, alpha)));
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private static void drawBorder(float x, float y, float width, float height,
                                   Color color, float alpha, float lineWidth) {
        if (width <= 0f || height <= 0f || alpha <= 0f) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, Math.max(0f, Math.min(1f, alpha)));
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    @Override
    public void advance(float amount) {
        if (rebuildPending) {
            rebuildPending = false;
            rebuild();
            return;
        }
        if (!factionDropdownOpen || factionSearch == null) return;
        String current = factionSearch.getText();
        factionQuery = current == null ? "" : current;
        String normalized = normalizeQuery(factionQuery);
        if (!normalized.equals(renderedFactionQuery)) {
            renderedFactionQuery = normalized;
            rebuildFactionResults();
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (position == null) return;
        for (InputEventAPI event : events) {
            if (event == null || event.isConsumed()) continue;
            if (factionDropdownOpen && event.isKeyDownEvent()
                    && event.getEventValue() == Keyboard.KEY_ESCAPE) {
                factionDropdownOpen = false;
                factionQuery = "";
                requestRebuild();
                event.consume();
                return;
            }
            if (factionDropdownOpen && event.isMouseDownEvent()
                    && event.getEventValue() == 0
                    && !containsEvent(factionSearchPanel, event)
                    && !containsEvent(factionResults, event)
                    && !containsEvent(controls, event)) {
                factionDropdownOpen = false;
                factionQuery = "";
                requestRebuild();
                event.consume();
                return;
            }
            if (event.isMouseDownEvent() && event.getEventValue() == 1) {
                int manifestIndex = manifestAt(event.getX(), event.getY());
                if (manifestIndex >= 0) {
                    removeFromTourManifest(manifestIndex);
                    event.consume();
                    return;
                }
            }
            if (visibleShips.isEmpty()) continue;
            int iconIndex = iconAt(event.getX(), event.getY());
            if (event.isMouseMoveEvent()) {
                hoveredIndex = iconIndex;
            } else if (event.isMouseScrollEvent() && isInsideRail(event.getX(), event.getY())) {
                int direction = event.getEventValue() > 0 ? -1 : 1;
                selectRelative(direction);
                event.consume();
                return;
            } else if (event.isMouseDownEvent() && event.getEventValue() == 0
                    && iconIndex >= 0) {
                select(iconIndex);
                addToTourManifest(visibleShips.get(iconIndex));
                event.consume();
                return;
            }
        }
    }

    private boolean isInsideRail(float x, float y) {
        float left = position.getX() + RAIL_SIDE_PAD;
        float bottom = position.getY() + RAIL_BOTTOM;
        return x >= left && x <= left + width - RAIL_SIDE_PAD * 2f
                && y >= bottom && y <= bottom + RAIL_HEIGHT;
    }

    private int iconAt(float x, float y) {
        if (!isInsideRail(x, y)) return -1;
        int count = getWindowCount();
        float left = getWindowLeft();
        if (x < left || x >= left + count * ICON_SLOT) return -1;
        int slot = (int) ((x - left) / ICON_SLOT);
        return getWindowStart() + slot;
    }

    private int manifestAt(float x, float y) {
        float stageX = position.getX() + OUTER_PAD;
        float stageY = position.getY() + RAIL_BOTTOM + RAIL_HEIGHT + 14f;
        float stageHeight = getStageHeight();
        for (int index = 0; index < tourManifest.size(); index++) {
            float slotX = manifestSlotX(stageX, index);
            float slotY = manifestSlotY(stageY, stageHeight, index);
            if (x >= slotX && x <= slotX + MANIFEST_SLOT
                    && y >= slotY && y <= slotY + MANIFEST_SLOT) {
                return index;
            }
        }
        return -1;
    }

    private static boolean containsEvent(TooltipMakerAPI component, InputEventAPI event) {
        return component != null && component.getPosition() != null
                && component.getPosition().containsEvent(event);
    }

    private static String normalizeQuery(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private final class ContentButtonRelay extends BaseCustomUIPanelPlugin {
        @Override
        public void buttonPressed(Object buttonId) {
            ShipGalleryPanelPlugin.this.buttonPressed(buttonId);
        }
    }

    private static final class FactionChoice {
        private final String manufacturer;

        private FactionChoice(String manufacturer) {
            this.manufacturer = manufacturer == null ? "" : manufacturer;
        }
    }
}
