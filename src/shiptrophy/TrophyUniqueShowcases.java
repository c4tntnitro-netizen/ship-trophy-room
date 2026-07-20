package shiptrophy;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;

public class TrophyUniqueShowcases {
    private static final ShowcaseSpec[] SPECS = new ShowcaseSpec[] {
            new ShowcaseSpec("kh_invictus", "Abundant Mercy", "Abundant Mercy", "invictus_kh", "ship_trophy_abundant_mercy", "knights_hospitallar", true),
            new ShowcaseSpec("black_lion", "The Black Lion", "The Black Lion", "executor_2", "ship_trophy_black_lion", "black_lion_ships", true)
    };

    public static List<ShowcaseSpec> getAllShowcases() {
        List<ShowcaseSpec> result = new ArrayList<ShowcaseSpec>();
        for (ShowcaseSpec spec : SPECS) {
            result.add(spec);
        }
        return result;
    }

    public static List<ShowcaseSpec> getActiveShowcases() {
        List<ShowcaseSpec> result = new ArrayList<ShowcaseSpec>();
        for (ShowcaseSpec spec : SPECS) {
            if (spec.isActive()) result.add(spec);
        }
        return result;
    }

    public static boolean isOptionalUniqueHull(String hullId) {
        if (hullId == null) return false;
        for (ShowcaseSpec spec : SPECS) {
            if (spec.isActive() && spec.hullId.equalsIgnoreCase(hullId)) return true;
        }
        return false;
    }

    public static class ShowcaseSpec {
        public final String id;
        public final String displayName;
        public final String showcaseName;
        public final String hullId;
        public final String hullModId;
        public final String requiredModId;
        public final boolean featuredByDefault;

        public ShowcaseSpec(String id, String displayName, String showcaseName, String hullId, String hullModId,
                String requiredModId, boolean featuredByDefault) {
            this.id = id;
            this.displayName = displayName;
            this.showcaseName = showcaseName;
            this.hullId = hullId;
            this.hullModId = hullModId;
            this.requiredModId = requiredModId;
            this.featuredByDefault = featuredByDefault;
        }

        public boolean isModIntegration() {
            return requiredModId != null && requiredModId.length() > 0;
        }

        public boolean isActive() {
            return featuredByDefault || requiredModId == null || requiredModId.length() <= 0
                    || (Global.getSettings() != null
                    && Global.getSettings().getModManager() != null
                    && Global.getSettings().getModManager().isModEnabled(requiredModId));
        }
    }
}