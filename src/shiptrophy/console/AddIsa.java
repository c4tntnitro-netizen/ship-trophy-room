package shiptrophy.console;

import com.fs.starfarer.api.Global;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import shiptrophy.IsaTrophyManager;
import shiptrophy.ShipTrophyRoomIds;

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

        boolean alreadyInRoster = IsaTrophyManager.isIsaOfficerInPlayerFleet();
        if (!alreadyInRoster && !IsaTrophyManager.addIsaOfficerImmediately()) {
            Console.showMessage("Unable to add Isa Leicester: the player fleet is unavailable.");
            return CommandResult.ERROR;
        }

        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_ISA_OFFICER_GRANTED, true);
        Console.showMessage(alreadyInRoster
                ? "Isa Leicester is already in your officer roster; her recruitment flag is set."
                : "Isa Leicester has been added to your officer roster.");
        return CommandResult.SUCCESS;
    }
}
