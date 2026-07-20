package shiptrophy;

import java.util.Collection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import org.json.JSONObject;

public class TrophySubtypeSpec {
    public final String id;
    public final String displayName;
    public final String showcaseName;
    public final String hullModId;
    public final String requiredModId;
    public final float unlockDp;
    public final String installStyle;

    private final String[] hullIdContains;
    private final String[] baseHullIdContains;
    private final String[] manufacturerContains;
    private final String[] hullNameContains;
    private final String[] hullTagMatches;
    private final String[] variantTagMatches;

    public TrophySubtypeSpec(String id, String displayName, String showcaseName, String hullModId,
            String requiredModId, float unlockDp, String installStyle, String hullIdContains,
            String baseHullIdContains, String manufacturerContains, String hullNameContains,
            String hullTagMatches, String variantTagMatches) {
        this.id = cleanId(id);
        this.displayName = emptyToDefault(displayName, this.id);
        this.showcaseName = emptyToDefault(showcaseName, this.displayName);
        this.hullModId = safe(hullModId);
        this.requiredModId = safe(requiredModId);
        this.unlockDp = unlockDp <= 0f ? TrophyNetwork.DOCTRINE_UNLOCK_DP : unlockDp;
        this.installStyle = emptyToDefault(installStyle, "any");
        this.hullIdContains = split(hullIdContains);
        this.baseHullIdContains = split(baseHullIdContains);
        this.manufacturerContains = split(manufacturerContains);
        this.hullNameContains = split(hullNameContains);
        this.hullTagMatches = split(hullTagMatches);
        this.variantTagMatches = split(variantTagMatches);
    }

    public static TrophySubtypeSpec fromCsv(JSONObject row) {
        return new TrophySubtypeSpec(
                row.optString("id"),
                row.optString("displayName"),
                row.optString("showcaseName"),
                row.optString("hullModId"),
                row.optString("requiredModId"),
                (float) row.optDouble("unlockDp", TrophyNetwork.DOCTRINE_UNLOCK_DP),
                row.optString("installStyle"),
                row.optString("hullIdContains"),
                row.optString("baseHullIdContains"),
                row.optString("manufacturerContains"),
                row.optString("hullNameContains"),
                row.optString("hullTagMatches"),
                row.optString("variantTagMatches"));
    }

    public boolean isModIntegration() {
        return requiredModId.length() > 0;
    }

    public boolean isActive() {
        if (requiredModId.length() <= 0) return true;
        return Global.getSettings() != null
                && Global.getSettings().getModManager() != null
                && Global.getSettings().getModManager().isModEnabled(requiredModId);
    }

    public boolean hasHullModUnlock() {
        return hullModId.length() > 0;
    }

    public boolean matches(FleetMemberAPI member) {
        if (!isActive() || member == null || member.getHullSpec() == null) return false;

        ShipHullSpecAPI spec = member.getHullSpec();
        ShipVariantAPI variant = member.getVariant();
        String hullId = lower(member.getHullId());
        String baseHullId = lower(TrophyNetwork.getBaseHullId(member));
        String manufacturer = lower(spec.getManufacturer());
        String hullName = lower(spec.getHullNameWithDashClass());

        boolean hasRules = hasAnyRules();
        if (!hasRules) return false;

        if (containsAny(hullId, hullIdContains)) return true;
        if (containsAny(baseHullId, baseHullIdContains)) return true;
        if (containsAny(manufacturer, manufacturerContains)) return true;
        if (containsAny(hullName, hullNameContains)) return true;
        if (hasAnyTag(spec.getTags(), hullTagMatches)) return true;
        return variant != null && hasAnyTag(variant.getTags(), variantTagMatches);
    }

    public boolean matchesInstallStyle(ShipAPI ship) {
        if (ship == null || "any".equalsIgnoreCase(installStyle)) return true;
        String style = lower(ship.getHullStyleId());
        if ("low-tech".equalsIgnoreCase(installStyle)) return "low_tech".equals(style) || "low-tech".equals(style);
        if ("midline".equalsIgnoreCase(installStyle)) return "midline".equals(style);
        if ("high-tech".equalsIgnoreCase(installStyle)) return "high_tech".equals(style) || "high-tech".equals(style);
        return true;
    }

    private boolean hasAnyRules() {
        return hullIdContains.length > 0
                || baseHullIdContains.length > 0
                || manufacturerContains.length > 0
                || hullNameContains.length > 0
                || hullTagMatches.length > 0
                || variantTagMatches.length > 0;
    }

    private static boolean containsAny(String text, String[] parts) {
        if (text == null || parts == null) return false;
        for (String part : parts) {
            if (part.length() > 0 && text.contains(part)) return true;
        }
        return false;
    }

    private static boolean hasAnyTag(Collection<String> tags, String[] wanted) {
        if (tags == null || wanted == null) return false;
        for (String tag : wanted) {
            if (tag.length() > 0 && tags.contains(tag)) return true;
        }
        return false;
    }

    private static String[] split(String value) {
        value = safe(value).trim().toLowerCase();
        if (value.length() <= 0) return new String[0];
        String[] raw = value.split("[|;]");
        int count = 0;
        for (int i = 0; i < raw.length; i++) {
            raw[i] = raw[i].trim();
            if (raw[i].length() > 0) count++;
        }
        String[] result = new String[count];
        int index = 0;
        for (String part : raw) {
            if (part.length() > 0) result[index++] = part;
        }
        return result;
    }

    private static String cleanId(String value) {
        return safe(value).trim().toLowerCase();
    }

    private static String emptyToDefault(String value, String fallback) {
        value = safe(value).trim();
        return value.length() <= 0 ? fallback : value;
    }

    private static String lower(String value) {
        return safe(value).toLowerCase();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
