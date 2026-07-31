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
                ? "A Name on a Suit — Complete"
                : "A Name on a Suit";
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
                        + "Leicester. A damaged transit record at the "
                        + "Shattered Ring may be the first real clue to her "
                        + "family.",
                0f);
        info.addSpacer(10f);
        info.addSectionHeading("Current objective",
                com.fs.starfarer.api.ui.Alignment.MID, 0f);
        info.addPara(shortObjective(), 10f, Misc.getTextColor(),
                Misc.getHighlightColor(), highlightedObjective());

        if (GanEdenQuestManager.getStage()
                == Stage.REACTIVATE_HYPERSHUNTS) {
            int repaired = GanEdenQuestManager.getRepairedHypershuntCount();
            info.addPara("Coronal Hypershunts reactivated: %s / %s", 10f,
                    Misc.getGrayColor(), Misc.getHighlightColor(),
                    Integer.toString(repaired), "2");
        }
        if (GanEdenQuestManager.isCompleted()) {
            info.addSpacer(10f);
            info.addPara(
                    "Isa found Isaac Leicester's grave in Gan Eden. The "
                            + "golden Omega guardians called Cherubim and "
                            + "Lahat Haharev were destroyed, ending the "
                            + "search—though aureate echoes may still appear "
                            + "among Remnant Ordos.",
                    0f);
        }
    }

    private String shortObjective() {
        switch (GanEdenQuestManager.getStage()) {
            case CONTACT_GARGOYLE:
                return "Return to the Shattered Ring and recruit Gargoyle "
                        + "to recover its damaged transit logs.";
            case REACTIVATE_HYPERSHUNTS:
                return "Reactivate both Coronal Hypershunts to triangulate "
                        + "the lost ring's coordinates.";
            case GAN_EDEN_REVEALED:
                return "Travel to the revealed Gan Eden Transit Ring and "
                        + "search Eden Prime with Isa.";
            case GRAVE_FOUND:
                return "Defeat Cherubim and Lahat Haharev, the golden Omega "
                        + "Shards guarding Gan Eden.";
            case COMPLETED:
                return "Isa's search for Isaac Leicester is complete.";
            default:
                return "Bring Isa home to the Shattered Ring.";
        }
    }

    private String highlightedObjective() {
        switch (GanEdenQuestManager.getStage()) {
            case CONTACT_GARGOYLE:
                return "Shattered Ring";
            case REACTIVATE_HYPERSHUNTS:
                return "both Coronal Hypershunts";
            case GAN_EDEN_REVEALED:
                return "Gan Eden Transit Ring";
            case GRAVE_FOUND:
                return "Cherubim and Lahat Haharev";
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
        if (stage == Stage.REACTIVATE_HYPERSHUNTS) {
            return GanEdenQuestManager.getFirstUnrepairedHypershunt();
        }
        if (stage.ordinal() >= Stage.GAN_EDEN_REVEALED.ordinal()) {
            SectorEntityToken ring = GanEdenQuestManager.getExternalRing();
            if (ring != null) return ring;
        }
        return GanEdenQuestManager.getShatteredRing();
    }
}
