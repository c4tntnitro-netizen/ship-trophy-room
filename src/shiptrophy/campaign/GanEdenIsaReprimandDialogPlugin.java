package shiptrophy.campaign;

import java.util.Collections;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.IsaTrophyManager;

/** Post-victory reckoning for Isa forcing the second hypershunt battle. */
public final class GanEdenIsaReprimandDialogPlugin
        implements InteractionDialogPlugin {
    private static final String DISMISS =
            "ship_trophy_gan_eden_isa_reprimand_dismiss";

    private final boolean isaWasCaptain;
    private final String shipName;
    private final String dModName;
    private InteractionDialogAPI dialog;

    public GanEdenIsaReprimandDialogPlugin(
            boolean isaWasCaptain, String shipName, String dModName) {
        this.isaWasCaptain = isaWasCaptain;
        this.shipName = shipName == null ? "" : shipName;
        this.dModName = dModName == null ? "" : dModName;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(
                IsaTrophyManager.findHomeMarket());
        if (isa != null) dialog.getVisualPanel().showPersonInfo(isa);

        if (isaWasCaptain) {
            dialog.getTextPanel().addPara(
                    "Once the hypershunt is secure, Isa reports to your "
                            + "bridge. The anger that carried her ship out of "
                            + "formation has burned down to a brittle silence.");
            if (!shipName.isEmpty()) {
                dialog.getTextPanel().addPara(
                        "Damage control reports permanent structural damage "
                                + "aboard the " + shipName + ".");
            }
            if (!shipName.isEmpty() && !dModName.isEmpty()) {
                dialog.getTextPanel().addPara(
                        "[The " + shipName + " has acquired " + dModName
                                + ".]",
                        Misc.getNegativeHighlightColor(),
                        shipName,
                        dModName);
            }
            dialog.getTextPanel().addPara(
                    "\"You broke formation, committed the fleet without an "
                            + "order, and put every ship behind you in the line "
                            + "of fire,\" you tell her.");
        } else {
            dialog.getTextPanel().addPara(
                    "Once the hypershunt is secure, Isa returns to your bridge. "
                            + "The comm station she commandeered still carries "
                            + "the recording of her threat on its main display.");
            dialog.getTextPanel().addPara(
                    "\"You used my bridge to issue an ultimatum and committed "
                            + "this fleet to battle before I gave the order,\" "
                            + "you tell her.");
        }

        dialog.getTextPanel().addPara(
                "Isa starts to answer. \"They were destroying it.\"");
        dialog.getTextPanel().addPara(
                "\"I know exactly what they were doing. You still ask before "
                        + "you start a war.\"");
        dialog.getTextPanel().addPara(
                "\"You're retaining your command,\" you say. \"But not your "
                        + "freedom. Three months in the brig.\"");
        dialog.getTextPanel().addPara(
                "Her jaw works once. Then she looks away. \"Understood, "
                        + "Captain.\"");
        dialog.getOptionPanel().addOption("Dismissed.", DISMISS);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (DISMISS.equals(optionData) && dialog != null) dialog.dismiss();
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
