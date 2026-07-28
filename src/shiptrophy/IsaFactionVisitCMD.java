package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc.Token;

/** One-time faction station vignettes shown after Isa joins the fleet. */
public class IsaFactionVisitCMD implements CommandPlugin {
    private static final String CURRENT_FACTION = "$shipTrophyIsaFactionVisitId";
    private static final String KOL_MOD_ID = "knights_of_ludd";
    private static final String KOL_FACTION_ID = "knights_of_selkie";
    private static final String IRON_SHELL_MOD_ID = "timid_xiv";
    private static final String IRON_SHELL_FACTION_ID = "ironshell";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;

        String command = value(params, 0, memoryMap);
        String requestedFaction = canonicalFaction(value(params, 1, memoryMap));

        if ("shouldShow".equals(command)) {
            return shouldShow(dialog, requestedFaction);
        }

        MemoryAPI local = memoryMap == null ? null : memoryMap.get(MemKeys.LOCAL);
        if (local == null) return false;

        if ("prepare".equals(command)) {
            if (!shouldShow(dialog, requestedFaction)) return false;
            local.set(CURRENT_FACTION, requestedFaction, 0f);
            showIsa(dialog);
            return true;
        }
        if ("current".equals(command)) {
            return requestedFaction.equals(local.getString(CURRENT_FACTION));
        }
        if ("markSeen".equals(command)) {
            String factionId = canonicalFaction(local.getString(CURRENT_FACTION));
            if (isSupportedFaction(factionId)) {
                IsaTrophyManager.setFactionVisitSceneShown(factionId);
            }
            return true;
        }
        return false;
    }

    private static boolean shouldShow(InteractionDialogAPI dialog, String requestedFaction) {
        if (!IsaTrophyManager.wasOfficerGranted() || !isSupportedFaction(requestedFaction)) return false;

        SectorEntityToken target = dialog.getInteractionTarget();
        if (target == null || !target.hasTag(Tags.STATION)) return false;

        MarketAPI market = target.getMarket();
        if (market == null || market.isPlayerOwned()) return false;

        String actualFaction = canonicalFaction(market.getFactionId());
        return requestedFaction.equals(actualFaction)
                && !IsaTrophyManager.wasFactionVisitSceneShown(actualFaction);
    }

    private static void showIsa(InteractionDialogAPI dialog) {
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(home);
        if (isa != null) dialog.getVisualPanel().showPersonInfo(isa);
    }

    private static String canonicalFaction(String factionId) {
        return factionId == null ? "" : factionId;
    }

    private static boolean isSupportedFaction(String factionId) {
        if (Factions.HEGEMONY.equals(factionId)
                || Factions.PERSEAN.equals(factionId)
                || Factions.TRITACHYON.equals(factionId)
                || Factions.DIKTAT.equals(factionId)
                || Factions.LUDDIC_CHURCH.equals(factionId)
                || Factions.LUDDIC_PATH.equals(factionId)
                || Factions.PIRATES.equals(factionId)
                || Factions.INDEPENDENT.equals(factionId)) {
            return true;
        }
        if (KOL_FACTION_ID.equals(factionId)) {
            return isOptionalFactionAvailable(KOL_MOD_ID, KOL_FACTION_ID);
        }
        if (IRON_SHELL_FACTION_ID.equals(factionId)) {
            return isOptionalFactionAvailable(IRON_SHELL_MOD_ID, IRON_SHELL_FACTION_ID);
        }
        return false;
    }

    private static boolean isOptionalFactionAvailable(String modId, String factionId) {
        try {
            return Global.getSettings() != null
                    && Global.getSettings().getModManager() != null
                    && Global.getSettings().getModManager().isModEnabled(modId)
                    && Global.getSector() != null
                    && Global.getSector().getFaction(factionId) != null;
        } catch (RuntimeException ignored) {
            return false;
        }
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
