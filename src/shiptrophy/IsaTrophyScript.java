package shiptrophy;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class IsaTrophyScript implements EveryFrameScript {
    private final IntervalUtil interval = new IntervalUtil(0.5f, 1f);
    private final IntervalUtil completionCheck = new IntervalUtil(0.5f, 1f);

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

        completionCheck.advance(amount);
        if (completionCheck.intervalElapsed()) {
            tryShowHallCompletionScene();
        }

        interval.advance(Global.getSector().getClock().convertToDays(amount));
        if (!interval.intervalElapsed()) return;

        IsaQualityOfLifeBonuses.advance(interval.getIntervalDuration());
        IsaFactionVisitCMD.applyPersistentBonuses();
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        if (home == null) return;

        IsaTrophyManager.ensureBarEventCreator();
        if (IsaTrophyManager.isIntroduced()) {
            IsaTrophyManager.ensureContact(home, null);
        }
        IsaTrophyManager.refreshIsaOfficerSkills();
        IsaTrophyManager.refreshIsaHullmod();
    }

    private void tryShowHallCompletionScene() {
        if (!IsaTrophyManager.wasOfficerGranted()
                || IsaTrophyManager.wasFactionCompletionSceneValidated()) return;

        boolean allVanillaComplete = IsaTrophyManager.areAllVanillaProgramsComplete(
                TrophyNetwork.computeNetworkStats());
        if (!allVanillaComplete) {
            if (IsaTrophyManager.wasFactionCompletionSceneShown()) {
                IsaTrophyManager.markFactionCompletionSceneForReplay();
            }
            return;
        }

        // Migrate saves where the original completion-gated scene played correctly.
        if (IsaTrophyManager.wasFactionCompletionSceneShown()
                && !IsaTrophyManager.doesFactionCompletionSceneNeedReplay()) {
            IsaTrophyManager.setFactionCompletionSceneValidated();
            return;
        }

        MarketAPI home = IsaTrophyManager.findHomeMarket();
        if (home == null || Global.getSector().getPlayerFleet() == null) return;

        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui == null || ui.isShowingDialog() || ui.isShowingMenu() || ui.getCurrentCoreTab() != null) return;

        boolean shown = ui.showInteractionDialog(
                new HallOfTriumphCompletionDialogPlugin(home), Global.getSector().getPlayerFleet());
        if (shown) {
            IsaTrophyManager.setFactionCompletionSceneShown();
        }
    }
}
