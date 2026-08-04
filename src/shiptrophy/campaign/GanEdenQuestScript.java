package shiptrophy.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;

/** Low-frequency quest state checks; no per-frame world scanning. */
public final class GanEdenQuestScript implements EveryFrameScript {
    private float interval;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null) return;
        interval += amount;
        if (interval < 1f) return;
        interval = 0f;

        GanEdenQuestManager.ensureForCurrentSave();
        GanEdenQuestManager.checkHypershunts();
        GanEdenQuestManager.completeIfReady();
        tryShowArrivalScene();
    }

    private void tryShowArrivalScene() {
        if (!GanEdenQuestManager.shouldShowArrivalScene()
                || Global.getSector().getPlayerFleet() == null) {
            return;
        }
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui == null
                || ui.isShowingDialog()
                || ui.isShowingMenu()
                || ui.getCurrentCoreTab() != null) {
            return;
        }
        if (ui.showInteractionDialog(
                new GanEdenArrivalDialogPlugin(),
                Global.getSector().getPlayerFleet())) {
            GanEdenQuestManager.markArrivalSceneShown();
        }
    }
}
