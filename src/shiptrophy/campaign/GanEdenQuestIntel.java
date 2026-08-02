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
        info.addPara(shortObjective(), initPad, tc,
                Misc.getHighlightColor(), highlightedObjective());
    }

    @Override
    public void createSmallDescription(
            TooltipMakerAPI info, float width, float height) {
        info.addPara(
                "Isa Leicester was recovered from a cryopod as an infant, "
                        + "swaddled in a spacer suit bearing the name Isaac "
                        + "Leicester. A concealed identification wafer in the "
                        + "suit recovered Isaac Thomas Leicester's first "
                        + "personal log and linked him to both surviving "
                        + "Coronal Hypershunts.",
                0f);
        info.addSpacer(10f);
        info.addSectionHeading("Current objective",
                com.fs.starfarer.api.ui.Alignment.MID, 0f);
        info.addPara(shortObjective(), 10f, Misc.getTextColor(),
                Misc.getHighlightColor(), highlightedObjective());

        if (GanEdenQuestManager.getStage()
                == Stage.INVESTIGATE_HYPERSHUNTS) {
            int surveyed = GanEdenHypershuntManager.getSurveyedCount();
            int required = GanEdenHypershuntManager.getRequiredCount();
            info.addPara("Hypershunt routing records recovered: %s / %s", 10f,
                    Misc.getGrayColor(), Misc.getHighlightColor(),
                    Integer.toString(surveyed), Integer.toString(required));
        }
        if (GanEdenQuestManager.isCompleted()) {
            info.addSpacer(10f);
            info.addPara(
                    "Five scattered records reconstructed Isaac Thomas "
                            + "Leicester's Epitaph. Isa recovered the final "
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
            case ASK_AROUND_SHATTERED_RING:
            case FIND_BLACK_MARKET_CLUE:
            case INVESTIGATE_HYPERSHUNTS:
                return "Recover the next two personal logs from both "
                        + "surviving Coronal Hypershunts.";
            case GAN_EDEN_REVEALED:
                return "Travel to POWER TRANSIT GATE - GAN EDEN on the "
                        + "northeastern rim of the Abyss, enter Gan Eden, and "
                        + "recover the fourth log at the Tree of Life.";
            case DEFEAT_GOLDEN_SHARDS:
                return "Defeat Cherubim and Lahat Haharev, the golden Omega "
                        + "Shards sealing the Space Elevator.";
            case SPACE_ELEVATOR:
                return "Approach the newly accessible Gan Eden Space Elevator "
                        + "with Isa and recover the final record.";
            case COMPLETED:
                return "Isa's search for Isaac Leicester is complete.";
            default:
                return "Bring Isa home to the Shattered Ring.";
        }
    }

    private String highlightedObjective() {
        switch (GanEdenQuestManager.getStage()) {
            case ASK_AROUND_SHATTERED_RING:
            case FIND_BLACK_MARKET_CLUE:
            case INVESTIGATE_HYPERSHUNTS:
                return "both surviving Coronal Hypershunts";
            case GAN_EDEN_REVEALED:
                return "POWER TRANSIT GATE - GAN EDEN";
            case DEFEAT_GOLDEN_SHARDS:
                return "Cherubim and Lahat Haharev";
            case SPACE_ELEVATOR:
                return "Gan Eden Space Elevator";
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
