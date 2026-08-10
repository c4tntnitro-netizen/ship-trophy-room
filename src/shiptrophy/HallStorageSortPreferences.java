package shiptrophy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

/** Base-mod storage sorting preferences, persisted in the campaign save. */
public final class HallStorageSortPreferences {
    public enum SortKey {
        HULL_NAME("Hull Name"),
        FACTION("Faction"),
        SIZE("Size"),
        DP_COST("DP Cost");

        private final String label;

        SortKey(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static SortKey fromLabel(String value, SortKey fallback) {
            if (value != null) {
                for (SortKey key : values()) {
                    if (key.label.equalsIgnoreCase(value.trim())) return key;
                }
            }
            return fallback;
        }
    }

    private HallStorageSortPreferences() {
    }

    public static SortKey getPrimary() {
        return getSaved(ShipTrophyRoomIds.MEMORY_STORAGE_PRIMARY_SORT,
                HallOfTriumphFeatures.getPrimaryStorageSort(), SortKey.HULL_NAME);
    }

    public static SortKey getSecondary() {
        return getSaved(ShipTrophyRoomIds.MEMORY_STORAGE_SECONDARY_SORT,
                HallOfTriumphFeatures.getSecondaryStorageSort(), SortKey.FACTION);
    }

    public static void setPrimary(SortKey key) {
        setSaved(ShipTrophyRoomIds.MEMORY_STORAGE_PRIMARY_SORT, key);
    }

    public static void setSecondary(SortKey key) {
        setSaved(ShipTrophyRoomIds.MEMORY_STORAGE_SECONDARY_SORT, key);
    }

    private static SortKey getSaved(String memoryKey, String configuredDefault,
                                    SortKey fallback) {
        try {
            MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
            if (memory.contains(memoryKey)) {
                return SortKey.fromLabel(memory.getString(memoryKey), fallback);
            }
        } catch (Throwable ignored) {
        }
        return SortKey.fromLabel(configuredDefault, fallback);
    }

    private static void setSaved(String memoryKey, SortKey key) {
        if (key == null || Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(memoryKey, key.getLabel());
    }
}
