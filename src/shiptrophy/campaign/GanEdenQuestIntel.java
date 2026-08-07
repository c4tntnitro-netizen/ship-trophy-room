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

import shiptrophy.campaign.GanEdenQuestManager.Stage;

/** Dynamic intel entry for Isa's search for Isaac Leicester. */
public final class GanEdenQuestIntel extends BaseIntelPlugin {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getName() {
        return GanEdenQuestManager.isCompleted()
                ? "A Borrowed Name — Complete"
                : "A Borrowed Name";
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
            info.addPara("Quest completed: %s", initPad, tc,
                    Misc.getPositiveHighlightColor(), "A Borrowed Name");
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
            info.addPara(
                    "Isa Leicester was recovered from a cryopod as an infant, "
                            + "swaddled in a spacer suit bearing the name "
                            + "Isaac Leicester. The Shattered Ring has "
                            + "returned the preserved suit to her, and she "
                            + "has taken it to her old workshop for study.",
                    0f);
        } else {
            info.addPara(
                    "Isa Leicester was recovered from a cryopod as an infant, "
                            + "swaddled in a spacer suit bearing the name "
                            + "Isaac Leicester. A concealed identification "
                            + "wafer in the suit recovered Isaac Thomas "
                            + "Leicester's first personal log and linked him "
                            + "to both surviving Coronal Hypershunts.",
                    0f);
        }
        info.addSpacer(10f);
        info.addSectionHeading("Current objective",
                com.fs.starfarer.api.ui.Alignment.MID, 0f);
        info.addPara(shortObjective(), 10f, Misc.getTextColor(),
                Misc.getHighlightColor(), highlightedObjective());

        if (GanEdenQuestManager.isCompleted()) {
            info.addSpacer(10f);
            info.addPara(
                    "Five scattered records reconstructed Isaac Thomas "
                            + "Leicester's logs. Isa recovered the final "
                            + "entry from Gan Eden's Space Elevator after "
                            + "Cherubim and Lahat Haharev were destroyed—"
                            + "though ordinary Omega Shards and Facets have "
                            + "since begun appearing among some Remnant "
                            + "Ordos.",
                    0f);
        }
    }

    private String shortObjective() {
        switch (GanEdenQuestManager.getStage()) {
            case INHERITANCE_RECOVERED:
                return "Investigate Isa's inherited spacer suit in her old "
                        + "workshop at the Shattered Ring.";
            case ASK_AROUND_SHATTERED_RING:
            case FIND_BLACK_MARKET_CLUE:
            case INVESTIGATE_HYPERSHUNTS:
                return "Search the Coronal Hypershunts for clues about "
                        + "Isaac Leicester.";
            case GAN_EDEN_REVEALED:
                return "Travel through Power Transit Gate - Gan Eden and "
                        + "find Isaac Leicester.";
            case DEFEAT_GOLDEN_SHARDS:
                return "Defeat Cherubim and Lahat Haharev, the golden Omega "
                        + "Shards sealing the Space Elevator.";
            case SPACE_ELEVATOR:
                return "Search the Gan Eden Space Elevator and find Isaac "
                        + "Leicester.";
            case COMPLETED:
                return "Isa's search for Isaac Leicester is complete.";
            default:
                return "Bring Isa home to the Shattered Ring.";
        }
    }

    private String highlightedObjective() {
        switch (GanEdenQuestManager.getStage()) {
            case INHERITANCE_RECOVERED:
                return "old workshop";
            case ASK_AROUND_SHATTERED_RING:
            case FIND_BLACK_MARKET_CLUE:
            case INVESTIGATE_HYPERSHUNTS:
                return "Coronal Hypershunts";
            case GAN_EDEN_REVEALED:
                return "Isaac Leicester";
            case DEFEAT_GOLDEN_SHARDS:
                return "Cherubim and Lahat Haharev";
            case SPACE_ELEVATOR:
                return "Isaac Leicester";
            case COMPLETED:
                return "complete";
            default:
                return "Shattered Ring";
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
