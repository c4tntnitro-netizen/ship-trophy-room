package shiptrophy.campaign;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.ShipTrophyL10n;
import shiptrophy.campaign.GanEdenQuestManager.Stage;

/** Dynamic intel entry for Isa's search for Isaac Leicester. */
public final class GanEdenQuestIntel extends BaseIntelPlugin {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getName() {
        return GanEdenQuestManager.isCompleted()
                ? ShipTrophyL10n.get("quest_title_complete")
                : ShipTrophyL10n.get("quest_title");
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), getTitleColor(mode), 0f);
        addBulletPoints(info, mode, false, Misc.getTextColor(), 3f);
    }

    @Override
    protected void addBulletPoints(
            TooltipMakerAPI info,
            ListInfoMode mode,
            boolean isUpdate,
            Color tc,
            float initPad) {
        if (isUpdate && GanEdenQuestManager.isCompleted()) {
            info.addPara(ShipTrophyL10n.get("quest_completed"), initPad, tc,
                    Misc.getPositiveHighlightColor(),
                    ShipTrophyL10n.get("quest_title"));
            return;
        }
        info.addPara(shortObjective(), initPad, tc,
                Misc.getHighlightColor(), highlightedObjective());
    }

    @Override
    public void createSmallDescription(
            TooltipMakerAPI info, float width, float height) {
        info.setParaInsigniaLarge();
        if (GanEdenQuestManager.getStage()
                == Stage.INHERITANCE_RECOVERED) {
            info.addPara(ShipTrophyL10n.get("quest_intro_inheritance"), 0f);
        } else {
            info.addPara(ShipTrophyL10n.get("quest_intro_revealed"), 0f);
        }
        info.addSpacer(10f);
        info.addSectionHeading(ShipTrophyL10n.get("quest_current_objective"),
                com.fs.starfarer.api.ui.Alignment.MID, 0f);
        info.addPara(shortObjective(), 10f, Misc.getTextColor(),
                Misc.getHighlightColor(), highlightedObjective());

        if (GanEdenQuestManager.isCompleted()) {
            info.addSpacer(10f);
            info.addPara(ShipTrophyL10n.get("quest_summary_complete"), 0f);
        }
    }

    private String shortObjective() {
        switch (GanEdenQuestManager.getStage()) {
            case INHERITANCE_RECOVERED:
                return ShipTrophyL10n.get("quest_objective_inheritance");
            case ASK_AROUND_SHATTERED_RING:
            case FIND_BLACK_MARKET_CLUE:
            case INVESTIGATE_HYPERSHUNTS:
                return ShipTrophyL10n.get("quest_objective_hypershunts");
            case GAN_EDEN_REVEALED:
                return ShipTrophyL10n.get("quest_objective_gan_eden");
            case DEFEAT_GOLDEN_SHARDS:
                return ShipTrophyL10n.get("quest_objective_bosses");
            case SPACE_ELEVATOR:
                return ShipTrophyL10n.get("quest_objective_elevator");
            case COMPLETED:
                return ShipTrophyL10n.get("quest_objective_complete");
            default:
                return ShipTrophyL10n.get("quest_objective_home");
        }
    }

    private String highlightedObjective() {
        switch (GanEdenQuestManager.getStage()) {
            case INHERITANCE_RECOVERED:
                return ShipTrophyL10n.get("quest_highlight_workshop");
            case ASK_AROUND_SHATTERED_RING:
            case FIND_BLACK_MARKET_CLUE:
            case INVESTIGATE_HYPERSHUNTS:
                return ShipTrophyL10n.get("quest_highlight_hypershunts");
            case GAN_EDEN_REVEALED:
                return ShipTrophyL10n.get("quest_highlight_isaac");
            case DEFEAT_GOLDEN_SHARDS:
                return ShipTrophyL10n.get("quest_highlight_bosses");
            case SPACE_ELEVATOR:
                return ShipTrophyL10n.get("quest_highlight_isaac");
            case COMPLETED:
                return ShipTrophyL10n.get("quest_highlight_complete");
            default:
                return ShipTrophyL10n.get("quest_highlight_ring");
        }
    }

    @Override
    public String getIcon() {
        String key = GanEdenQuestManager.isAtLeast(Stage.GAN_EDEN_REVEALED)
                ? "gate_active" : "gate_inactive";
        return Global.getSettings().getSpriteName("intel", key);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_STORY);
        tags.add(Tags.INTEL_ACCEPTED);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        Stage stage = GanEdenQuestManager.getStage();
        if (stage == Stage.INHERITANCE_RECOVERED) {
            return GanEdenQuestManager.getShatteredRing();
        }
        if (stage == Stage.ASK_AROUND_SHATTERED_RING
                || stage == Stage.FIND_BLACK_MARKET_CLUE
                || stage == Stage.INVESTIGATE_HYPERSHUNTS) {
            return GanEdenHypershuntManager.getFirstUnsurveyedHypershunt();
        }
        if (stage == Stage.SPACE_ELEVATOR) {
            if (GanEdenGenerator.findSystem() != null) {
                return GanEdenGenerator.findSystem().getEntityById(
                        GanEdenGenerator.SPACE_ELEVATOR_ID);
            }
        }
        if (stage.ordinal() >= Stage.GAN_EDEN_REVEALED.ordinal()) {
            SectorEntityToken gate = GanEdenQuestManager.getExternalRing();
            if (gate != null) return gate;
        }
        return GanEdenQuestManager.getShatteredRing();
    }
}
