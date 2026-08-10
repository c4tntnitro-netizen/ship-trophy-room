package shiptrophy.gallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String AOTD_TRACKER = "data.kaysaar.aotd.vok.scripts.CoreUITracker";
    private static final String AOTD_TAB_MEMORY = "$aotd_outpost_state";

    private transient UIPanelAPI root;
    private transient ButtonAPI galleryButton;
    private transient CustomPanelAPI galleryPanel;
    private transient ButtonAPI currentButton;
    private transient Map<ButtonAPI, Object> panelMap;
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
            if (galleryButton == null || panelMap == null) return;
            repositionAfterRightmostTab();

            Object aotd = findAotdTracker();
            if (aotd != null && linkIntoAotd(aotd)) return;
            handleTabsWithoutAotd();
        } catch (Throwable ex) {
            if (!loggedFailure) {
                loggedFailure = true;
                System.err.println("Hall of Triumph: unable to attach Ship Gallery "
                        + "to the Command screen: " + ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void inject() {
        ButtonAPI income = findButton("income");
        ButtonAPI colonies = findButton("colonies");
        if (income == null || colonies == null) return;

        Object rawMap = UiReflection.invoke(root, "getButtonToTab");
        if (!(rawMap instanceof Map<?, ?>)) return;
        panelMap = (Map<ButtonAPI, Object>) rawMap;

        Object incomePanel = panelMap.get(income);
        if (!(incomePanel instanceof UIComponentAPI)) return;
        float contentWidth = Math.max(700f,
                Global.getSettings().getScreenWidth() - colonies.getPosition().getX());
        float contentHeight = Math.max(500f,
                ((UIComponentAPI) incomePanel).getPosition().getHeight());

        ShipGalleryPanelPlugin plugin = new ShipGalleryPanelPlugin(contentWidth, contentHeight);
        galleryPanel = Global.getSettings().createCustom(contentWidth, contentHeight, plugin);
        plugin.init(galleryPanel);
        galleryButton = createTabButton(150f, income.getPosition().getHeight());
        root.addComponent(galleryButton);
        repositionAfterRightmostTab();
        root.bringComponentToTop(galleryButton);
        panelMap.put(galleryButton, galleryPanel);

        currentButton = findHighlightedButton();
        if (currentButton == null) currentButton = income;
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
        ButtonAPI income = findButton("income");
        if (income == null) return;
        float baseline = income.getPosition().getX();
        float right = baseline;
        float targetY = income.getPosition().getY();
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

    private ButtonAPI findHighlightedButton() {
        if (panelMap == null) return null;
        for (ButtonAPI button : new ArrayList<ButtonAPI>(panelMap.keySet())) {
            if (button != null && button.isHighlighted()) return button;
        }
        return null;
    }

    private Object findAotdTracker() {
        if (Global.getSector() == null) return null;
        List<EveryFrameScript> scripts = new ArrayList<EveryFrameScript>();
        scripts.addAll(Global.getSector().getTransientScripts());
        scripts.addAll(Global.getSector().getScripts());
        for (EveryFrameScript script : scripts) {
            if (script != null && AOTD_TRACKER.equals(script.getClass().getName())) return script;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean linkIntoAotd(Object tracker) {
        Object rawMap = UiReflection.getField(tracker, "panelMap");
        if (!(rawMap instanceof HashMap<?, ?>)) return false;
        Map<ButtonAPI, Object> aotdMap = (Map<ButtonAPI, Object>) rawMap;
        aotdMap.put(galleryButton, galleryPanel);
        return true;
    }

    private void handleTabsWithoutAotd() {
        if (currentButton == null) currentButton = findHighlightedButton();
        for (ButtonAPI button : new ArrayList<ButtonAPI>(panelMap.keySet())) {
            if (button == null || !button.isChecked()) continue;
            button.setChecked(false);
            if (button == currentButton) continue;
            Object oldPanel = currentButton == null ? null : panelMap.get(currentButton);
            if (oldPanel instanceof UIComponentAPI
                    && UiReflection.children(root).contains(oldPanel)) {
                root.removeComponent((UIComponentAPI) oldPanel);
            }
            currentButton = button;
            Object newPanel = panelMap.get(currentButton);
            if (newPanel instanceof UIComponentAPI
                    && !UiReflection.children(root).contains(newPanel)) {
                root.addComponent((UIComponentAPI) newPanel);
            }
        }
        if (currentButton == null) return;
        for (ButtonAPI button : new ArrayList<ButtonAPI>(panelMap.keySet())) {
            if (button == currentButton) button.highlight();
            else button.unhighlight();
        }
    }

    private void reset() {
        resetAotdReopenState();
        root = null;
        galleryButton = null;
        galleryPanel = null;
        currentButton = null;
        panelMap = null;
        loggedFailure = false;
    }

    private void resetAotdReopenState() {
        if (Global.getSector() == null) return;
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(AOTD_TAB_MEMORY)) {
                String value = Global.getSector().getMemoryWithoutUpdate()
                        .getString(AOTD_TAB_MEMORY);
                if (value != null && value.toLowerCase().contains("ship gallery")) {
                    Global.getSector().getMemoryWithoutUpdate()
                            .set(AOTD_TAB_MEMORY, "income");
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
