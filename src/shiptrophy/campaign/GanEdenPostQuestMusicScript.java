package shiptrophy.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;

/** Cycles Gan Eden's three post-quest themes on return visits. */
public final class GanEdenPostQuestMusicScript implements EveryFrameScript {
    private static final String UNLOCKED_KEY =
            "$shipTrophyGanEdenPostQuestMusicUnlocked";
    private static final String[] MUSIC = new String[] {
        "ship_trophy_gan_eden_music",
        "ship_trophy_isa_final_log_intro",
        "ship_trophy_isa_final_log_loop",
        "ship_trophy_time_leads_to_the_end"
    };
    private static final long[] DURATION_NANOS = new long[] {
        seconds(189.443813d),
        seconds(167.433250d),
        seconds(167.433250d),
        seconds(134.747062d)
    };

    private static boolean active;
    private static boolean suspendedForCombat;
    private static int phase;
    private static long phaseStartedAt;

    public static void unlockAfterFinalDeparture() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                UNLOCKED_KEY, true);
    }

    public static void resetForGameLoad() {
        active = false;
        suspendedForCombat = false;
        phase = 0;
        phaseStartedAt = 0L;
    }

    /** Lets combat temporarily own the process-wide music channel. */
    public static void suspendForCombat() {
        if (!active || suspendedForCombat) return;
        suspendedForCombat = true;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null) return;

        boolean eligible = isUnlocked()
                && GanEdenQuestManager.isCompleted()
                && !GanEdenFinalLogMusicScript.isActive();
        if (!eligible || !isPlayerInGanEden()) {
            if (active) stop();
            return;
        }

        // Do this before start(): on an immediate or repeat encounter the
        // campaign playlist may not have acquired the channel yet, so there
        // is no active instance for suspendForCombat() to mark. It must still
        // decline ownership until the battle controller releases it.
        if (GanEdenBattleCreationPlugin.isGoldenOmegaMusicActive()) {
            if (active) suspendedForCombat = true;
            return;
        }

        if (!active) {
            start();
            return;
        }
        if (suspendedForCombat) {
            suspendedForCombat = false;
            playPhase((phase + 1) % MUSIC.length);
            return;
        }
        if (System.nanoTime() - phaseStartedAt
                >= DURATION_NANOS[phase]) {
            playPhase((phase + 1) % MUSIC.length);
        }
    }

    private static void start() {
        active = true;
        suspendedForCombat = false;
        playPhase(0);
    }

    private static void playPhase(int nextPhase) {
        phase = nextPhase;
        phaseStartedAt = System.nanoTime();
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
            Global.getSoundPlayer().pauseMusic();
            Global.getSoundPlayer().playCustomMusic(
                    0, 0, MUSIC[phase], false);
        } catch (RuntimeException ex) {
            System.err.println(
                    "Hall of Triumph: failed to play Gan Eden post-quest "
                            + "music phase " + phase + ".");
            ex.printStackTrace(System.err);
            stop();
        }
    }

    private static void stop() {
        if (!active) return;
        active = false;
        suspendedForCombat = false;
        phaseStartedAt = 0L;
        try {
            Global.getSoundPlayer().pauseCustomMusic();
        } catch (RuntimeException ex) {
            logResetFailure("stop the Gan Eden return playlist", ex);
        }
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
        } catch (RuntimeException ex) {
            logResetFailure("release the Gan Eden playlist suspension", ex);
        }
        try {
            Global.getSoundPlayer().restartCurrentMusic();
        } catch (RuntimeException ex) {
            logResetFailure("restart normal music after leaving Gan Eden", ex);
        }
    }

    private static boolean isUnlocked() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate()
                        .getBoolean(UNLOCKED_KEY);
    }

    private static boolean isPlayerInGanEden() {
        if (Global.getSector() == null) return false;
        if (GanEdenGenerator.isGanEden(
                Global.getSector().getCurrentLocation())) return true;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        return GanEdenGenerator.isGanEden(player);
    }

    private static long seconds(double value) {
        return (long) (value * 1_000_000_000d);
    }

    private static void logResetFailure(
            String operation, RuntimeException ex) {
        System.err.println(
                "Hall of Triumph: failed to " + operation + ".");
        ex.printStackTrace(System.err);
    }
}
