package shiptrophy.gallery;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomDialogDelegate.CustomDialogCallback;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

/** Opens the Ship Gallery through Starsector's supported custom-dialog API. */
public final class ShipGalleryDialog {
    private static final float MAX_WIDTH = 1400f;
    private static final float MAX_HEIGHT = 820f;
    private static final float SCREEN_MARGIN_X = 180f;
    private static final float SCREEN_MARGIN_Y = 180f;

    private ShipGalleryDialog() {
    }

    public static void show(InteractionDialogAPI dialog) {
        if (dialog == null) return;
        float width = Math.max(700f, Math.min(MAX_WIDTH,
                Global.getSettings().getScreenWidth() - SCREEN_MARGIN_X));
        float height = Math.max(500f, Math.min(MAX_HEIGHT,
                Global.getSettings().getScreenHeight() - SCREEN_MARGIN_Y));
        dialog.showCustomDialog(width, height, new GalleryDelegate(width, height));
    }

    private static final class GalleryDelegate extends BaseCustomDialogDelegate {
        private final ShipGalleryPanelPlugin plugin;

        private GalleryDelegate(float width, float height) {
            plugin = new ShipGalleryPanelPlugin(width, height);
        }

        @Override
        public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
            plugin.init(panel);
        }

        @Override
        public String getConfirmText() {
            return "Return to Isa";
        }

        @Override
        public CustomUIPanelPlugin getCustomPanelPlugin() {
            return plugin;
        }
    }
}
