package shiptrophy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrophySubtypeRegistry {
    public static final String CONFIG_PATH = "data/config/ship_trophy_room/subtypes.csv";

    private static final Map<String, TrophySubtypeSpec> SUBTYPES = new LinkedHashMap<String, TrophySubtypeSpec>();
    private static boolean loaded = false;

    public static void reload() {
        loaded = false;
        SUBTYPES.clear();
        loadIfNeeded();
    }

    public static List<TrophySubtypeSpec> getAllSubtypes() {
        loadIfNeeded();
        return new ArrayList<TrophySubtypeSpec>(SUBTYPES.values());
    }

    public static List<TrophySubtypeSpec> getActiveSubtypes() {
        loadIfNeeded();
        List<TrophySubtypeSpec> active = new ArrayList<TrophySubtypeSpec>();
        for (TrophySubtypeSpec spec : SUBTYPES.values()) {
            if (spec.isActive()) active.add(spec);
        }
        return active;
    }

    public static TrophySubtypeSpec getSubtype(String id) {
        loadIfNeeded();
        if (id == null) return null;
        return SUBTYPES.get(id.toLowerCase());
    }

    public static TrophySubtypeSpec getSubtype(TrophyDoctrine doctrine) {
        return doctrine == null ? null : getSubtype(doctrine.id);
    }

    public static TrophySubtypeSpec getSubtypeForHullMod(String hullModId) {
        loadIfNeeded();
        if (hullModId == null || hullModId.length() <= 0) return null;
        for (TrophySubtypeSpec spec : SUBTYPES.values()) {
            if (hullModId.equals(spec.hullModId)) return spec;
        }
        return null;
    }

    private static void loadIfNeeded() {
        if (loaded) return;
        loaded = true;
        SUBTYPES.clear();

        try {
            JSONArray rows = Global.getSettings().getMergedSpreadsheetData("id", CONFIG_PATH);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String id = row.optString("id", "").trim();
                if (id.length() <= 0 || id.startsWith("#")) continue;
                TrophySubtypeSpec spec = TrophySubtypeSpec.fromCsv(row);
                if (spec.id.length() > 0) SUBTYPES.put(spec.id, spec);
            }
        } catch (IOException ex) {
            addFallbackSubtypes();
        } catch (JSONException ex) {
            addFallbackSubtypes();
        }

        if (SUBTYPES.isEmpty()) addFallbackSubtypes();
    }

    private static void addFallbackSubtypes() {
        add(new TrophySubtypeSpec("xiv", "XIV Battlegroup", "XIV Battlegroup", "ship_trophy_xiv_legacy",
                "", 60f, "low-tech", "_xiv|xiv_", "", "xiv|14th|fourteenth", "(xiv)|xiv", "", ""));
        add(new TrophySubtypeSpec("lp", "Luddic Path", "Luddic Path", "ship_trophy_lp_zeal",
                "", 60f, "low-tech", "luddic_path|pather", "", "luddic path", "(lp)|luddic path", "", ""));
        add(new TrophySubtypeSpec("lg", "Lion's Guard", "Lion's Guard", "ship_trophy_lg_pageantry",
                "", 60f, "midline", "_lg|lg_|executor", "", "lion's guard|lions guard", "(lg)|lion|executor", "", ""));
        add(new TrophySubtypeSpec("tt", "Tri-Tachyon", "Tri-Tachyon", "ship_trophy_tt_optimization",
                "", 60f, "high-tech", "tritachyon|_tt|tt_", "", "tri-tachyon|tritachyon|high tech|high-tech", "(tt)|tri-tachyon", "", ""));
        add(new TrophySubtypeSpec("remnant", "Remnant", "Remnant", "ship_trophy_humanity",
                "", 60f, "any", "", "", "remnant", "", "", ""));
        add(new TrophySubtypeSpec("domain_derelict", "Domain Derelicts", "Explorarium", "ship_trophy_memory",
                "", 60f, "any", "", "", "explorarium|derelict", "", "", ""));
    }

    private static void add(TrophySubtypeSpec spec) {
        SUBTYPES.put(spec.id, spec);
    }
}
