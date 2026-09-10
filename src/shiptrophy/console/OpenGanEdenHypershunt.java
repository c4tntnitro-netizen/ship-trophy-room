package shiptrophy.console;

import java.util.List;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;

import shiptrophy.campaign.GanEdenHypershuntManager;
import shiptrophy.campaign.GanEdenQuestManager;

/** Opens a quest hypershunt directly when another mod masks its interaction. */
public class OpenGanEdenHypershunt implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("OpenGanEdenHypershunt can only be used during a campaign.");
            return CommandResult.WRONG_CONTEXT;
        }

        int requested = 0;
        String value = args == null ? "" : args.trim();
        if (value.length() > 0) {
            try {
                requested = Integer.parseInt(value) - 1;
            } catch (NumberFormatException ex) {
                return CommandResult.BAD_SYNTAX;
            }
        }

        if (!GanEdenQuestManager.forceHypershuntInvestigation()) {
            Console.showMessage("Unable to initialize the Gan Eden hypershunt investigation.");
            return CommandResult.ERROR;
        }
        List<SectorEntityToken> taps = GanEdenHypershuntManager.getQuestHypershunts();
        if (taps.isEmpty()) {
            Console.showMessage("No Coronal Hypershunts were found in this campaign.");
            return CommandResult.ERROR;
        }
        SectorEntityToken tap;
        if (value.length() == 0) {
            tap = GanEdenHypershuntManager.getFirstUnsurveyedHypershunt();
            if (tap == null) tap = taps.get(0);
        } else if (requested >= 0 && requested < taps.size()) {
            tap = taps.get(requested);
        } else {
            return CommandResult.BAD_SYNTAX;
        }

        GanEdenHypershuntManager.forceClearBlockade(tap);
        Console.showDialogOnClose(
                new RuleBasedInteractionDialogPluginImpl(), tap);
        Console.showMessage("Close the console to open the hypershunt dialogue.");
        return CommandResult.SUCCESS;
    }
}
