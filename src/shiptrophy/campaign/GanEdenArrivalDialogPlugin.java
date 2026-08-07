package shiptrophy.campaign;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;

import shiptrophy.HallOfTriumphCompletionDialogPlugin;

/** One-time cinematic played after Isa first enters Gan Eden. */
public final class GanEdenArrivalDialogPlugin
        implements InteractionDialogPlugin {
    private static final String DIALOGUE_TRIGGER =
            "ShipTrophyGanEdenArrivalScene";
    private static final String PAGE_TWO_TRIGGER =
            "ShipTrophyGanEdenArrivalSceneTwo";
    private static final String PAGE_THREE_TRIGGER =
            "ShipTrophyGanEdenArrivalSceneThree";
    private static final String PAGE_FOUR_TRIGGER =
            "ShipTrophyGanEdenArrivalSceneFour";
    private static final String PAGE_TWO =
            "ship_trophy_gan_eden_arrival_page_two";
    private static final String PAGE_THREE =
            "ship_trophy_gan_eden_arrival_page_three";
    private static final String PAGE_FOUR =
            "ship_trophy_gan_eden_arrival_page_four";
    private static final String FINISH =
            "ship_trophy_gan_eden_arrival_continue";
    private final Map<String, MemoryAPI> memoryMap =
            new HashMap<String, MemoryAPI>();
    private InteractionDialogAPI dialog;

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        HallOfTriumphCompletionDialogPlugin.showLetterboxedIllustration(
                dialog, "ship_trophy_gan_eden_eden_prime");
        prepareMemoryMap();

        if (!FireBest.fire(null, dialog, memoryMap, DIALOGUE_TRIGGER)) {
            dialog.dismiss();
        }
    }

    private void prepareMemoryMap() {
        MemoryAPI global = Global.getSector().getMemoryWithoutUpdate();
        memoryMap.put(MemKeys.GLOBAL, global);
        memoryMap.put(MemKeys.LOCAL, global);
        if (Global.getSector().getPlayerFleet() != null) {
            memoryMap.put(MemKeys.PLAYER,
                    Global.getSector().getPlayerFleet().getMemoryWithoutUpdate());
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (dialog == null) return;
        if (PAGE_TWO.equals(optionData)) {
            showPage(PAGE_TWO_TRIGGER);
        } else if (PAGE_THREE.equals(optionData)) {
            showPage(PAGE_THREE_TRIGGER);
        } else if (PAGE_FOUR.equals(optionData)) {
            showPage(PAGE_FOUR_TRIGGER);
        } else if (FINISH.equals(optionData)) {
            dialog.dismiss();
        }
    }

    private void showPage(String trigger) {
        dialog.getOptionPanel().clearOptions();
        if (!FireBest.fire(null, dialog, memoryMap, trigger)) {
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
