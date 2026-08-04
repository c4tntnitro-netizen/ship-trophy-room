package shiptrophy.campaign;

import java.awt.Color;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import shiptrophy.IsaTrophyManager;

/** Low-frequency quest state checks; no per-frame world scanning. */
public final class GanEdenQuestScript implements EveryFrameScript {
    private static final Color ISA_COMMS_COLOR =
            new Color(244, 163, 79);
    private static final String[] ISA_ENTRY_COMMS = {
        "Isa: \"...oh.\"",
        "Isa: \"Oh! Oh, look at it!\"",
        "Isa: \"Captain, it's beautiful.\"",
        "Isa: \"It's so beautiful.\"",
    };

    private float interval;
    private boolean locationInitialized;
    private boolean wasInGanEden;
    private boolean entryCommsActive;
    private int entryCommsIndex;
    private float entryCommsDelay;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null) return;
        interval += amount;
        if (interval < 1f) return;
        float elapsed = interval;
        interval = 0f;

        GanEdenQuestManager.ensureForCurrentSave();
        GanEdenQuestManager.checkHypershunts();
        GanEdenQuestManager.completeIfReady();
        tryShowHypershuntReprimand();
        tryShowArrivalScene();
        advanceIsaEntryComms(elapsed);
    }

    private void advanceIsaEntryComms(float amount) {
        if (Global.getSector().getPlayerFleet() == null) return;
        boolean inGanEden = GanEdenGenerator.isGanEden(
                Global.getSector().getPlayerFleet());

        if (!locationInitialized) {
            locationInitialized = true;
            wasInGanEden = inGanEden;
            return;
        }

        if (inGanEden && !wasInGanEden
                && IsaTrophyManager.isIsaOfficerInPlayerFleet()) {
            entryCommsActive = true;
            entryCommsIndex = 0;
            entryCommsDelay = 0.5f;
        } else if (!inGanEden) {
            entryCommsActive = false;
        }
        wasInGanEden = inGanEden;

        if (!entryCommsActive || !inGanEden) return;
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui == null
                || ui.isShowingDialog()
                || ui.isShowingMenu()
                || ui.getCurrentCoreTab() != null) {
            return;
        }

        entryCommsDelay -= amount;
        if (entryCommsDelay > 0f) return;
        ui.addMessage(
                new IsaEntryCommsMessage(
                        ISA_ENTRY_COMMS[entryCommsIndex], isaPortrait()),
                MessageClickAction.NOTHING);
        entryCommsIndex++;
        if (entryCommsIndex >= ISA_ENTRY_COMMS.length) {
            entryCommsActive = false;
        } else {
            entryCommsDelay = 1.5f;
        }
    }

    private String isaPortrait() {
        PersonAPI isa = Global.getSector().getImportantPeople()
                .getPerson(IsaTrophyManager.PERSON_ID);
        if (isa == null) {
            isa = IsaTrophyManager.getOrCreateIsa(
                    IsaTrophyManager.findHomeMarket());
        }
        return isa == null
                ? IsaTrophyManager.PORTRAIT_PATH
                : isa.getPortraitSprite();
    }

    /** A portrait-backed, non-clicking campaign message; not an intel entry. */
    private static final class IsaEntryCommsMessage
            extends BaseIntelPlugin {
        private static final long serialVersionUID = 1L;

        private final String text;
        private final String portrait;

        private IsaEntryCommsMessage(String text, String portrait) {
            this.text = text;
            this.portrait = portrait;
        }

        @Override
        protected String getName() {
            return text;
        }

        @Override
        public String getIcon() {
            return portrait;
        }

        @Override
        public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
            info.addPara(text, ISA_COMMS_COLOR, 0f);
        }
    }

    private void tryShowArrivalScene() {
        if (!GanEdenQuestManager.shouldShowArrivalScene()
                || Global.getSector().getPlayerFleet() == null) {
            return;
        }
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui == null
                || ui.isShowingDialog()
                || ui.isShowingMenu()
                || ui.getCurrentCoreTab() != null) {
            return;
        }
        if (ui.showInteractionDialog(
                new GanEdenArrivalDialogPlugin(),
                Global.getSector().getPlayerFleet())) {
            GanEdenQuestManager.markArrivalSceneShown();
        }
    }

    private void tryShowHypershuntReprimand() {
        if (!GanEdenHypershuntManager.isCrisisReprimandPending()
                || Global.getSector().getPlayerFleet() == null) {
            return;
        }
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui == null
                || ui.isShowingDialog()
                || ui.isShowingMenu()
                || ui.getCurrentCoreTab() != null) {
            return;
        }
        GanEdenIsaReprimandDialogPlugin plugin =
                new GanEdenIsaReprimandDialogPlugin(
                        GanEdenHypershuntManager.wasIsaCrisisCaptain(),
                        GanEdenHypershuntManager
                                .getCrisisReprimandShipName(),
                        GanEdenHypershuntManager
                                .getCrisisReprimandDModName());
        if (ui.showInteractionDialog(
                plugin, Global.getSector().getPlayerFleet())) {
            GanEdenHypershuntManager.markCrisisReprimandShown();
        }
    }
}
