package shiptrophy;

import java.util.ArrayList;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener;

import shiptrophy.campaign.GanEdenAmbushScript;
import shiptrophy.campaign.GanEdenBattleCreationPlugin;
import shiptrophy.campaign.GanEdenGenerator;
import shiptrophy.campaign.GanEdenOrdoListener;
import shiptrophy.campaign.GanEdenQuestManager;
import shiptrophy.campaign.GanEdenQuestScript;
import shiptrophy.campaign.ShatteredRingGenerator;
import shiptrophy.hullmods.ConfigurableTrophyHullMod;

public class ShipTrophyRoomModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        ensureScript();
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        ensureScript();
    }

    private void ensureScript() {
        if (Global.getSector() == null) return;
        Global.getSector().unregisterPlugin(GanEdenBattleCreationPlugin.ID);
        Global.getSector().registerPlugin(new GanEdenBattleCreationPlugin());
        TrophySubtypeRegistry.reload();
        ConfigurableTrophyHullMod.reload();
        ShatteredRingGenerator.ensureGenerated();
        GanEdenGenerator.ensureGenerated();
        GanEdenQuestManager.ensureForCurrentSave();
        Global.getSector().removeScriptsOfClass(GanEdenQuestScript.class);
        Global.getSector().addScript(new GanEdenQuestScript());
        Global.getSector().removeScriptsOfClass(GanEdenAmbushScript.class);
        GanEdenAmbushScript.ensureFleet();
        Global.getSector().addScript(new GanEdenAmbushScript());
        for (CampaignEventListener listener : new ArrayList<CampaignEventListener>(
                Global.getSector().getAllListeners())) {
            if (listener instanceof GanEdenOrdoListener) {
                Global.getSector().removeListener(listener);
            }
        }
        Global.getSector().addTransientListener(new GanEdenOrdoListener());
        Global.getSector().removeScriptsOfClass(StoryPointGeneratorScript.class);
        Global.getSector().addScript(new StoryPointGeneratorScript());
        Global.getSector().removeScriptsOfClass(IsaTrophyScript.class);
        Global.getSector().addScript(new IsaTrophyScript());
        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        TrophyNetwork.syncDmodMarkers(stats);
        TrophyNetwork.syncUniqueDiscountMarkers(stats);
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);
        if (IsaTrophyManager.isIntroduced()) {
            IsaTrophyManager.ensureContact(IsaTrophyManager.findHomeMarket(), null);
        }
        IsaTrophyManager.refreshIsaHullmod();
        IsaTrophyManager.refreshIsaOfficerSkills();
    }
}
