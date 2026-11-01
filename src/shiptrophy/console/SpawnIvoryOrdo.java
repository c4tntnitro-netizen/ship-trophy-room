package shiptrophy.console;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import shiptrophy.campaign.GanEdenTransitAmbushManager;

/** Spawns a repeatable Custodian fight without changing quest progress. */
public class SpawnIvoryOrdo implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("SpawnIvoryOrdo can only be used during a campaign.");
            return CommandResult.WRONG_CONTEXT;
        }
        if (args != null && args.trim().length() > 0) {
            return CommandResult.BAD_SYNTAX;
        }
        if (GanEdenTransitAmbushManager.spawnReplayOrdo() == null) {
            Console.showMessage("Unable to spawn the Ivory Ordo: the player fleet must be outside battle in a campaign location.");
            return CommandResult.ERROR;
        }
        Console.showMessage("A fresh Ivory Custodian Ordo has spawned nearby and is moving to intercept. Quest progress is unchanged.");
        return CommandResult.SUCCESS;
    }
}
