package shiptrophy;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.PositionAPI;

public class HallOfTriumphCompletionDialogPlugin implements InteractionDialogPlugin {
    private static final String ILLUSTRATION_CATEGORY = "illustrations";
    private static final String ILLUSTRATION_ID = "ship_trophy_hall_complete";
    private static final String LETTERBOX_ID = "ship_trophy_letterbox_black";
    private static final String DIALOGUE_TRIGGER = "ShipTrophyIsaHallCompletion";
    private static final String HOME_NAME = "$shipTrophyIsaCompletionHomeName";
    private static final String LEAVE = "ship_trophy_hall_complete_leave";
    private static final float ILLUSTRATION_WIDTH = 480f;
    private static final float ILLUSTRATION_HEIGHT = 300f;

    private final MarketAPI home;
    private final Map<String, MemoryAPI> memoryMap = new HashMap<String, MemoryAPI>();
    private InteractionDialogAPI dialog;

    public HallOfTriumphCompletionDialogPlugin(MarketAPI home) {
        this.home = home;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        showIllustration();
        prepareMemoryMap();

        if (!FireBest.fire(null, dialog, memoryMap, DIALOGUE_TRIGGER)) {
            dialog.dismiss();
        }
    }

    private void prepareMemoryMap() {
        MemoryAPI global = Global.getSector().getMemoryWithoutUpdate();
        memoryMap.put(MemKeys.GLOBAL, global);
        memoryMap.put(MemKeys.LOCAL, global);
        if (home != null) memoryMap.put(MemKeys.MARKET, home.getMemoryWithoutUpdate());
        if (Global.getSector().getPlayerFleet() != null) {
            memoryMap.put(MemKeys.PLAYER, Global.getSector().getPlayerFleet().getMemoryWithoutUpdate());
        }
        global.set(HOME_NAME, home == null ? "your colony" : home.getName());
    }

    private void showIllustration() {
        showLetterboxedIllustration(dialog, ILLUSTRATION_ID);
    }

    /**
     * Reuses the Hall-completion cinematic framing for other story beats.
     */
    public static void showLetterboxedIllustration(
            InteractionDialogAPI dialog, String illustrationId) {
        if (dialog == null || illustrationId == null) return;
        SpriteAPI illustration = Global.getSettings().getSprite(
                ILLUSTRATION_CATEGORY, illustrationId);
        SpriteAPI background = Global.getSettings().getSprite(ILLUSTRATION_CATEGORY, LETTERBOX_ID);
        dialog.getVisualPanel().showCustomPanel(
                ILLUSTRATION_WIDTH, ILLUSTRATION_HEIGHT,
                new LetterboxIllustrationPlugin(illustration, background));
    }

    private static class LetterboxIllustrationPlugin extends BaseCustomUIPanelPlugin {
        private final SpriteAPI illustration;
        private final SpriteAPI background;
        private final float sourceWidth;
        private final float sourceHeight;
        private PositionAPI position;

        LetterboxIllustrationPlugin(SpriteAPI illustration, SpriteAPI background) {
            this.illustration = illustration;
            this.background = background;
            this.sourceWidth = illustration.getWidth();
            this.sourceHeight = illustration.getHeight();
        }

        @Override
        public void positionChanged(PositionAPI position) {
            this.position = position;
        }

        @Override
        public void renderBelow(float alphaMult) {
            if (position == null || sourceWidth <= 0f || sourceHeight <= 0f) return;

            float centerX = position.getX() + position.getWidth() * 0.5f;
            float centerY = position.getY() + position.getHeight() * 0.5f;
            renderSprite(background, centerX, centerY,
                    position.getWidth(), position.getHeight(), alphaMult);

            float scale = Math.min(position.getWidth() / sourceWidth, position.getHeight() / sourceHeight);
            renderSprite(illustration, centerX, centerY,
                    sourceWidth * scale, sourceHeight * scale, alphaMult);
        }

        private void renderSprite(SpriteAPI sprite, float centerX, float centerY,
                float width, float height, float alphaMult) {
            float previousWidth = sprite.getWidth();
            float previousHeight = sprite.getHeight();
            float previousAlpha = sprite.getAlphaMult();
            sprite.setSize(width, height);
            sprite.setAlphaMult(alphaMult);
            sprite.renderAtCenter(centerX, centerY);
            sprite.setSize(previousWidth, previousHeight);
            sprite.setAlphaMult(previousAlpha);
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (LEAVE.equals(optionData) && dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return memoryMap;
    }
}
