package shiptrophy.campaign;

import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;

/** Releases authored Golden Omega music at the true campaign battle boundary. */
public final class GanEdenMusicBattleListener
        extends BaseCampaignEventListener {
    public GanEdenMusicBattleListener() {
        super(false);
    }

    @Override
    public void reportBattleFinished(
            CampaignFleetAPI primaryWinner,
            BattleAPI battle) {
        // restoreGoldenOmegaMusic() is session-gated, so unrelated battles
        // are harmless. Unlike an every-frame campaign script, this callback
        // cannot run in the startup window between battle construction and
        // the Golden music plugin's first combat frame.
        GanEdenBattleCreationPlugin.restoreGoldenOmegaMusic();
    }
}
