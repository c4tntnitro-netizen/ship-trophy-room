package shiptrophy.gallery;

import java.util.ArrayList;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

/** Injects the Ship Gallery into the vanilla Command screen. */
public final class ShipGalleryCoreScript implements EveryFrameScript,
        ShipGalleryPanelPlugin.NavigationListener {
    private static final String TAB_NAME = "Ship Gallery";

    private transient UIPanelAPI root;
    private transient ButtonAPI galleryButton;
    private transient CustomPanelAPI galleryPanel;
    private transient boolean galleryActive;
    private transient boolean loggedFailure;
    private transient boolean loggedNotReady;
    private transient float contentWidth;
    private transient float contentHeight;
    private transient boolean foreignTabRequested;

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
        if (Global.getSector() == null || Global.getSector().getCampaignUI() == null
                || Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.OUTPOSTS) {
            if (root != null) reset();
            return;
        }

        try {
            UIPanelAPI activeRoot = UiReflection.getCurrentCorePanel();
            if (activeRoot == null) {
                logNotReady("the active Command panel is not available yet");
                return;
            }
            if (activeRoot != root) {
                reset();
                root = activeRoot;
                inject();
            } else if (galleryButton == null
                    || !UiReflection.children(activeRoot).contains(galleryButton)) {
                clearAttachedState();
                inject();
            }
            if (galleryButton == null) return;
            repositionAfterRightmostTab();
            handleGalleryTab();
        } catch (Throwable ex) {
            if (!loggedFailure) {
                loggedFailure = true;
                Global.getLogger(ShipGalleryCoreScript.class).error(
                        "Hall of Triumph: unable to attach Ship Gallery to the Command screen", ex);
            }
        }
    }

    private void inject() {
        ButtonAPI income = findButton("income");
        ButtonAPI colonies = findButton("colonies");
        if (income == null || colonies == null) {
            logNotReady("the Colonies and Income tab buttons are not available yet");
            return;
        }

        Object rawMap = UiReflection.invoke(root, "getButtonToTab");
        Object incomePanel = rawMap instanceof Map<?, ?>
                ? ((Map<?, ?>) rawMap).get(income) : null;
        contentWidth = Math.max(700f,
                Global.getSettings().getScreenWidth() - colonies.getPosition().getX());
        contentHeight = Math.max(500f, incomePanel instanceof UIComponentAPI
                ? ((UIComponentAPI) incomePanel).getPosition().getHeight()
                : root.getPosition().getHeight() - income.getPosition().getHeight());

        galleryButton = createTabButton(140f, income.getPosition().getHeight());
        root.addComponent(galleryButton);
        repositionAfterRightmostTab();
        root.bringComponentToTop(galleryButton);
        Global.getLogger(ShipGalleryCoreScript.class).info(
                "Hall of Triumph: attached Ship Gallery tab at x="
                        + galleryButton.getPosition().getX() + " to "
                        + root.getClass().getName());
    }

    private ButtonAPI createTabButton(float width, float height) {
        CustomPanelAPI holder = Global.getSettings().createCustom(width, height, null);
        TooltipMakerAPI element = holder.createUIElement(width, height, false);
        ButtonAPI button = element.addButton(TAB_NAME, TAB_NAME,
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                Alignment.MID, CutStyle.TOP, width, height, 0f);
        holder.addUIElement(element).inTL(0f, 0f);
        return button;
    }

    private void repositionAfterRightmostTab() {
        if (root == null || galleryButton == null) return;
        ButtonAPI colonies = findButton("colonies");
        if (colonies == null) return;
        float baseline = colonies.getPosition().getX();
        float right = baseline;
        float targetY = colonies.getPosition().getY();
        float targetHeight = colonies.getPosition().getHeight();
        float screenWidth = Global.getSettings().getScreenWidth();
        for (UIComponentAPI component : UiReflection.children(root)) {
            if (!(component instanceof ButtonAPI) || component == galleryButton) continue;
            ButtonAPI button = (ButtonAPI) component;
            if (Math.abs(button.getPosition().getY() - targetY) > 4f) continue;
            if (Math.abs(button.getPosition().getHeight() - targetHeight) > 4f) continue;
            if (button.getText() == null || button.getText().trim().isEmpty()) continue;
            if (button.getPosition().getX() < baseline
                    || button.getPosition().getX() >= screenWidth) continue;
            right = Math.max(right,
                    button.getPosition().getX() + button.getPosition().getWidth());
        }
        float localRight = Math.max(0f, right - baseline + 1f);
        float maxLocalX = Math.max(0f,
                screenWidth - baseline - galleryButton.getPosition().getWidth());
        galleryButton.getPosition().inTL(Math.min(localRight, maxLocalX), 0f);
    }

    private ButtonAPI findButton(String text) {
        if (root == null) return null;
        String wanted = text.toLowerCase();
        for (UIComponentAPI component : UiReflection.children(root)) {
            if (component instanceof ButtonAPI) {
                ButtonAPI button = (ButtonAPI) component;
                if (button.getText() != null
                        && button.getText().trim().toLowerCase().contains(wanted)) return button;
            }
        }
        return null;
    }

    private void handleGalleryTab() {
        if (foreignTabRequested) {
            foreignTabRequested = false;
            galleryActive = false;
            removeGalleryPanel();
            galleryButton.unhighlight();
            return;
        }

        if (galleryButton.isChecked()) {
            galleryButton.setChecked(false);
            galleryActive = true;
            ensureGalleryPanel();
        }

        for (UIComponentAPI component : UiReflection.children(root)) {
            if (!(component instanceof ButtonAPI) || component == galleryButton) continue;
            ButtonAPI button = (ButtonAPI) component;
            if (button.isChecked()) {
                galleryActive = false;
                removeGalleryPanel();
                galleryButton.unhighlight();
                return;
            }
        }

        if (!galleryActive || galleryPanel == null) return;
        removeContentPanelsExceptGallery();
        if (!UiReflection.children(root).contains(galleryPanel)) {
            root.addComponent(galleryPanel);
        }
        for (UIComponentAPI component : UiReflection.children(root)) {
            if (component instanceof ButtonAPI && component != galleryButton) {
                ((ButtonAPI) component).unhighlight();
            }
        }
        galleryButton.highlight();
    }

    private void removeContentPanelsExceptGallery() {
        for (UIComponentAPI component : new ArrayList<UIComponentAPI>(
                UiReflection.children(root))) {
            if (!(component instanceof ButtonAPI) && component != galleryPanel) {
                root.removeComponent(component);
            }
        }
    }

    private void removeGalleryPanel() {
        if (root != null && galleryPanel != null
                && UiReflection.children(root).contains(galleryPanel)) {
            root.removeComponent(galleryPanel);
        }
    }

    private void ensureGalleryPanel() {
        if (galleryPanel != null) return;
        try {
            ShipGalleryPanelPlugin plugin = new ShipGalleryPanelPlugin(
                    contentWidth, contentHeight, this);
            galleryPanel = Global.getSettings().createCustom(contentWidth, contentHeight, plugin);
            plugin.init(galleryPanel);
        } catch (Throwable ex) {
            Global.getLogger(ShipGalleryCoreScript.class).error(
                    "Hall of Triumph: unable to build the Ship Gallery panel", ex);
            galleryPanel = null;
            try {
                galleryPanel = buildFailurePanel();
            } catch (Throwable fallbackEx) {
                Global.getLogger(ShipGalleryCoreScript.class).error(
                        "Hall of Triumph: unable to build the Ship Gallery error panel", fallbackEx);
                galleryActive = false;
            }
        }
    }

    private CustomPanelAPI buildFailurePanel() {
        CustomPanelAPI failure = Global.getSettings().createCustom(
                Math.max(700f, contentWidth), Math.max(500f, contentHeight), null);
        TooltipMakerAPI message = failure.createUIElement(
                Math.max(640f, contentWidth - 60f), 130f, false);
        message.addTitle("Ship Gallery", Misc.getBasePlayerColor());
        message.addPara("The gallery could not be constructed. The full error has been written "
                + "to starsector.log.", 8f, Misc.getNegativeHighlightColor(), "starsector.log");
        failure.addUIElement(message).inTL(30f, 30f);
        return failure;
    }

    private void logNotReady(String reason) {
        if (loggedNotReady) return;
        loggedNotReady = true;
        Global.getLogger(ShipGalleryCoreScript.class).info(
                "Hall of Triumph: waiting to attach Ship Gallery because " + reason);
    }

    @Override
    public boolean isForeignTabAt(float x, float y) {
        if (root == null || galleryButton == null) return false;
        ButtonAPI colonies = findButton("colonies");
        if (colonies == null) return false;
        float targetY = colonies.getPosition().getY();
        float targetHeight = colonies.getPosition().getHeight();
        for (UIComponentAPI component : UiReflection.children(root)) {
            if (!(component instanceof ButtonAPI) || component == galleryButton) continue;
            ButtonAPI button = (ButtonAPI) component;
            if (Math.abs(button.getPosition().getY() - targetY) > 4f) continue;
            if (Math.abs(button.getPosition().getHeight() - targetHeight) > 4f) continue;
            if (button.getText() == null || button.getText().trim().isEmpty()) continue;
            float left = button.getPosition().getX();
            float bottom = button.getPosition().getY();
            if (x >= left && x <= left + button.getPosition().getWidth()
                    && y >= bottom && y <= bottom + button.getPosition().getHeight()) return true;
        }
        return false;
    }

    @Override
    public void foreignTabPressed() {
        foreignTabRequested = true;
    }

    private void clearAttachedState() {
        if (root != null) {
            if (galleryPanel != null && UiReflection.children(root).contains(galleryPanel)) {
                root.removeComponent(galleryPanel);
            }
            if (galleryButton != null && UiReflection.children(root).contains(galleryButton)) {
                root.removeComponent(galleryButton);
            }
        }
        galleryButton = null;
        galleryPanel = null;
        galleryActive = false;
        foreignTabRequested = false;
        contentWidth = 0f;
        contentHeight = 0f;
    }

    private void reset() {
        clearAttachedState();
        root = null;
        loggedFailure = false;
        loggedNotReady = false;
    }
}
