package shiptrophy.campaign;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.fs.starfarer.api.Global;

/** Metadata and source text for the recoverable Gan Eden archives. */
public final class GanEdenLogSpec {
    public static final String ARCHIVE_TAG = "Gan Eden Archives";

    public static final GanEdenLogSpec PART_ONE = new GanEdenLogSpec(
            "part_one", "Personal Log 1765", ShatteredRingGenerator.ENTITY_ID,
            "The Shattered Ring", "one");
    public static final GanEdenLogSpec PART_TWO = new GanEdenLogSpec(
            "part_two", "Epitaph — Part II", null,
            "the first Coronal Hypershunt", "two");
    public static final GanEdenLogSpec PART_THREE = new GanEdenLogSpec(
            "part_three", "Epitaph — Part III", null,
            "the second Coronal Hypershunt", "three");
    public static final GanEdenLogSpec PART_FOUR = new GanEdenLogSpec(
            "part_four", "Epitaph — Part IV", GanEdenGenerator.TREE_OF_LIFE_ID,
            "Tree of Life", "four");
    public static final GanEdenLogSpec FINAL = new GanEdenLogSpec(
            "final", "Epitaph — Final", GanEdenGenerator.SPACE_ELEVATOR_ID,
            "the Gan Eden Space Elevator", "five");

    private static final List<GanEdenLogSpec> ORDERED =
            Collections.unmodifiableList(Arrays.asList(
                    PART_ONE, PART_TWO, PART_THREE, PART_FOUR, FINAL));
    private static final Map<String, String> BODY_BY_SECTION =
            new LinkedHashMap<String, String>();
    private static boolean bodiesLoaded;

    private final String id;
    private final String title;
    private final String siteId;
    private final String siteName;
    private final String inkSection;

    private GanEdenLogSpec(
            String id,
            String title,
            String siteId,
            String siteName,
            String inkSection) {
        this.id = id;
        this.title = title;
        this.siteId = siteId;
        this.siteName = siteName;
        this.inkSection = inkSection;
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
        String body = BODY_BY_SECTION.get(inkSection);
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
            String ink = Global.getSettings().loadText(
                    "dialogue/Logs.ink", "ship_trophy_room");
            for (GanEdenLogSpec spec : ORDERED) {
                String marker = "=== " + spec.inkSection + " ===";
                int start = ink.indexOf(marker);
                if (start < 0) continue;
                start += marker.length();
                int end = ink.indexOf("-> END", start);
                if (end < 0) continue;
                BODY_BY_SECTION.put(
                        spec.inkSection, ink.substring(start, end).trim());
            }
        } catch (IOException ex) {
            System.err.println(
                    "Hall of Triumph: unable to load Gan Eden archive text.");
            ex.printStackTrace(System.err);
        } catch (JSONException ex) {
            System.err.println(
                    "Hall of Triumph: unable to parse Gan Eden archive text.");
            ex.printStackTrace(System.err);
        }
    }
}
