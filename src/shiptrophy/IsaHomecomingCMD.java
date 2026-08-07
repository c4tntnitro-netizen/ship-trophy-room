package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc.Token;

import shiptrophy.campaign.ShatteredRingGenerator;
import shiptrophy.campaign.GanEdenQuestManager;

/** One-time scene when officer Isa is brought home to the Shattered Ring. */
public class IsaHomecomingCMD implements CommandPlugin {
    private static final String PLAYER_RANK = "$playerRank";
    private static final String APPROACH_ILLUSTRATION =
            "ship_trophy_shattered_ring_approach";
    private static final String WORKSHOP_ILLUSTRATION =
            "ship_trophy_shattered_ring_workshop";

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
            setDialogueVariables(memoryMap);
            showApproachIllustrationAfterDialogOpens(dialog);
            return true;
        }
        if ("showIsa".equals(command)) {
            showIsa(dialog);
            return true;
        }
        if ("receiveSuit".equals(command)) {
            setSuitReceived(true);
            GanEdenQuestManager.start(dialog.getTextPanel());
            return true;
        }
        if ("shouldShowWorkshop".equals(command)) {
            return shouldShowWorkshop(dialog);
        }
        if ("prepareWorkshop".equals(command)) {
            if (!shouldShowWorkshop(dialog)) return false;
            setDialogueVariables(memoryMap);
            HallOfTriumphCompletionDialogPlugin.showLetterboxedIllustration(
                    dialog, WORKSHOP_ILLUSTRATION);
            return true;
        }
        if ("markSeen".equals(command)) {
            setSuitReceived(false);
            IsaTrophyManager.setShatteredRingHomecomingShown();
            GanEdenQuestManager.finishHomecoming(dialog.getTextPanel());
            return true;
        }
        return false;
    }

    private static boolean shouldShow(InteractionDialogAPI dialog) {
        if (!IsaTrophyManager.wasOfficerGranted()
                || !IsaTrophyManager.isIsaOfficerInPlayerFleet()
                || IsaTrophyManager.wasShatteredRingHomecomingShown()
                || isSuitReceived()) {
            return false;
        }

        SectorEntityToken target = dialog.getInteractionTarget();
        if (target == null || !target.hasTag(Tags.STATION)) return false;
        MarketAPI market = target.getMarket();
        return isShatteredRing(target, market);
    }

    private static boolean shouldShowWorkshop(InteractionDialogAPI dialog) {
        if (dialog == null
                || !IsaTrophyManager.wasOfficerGranted()
                || !IsaTrophyManager.isIsaOfficerInPlayerFleet()
                || IsaTrophyManager.wasShatteredRingHomecomingShown()
                || !isSuitReceived()) {
            return false;
        }
        SectorEntityToken target = dialog.getInteractionTarget();
        // PopulateOptions also fires while a comm-directory person is active.
        // Keep this station-level objective out of individual NPC menus such
        // as the Shattered Ring's D-mod vendor conversation.
        if (target != null && target.getActivePerson() != null) return false;
        MarketAPI market = target == null ? null : target.getMarket();
        return isShatteredRing(target, market);
    }

    private static boolean isSuitReceived() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                        ShipTrophyRoomIds
                                .MEMORY_ISA_SHATTERED_RING_SUIT_RECEIVED);
    }

    private static void setSuitReceived(boolean received) {
        if (Global.getSector() == null) return;
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        if (received) {
            memory.set(
                    ShipTrophyRoomIds
                            .MEMORY_ISA_SHATTERED_RING_SUIT_RECEIVED,
                    true);
        } else {
            memory.unset(
                    ShipTrophyRoomIds
                            .MEMORY_ISA_SHATTERED_RING_SUIT_RECEIVED);
        }
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

    /**
     * OpenInteractionDialog applies the target's normal visual after running
     * rule commands. Defer this custom panel by two paused frames so the wide
     * approach illustration wins that ordering race, just as it does once a
     * dialogue is already open.
     */
    private static void showApproachIllustrationAfterDialogOpens(
            InteractionDialogAPI dialog) {
        if (Global.getSector() == null || dialog == null) return;
        Global.getSector().addTransientScript(
                new DeferredApproachIllustration(dialog));
    }

    private static final class DeferredApproachIllustration
            implements EveryFrameScript {
        private final InteractionDialogAPI dialog;
        private int frames;
        private boolean done;

        private DeferredApproachIllustration(InteractionDialogAPI dialog) {
            this.dialog = dialog;
        }

        @Override
        public void advance(float amount) {
            if (done) return;
            frames++;
            if (frames < 2) return;

            InteractionDialogAPI current = Global.getSector() == null
                    || Global.getSector().getCampaignUI() == null
                    ? null
                    : Global.getSector().getCampaignUI()
                            .getCurrentInteractionDialog();
            if (current == dialog) {
                HallOfTriumphCompletionDialogPlugin
                        .showLetterboxedIllustration(
                                dialog, APPROACH_ILLUSTRATION);
            }
            done = true;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }
    }

    private static void setDialogueVariables(
            Map<String, MemoryAPI> memoryMap) {
        if (memoryMap == null) return;
        MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
        if (local == null) return;

        PersonAPI player = Global.getSector() == null
                ? null
                : Global.getSector().getPlayerPerson();
        String rank = player == null ? null : player.getRank();
        local.set(
                PLAYER_RANK,
                rank == null || rank.length() <= 0 ? "Captain" : rank,
                0f);
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
