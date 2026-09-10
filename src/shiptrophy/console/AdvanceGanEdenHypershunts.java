package shiptrophy.console;

import java.util.List;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import com.fs.starfarer.api.campaign.SectorEntityToken;

import shiptrophy.campaign.GanEdenHypershuntManager;
import shiptrophy.campaign.GanEdenLogSpec;
import shiptrophy.campaign.GanEdenQuestManager;

/** Save-recovery command that completes one or both hypershunt surveys. */
public class AdvanceGanEdenHypershunts implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("AdvanceGanEdenHypershunts can only be used during a campaign.");
            return CommandResult.WRONG_CONTEXT;
        }
        String value = args == null ? "" : args.trim();
        boolean all = "all".equalsIgnoreCase(value);
        if (value.length() > 0 && !all) return CommandResult.BAD_SYNTAX;

        if (!GanEdenQuestManager.forceHypershuntInvestigation()) {
            Console.showMessage("Unable to initialize the Gan Eden hypershunt investigation.");
            return CommandResult.ERROR;
        }
        List<SectorEntityToken> taps = GanEdenHypershuntManager.getQuestHypershunts();
        int advanced = 0;
        for (SectorEntityToken tap : taps) {
            GanEdenLogSpec recovered = GanEdenHypershuntManager.markTapSurveyed(tap);
            if (recovered != null) {
                advanced++;
                Console.showMessage("Recovered " + recovered.getId() + " from " + tap.getName() + ".");
                if (!all) break;
            }
        }
        if (advanced == 0) {
            Console.showMessage("Both Gan Eden hypershunt records are already recovered.");
        } else if (GanEdenQuestManager.isAtLeast(
                GanEdenQuestManager.Stage.GAN_EDEN_REVEALED)) {
            Console.showMessage("Both routing vectors are recovered; Power Transit Gate - Gan Eden is now available.");
        } else {
            Console.showMessage("Advanced one hypershunt survey; run the command again for the second.");
        }
        return CommandResult.SUCCESS;
    }
}
