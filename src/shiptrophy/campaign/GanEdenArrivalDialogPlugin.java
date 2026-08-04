package shiptrophy.campaign;

import java.util.Collections;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import shiptrophy.HallOfTriumphCompletionDialogPlugin;

/** One-time cinematic played after Isa first enters Gan Eden. */
public final class GanEdenArrivalDialogPlugin
        implements InteractionDialogPlugin {
    private static final String CONTINUE =
            "ship_trophy_gan_eden_arrival_continue";
    private InteractionDialogAPI dialog;

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        HallOfTriumphCompletionDialogPlugin.showLetterboxedIllustration(
                dialog, "ship_trophy_gan_eden_eden_prime");

        dialog.getTextPanel().addPara(
                "The Power Transit Gate closes behind your fleet. Gan Eden "
                        + "curves above and around you: oceans, mountain ranges, "
                        + "and cloud systems climbing the inside of an impossible "
                        + "world.");
        dialog.getTextPanel().addPara(
                "Isa's slate erupts in warnings. She silences them one by one, "
                        + "then freezes over a surviving emergency channel.");
        dialog.getTextPanel().addPara(
                "\"Active distress beacon,\" she says. Her voice rises with "
                        + "excitement before catching on the last word. \"Human "
                        + "format. It's pointing to a place called the Tree of "
                        + "Life.\"");
        dialog.getTextPanel().addPara(
                "She sends the coordinates to navigation, smiles, and immediately "
                        + "checks them again. \"Someone might still be here. Or "
                        + "something they left for us.\"");
        dialog.getOptionPanel().addOption(
                "Follow the distress beacon to the Tree of Life.", CONTINUE);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (CONTINUE.equals(optionData) && dialog != null) dialog.dismiss();
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
        return Collections.emptyMap();
    }
}
