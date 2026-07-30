package shiptrophy;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import shiptrophy.campaign.GanEdenAmbushScript;
import shiptrophy.campaign.GanEdenGenerator;
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
        TrophySubtypeRegistry.reload();
        ConfigurableTrophyHullMod.reload();
        ShatteredRingGenerator.ensureGenerated();
        GanEdenGenerator.ensureGenerated();
        Global.getSector().removeScriptsOfClass(GanEdenAmbushScript.class);
        Global.getSector().addScript(new GanEdenAmbushScript());
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
