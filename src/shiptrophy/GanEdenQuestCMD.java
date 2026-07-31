package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

import shiptrophy.campaign.GanEdenGenerator;
import shiptrophy.campaign.GanEdenQuestManager;
import shiptrophy.campaign.GanEdenQuestManager.Stage;

/** Rule-command bridge for the Gan Eden quest scenes and transit rings. */
public final class GanEdenQuestCMD implements CommandPlugin {
    @Override
    public boolean execute(
            String ruleId,
            InteractionDialogAPI dialog,
            List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;
        String command = value(params, 0, memoryMap);

        if ("canContactGargoyle".equals(command)) {
            return GanEdenQuestManager.getStage() == Stage.CONTACT_GARGOYLE
                    && IsaHomecomingCMD.isShatteredRing(
                            dialog.getInteractionTarget(),
                            dialog.getInteractionTarget() == null ? null
                                    : dialog.getInteractionTarget().getMarket());
        }
        if ("prepareGargoyle".equals(command)) {
            prepareGargoyle(dialog);
            return true;
        }
        if ("finishGargoyle".equals(command)) {
            GanEdenQuestManager.finishGargoyleInvestigation(
                    dialog.getTextPanel());
            return true;
        }
        if ("shouldShowGrave".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.isAtLeast(Stage.GAN_EDEN_REVEALED)
                    && !GanEdenQuestManager.isGraveFound()
                    && IsaTrophyManager.isIsaOfficerInPlayerFleet();
        }
        if ("graveNeedsIsa".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.isAtLeast(Stage.GAN_EDEN_REVEALED)
                    && !GanEdenQuestManager.isGraveFound()
                    && !IsaTrophyManager.isIsaOfficerInPlayerFleet();
        }
        if ("graveInspected".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.isGraveFound();
        }
        if ("prepareGrave".equals(command)) {
            showIsa(dialog);
            return true;
        }
        if ("markGrave".equals(command)) {
            GanEdenQuestManager.markGraveFound(dialog.getTextPanel());
            return true;
        }
        if ("isExternalRing".equals(command)) {
            return isTarget(dialog, GanEdenQuestManager.EXTERNAL_RING_ID)
                    && GanEdenQuestManager.isAtLeast(
                            Stage.GAN_EDEN_REVEALED);
        }
        if ("isInternalRing".equals(command)) {
            return isTarget(dialog, GanEdenGenerator.ARRIVAL_RING_ID)
                    && GanEdenQuestManager.isAtLeast(
                            Stage.GAN_EDEN_REVEALED);
        }
        if ("transitIn".equals(command)) {
            GanEdenQuestManager.transitIntoGanEden(
                    dialog.getInteractionTarget());
            return true;
        }
        if ("transitOut".equals(command)) {
            GanEdenQuestManager.transitOutOfGanEden(
                    dialog.getInteractionTarget());
            return true;
        }
        return false;
    }

    private static void prepareGargoyle(InteractionDialogAPI dialog) {
        showIsa(dialog);
        PersonAPI gargoyle = Global.getSector().getImportantPeople()
                .getPerson("gargoyle");
        if (gargoyle == null) {
            gargoyle = Global.getFactory().createPerson();
            gargoyle.setName(new FullName(
                    "Gargoyle", "", FullName.Gender.ANY));
            gargoyle.setPortraitSprite(
                    "graphics/portraits/characters/gargoyle.png");
        }
        dialog.getVisualPanel().showSecondPerson(gargoyle);
    }

    private static void showIsa(InteractionDialogAPI dialog) {
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(
                IsaTrophyManager.findHomeMarket());
        if (isa != null) dialog.getVisualPanel().showPersonInfo(isa);
    }

    private static boolean isSpaceElevator(SectorEntityToken target) {
        return target != null
                && GanEdenGenerator.SPACE_ELEVATOR_ID.equals(target.getId());
    }

    private static boolean isTarget(
            InteractionDialogAPI dialog, String id) {
        return dialog.getInteractionTarget() != null
                && id.equals(dialog.getInteractionTarget().getId());
    }

    private static String value(
            List<Token> params,
            int index,
            Map<String, MemoryAPI> memoryMap) {
        if (index < 0 || index >= params.size()) return "";
        String result = params.get(index).getString(memoryMap);
        return result == null ? "" : result;
    }

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public int getOptionOrder(
            List<Token> params, Map<String, MemoryAPI> memoryMap) {
        return 0;
    }
}
