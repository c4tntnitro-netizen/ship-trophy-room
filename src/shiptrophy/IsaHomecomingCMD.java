package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc.Token;

import shiptrophy.campaign.ShatteredRingGenerator;
import shiptrophy.campaign.GanEdenQuestManager;

/** One-time scene when officer Isa is brought home to the Shattered Ring. */
public class IsaHomecomingCMD implements CommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;

        String command = value(params, 0, memoryMap);
        if ("shouldShow".equals(command)) {
            return shouldShow(dialog);
        }
        if ("prepare".equals(command)) {
            if (!shouldShow(dialog)) return false;
            showIsa(dialog);
            return true;
        }
        if ("markSeen".equals(command)) {
            IsaTrophyManager.setShatteredRingHomecomingShown();
            GanEdenQuestManager.start(dialog.getTextPanel());
            return true;
        }
        return false;
    }

    private static boolean shouldShow(InteractionDialogAPI dialog) {
        if (!IsaTrophyManager.wasOfficerGranted()
                || !IsaTrophyManager.isIsaOfficerInPlayerFleet()
                || !GanEdenQuestManager.isAtTheGatesCompleted()
                || IsaTrophyManager.wasShatteredRingHomecomingShown()) {
            return false;
        }

        SectorEntityToken target = dialog.getInteractionTarget();
        if (target == null || !target.hasTag(Tags.STATION)) return false;
        MarketAPI market = target.getMarket();
        return isShatteredRing(target, market);
    }

    static boolean isShatteredRing(SectorEntityToken target, MarketAPI market) {
        return target != null
                && (ShatteredRingGenerator.ENTITY_ID.equals(target.getId())
                        || (market != null && ShatteredRingGenerator.MARKET_ID.equals(market.getId())));
    }

    private static void showIsa(InteractionDialogAPI dialog) {
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(IsaTrophyManager.findHomeMarket());
        if (isa != null) dialog.getVisualPanel().showPersonInfo(isa);
    }

    private static String value(List<Token> params, int index, Map<String, MemoryAPI> memoryMap) {
        if (index < 0 || index >= params.size()) return "";
        String result = params.get(index).getString(memoryMap);
        return result == null ? "" : result;
    }

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public int getOptionOrder(List<Token> params, Map<String, MemoryAPI> memoryMap) {
        return 0;
    }
}
