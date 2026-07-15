package shiptrophy;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class IsaTrophyScript implements EveryFrameScript {
    private final IntervalUtil interval = new IntervalUtil(0.5f, 1f);

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null) return;

        interval.advance(Global.getSector().getClock().convertToDays(amount));
        if (!interval.intervalElapsed()) return;

        MarketAPI home = IsaTrophyManager.findHomeMarket();
        if (home == null) return;

        IsaTrophyManager.ensureBarEventCreator();
        if (IsaTrophyManager.isIntroduced()) {
            IsaTrophyManager.ensureContact(home, null);
        }
        IsaTrophyManager.refreshIsaHullmod();
    }
}
