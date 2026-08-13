package shiptrophy.campaign;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

import shiptrophy.ShipTrophyL10n;

/** Metadata and source text for the recoverable Gan Eden archives. */
public final class GanEdenLogSpec {
    public static final GanEdenLogSpec PART_ONE = new GanEdenLogSpec(
            "part_one", "archive_title_part_one", ShatteredRingGenerator.ENTITY_ID,
            "archive_site_shattered_ring", "shipTrophyGanEdenEpitaphOne");
    public static final GanEdenLogSpec PART_TWO = new GanEdenLogSpec(
            "part_two", "archive_title_part_two", null,
            "archive_site_first_hypershunt", "shipTrophyGanEdenEpitaphTwo");
    public static final GanEdenLogSpec PART_THREE = new GanEdenLogSpec(
            "part_three", "archive_title_part_three", null,
            "archive_site_second_hypershunt", "shipTrophyGanEdenEpitaphThree");
    public static final GanEdenLogSpec PART_FOUR = new GanEdenLogSpec(
            "part_four", "archive_title_part_four", GanEdenGenerator.TREE_OF_LIFE_ID,
            "archive_site_tree_of_life", "shipTrophyGanEdenEpitaphFour");
    public static final GanEdenLogSpec FINAL = new GanEdenLogSpec(
            "final", "archive_title_final", GanEdenGenerator.SPACE_ELEVATOR_ID,
            "archive_site_space_elevator", "shipTrophyGanEdenEpitaphFive");

    private static final List<GanEdenLogSpec> ORDERED =
            Collections.unmodifiableList(Arrays.asList(
                    PART_ONE, PART_TWO, PART_THREE, PART_FOUR, FINAL));
    private static final Map<String, String> BODY_BY_RULE_ID =
            new LinkedHashMap<String, String>();
    private static boolean bodiesLoaded;

    private final String id;
    private final String titleKey;
    private final String siteId;
    private final String siteNameKey;
    private final String ruleId;

    private GanEdenLogSpec(
            String id,
            String titleKey,
            String siteId,
            String siteNameKey,
            String ruleId) {
        this.id = id;
        this.titleKey = titleKey;
        this.siteId = siteId;
        this.siteNameKey = siteNameKey;
        this.ruleId = ruleId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return ShipTrophyL10n.get(titleKey);
    }

    public String getSiteId() {
        return siteId;
    }

    public String getSiteName() {
        return ShipTrophyL10n.get(siteNameKey);
    }

    public String getBody() {
        ensureBodiesLoaded();
        String body = BODY_BY_RULE_ID.get(ruleId);
        if (body == null || body.trim().isEmpty()) {
            return ShipTrophyL10n.format("archive_data_unavailable", getTitle());
        }
        return body;
    }

    public static List<GanEdenLogSpec> ordered() {
        return ORDERED;
    }

    public static String getArchiveTag() {
        return ShipTrophyL10n.get("archive_tag");
    }

    public static GanEdenLogSpec forId(String id) {
        if (id == null) return null;
        for (GanEdenLogSpec spec : ORDERED) {
            if (id.equals(spec.id)) return spec;
        }
        return null;
    }

    public static GanEdenLogSpec nextUnrecoveredAtSite(String siteId) {
        if (siteId == null) return null;
        for (GanEdenLogSpec spec : ORDERED) {
            if (siteId.equals(spec.siteId)
                    && !GanEdenLogManager.isRecovered(spec)) {
                return spec;
            }
        }
        return null;
    }

    private static synchronized void ensureBodiesLoaded() {
        if (bodiesLoaded) return;
        bodiesLoaded = true;
        try {
            JSONArray rows = Global.getSettings().loadCSV(
                    "data/campaign/rules.csv", "ship_trophy_room");
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String id = row.optString("id", "").trim();
                if (id.length() <= 0) continue;
                for (GanEdenLogSpec spec : ORDERED) {
                    if (!spec.ruleId.equals(id)) continue;
                    String text = row.optString("text", "").trim();
                    if (text.length() > 0) {
                        BODY_BY_RULE_ID.put(spec.ruleId, text);
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(
                    "Hall of Triumph: unable to load Gan Eden rules.csv text.");
            ex.printStackTrace(System.err);
        } catch (JSONException ex) {
            System.err.println(
                    "Hall of Triumph: unable to parse Gan Eden rules.csv text.");
            ex.printStackTrace(System.err);
        }
    }

}
