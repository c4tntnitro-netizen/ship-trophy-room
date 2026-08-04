package shiptrophy.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;

/**
 * Owns Isa's final-log music from the moment Log V opens until the player
 * leaves Gan Eden. Combat temporarily borrows the music channel and returns it
 * to the final-log loop when campaign control resumes.
 */
public final class GanEdenFinalLogMusicScript implements EveryFrameScript {
    private static final String INTRO_MUSIC =
            "ship_trophy_isa_final_log_intro";
    private static final String LOOP_MUSIC =
            "ship_trophy_isa_final_log_loop";
    private static final String ACTIVE_MEMORY_KEY =
            "$shipTrophyGanEdenFinalLogMusicActive";
    private static final long INTRO_DURATION_NANOS =
            (long) (167.43325d * 1_000_000_000d);
    private static final long INTRO_START_DELAY_NANOS =
            (long) (0.25d * 1_000_000_000d);

    private static boolean active;
    private static boolean loopStarted;
    private static boolean introPending;
    private static boolean finalLogDialogOpen;
    private static boolean suspendedForCombat;
    private static long introStartedAt;
    private static long introRequestedAt;

    public static void start() {
        if (Global.getSector() == null) return;

        // Combat can return directly into the Space Elevator interaction,
        // before persistent campaign scripts receive a cleanup frame. Stop
        // Strike as a handoff, without restarting a default encounter cue
        // that could race and overwrite Isa's track.
        GanEdenBattleCreationPlugin.handOffGoldenOmegaMusic();
        active = true;
        loopStarted = false;
        introPending = true;
        finalLogDialogOpen = true;
        suspendedForCombat = false;
        introStartedAt = 0L;
        introRequestedAt = System.nanoTime();
        setPersisted(true);
    }

    public static void stop() {
        boolean hadOwnership = active || isPersisted();
        boolean unlockReturnPlaylist = hadOwnership
                && GanEdenQuestManager.isCompleted()
                && !isPlayerInGanEden();
        active = false;
        loopStarted = false;
        introPending = false;
        finalLogDialogOpen = false;
        suspendedForCombat = false;
        introStartedAt = 0L;
        introRequestedAt = 0L;
        setPersisted(false);
        if (unlockReturnPlaylist) {
            GanEdenPostQuestMusicScript.unlockAfterFinalDeparture();
        }
        if (!hadOwnership) return;

        try {
            Global.getSoundPlayer().pauseCustomMusic();
        } catch (RuntimeException ex) {
            logResetFailure("stop Isa final-log music", ex);
        }
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
        } catch (RuntimeException ex) {
            logResetFailure(
                    "release the default-music suspension after Isa's final log",
                    ex);
        }
        try {
            Global.getSoundPlayer().restartCurrentMusic();
        } catch (RuntimeException ex) {
            logResetFailure("restart music after Isa's final log", ex);
        }
    }

    /** Lets combat use either vanilla music or its own authored custom track. */
    public static void suspendForCombat() {
        if (!active || suspendedForCombat) return;
        suspendedForCombat = true;
        try {
            Global.getSoundPlayer().pauseCustomMusic();
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
            Global.getSoundPlayer().restartCurrentMusic();
        } catch (RuntimeException ex) {
            System.err.println(
                    "Hall of Triumph: failed to yield final-log music to combat.");
            ex.printStackTrace(System.err);
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static void resetForGameLoad() {
        boolean wasPersisted = isPersisted();
        boolean resume = wasPersisted && isPlayerInGanEden();
        active = false;
        loopStarted = false;
        introPending = false;
        finalLogDialogOpen = false;
        suspendedForCombat = false;
        introStartedAt = 0L;
        introRequestedAt = 0L;

        // Always clear stale process-wide ownership before restoring the state
        // recorded in the campaign save.
        try {
            Global.getSoundPlayer().pauseCustomMusic();
        } catch (RuntimeException ex) {
            logResetFailure("stop stale custom music", ex);
        }
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
        } catch (RuntimeException ex) {
            logResetFailure("release stale default-music suspension", ex);
        }
        try {
            Global.getSoundPlayer().restartCurrentMusic();
        } catch (RuntimeException ex) {
            logResetFailure("restart normal music", ex);
        }

        if (resume) {
            resumeLoop();
        } else {
            setPersisted(false);
            if (wasPersisted && GanEdenQuestManager.isCompleted()) {
                GanEdenPostQuestMusicScript.unlockAfterFinalDeparture();
            }
        }
    }

    private static void resumeLoop() {
        if (Global.getSector() == null || !isPlayerInGanEden()) {
            stop();
            return;
        }
        active = true;
        loopStarted = true;
        introPending = false;
        finalLogDialogOpen = false;
        suspendedForCombat = false;
        introStartedAt = 0L;
        introRequestedAt = 0L;
        setPersisted(true);
        try {
            claimMusicChannel();
            Global.getSoundPlayer().playCustomMusic(
                    0, 0, LOOP_MUSIC, true);
        } catch (RuntimeException ex) {
            System.err.println(
                    "Hall of Triumph: failed to resume Isa final-log loop.");
            ex.printStackTrace(System.err);
            stop();
        }
    }

    /**
     * Reclaims the exact stream paused by Starsector when DismissDialog closes
     * the Space Elevator interaction. SoundPlayerAPI retains that custom
     * stream and its playback position; starting LOOP_MUSIC here would instead
     * make the theme audibly jump back to its beginning.
     */
    private static void resumeAfterFinalLogDialog() {
        finalLogDialogOpen = false;
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
            Global.getSoundPlayer().pauseMusic();
            Global.getSoundPlayer().resumeCustomMusic();
        } catch (RuntimeException ex) {
            System.err.println(
                    "Hall of Triumph: failed to resume Isa final-log music "
                            + "after closing the archive.");
            ex.printStackTrace(System.err);
            // A fresh loop is preferable to losing the score entirely if a
            // third-party music plugin has discarded Starsector's saved
            // custom stream.
            resumeLoop();
        }
    }

    private static void claimMusicChannel() {
        Global.getSoundPlayer().pauseCustomMusic();
        Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
        Global.getSoundPlayer().pauseMusic();
    }

    private static boolean isPlayerInGanEden() {
        if (Global.getSector() == null) return false;
        if (GanEdenGenerator.isGanEden(
                Global.getSector().getCurrentLocation())) return true;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        return GanEdenGenerator.isGanEden(player);
    }

    private static boolean isPersisted() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate()
                        .getBoolean(ACTIVE_MEMORY_KEY);
    }

    private static void setPersisted(boolean value) {
        if (Global.getSector() == null) return;
        if (value) {
            Global.getSector().getMemoryWithoutUpdate().set(
                    ACTIVE_MEMORY_KEY, true);
        } else {
            Global.getSector().getMemoryWithoutUpdate().unset(
                    ACTIVE_MEMORY_KEY);
        }
    }

    private static void logResetFailure(
            String operation, RuntimeException ex) {
        System.err.println(
                "Hall of Triumph: failed to " + operation + ".");
        ex.printStackTrace(System.err);
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
        if (!active || Global.getSector() == null) return;

        if (!isPlayerInGanEden()) {
            stop();
            return;
        }

        if (suspendedForCombat) {
            resumeLoop();
            return;
        }

        if (finalLogDialogOpen
                && Global.getSector().getCampaignUI()
                        .getCurrentInteractionDialog() == null) {
            resumeAfterFinalLogDialog();
            return;
        }

        if (introPending) {
            if (System.nanoTime() - introRequestedAt
                    < INTRO_START_DELAY_NANOS) return;
            introPending = false;
            introStartedAt = System.nanoTime();
            try {
                // The interaction dialog chooses its encounter music after
                // processing the option that opens Log V. Claiming the
                // channel here, on a later campaign frame, prevents that
                // selection from immediately replacing Isa's theme.
                claimMusicChannel();
                Global.getSoundPlayer().playCustomMusic(
                        0, 0, INTRO_MUSIC, false);
            } catch (RuntimeException ex) {
                System.err.println(
                        "Hall of Triumph: failed to start Isa final-log music.");
                ex.printStackTrace(System.err);
                stop();
            }
            return;
        }

        if (loopStarted
                || System.nanoTime() - introStartedAt
                        < INTRO_DURATION_NANOS) return;

        loopStarted = true;
        try {
            Global.getSoundPlayer().playCustomMusic(
                    0, 0, LOOP_MUSIC, true);
        } catch (RuntimeException ex) {
            System.err.println(
                    "Hall of Triumph: failed to start Isa final-log loop.");
            ex.printStackTrace(System.err);
            stop();
        }
    }
}
