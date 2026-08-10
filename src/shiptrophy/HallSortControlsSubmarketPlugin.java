package shiptrophy;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;

/** Hidden legacy shell retained only so saves from the test build can load. */
public final class HallSortControlsSubmarketPlugin extends BaseSubmarketPlugin {
    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        return false;
    }

    @Override
    public boolean showInFleetScreen() {
        return false;
    }

    @Override
    public boolean showInCargoScreen() {
        return false;
    }
}
