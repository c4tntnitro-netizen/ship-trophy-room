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
public final class ShipGalleryCoreScript implements EveryFrameScript {
    private static final String TAB_NAME = "Ship Gallery";

    private transient UIPanelAPI root;
    private transient ButtonAPI galleryButton;
    private transient CustomPanelAPI galleryPanel;
    private transient boolean galleryActive;
    private transient boolean loggedFailure;

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
            reset();
            return;
        }

        try {
            UIPanelAPI activeRoot = UiReflection.getCurrentCorePanel();
            if (activeRoot == null) return;
            if (activeRoot != root || galleryButton == null
                    || !UiReflection.children(activeRoot).contains(galleryButton)) {
                reset();
                root = activeRoot;
                inject();
            }
            if (galleryButton == null) return;
            repositionAfterRightmostTab();
            handleGalleryTab();
        } catch (Throwable ex) {
            if (!loggedFailure) {
                loggedFailure = true;
                System.err.println("Hall of Triumph: unable to attach Ship Gallery "
                        + "to the Command screen: " + ex.getMessage());
            }
        }
    }

    private void inject() {
        ButtonAPI income = findButton("income");
        ButtonAPI colonies = findButton("colonies");
        if (income == null || colonies == null) return;

        Object rawMap = UiReflection.invoke(root, "getButtonToTab");
        Object incomePanel = rawMap instanceof Map<?, ?>
                ? ((Map<?, ?>) rawMap).get(income) : null;
        float contentWidth = Math.max(700f,
                Global.getSettings().getScreenWidth() - colonies.getPosition().getX());
        float contentHeight = Math.max(500f, incomePanel instanceof UIComponentAPI
                ? ((UIComponentAPI) incomePanel).getPosition().getHeight()
                : root.getPosition().getHeight() - income.getPosition().getHeight());

        ShipGalleryPanelPlugin plugin = new ShipGalleryPanelPlugin(contentWidth, contentHeight);
        galleryPanel = Global.getSettings().createCustom(contentWidth, contentHeight, plugin);
        plugin.init(galleryPanel);
        galleryButton = createTabButton(140f, income.getPosition().getHeight());
        root.addComponent(galleryButton);
        repositionAfterRightmostTab();
        root.bringComponentToTop(galleryButton);
        System.out.println("Hall of Triumph: attached independent Ship Gallery "
                + "tab to the Command screen");
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
        for (UIComponentAPI component : UiReflection.children(root)) {
            if (!(component instanceof ButtonAPI) || component == galleryButton) continue;
            ButtonAPI button = (ButtonAPI) component;
            if (Math.abs(button.getPosition().getY() - targetY) > 4f) continue;
            right = Math.max(right,
                    button.getPosition().getX() + button.getPosition().getWidth());
        }
        galleryButton.getPosition().inTL(Math.max(0f, right - baseline + 1f), 0f);
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
        if (galleryButton.isChecked()) {
            galleryButton.setChecked(false);
            galleryActive = true;
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

        if (!galleryActive) return;
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

    private void reset() {
        root = null;
        galleryButton = null;
        galleryPanel = null;
        galleryActive = false;
        loggedFailure = false;
    }
}
