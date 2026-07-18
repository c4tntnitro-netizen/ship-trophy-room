package shiptrophy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.hullmods.Contempt;
import shiptrophy.hullmods.Gaze;

public class IsaContactDialogPlugin implements InteractionDialogPlugin {
    private static final String MASTERWORK = "ship_trophy_contact_masterwork";
    private static final String UNIQUES = "ship_trophy_contact_uniques";
    private static final String SUBTYPES = "ship_trophy_contact_subtypes";
    private static final String BACK = "ship_trophy_contact_back";
    private static final String LEAVE = "ship_trophy_contact_leave";
    private static final String SUBTYPE_PREFIX = "ship_trophy_contact_subtype:";

    private final InteractionDialogPlugin originalPlugin;
    private final Map<String, MemoryAPI> memoryMap;
    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;

    public IsaContactDialogPlugin(InteractionDialogPlugin originalPlugin, Map<String, MemoryAPI> memoryMap) {
        this.originalPlugin = originalPlugin;
        this.memoryMap = memoryMap;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        this.text = dialog.getTextPanel();
        this.options = dialog.getOptionPanel();
        showMainMenu();
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null) return;
        String option = optionData.toString();

        if (MASTERWORK.equals(option)) {
            showMasterwork();
        } else if (UNIQUES.equals(option)) {
            showUniques();
        } else if (SUBTYPES.equals(option)) {
            showSubtypes();
        } else if (BACK.equals(option)) {
            showMainMenu();
        } else if (option.startsWith(SUBTYPE_PREFIX)) {
            showSubtype(option.substring(SUBTYPE_PREFIX.length()));
        } else if (LEAVE.equals(option)) {
            close();
        }
    }

    private void showMainMenu() {
        text.clear();
        showIsa();

        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);
        IsaTrophyManager.refreshIsaHullmod();

        text.addPara("Isa has the Triumph Hall network up on a battered slate: %s functional rooms, %s unique hull types, %s unique deployment points.",
                Misc.getHighlightColor(), "" + stats.functionalRooms, "" + stats.uniqueHullIds.size(), "" + Math.round(stats.uniqueDeploymentPoints));
        text.addPara("\"The trick is not owning ships,\" she says. \"It's owning examples. Hulls with enough history that they teach the rest of the dockyard something.\"");

        options.clearOptions();
        options.addOption("Review Isa's five-hull showcase.", MASTERWORK);
        options.addOption("Ask about one-of-a-kind trophy hullmods.", UNIQUES);
        options.addOption("Ask about doctrine and subtype trophy programs.", SUBTYPES);
        options.addOption("Cut the comm link.", LEAVE);
    }

    private void showMasterwork() {
        text.clear();
        showIsa();
        IsaTrophyManager.setMasterworkBriefed();

        boolean complete = IsaTrophyManager.isMasterworkComplete();
        List<IsaTrophyManager.ShowcaseRequirement> requirements = IsaTrophyManager.getMasterworkRequirements();

        text.addPara("\"For a proper capital-line provenance program, I need five anchors,\" Isa says. \"Onslaught XIV. Paragon. Invictus. Conquest. Executor. Display them, not just park them in a fleet roster.\"");
        for (IsaTrophyManager.ShowcaseRequirement requirement : requirements) {
            addStatusLine(requirement.met, requirement.displayName);
        }

        if (complete) {
            IsaTrophyManager.setMasterworkCompleted();
            IsaTrophyManager.refreshIsaHullmod();
            text.addPara("\"That's the set. A full spread: Domain armor gospel, high-tech cathedral work, League audacity, Diktat vanity, and the Hegemony's favorite blunt instrument.\"");
            text.addPara("Unlocked: %s. Isa's yard certification doubles positive S-mod bonus effects from built-in hullmods.",
                    Misc.getHighlightColor(), IsaTrophyManager.getHullModName(IsaTrophyManager.PROVENANCE_HULLMOD_ID), "doubles positive S-mod bonus effects");
        } else {
            text.addPara("\"Bring me the missing hulls and I'll certify the program. The mark lets my team re-tune a ship around its built-in modifications, but only if the display history is strong enough to justify the work.\"");
        }

        showBackOptions();
    }

    private void showUniques() {
        text.clear();
        showIsa();

        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);

        boolean gaze = TrophyNetwork.hasShowcasedHull(stats, Gaze.REQUIRED_BASE_HULL_ID);
        boolean contempt = TrophyNetwork.hasShowcasedHull(stats, Contempt.REQUIRED_BASE_HULL_ID);

        text.addPara("\"Some hulls are so singular they don't need a doctrine category,\" Isa says. \"The network either has the example, or it doesn't.\"");
        addStatusLine(gaze, "Ziggurat display: " + IsaTrophyManager.getHullModName(Gaze.HULLMOD_ID));
        addStatusLine(contempt, "Onslaught Mk.I display: " + IsaTrophyManager.getHullModName(Contempt.HULLMOD_ID));

        if (gaze) {
            text.addPara("\"The Ziggurat ledger is ugly in ways I don't like staring at. But if you're mounting Omega weapons, Gaze will make room for them.\"");
        } else {
            text.addPara("\"If you ever put the Ziggurat on display, come find me. Quietly.\"");
        }

        if (contempt) {
            text.addPara("\"The Mk.I is not subtle. Good. Contempt is for ships that need to make ugly weapons fit before anyone has time to object.\"");
        } else {
            text.addPara("\"An Onslaught Mk.I would give the yard enough threat-pattern data for something mean.\"");
        }

        showBackOptions();
    }

    private void showSubtypes() {
        text.clear();
        showIsa();
        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);

        text.addPara("\"Pick a family and I'll tell you whether the displays have enough mass to teach us anything useful.\"");

        options.clearOptions();
        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
            if (!subtype.hasHullModUnlock() || !IsaTrophyManager.hullModExists(subtype.hullModId)) continue;
            int current = Math.round(stats.getSubtypeDp(subtype.id));
            int needed = Math.round(subtype.unlockDp);
            options.addOption(subtype.displayName + " (" + current + "/" + needed + " DP)", SUBTYPE_PREFIX + subtype.id);
        }
        options.addOption("Back.", BACK);
        options.addOption("Cut the comm link.", LEAVE);
    }

    private void showSubtype(String subtypeId) {
        text.clear();
        showIsa();

        TrophySubtypeSpec subtype = TrophySubtypeRegistry.getSubtype(subtypeId);
        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);

        if (subtype == null || !subtype.isActive()) {
            text.addPara("\"That ledger isn't active right now,\" Isa says. \"Might be a missing mod, might be a bad index.\"");
            showBackOptions();
            return;
        }

        float current = stats.getSubtypeDp(subtype.id);
        boolean unlocked = current >= subtype.unlockDp;
        String hullmodName = IsaTrophyManager.getHullModName(subtype.hullModId);

        text.addPara(getSubtypeComment(subtype));
        text.addPara("Showcase progress: %s / %s DP worth of %s ships.",
                Misc.getHighlightColor(), "" + Math.round(current), "" + Math.round(subtype.unlockDp), subtype.showcaseName);

        if (unlocked) {
            text.addPara("\"That's enough. I can sign off on %s,\" Isa says. \"Check your refit crews; the spec is in the system.\"",
                    Misc.getHighlightColor(), hullmodName);
        } else {
            int remaining = Math.max(0, Math.round(subtype.unlockDp - current));
            text.addPara("\"Not enough examples yet. Bring me roughly %s more DP worth and the pattern should stop lying to us.\"",
                    Misc.getHighlightColor(), "" + remaining);
        }

        showBackOptions();
    }

    private String getSubtypeComment(TrophySubtypeSpec subtype) {
        if ("xiv".equals(subtype.id)) {
            return "\"XIV hulls are doctrine with rivets,\" Isa says. \"Crude, stubborn, and very hard to argue with once the armor starts moving.\"";
        }
        if ("lp".equals(subtype.id)) {
            return "\"Path ships are warnings with engines. Dangerous lesson set, but useful if you respect how much they are willing to burn.\"";
        }
        if ("lg".equals(subtype.id)) {
            return "\"Lion's Guard work is parade paint over surprisingly focused energy tuning. Ignore the gold leaf; read the capacitors.\"";
        }
        if ("tt".equals(subtype.id)) {
            return "\"Tri-Tachyon legacy hulls hide their best tricks in the absence of obvious machinery. Clean baffling, clean lies.\"";
        }
        return "\"This family has its own habits,\" Isa says. \"Get enough examples in one network and the repeated choices start showing through.\"";
    }

    private void addStatusLine(boolean met, String label) {
        Color h = met ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor();
        LabelAPI line = text.addPara((met ? "Complete: " : "Needed: ") + label);
        line.setHighlight(met ? "Complete" : "Needed");
        line.setHighlightColor(h);
    }

    private void showBackOptions() {
        options.clearOptions();
        options.addOption("Back.", BACK);
        options.addOption("Cut the comm link.", LEAVE);
    }

    private void showIsa() {
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(home);
        if (dialog != null && isa != null) {
            dialog.getVisualPanel().showPersonInfo(isa);
        }
    }

    private void close() {
        if (dialog == null) return;
        if (originalPlugin != null) dialog.setPlugin(originalPlugin);
        dialog.dismiss();
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
        return memoryMap;
    }
}
