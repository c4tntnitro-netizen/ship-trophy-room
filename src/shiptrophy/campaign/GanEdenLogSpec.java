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

/** Metadata and source text for the recoverable Gan Eden archives. */
public final class GanEdenLogSpec {
    public static final String ARCHIVE_TAG = "Gan Eden Archives";

    public static final GanEdenLogSpec PART_ONE = new GanEdenLogSpec(
            "part_one", "Personal Log 1765", ShatteredRingGenerator.ENTITY_ID,
            "The Shattered Ring", "shipTrophyGanEdenEpitaphOne");
    public static final GanEdenLogSpec PART_TWO = new GanEdenLogSpec(
            "part_two", "Log — Part II", null,
            "the first Coronal Hypershunt", "shipTrophyGanEdenEpitaphTwo");
    public static final GanEdenLogSpec PART_THREE = new GanEdenLogSpec(
            "part_three", "Log — Part III", null,
            "the second Coronal Hypershunt", "shipTrophyGanEdenEpitaphThree");
    public static final GanEdenLogSpec PART_FOUR = new GanEdenLogSpec(
            "part_four", "Log — Part IV", GanEdenGenerator.TREE_OF_LIFE_ID,
            "Tree of Life", "shipTrophyGanEdenEpitaphFour");
    public static final GanEdenLogSpec FINAL = new GanEdenLogSpec(
            "final", "Log — Final", GanEdenGenerator.SPACE_ELEVATOR_ID,
            "the Gan Eden Space Elevator", "shipTrophyGanEdenEpitaphFive");

    private static final List<GanEdenLogSpec> ORDERED =
            Collections.unmodifiableList(Arrays.asList(
                    PART_ONE, PART_TWO, PART_THREE, PART_FOUR, FINAL));
    private static final Map<String, String> BODY_BY_RULE_ID =
            new LinkedHashMap<String, String>();
    private static boolean bodiesLoaded;

    private final String id;
    private final String title;
    private final String siteId;
    private final String siteName;
    private final String ruleId;

    private GanEdenLogSpec(
            String id,
            String title,
            String siteId,
            String siteName,
            String ruleId) {
        this.id = id;
        this.title = title;
        this.siteId = siteId;
        this.siteName = siteName;
        this.ruleId = ruleId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getBody() {
        ensureBodiesLoaded();
        String body = BODY_BY_RULE_ID.get(ruleId);
        if (body == null || body.trim().isEmpty()) {
            return "[Archive data unavailable: " + title + "]";
        }
        return body;
    }

    public static List<GanEdenLogSpec> ordered() {
        return ORDERED;
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
