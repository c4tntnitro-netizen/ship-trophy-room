package shiptrophy.gallery;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.gallery.ShipGalleryData.SizeFilter;
import shiptrophy.gallery.ShipGalleryData.SortKey;

/** Cover Flow-style, read-only browser for ships displayed in every Hall. */
final class ShipGalleryPanelPlugin implements CustomUIPanelPlugin {
    interface NavigationListener {
        boolean isForeignTabAt(float x, float y);

        void foreignTabPressed();
    }

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

    private static final float OUTER_PAD = 15f;
    private static final float HEADER_HEIGHT = 62f;
    private static final float SIDEBAR_TOP = 76f;
    private static final float CONTROLS_HEIGHT = 168f;
    private static final float DETAILS_HEIGHT = 136f;
    private static final float RAIL_BOTTOM = 16f;
    private static final float RAIL_HEIGHT = 96f;
    private static final float RAIL_SIDE_PAD = 58f;
    private static final float ICON_SLOT = 82f;

    private static final Color VOID = new Color(5, 11, 15);
    private static final Color STAGE = new Color(9, 20, 26);
    private static final Color RAIL = new Color(12, 26, 33);
    private static final Color SOFT_WHITE = new Color(220, 232, 235);

    private final float width;
    private final float height;
    private final NavigationListener navigationListener;
    private CustomPanelAPI panel;
    private PositionAPI position;

    private TooltipMakerAPI header;
    private TooltipMakerAPI controls;
    private TooltipMakerAPI details;
    private TooltipMakerAPI emptyState;
    private TooltipMakerAPI previousControl;
    private TooltipMakerAPI nextControl;

    private List<FleetMemberAPI> allShips = Collections.emptyList();
    private List<FleetMemberAPI> visibleShips = Collections.emptyList();
    private int selectedIndex = -1;
    private int hoveredIndex = -1;

    ShipGalleryPanelPlugin(float width, float height,
                           NavigationListener navigationListener) {
        this.width = width;
        this.height = height;
        this.navigationListener = navigationListener;
    }

    void init(CustomPanelAPI panel) {
        this.panel = panel;
        rebuild();
    }

    private void rebuild() {
        if (panel == null) return;
        removeUi();
        refreshModel();

        header = panel.createUIElement(Math.max(560f, width - OUTER_PAD * 2f),
                HEADER_HEIGHT, false);
        header.addTitle("Ship Gallery", Misc.getBasePlayerColor());
        if (allShips.isEmpty()) {
            header.addPara("A read-only catalog of the vessels preserved across your functional Halls of Triumph.",
                    4f, Misc.getGrayColor(), "read-only");
        } else {
            header.addPara("Select a hull in the filmstrip, or scroll over it to browse. Showing %s of %s stored ships.",
                    4f, Misc.getHighlightColor(), Integer.toString(visibleShips.size()),
                    Integer.toString(allShips.size()));
        }
        panel.addUIElement(header).inTL(OUTER_PAD, 8f);

        buildControls();

        if (allShips.isEmpty()) {
            buildEmptyState("No ships are currently stored in a functional Hall of Triumph.", false);
        } else if (visibleShips.isEmpty()) {
            buildEmptyState("No displayed ships match the active filters.", true);
        } else {
            buildDetails();
            buildNavigation();
        }
    }

    private void refreshModel() {
        allShips = ShipGalleryData.getAllShips();
        List<String> manufacturers = ShipGalleryData.getManufacturers(allShips);
        String manufacturer = validateManufacturer(getManufacturer(), manufacturers);
        if (!manufacturer.equals(getManufacturer())) setMemory(FACTION_MEMORY, manufacturer);

        visibleShips = ShipGalleryData.filterAndSort(allShips, getSizeFilter(), manufacturer,
                getPrimary(), getSecondary());
        selectedIndex = findShipById(visibleShips, getMemory(SELECTED_MEMORY));
        if (selectedIndex < 0 && !visibleShips.isEmpty()) selectedIndex = 0;
        if (selectedIndex >= 0) {
            setMemory(SELECTED_MEMORY, safe(visibleShips.get(selectedIndex).getId()));
        }
        hoveredIndex = -1;
    }

    private void buildControls() {
        float sidebarWidth = getSidebarWidth();
        controls = panel.createUIElement(sidebarWidth, CONTROLS_HEIGHT, false);
        controls.addSectionHeading("Catalog order", Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(), Alignment.MID, 0f);
        controls.addPara("Each control cycles through its available values.", 4f,
                Misc.getGrayColor(), "cycles");
        addControl("Primary: " + getPrimary().label, PRIMARY_ID, sidebarWidth);
        addControl("Then by: " + getSecondary().label, SECONDARY_ID, sidebarWidth);
        addControl("Hull size: " + getSizeFilter().label, SIZE_ID, sidebarWidth);
        String manufacturer = getManufacturer();
        addControl("Faction: " + (manufacturer.isEmpty() ? "All" : manufacturer),
                FACTION_ID, sidebarWidth);
        panel.addUIElement(controls).inTR(OUTER_PAD, SIDEBAR_TOP);
    }

    private void buildDetails() {
        FleetMemberAPI selected = getSelected();
        if (selected == null || selected.getHullSpec() == null) return;

        float sidebarWidth = getSidebarWidth();
        details = panel.createUIElement(sidebarWidth, DETAILS_HEIGHT, false);
        details.addTitle(displayShipName(selected), Misc.getBasePlayerColor());
        details.addPara(selected.getHullSpec().getHullNameWithDashClass(), 2f,
                Misc.getGrayColor(), selected.getHullSpec().getHullName());
        details.beginGrid(Math.max(110f, sidebarWidth * 0.46f), 1);
        details.addToGrid(0, 0, "Manufacturer", displayManufacturer(selected));
        details.addToGrid(0, 1, "Hull size", displayHullSize(selected));
        details.addToGrid(0, 2, "Deployment points", displayDp(selected));
        details.addToGrid(0, 3, "Gallery status", "Preserved", Misc.getHighlightColor());
        details.addGrid(4f);
        panel.addUIElement(details).inTR(OUTER_PAD,
                SIDEBAR_TOP + CONTROLS_HEIGHT + 8f);
    }

    private void buildNavigation() {
        if (visibleShips.size() <= 1) return;

        previousControl = panel.createUIElement(42f, 32f, false);
        ButtonAPI previous = previousControl.addButton("<", PREVIOUS_ID,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), 38f, 26f, 0f);
        previous.setButtonPressedSound("ui_button_pressed");
        previous.setEnabled(selectedIndex > 0);
        panel.addUIElement(previousControl).inBL(OUTER_PAD, RAIL_BOTTOM + 31f);

        nextControl = panel.createUIElement(42f, 32f, false);
        ButtonAPI next = nextControl.addButton(">", NEXT_ID,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), 38f, 26f, 0f);
        next.setButtonPressedSound("ui_button_pressed");
        next.setEnabled(selectedIndex + 1 < visibleShips.size());
        panel.addUIElement(nextControl).inBR(OUTER_PAD, RAIL_BOTTOM + 31f);
    }

    private void buildEmptyState(String message, boolean filtered) {
        float stageWidth = getStageWidth();
        emptyState = panel.createUIElement(Math.max(280f, stageWidth - 60f), 90f, false);
        emptyState.setParaOrbitronLarge();
        emptyState.addPara(message, 0f,
                filtered ? Misc.getNegativeHighlightColor() : Misc.getGrayColor(),
                filtered ? "No displayed ships" : "No ships");
        emptyState.setParaFontDefault();
        emptyState.addPara(filtered
                ? "Cycle the size or faction controls to widen the collection."
                : "Store a ship in a Hall's dedicated storage to add it here.", 8f,
                Misc.getGrayColor());
        panel.addUIElement(emptyState).inTL(OUTER_PAD + 30f, 150f);
    }

    private void removeUi() {
        remove(header);
        remove(controls);
        remove(details);
        remove(emptyState);
        remove(previousControl);
        remove(nextControl);
        header = null;
        controls = null;
        details = null;
        emptyState = null;
        previousControl = null;
        nextControl = null;
    }

    private void remove(TooltipMakerAPI component) {
        if (component != null) panel.removeComponent(component);
    }

    private void addControl(String text, Object id, float sidebarWidth) {
        ButtonAPI button = controls.addButton(text, id,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                Math.max(230f, sidebarWidth - 2f), 24f, 4f);
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
        } else if (PREVIOUS_ID.equals(buttonId)) {
            selectRelative(-1);
            return;
        } else if (NEXT_ID.equals(buttonId)) {
            selectRelative(1);
            return;
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

    private void selectRelative(int amount) {
        if (visibleShips.isEmpty()) return;
        select(Math.max(0, Math.min(visibleShips.size() - 1, selectedIndex + amount)));
    }

    private void select(int index) {
        if (index < 0 || index >= visibleShips.size() || index == selectedIndex) return;
        selectedIndex = index;
        setMemory(SELECTED_MEMORY, safe(visibleShips.get(index).getId()));
        rebuild();
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

    private static int findShipById(List<FleetMemberAPI> ships, String id) {
        if (id == null || id.isEmpty()) return -1;
        for (int i = 0; i < ships.size(); i++) {
            if (id.equals(safe(ships.get(i).getId()))) return i;
        }
        return -1;
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
        float stageHeight = Math.max(140f,
                position.getY() + height - SIDEBAR_TOP - stageY);
        drawRect(stageX, stageY, stageWidth, stageHeight, VOID, 0.88f * alphaMult);
        drawRect(stageX + 1f, stageY + 1f, stageWidth - 2f, stageHeight - 2f,
                STAGE, 0.68f * alphaMult);
        drawBorder(stageX, stageY, stageWidth, stageHeight,
                Misc.getDarkPlayerColor(), 0.55f * alphaMult, 1f);

        float railX = position.getX() + RAIL_SIDE_PAD;
        float railY = position.getY() + RAIL_BOTTOM;
        float railWidth = width - RAIL_SIDE_PAD * 2f;
        drawRect(railX, railY, railWidth, RAIL_HEIGHT, RAIL, 0.92f * alphaMult);
        drawBorder(railX, railY, railWidth, RAIL_HEIGHT,
                Misc.getDarkPlayerColor(), 0.75f * alphaMult, 1f);

        if (selectedIndex >= 0 && !visibleShips.isEmpty()) {
            renderRailFrames(alphaMult);
        }
    }

    @Override
    public void render(float alphaMult) {
        if (position == null || selectedIndex < 0 || visibleShips.isEmpty()) return;

        FleetMemberAPI selected = getSelected();
        float stageX = position.getX() + OUTER_PAD;
        float stageY = position.getY() + RAIL_BOTTOM + RAIL_HEIGHT + 14f;
        float stageWidth = getStageWidth();
        float stageHeight = Math.max(140f,
                position.getY() + height - SIDEBAR_TOP - stageY);
        renderShip(selected, stageX + stageWidth * 0.5f,
                stageY + stageHeight * 0.52f,
                Math.max(80f, stageWidth - 54f),
                Math.max(80f, stageHeight - 42f), alphaMult);

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
                    alphaMult * (selectedIcon ? 1f : 0.64f));
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
                                   float maxWidth, float maxHeight, float alpha) {
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
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (position == null) return;
        for (InputEventAPI event : events) {
            if (event == null || event.isConsumed()) continue;
            if (event.isMouseDownEvent() && event.getEventValue() == 0
                    && navigationListener != null
                    && navigationListener.isForeignTabAt(event.getX(), event.getY())) {
                navigationListener.foreignTabPressed();
                return;
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
}
