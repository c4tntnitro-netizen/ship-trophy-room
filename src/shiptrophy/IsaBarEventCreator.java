package shiptrophy;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class IsaBarEventCreator extends BaseBarEventCreator {
    public static final String ID = "ship_trophy_isa";

    @Override
    public PortsideBarEvent createBarEvent() {
        return new IsaBarEvent();
    }

    @Override
    public String getBarEventId() {
        return ID;
    }

    @Override
    public float getBarEventFrequencyWeight() {
        return 100f;
    }

    @Override
    public boolean isPriority() {
        return true;
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        return 1f;
    }

    @Override
    public float getBarEventTimeoutDuration() {
        return 1f;
    }
}
