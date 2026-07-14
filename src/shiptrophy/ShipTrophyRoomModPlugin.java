package shiptrophy;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

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
        Global.getSector().removeScriptsOfClass(StoryPointGeneratorScript.class);
        Global.getSector().addScript(new StoryPointGeneratorScript());
        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        TrophyNetwork.syncDmodMarkers(stats);
        TrophyNetwork.syncUniqueDiscountMarkers(stats);
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);
    }
}
