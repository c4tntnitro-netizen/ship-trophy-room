package shiptrophy.campaign;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/** Permanent Intel archive for one recovered Gan Eden record. */
public final class GanEdenLogIntel extends BaseIntelPlugin {
    private static final long serialVersionUID = 1L;

    private final String logId;

    public GanEdenLogIntel(String logId) {
        this.logId = logId;
    }

    public String getLogId() {
        return logId;
    }

    private GanEdenLogSpec spec() {
        return GanEdenLogSpec.forId(logId);
    }

    @Override
    protected String getName() {
        GanEdenLogSpec spec = spec();
        return spec == null ? "Gan Eden Archive" : spec.getTitle();
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), getTitleColor(mode), 0f);
        GanEdenLogSpec spec = spec();
        if (spec != null) {
            info.addPara("Recovered at " + spec.getSiteName(), 3f,
                    Misc.getGrayColor(), Misc.getHighlightColor(),
                    spec.getSiteName());
        }
    }

    @Override
    protected void addBulletPoints(
            TooltipMakerAPI info,
            ListInfoMode mode,
            boolean isUpdate,
            Color tc,
            float initPad) {
        GanEdenLogSpec spec = spec();
        if (spec != null) {
            info.addPara("Recovered at " + spec.getSiteName(), initPad, tc,
                    Misc.getHighlightColor(), spec.getSiteName());
        }
    }

    @Override
    public void createSmallDescription(
            TooltipMakerAPI info, float width, float height) {
        GanEdenLogSpec spec = spec();
        if (spec == null) {
            info.addPara("The recovered archive index is unreadable.", 0f);
            return;
        }

        info.addPara("Recovered from %s and retained in the fleet archives.",
                0f, Misc.getGrayColor(), Misc.getHighlightColor(),
                spec.getSiteName());
        info.addSpacer(10f);
        info.setParaSmallInsignia();
        info.addPara(spec.getBody(), 0f);
        info.setParaFontDefault();
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(GanEdenLogSpec.ARCHIVE_TAG);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        GanEdenLogSpec spec = spec();
        if (spec == null || spec.getSiteId() == null) return null;
        if (ShatteredRingGenerator.ENTITY_ID.equals(spec.getSiteId())) {
            return GanEdenQuestManager.getShatteredRing();
        }
        if (GanEdenGenerator.findSystem() == null) return null;
        return GanEdenGenerator.findSystem().getEntityById(spec.getSiteId());
    }
}
