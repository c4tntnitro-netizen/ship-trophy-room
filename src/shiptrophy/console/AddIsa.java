package shiptrophy.console;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import shiptrophy.IsaTrophyManager;

/** Console Commands integration for immediately adding Isa to the player fleet. */
public class AddIsa implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("AddIsa can only be used during a campaign.");
            return CommandResult.WRONG_CONTEXT;
        }
        if (args != null && args.trim().length() > 0) {
            return CommandResult.BAD_SYNTAX;
        }

        if (IsaTrophyManager.isIsaOfficerInPlayerFleet()) {
            Console.showMessage("Isa Leicester is already in your officer roster.");
            return CommandResult.SUCCESS;
        }
        if (!IsaTrophyManager.addIsaOfficerImmediately()) {
            Console.showMessage("Unable to add Isa Leicester: the player fleet is unavailable.");
            return CommandResult.ERROR;
        }

        Console.showMessage("Isa Leicester has been added to your officer roster.");
        return CommandResult.SUCCESS;
    }
}
