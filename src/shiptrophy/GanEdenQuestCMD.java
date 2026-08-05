package shiptrophy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import shiptrophy.campaign.GanEdenGenerator;
import shiptrophy.campaign.GanEdenFinalLogMusicScript;
import shiptrophy.campaign.GanEdenAmbushScript;
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
        if ("isGoldenOmega".equals(command)) {
            return GanEdenAmbushScript.isGoldenFleet(
                    dialog.getInteractionTarget());
        }
        if ("isGoldenOmegaFightable".equals(command)) {
            return GanEdenAmbushScript.isGoldenFleet(
                            dialog.getInteractionTarget())
                    && GanEdenQuestManager.isAtLeast(
                            Stage.DEFEAT_GOLDEN_SHARDS);
        }
        if ("questCompleted".equals(command)) {
            return GanEdenQuestManager.isCompleted();
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
            return showLogPage(dialog, recovered, 0);
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
        if ("spaceElevatorRepels".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.getStage()
                            == Stage.GAN_EDEN_REVEALED;
        }
        if ("spaceElevatorGuarded".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.getStage()
                            == Stage.DEFEAT_GOLDEN_SHARDS
                    && !GanEdenAmbushScript.isDefeated();
        }
        if ("canLureGoldenOmega".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenQuestManager.isCompleted()
                    && GanEdenAmbushScript.canLureNextWave();
        }
        if ("lureGoldenOmega".equals(command)) {
            return isSpaceElevator(dialog.getInteractionTarget())
                    && GanEdenAmbushScript.lureNextWave();
        }
        if ("prepareEpitaph".equals(command)
                || "prepareGrave".equals(command)) {
            HallOfTriumphCompletionDialogPlugin.showLetterboxedIllustration(
                    dialog, "ship_trophy_gan_eden_log_five");
            return true;
        }
        if ("markEpitaph".equals(command)
                || "markGrave".equals(command)) {
            dialog.getTextPanel().setFontInsignia();
            GanEdenLogManager.recoverSilently(GanEdenLogSpec.FINAL);
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
            return showLogPage(dialog, spec, 0);
        }
        if ("showLogPage".equals(command)) {
            return showLogPage(
                    dialog,
                    GanEdenLogSpec.forId(value(params, 1, memoryMap)),
                    integerValue(params, 2, memoryMap));
        }
        if ("startFinalLogMusic".equals(command)) {
            GanEdenFinalLogMusicScript.start();
            return true;
        }
        if ("stopFinalLogMusic".equals(command)) {
            GanEdenFinalLogMusicScript.stop();
            return true;
        }
        if ("finishEpitaphLogs".equals(command)) {
            dialog.getTextPanel().setFontInsignia();
            return true;
        }
        if ("showInitialLog".equals(command)) {
            // Compatibility alias for rule packs predating pagination.
            return showInitialLogPage(dialog, 0);
        }
        if ("showInitialLogPage".equals(command)) {
            return showInitialLogPage(
                    dialog, integerValue(params, 1, memoryMap));
        }
        if ("recoverInitialLog".equals(command)) {
            GanEdenLogManager.recoverSilently(GanEdenLogSpec.PART_ONE);
            return true;
        }
        if ("canTransitFromGate".equals(command)) {
            return GanEdenQuestManager.canTransitFromGate(
                    dialog.getInteractionTarget());
        }
        if ("canUseJanusGate".equals(command)) {
            return canUseJanusGate();
        }
        if ("lacksUsableJanusGate".equals(command)) {
            return !canUseJanusGate();
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
            if (!canUseJanusGate()) return false;
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

    /** Mirrors vanilla Gate access: acquired Janus Device plus integration. */
    private static boolean canUseJanusGate() {
        if (Global.getSector() == null) return false;
        MemoryAPI global = Global.getSector().getMemoryWithoutUpdate();
        return global.getBoolean("$gatesActive")
                && global.getBoolean("$playerCanUseGates");
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
        GanEdenLogManager.recoverSilently(spec);
        if (GanEdenLogSpec.PART_FOUR.getId().equals(spec.getId())) {
            GanEdenQuestManager.markTreeLogFound(text);
        }
        return true;
    }

    private static boolean prepareEpitaphLog(
            InteractionDialogAPI dialog, String logId) {
        GanEdenLogSpec spec = GanEdenLogSpec.forId(logId);
        if (spec == null) return false;
        return showLogPage(dialog, spec, 0);
    }

    private static boolean isSpaceElevator(SectorEntityToken target) {
        return target != null
                && GanEdenGenerator.SPACE_ELEVATOR_ID.equals(target.getId());
    }

    /**
     * Plays Personal Log 1765 in three readable screens. The Intel entry is
     * deliberately not created here; it is filed only after Isa responds.
     */
    private static boolean showInitialLogPage(
            InteractionDialogAPI dialog, int page) {
        return showLogPage(dialog, GanEdenLogSpec.PART_ONE, page);
    }

    /** Renders one deliberately paced page of a recovered archive. */
    private static boolean showLogPage(
            InteractionDialogAPI dialog, GanEdenLogSpec spec, int page) {
        if (dialog == null || spec == null || page < 0) return false;
        String[] paragraphs = spec.getBody().split("\\r?\\n\\s*\\r?\\n");
        int[] breaks = pageBreaks(spec, paragraphs.length);
        if (page + 1 >= breaks.length) return false;
        int start = Math.min(breaks[page], paragraphs.length);
        int end = Math.min(breaks[page + 1], paragraphs.length);

        TextPanelAPI text = dialog.getTextPanel();
        text.setFontSmallInsignia();
        for (int i = start; i < end; i++) {
            addArchiveParagraph(text, paragraphs[i].trim());
        }
        text.setFontInsignia();
        return true;
    }

    private static int[] pageBreaks(GanEdenLogSpec spec, int paragraphCount) {
        if (spec == GanEdenLogSpec.PART_ONE) {
            return boundedBreaks(paragraphCount, 0, 7, 13, paragraphCount);
        }
        if (spec == GanEdenLogSpec.PART_TWO) {
            return boundedBreaks(paragraphCount, 0, 7, 15, paragraphCount);
        }
        if (spec == GanEdenLogSpec.PART_THREE) {
            return boundedBreaks(paragraphCount, 0, 6, 14, 22, paragraphCount);
        }
        if (spec == GanEdenLogSpec.PART_FOUR) {
            return boundedBreaks(
                    paragraphCount, 0, 7, 15, 23, 31, paragraphCount);
        }
        return boundedBreaks(
                paragraphCount, 0, 9, 18, 28, 38, 47, paragraphCount);
    }

    private static int[] boundedBreaks(int paragraphCount, int... requested) {
        int[] result = new int[requested.length];
        int previous = 0;
        for (int i = 0; i < requested.length; i++) {
            int value = Math.max(previous, Math.min(requested[i], paragraphCount));
            result[i] = value;
            previous = value;
        }
        return result;
    }

    private static void addArchiveParagraph(
            TextPanelAPI text, String paragraph) {
        if (paragraph == null || paragraph.length() <= 0) return;
        if (paragraph.startsWith(
                "DOMAIN INFOSEC VIOLATION THRESHOLD WARNING")) {
            for (String line : paragraph.split("\\r?\\n")) {
                text.addPara(line, Misc.getNegativeHighlightColor());
            }
            return;
        }
        if (paragraph.startsWith("FATAL ACCESS ERROR")) {
            text.addPara(paragraph, Misc.getNegativeHighlightColor());
            return;
        }
        if (paragraph.startsWith("This device is not authorized")) {
            text.addPara(
                    paragraph,
                    new Color(82, 88, 94),
                    Misc.getNegativeHighlightColor(),
                    "not authorized",
                    "Domain Information Security Standards");
            return;
        }
        text.addPara(paragraph);
    }

    private static int integerValue(
            List<Token> params,
            int index,
            Map<String, MemoryAPI> memoryMap) {
        try {
            return Integer.parseInt(value(params, index, memoryMap));
        } catch (NumberFormatException ex) {
            return -1;
        }
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
