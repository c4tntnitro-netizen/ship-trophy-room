package shiptrophy;

public enum TrophyDoctrine {
    XIV("xiv", "ship_trophy_xiv_legacy", "XIV Battlegroup", "XIV Battlegroup", "low-tech"),
    LP("lp", "ship_trophy_lp_zeal", "Luddic Path", "Luddic Path", "low-tech"),
    LG("lg", "ship_trophy_lg_pageantry", "Lion's Guard", "Lion's Guard", "midline"),
    TT("tt", "ship_trophy_tt_optimization", "Tri-Tachyon", "Tri-Tachyon", "high-tech");

    public final String id;
    public final String hullModId;
    public final String displayName;
    public final String showcaseName;
    public final String installStyle;

    TrophyDoctrine(String id, String hullModId, String displayName, String showcaseName, String installStyle) {
        this.id = id;
        this.hullModId = hullModId;
        this.displayName = displayName;
        this.showcaseName = showcaseName;
        this.installStyle = installStyle;
    }
}
