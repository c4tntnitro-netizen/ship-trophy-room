package shiptrophy;

import java.awt.Color;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.Option;
import com.fs.starfarer.api.campaign.rules.RuleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;

public class IsaBarEvent extends BaseBarEvent {
    private static final String INTRO_GREETING = "ship_trophy_intro_greeting";
    private static final String INTRO_LUDD = "ship_trophy_intro_ludd";
    private static final String INTRO_RESPECT = "ship_trophy_intro_respect";
    private static final String INTRO_RESPECT_2 = "ship_trophy_intro_respect_2";
    private static final String INTRO_EXPLANATION_2 = "ship_trophy_intro_explanation_2";
    private static final String INTRO_EXPLANATION_3 = "ship_trophy_intro_explanation_3";
    private static final String ACCEPT = "ship_trophy_accept";
    private static final String LEAVE = "ship_trophy_leave";

    private static final String PROMPT_TRIGGER = "ShipTrophyIsaBarPrompt";
    private static final String INTRO_TRIGGER = "ShipTrophyIsaBarIntro";
    private static final String GREETING_TRIGGER = "ShipTrophyIsaBarGreeting";
    private static final String LUDD_TRIGGER = "ShipTrophyIsaBarLudd";
    private static final String RESPECT_TRIGGER = "ShipTrophyIsaBarRespect";
    private static final String RESPECT_2_TRIGGER = "ShipTrophyIsaBarRespectTwo";
    private static final String EXPLANATION_2_TRIGGER = "ShipTrophyIsaBarPitchTwo";
    private static final String EXPLANATION_3_TRIGGER = "ShipTrophyIsaBarPitchThree";
    private static final String ACCEPTED_TRIGGER = "ShipTrophyIsaBarAccepted";

    private static final String MARKET_NAME = "$shipTrophyIsaBarMarketName";
    private static final String PLAYER_RANK = "$shipTrophyIsaBarPlayerRank";
    private static final String PLAYER_NAME = "$shipTrophyIsaBarPlayerName";
    private static final String MARINE_ONE_COLOR = "$shipTrophyIsaMarineOneColor";
    private static final String MARINE_TWO_COLOR = "$shipTrophyIsaMarineTwoColor";
    private static final String MARINE_ONE_LINE = "$shipTrophyIsaMarineOneLine";
    private static final String MARINE_TWO_LINE = "$shipTrophyIsaMarineTwoLine";

    @Override
    public String getBarEventId() {
        return IsaBarEventCreator.ID;
    }

    @Override
    public boolean isAlwaysShow() {
        return true;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        return !IsaTrophyManager.isIntroduced()
                && home != null && market != null && home.getId().equals(market.getId());
    }

    @Override
    public boolean shouldRemoveEvent() {
        return done;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI promptDialog, Map<String, MemoryAPI> promptMemoryMap) {
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        if (!shouldShowAtMarket(home)) return;

        setDialogueVariables(promptMemoryMap, home);
        RuleAPI rule = Global.getSector().getRules().getBestMatching(
                null, PROMPT_TRIGGER, promptDialog, promptMemoryMap);
        if (rule == null) {
            logMissingRule(PROMPT_TRIGGER);
            return;
        }

        String prompt = resolve(rule.getId(), rule.pickText(), promptDialog, promptMemoryMap);
        List<Option> promptOptions = rule.getOptions();
        if (prompt == null || promptOptions == null || promptOptions.isEmpty()) {
            logMissingRule(PROMPT_TRIGGER + " text/options");
            return;
        }

        promptDialog.getTextPanel().addPara(prompt);
        String optionText = resolve(rule.getId(), promptOptions.get(0).text, promptDialog, promptMemoryMap);
        promptDialog.getOptionPanel().addOption(optionText, this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        setDialogueVariables(memoryMap, IsaTrophyManager.findHomeMarket());
        showStage(INTRO_TRIGGER);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null) return;

        String option = optionData.toString();
        if (LEAVE.equals(option)) {
            done = true;
            return;
        }
        if (ACCEPT.equals(option)) {
            acceptIntro();
            showStage(ACCEPTED_TRIGGER);
            return;
        }

        String trigger = getTrigger(option);
        if (trigger != null) showStage(trigger);
    }

    private String getTrigger(String option) {
        if (INTRO_GREETING.equals(option)) return GREETING_TRIGGER;
        if (INTRO_LUDD.equals(option)) return LUDD_TRIGGER;
        if (INTRO_RESPECT.equals(option)) return RESPECT_TRIGGER;
        if (INTRO_RESPECT_2.equals(option)) return RESPECT_2_TRIGGER;
        if (INTRO_EXPLANATION_2.equals(option)) return EXPLANATION_2_TRIGGER;
        if (INTRO_EXPLANATION_3.equals(option)) return EXPLANATION_3_TRIGGER;
        return null;
    }

    private void showStage(String trigger) {
        text.clear();
        options.clearOptions();
        showIsa();
        if (!FireBest.fire(null, dialog, memoryMap, trigger)) {
            logMissingRule(trigger);
            done = true;
        }
    }

    private void acceptIntro() {
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        IsaTrophyManager.setIntroduced(true);
        IsaTrophyManager.ensureContact(home, null);
    }

    private void setDialogueVariables(Map<String, MemoryAPI> map, MarketAPI home) {
        if (map == null) return;
        MemoryAPI local = map.get(MemKeys.LOCAL);
        if (local == null) return;

        local.set(MARKET_NAME, home == null ? "the colony" : home.getName());
        PersonAPI player = Global.getSector() == null ? null : Global.getSector().getPlayerPerson();
        String rank = player == null ? null : player.getRank();
        String name = player == null ? null : player.getNameString();
        local.set(PLAYER_RANK, rank == null || rank.length() <= 0 ? "Captain" : rank);
        local.set(PLAYER_NAME, name == null ? "" : name);
        local.set(MARINE_ONE_COLOR, new Color(190, 105, 255));
        local.set(MARINE_TWO_COLOR, new Color(90, 220, 135));
        local.set(MARINE_ONE_LINE, "Respect costs extra,");
        local.set(MARINE_TWO_LINE, (rank == null || rank.length() <= 0 ? "Captain" : rank)
                + " don't even tip good.");
    }

    private String resolve(String ruleId, String value, InteractionDialogAPI targetDialog,
            Map<String, MemoryAPI> targetMemoryMap) {
        if (value == null || Global.getSector() == null || Global.getSector().getRules() == null) return value;
        return Global.getSector().getRules().performTokenReplacement(
                ruleId, value, targetDialog.getInteractionTarget(), targetMemoryMap);
    }

    private void showIsa() {
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(IsaTrophyManager.findHomeMarket());
        if (dialog != null && isa != null) {
            dialog.getVisualPanel().hideSecondPerson();
            dialog.getVisualPanel().showPersonInfo(isa);
        }
    }

    private void logMissingRule(String trigger) {
    }
}