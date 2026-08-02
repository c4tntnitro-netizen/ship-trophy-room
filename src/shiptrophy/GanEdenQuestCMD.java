package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

import shiptrophy.campaign.GanEdenGenerator;
import shiptrophy.campaign.GanEdenHypershuntManager;
import shiptrophy.campaign.GanEdenLogManager;
import shiptrophy.campaign.GanEdenLogSpec;
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

        if ("hasKnownHypershunt".equals(command)) {
            return GanEdenQuestManager.getRepairedHypershuntCount() > 0;
        }
        if ("isHypershuntGuard".equals(command)) {
            return GanEdenHypershuntManager.isGuard(
                    dialog.getInteractionTarget(),
                    value(params, 1, memoryMap));
        }
        if ("prepareHypershuntGuard".equals(command)) {
            GanEdenHypershuntManager.prepareGuard(dialog);
            return true;
        }
        if ("canPayHypershuntPirates".equals(command)) {
            return GanEdenHypershuntManager.canPayPirates();
        }
        if ("payHypershuntPirates".equals(command)) {
            return GanEdenHypershuntManager.payAndClearGuard(dialog);
        }
        if ("clearHypershuntGuard".equals(command)) {
            GanEdenHypershuntManager.clearGuard(dialog);
            return true;
        }
        if ("engageHypershuntGuard".equals(command)) {
            GanEdenHypershuntManager.engageGuard(dialog);
            return true;
        }
        if ("isBlockedHypershunt".equals(command)) {
            return GanEdenHypershuntManager.isBlockedTap(
                    dialog.getInteractionTarget());
        }
        if ("canInvestigateHypershunt".equals(command)) {
            return GanEdenHypershuntManager.canInvestigate(
                    dialog.getInteractionTarget());
        }
        if ("isSurveyedHypershunt".equals(command)) {
            return GanEdenHypershuntManager.isSurveyedTap(
                    dialog.getInteractionTarget());
        }
        if ("prepareHypershuntInvestigation".equals(command)) {
            GanEdenHypershuntManager.prepareInvestigation(dialog);
            return true;
        }
        if ("surveyHypershunt".equals(command)) {
            GanEdenLogSpec recovered =
                    GanEdenHypershuntManager.markCurrentTapSurveyed(dialog);
            if (recovered == null) return false;
            TextPanelAPI text = dialog.getTextPanel();
            text.setFontSmallInsignia();
            text.addPara(recovered.getBody());
            text.setFontInsignia();
            return true;
        }
        if ("hypershuntSurveyComplete".equals(command)) {
            return GanEdenQuestManager.isAtLeast(Stage.GAN_EDEN_REVEALED);
        }
        if ("hypershuntSurveyPending".equals(command)) {
            return GanEdenQuestManager.getStage()
                    == Stage.INVESTIGATE_HYPERSHUNTS;
        }
        if ("shouldShowEpitaph".equals(command)
                || "shouldShowGrave".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.getStage() == Stage.SPACE_ELEVATOR
                    && !GanEdenQuestManager.isEpitaphFound()
                    && IsaTrophyManager.isIsaOfficerInPlayerFleet();
        }
        if ("epitaphNeedsIsa".equals(command)
                || "graveNeedsIsa".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.getStage() == Stage.SPACE_ELEVATOR
                    && !GanEdenQuestManager.isEpitaphFound()
                    && !IsaTrophyManager.isIsaOfficerInPlayerFleet();
        }
        if ("epitaphInspected".equals(command)
                || "graveInspected".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && (GanEdenQuestManager.isEpitaphFound()
                            || GanEdenQuestManager.isCompleted());
        }
        if ("prepareEpitaph".equals(command)
                || "prepareGrave".equals(command)) {
            showIsa(dialog);
            return true;
        }
        if ("markEpitaph".equals(command)
                || "markGrave".equals(command)) {
            dialog.getTextPanel().setFontInsignia();
            GanEdenLogManager.recover(
                    GanEdenLogSpec.FINAL, dialog.getTextPanel());
            GanEdenQuestManager.markEpitaphFound(dialog.getTextPanel());
            return true;
        }
        if ("canAskRingNpc".equals(command)) {
            return GanEdenQuestManager.canAskAroundShatteredRing();
        }
        if ("revealBlackMarketClue".equals(command)) {
            shiptrophy.campaign.GanEdenClueManager.reveal();
            return true;
        }
        if ("canRecoverSurfaceLog".equals(command)) {
            return canRecoverSurfaceLog(dialog.getInteractionTarget());
        }
        if ("prepareSurfaceLog".equals(command)) {
            prepareSurfaceLog(dialog);
            return true;
        }
        if ("recoverSurfaceLog".equals(command)) {
            return recoverSurfaceLog(dialog);
        }
        if ("prepareEpitaphLog".equals(command)) {
            return prepareEpitaphLog(
                    dialog, value(params, 1, memoryMap));
        }
        if ("showEpitaphLog".equals(command)) {
            GanEdenLogSpec spec = GanEdenLogSpec.forId(
                    value(params, 1, memoryMap));
            if (spec == null) return false;
            GanEdenLogManager.recover(spec, dialog.getTextPanel());
            dialog.getTextPanel().setFontSmallInsignia();
            dialog.getTextPanel().addPara(spec.getBody());
            dialog.getTextPanel().setFontInsignia();
            return true;
        }
        if ("finishEpitaphLogs".equals(command)) {
            dialog.getTextPanel().setFontInsignia();
            return true;
        }
        if ("showInitialLog".equals(command)) {
            TextPanelAPI text = dialog.getTextPanel();
            GanEdenLogManager.recover(GanEdenLogSpec.PART_ONE, text);
            text.setFontSmallInsignia();
            text.addPara(GanEdenLogSpec.PART_ONE.getBody());
            text.setFontInsignia();
            return true;
        }
        if ("canTransitFromGate".equals(command)) {
            return GanEdenQuestManager.canTransitFromGate(
                    dialog.getInteractionTarget());
        }
        if ("isExternalRing".equals(command)) {
            return GanEdenQuestManager.isAtLeast(Stage.GAN_EDEN_REVEALED)
                    && isTarget(dialog, GanEdenQuestManager.EXTERNAL_RING_ID);
        }
        if ("isInternalRing".equals(command)) {
            // Always allow the Eden-side ring to act as an emergency exit.
            // This also covers old saves and console-based testing sessions
            // that entered Gan Eden before the normal quest unlock.
            return isTarget(dialog, GanEdenGenerator.ARRIVAL_RING_ID);
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

    private static void showIsa(InteractionDialogAPI dialog) {
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(
                IsaTrophyManager.findHomeMarket());
        if (isa != null) dialog.getVisualPanel().showPersonInfo(isa);
    }

    private static boolean canRecoverSurfaceLog(SectorEntityToken target) {
        if (target == null
                || !GanEdenQuestManager.isAtLeast(
                        Stage.GAN_EDEN_REVEALED)) {
            return false;
        }
        GanEdenLogSpec spec = GanEdenLogSpec.nextUnrecoveredAtSite(
                target.getId());
        return spec != null
                && GanEdenLogSpec.PART_FOUR.getId().equals(spec.getId());
    }

    private static void prepareSurfaceLog(InteractionDialogAPI dialog) {
        SectorEntityToken target = dialog.getInteractionTarget();
        GanEdenLogSpec spec = target == null ? null
                : GanEdenLogSpec.nextUnrecoveredAtSite(target.getId());
        if (spec == null) return;
        dialog.getTextPanel().addPara(
                "A sealed municipal archive beneath " + spec.getSiteName()
                        + " answers the Leicester continuity credentials. "
                        + "One surviving personal record is available for "
                        + "recovery.");
    }

    private static boolean recoverSurfaceLog(InteractionDialogAPI dialog) {
        SectorEntityToken target = dialog.getInteractionTarget();
        GanEdenLogSpec spec = target == null ? null
                : GanEdenLogSpec.nextUnrecoveredAtSite(target.getId());
        if (spec == null
                || !GanEdenLogSpec.PART_FOUR.getId().equals(spec.getId())) {
            return false;
        }

        TextPanelAPI text = dialog.getTextPanel();
        GanEdenLogManager.recover(spec, text);
        text.setFontSmallInsignia();
        text.addPara(spec.getBody());
        text.setFontInsignia();
        text.addPara("[Archived in Intel: " + spec.getTitle() + "]");
        if (GanEdenLogSpec.PART_FOUR.getId().equals(spec.getId())) {
            GanEdenQuestManager.markTreeLogFound(text);
        }
        return true;
    }

    private static boolean prepareEpitaphLog(
            InteractionDialogAPI dialog, String logId) {
        GanEdenLogSpec spec = GanEdenLogSpec.forId(logId);
        if (spec == null) return false;
        GanEdenLogManager.recover(spec, dialog.getTextPanel());
        dialog.getTextPanel().setFontSmallInsignia();
        return true;
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
