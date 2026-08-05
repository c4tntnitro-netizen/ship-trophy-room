package shiptrophy.campaign;

import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;

import shiptrophy.IsaTrophyManager;

/** Supplies controlled battle layouts for Gan Eden's quest encounters. */
public final class GanEdenBattleCreationPlugin extends BaseCampaignPlugin {
    public static final String ID = "ship_trophy_gan_eden_battle_creation";
    private static final String BACKGROUND =
            "graphics/planets/terran_eccentric_battle_4x.jpg";
    private static final String ATMOSPHERIC_CLOUDS =
            "graphics/planets/clouds_white.png";
    private static final String NATIVE_BACKGROUND =
            "graphics/backgrounds/wormhole_dest_black.jpg";
    private static final String GOLDEN_OMEGA_MUSIC_INTRO =
            "ship_trophy_golden_omega_combat_intro";
    private static final String GOLDEN_OMEGA_MUSIC_LOOP =
            "ship_trophy_golden_omega_combat_loop";
    private static final String GOLDEN_OMEGA_MUSIC_SILENCE =
            "ship_trophy_golden_omega_combat_silence";
    private static final long GOLDEN_OMEGA_INTRO_CUE_NANOS =
            (long) (5d * 1_000_000_000d);
    private static final long GOLDEN_OMEGA_INTRO_DURATION_NANOS =
            (long) (11.6d * 1_000_000_000d);
    private static final long GOLDEN_OMEGA_MUSIC_RETRY_NANOS =
            (long) (0.75d * 1_000_000_000d);
    private static final String GOLDEN_OMEGA_SILENCE_FILE =
            "ship_trophy_golden_omega_silent_channel.ogg";
    private static final String GOLDEN_OMEGA_INTRO_FILE =
            "ship_trophy_strike_from_the_sky_intro.ogg";
    private static final String GOLDEN_OMEGA_LOOP_FILE =
            "ship_trophy_strike_from_the_sky_loop.ogg";
    private static final String ATMOSPHERIC_FLOW_ID =
            "ship_trophy_gan_eden_atmospheric_flow";
    private static final String ATMOSPHERIC_FLOW_NAME =
            "Gan Eden atmospheric flow";
    private static final float ATMOSPHERIC_WEAPON_MULT = 0.95f;
    private static final float ATMOSPHERIC_SPEED_MULT = 1.20f;
    private static boolean goldenOmegaMusicActive;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(
            SectorEntityToken opponent) {
        if (!(opponent instanceof CampaignFleetAPI)) return null;

        boolean goldenOmega = opponent.getMemoryWithoutUpdate().getBoolean(
                GanEdenAmbushScript.FLEET_KEY);
        boolean ivoryCustodians = opponent.getMemoryWithoutUpdate().getBoolean(
                GanEdenTransitAmbushManager.FLEET_KEY);
        // Battle setup may temporarily detach the opposing fleet from its
        // containing system. The authored Golden encounter is always in Gan
        // Eden, and the player fleet remains a reliable location fallback for
        // other local battles.
        boolean atmospheric = goldenOmega
                || isInGanEden(opponent)
                || isInGanEden(Global.getSector() == null
                        ? null : Global.getSector().getPlayerFleet());
        if (goldenOmega || ivoryCustodians || atmospheric) {
            return new PluginPick<BattleCreationPlugin>(
                    new AuthoredBattle(
                            goldenOmega, ivoryCustodians, atmospheric),
                    PickPriority.HIGHEST);
        }

        if (opponent.getMemoryWithoutUpdate().getBoolean(
                GanEdenHypershuntManager.GUARD_KEY)) {
            // These fleets happen to orbit a hypershunt, but their battle is
            // against human claimants, not the hypershunt's Omega defenses.
            // Force the vanilla layout so broad environmental battle plugins
            // cannot inject unrelated hypershunt objectives into this fight.
            return new PluginPick<BattleCreationPlugin>(
                    new BattleCreationPluginImpl(), PickPriority.HIGHEST);
        }
        return null;
    }

    private static boolean isInGanEden(SectorEntityToken entity) {
        return GanEdenGenerator.isGanEden(entity);
    }

    private static final class AuthoredBattle
            extends BattleCreationPluginImpl {
        private final boolean goldenOmega;
        private final boolean ivoryCustodians;
        private boolean atmospheric;

        private AuthoredBattle(
                boolean goldenOmega,
                boolean ivoryCustodians,
                boolean atmospheric) {
            this.goldenOmega = goldenOmega;
            this.ivoryCustodians = ivoryCustodians;
            this.atmospheric = atmospheric;
        }

        @Override
        public void initBattle(
                BattleCreationContext context,
                MissionDefinitionAPI loader) {
            atmospheric = atmospheric
                    || goldenOmega
                    || isInGanEden(context == null
                            ? null : context.getPlayerFleet())
                    || isInGanEden(context == null
                            ? null : context.getOtherFleet());
            if (atmospheric) {
                // Reserve ownership before vanilla builds the battle. The
                // campaign music scripts can advance once during the
                // confirmation-dialog -> combat transition; without this
                // early, side-effect-free reservation they may briefly start
                // the Log V loop after vanilla's encounter track but before
                // afterDefinitionLoad() installs the authored handoff.
                reserveGoldenOmegaMusic();
            }
            // Retain vanilla deployment, objectives, map size, and terrain.
            super.initBattle(context, loader);
            if (atmospheric) {
                // Vanilla may select one of Gan Eden's colonizable PlanetAPI
                // anchors as combat scenery. Suppress that planet and keep
                // the native background on a different texture object from
                // our camera-locked atmospheric layer. Starsector mutates its
                // native background texture in place during combat startup.
                loader.setPlanetBgSize(1f, 1f);
                loader.setBackgroundSpriteName(NATIVE_BACKGROUND);
                loader.setBackgroundGlowColor(new Color(82, 142, 176, 70));
            }
        }

        @Override
        public void afterDefinitionLoad(CombatEngineAPI engine) {
            super.afterDefinitionLoad(engine);
            boolean musicReady = false;
            if (atmospheric) {
                // Establish a single owner after vanilla has completed battle
                // setup. Repeated pre-setup pause/restart calls can leave the
                // sound engine with several queued streams and no active one.
                musicReady = beginGoldenOmegaMusic();
                GanEdenFinalLogMusicScript.suspendForCombat();
                GanEdenPostQuestMusicScript.suspendForCombat();
                engine.setRenderStarfield(false);
                engine.setBackgroundColor(new Color(8, 20, 43));
                engine.setBackgroundGlowColor(new Color(95, 157, 184, 70));
                engine.setBackgroundGlowColorNonAdditive(true);
                engine.addLayeredRenderingPlugin(new AtmosphericBackdrop());
                engine.addPlugin(new AtmosphericFlowEffect(engine));
            }
            if (goldenOmega || ivoryCustodians) {
                engine.addPlugin(new IsaEncounterChatter(
                        engine, ivoryCustodians));
            }
            if (!atmospheric || !musicReady) return;
            // Claim music ownership now, but cue the authored intro from live
            // combat frames. Starsector starts its own combat track after this
            // callback and would otherwise overwrite an early custom stream.
            engine.addPlugin(new GoldenOmegaMusic(engine));
        }
    }

    /** Guarantees one authored Isa line in each of the two quest boss fights. */
    private static final class IsaEncounterChatter
            extends BaseEveryFrameCombatPlugin {
        private final CombatEngineAPI engine;
        private final boolean ivoryCustodians;
        private float elapsed;
        private boolean spoken;

        private IsaEncounterChatter(
                CombatEngineAPI engine, boolean ivoryCustodians) {
            this.engine = engine;
            this.ivoryCustodians = ivoryCustodians;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (spoken || engine == null || engine.isCombatOver()) return;
            if (engine.isPaused()) return;
            elapsed += amount;
            if (elapsed < 3f) return;

            for (ShipAPI ship : engine.getShips()) {
                if (ship == null
                        || ship.getOwner() != 0
                        || ship.getCaptain() == null
                        || !IsaTrophyManager.PERSON_ID.equals(
                                ship.getCaptain().getId())) {
                    continue;
                }
                String line = ivoryCustodians
                        ? "Ivory Remnants. Same architecture, new doctrine. "
                                + "Keep one busy long enough for me to read it."
                        : "Cherubim and Lahat are changing formation around "
                                + "us. Stay sharp!";
                engine.addFloatingText(
                        ship.getLocation(), line, 24f,
                        new Color(110, 220, 255), ship, 1f, 3f);
                spoken = true;
                return;
            }
        }
    }

    private static boolean beginGoldenOmegaMusic() {
        reserveGoldenOmegaMusic();
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
            return true;
        } catch (RuntimeException ex) {
            System.err.println(
                    "Hall of Triumph: failed to suspend default music for "
                            + "the Golden Omega encounter.");
            ex.printStackTrace(System.err);
            restoreGoldenOmegaMusic();
            return false;
        }
    }

    /**
     * Marks the campaign music channel as spoken for without touching the
     * asynchronous sound engine. Actual stream replacement remains deferred
     * until vanilla has finished battle construction.
     */
    private static void reserveGoldenOmegaMusic() {
        goldenOmegaMusicActive = true;
    }

    /** True while atmospheric combat owns Starsector's global music channel. */
    public static boolean isGoldenOmegaMusicActive() {
        return goldenOmegaMusicActive;
    }

    /**
     * Releases the process-wide music suspension owned by the Golden Omega
     * battle. Combat plugins are not guaranteed another frame after every
     * possible battle exit, so the persistent campaign script also calls this
     * as soon as control returns to the campaign layer.
     */
    public static void restoreGoldenOmegaMusic() {
        if (!goldenOmegaMusicActive) return;
        goldenOmegaMusicActive = false;

        try {
            Global.getSoundPlayer().pauseCustomMusic();
        } catch (RuntimeException ex) {
            logMusicRestoreFailure("stop custom music", ex);
        }
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
        } catch (RuntimeException ex) {
            logMusicRestoreFailure("release the default-music suspension", ex);
        }
        try {
            Global.getSoundPlayer().restartCurrentMusic();
        } catch (RuntimeException ex) {
            logMusicRestoreFailure("restart normal music", ex);
        }
    }

    /** Stops Strike without briefly restarting music before another owner. */
    public static void handOffGoldenOmegaMusic() {
        if (!goldenOmegaMusicActive) return;
        goldenOmegaMusicActive = false;
        try {
            Global.getSoundPlayer().pauseCustomMusic();
        } catch (RuntimeException ex) {
            logMusicRestoreFailure(
                    "hand Golden Omega music to the next scene", ex);
        }
    }

    private static void logMusicRestoreFailure(
            String operation, RuntimeException ex) {
        System.err.println(
                "Hall of Triumph: failed to " + operation
                        + " after Golden Omega combat.");
        ex.printStackTrace(System.err);
    }

    /** Applies Gan Eden's high-altitude flow to every combatant. */
    private static final class AtmosphericFlowEffect
            extends BaseEveryFrameCombatPlugin {
        private static final String STATUS_ICON =
                "graphics/icons/hullsys/plasma_jets.png";

        private final CombatEngineAPI engine;

        private AtmosphericFlowEffect(CombatEngineAPI engine) {
            this.engine = engine;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (engine == null || engine.isCombatOver()) return;
            for (ShipAPI ship : engine.getShips()) {
                applyTo(ship);
            }
            engine.maintainStatusForPlayerShip(
                    ATMOSPHERIC_FLOW_ID,
                    STATUS_ICON,
                    "Atmospheric flow",
                    "-5% ballistic/energy damage and range; "
                            + "+20% ship and missile speed",
                    false);
        }

        private void applyTo(ShipAPI ship) {
            if (ship == null) return;
            MutableShipStatsAPI stats = ship.getMutableStats();
            stats.getBallisticWeaponDamageMult().modifyMult(
                    ATMOSPHERIC_FLOW_ID,
                    ATMOSPHERIC_WEAPON_MULT,
                    ATMOSPHERIC_FLOW_NAME);
            stats.getEnergyWeaponDamageMult().modifyMult(
                    ATMOSPHERIC_FLOW_ID,
                    ATMOSPHERIC_WEAPON_MULT,
                    ATMOSPHERIC_FLOW_NAME);
            stats.getBallisticWeaponRangeBonus().modifyMult(
                    ATMOSPHERIC_FLOW_ID,
                    ATMOSPHERIC_WEAPON_MULT,
                    ATMOSPHERIC_FLOW_NAME);
            stats.getEnergyWeaponRangeBonus().modifyMult(
                    ATMOSPHERIC_FLOW_ID,
                    ATMOSPHERIC_WEAPON_MULT,
                    ATMOSPHERIC_FLOW_NAME);
            stats.getMaxSpeed().modifyMult(
                    ATMOSPHERIC_FLOW_ID,
                    ATMOSPHERIC_SPEED_MULT,
                    ATMOSPHERIC_FLOW_NAME);
            stats.getMissileMaxSpeedBonus().modifyMult(
                    ATMOSPHERIC_FLOW_ID,
                    ATMOSPHERIC_SPEED_MULT,
                    ATMOSPHERIC_FLOW_NAME);
        }
    }

    /** Plays the authored intro once, then hands off to its seamless loop. */
    private static final class GoldenOmegaMusic
            extends BaseEveryFrameCombatPlugin {
        private final CombatEngineAPI engine;
        private boolean introRequested;
        private boolean loopRequested;
        private boolean silenceRequested;
        private boolean silenceActive;
        private boolean introActive;
        private boolean restored;
        private long silenceStartedAt = -1L;
        private long introStartedAt = -1L;
        private long lastMusicRequestAt = -1L;

        private GoldenOmegaMusic(CombatEngineAPI engine) {
            this.engine = engine;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (restored) return;
            if (engine == null || engine.isCombatOver()) {
                restoreDefaultMusic();
                return;
            }

            // Audio stream replacement is asynchronous. Request each phase
            // once, return control to Starsector's audio thread, and do not
            // start that phase's clock until getCurrentMusicId() confirms the
            // exact OGG is active. This is important on repeat encounters,
            // where a prior Gan Eden custom stream may still occupy the
            // channel when combat is constructed.
            long now = System.nanoTime();
            if (!silenceRequested) {
                silenceRequested = true;
                // An inaudible custom music set occupies the same channel as
                // vanilla Remnant music. This preserves the five-second cue
                // without allowing the default track to leak into it.
                requestMusic(
                        now,
                        GOLDEN_OMEGA_MUSIC_SILENCE,
                        "silent cue");
                return;
            }

            // Audio continues while the deployment screen or combat is
            // paused, so music transitions must use wall-clock time rather
            // than unpaused simulation time.
            if (!silenceActive) {
                if (isCurrentMusic(GOLDEN_OMEGA_SILENCE_FILE)) {
                    silenceActive = true;
                    silenceStartedAt = now;
                } else {
                    retryMusic(
                            now,
                            GOLDEN_OMEGA_MUSIC_SILENCE,
                            GOLDEN_OMEGA_SILENCE_FILE,
                            "silent cue");
                    return;
                }
            }

            if (!introRequested) {
                if (now - silenceStartedAt
                        < GOLDEN_OMEGA_INTRO_CUE_NANOS) return;
                introRequested = true;
                requestMusic(
                        now,
                        GOLDEN_OMEGA_MUSIC_INTRO,
                        // Starsector releases a non-looping music stream
                        // roughly 0.9 seconds before this OGG's sample-true
                        // endpoint. Retain channel ownership; the dedicated
                        // loop replaces it at exactly 11.6 seconds below.
                        "intro");
                return;
            }

            if (!introActive) {
                if (isCurrentMusic(GOLDEN_OMEGA_INTRO_FILE)) {
                    introActive = true;
                    introStartedAt = now;
                } else {
                    retryMusic(
                            now,
                            GOLDEN_OMEGA_MUSIC_INTRO,
                            GOLDEN_OMEGA_INTRO_FILE,
                            "intro");
                    return;
                }
            }

            if (!loopRequested) {
                if (now - introStartedAt
                        < GOLDEN_OMEGA_INTRO_DURATION_NANOS) return;

                loopRequested = true;
                requestMusic(
                        now, GOLDEN_OMEGA_MUSIC_LOOP, "loop");
                return;
            }

            // Music-switcher mods and late encounter callbacks can replace a
            // custom stream after it was requested. Confirm the loop really
            // owns the channel and reassert it at a bounded rate if needed.
            if (!isCurrentMusic(GOLDEN_OMEGA_LOOP_FILE)) {
                retryMusic(
                        now,
                        GOLDEN_OMEGA_MUSIC_LOOP,
                        GOLDEN_OMEGA_LOOP_FILE,
                        "loop");
            }
        }

        private void retryMusic(
                long now,
                String musicSet,
                String expectedFile,
                String phase) {
            if (isCurrentMusic(expectedFile)) return;
            if (lastMusicRequestAt >= 0L
                    && now - lastMusicRequestAt
                            < GOLDEN_OMEGA_MUSIC_RETRY_NANOS) {
                return;
            }
            requestMusic(now, musicSet, phase);
        }

        private void requestMusic(
                long now, String musicSet, String phase) {
            lastMusicRequestAt = now;
            if (!playMusic(musicSet, true, 0, phase)) {
                restored = true;
                restoreGoldenOmegaMusic();
            }
        }

        private boolean isCurrentMusic(String expectedFile) {
            if (expectedFile == null) return false;
            try {
                return expectedFile.equals(
                        Global.getSoundPlayer().getCurrentMusicId());
            } catch (RuntimeException ex) {
                return false;
            }
        }

        private boolean playMusic(
                String musicSet,
                boolean loop,
                int fadeInSeconds,
                String phase) {
            try {
                Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
                Global.getSoundPlayer().playCustomMusic(
                        0, fadeInSeconds, musicSet, loop);
                return true;
            } catch (RuntimeException ex) {
                System.err.println(
                        "Hall of Triumph: failed to start Golden Omega combat "
                                + "music " + phase + ".");
                ex.printStackTrace(System.err);
                return false;
            }
        }

        private void restoreDefaultMusic() {
            restored = true;
            restoreGoldenOmegaMusic();
        }
    }

    /**
     * Keeps the Gan Eden terrain locked to the combat camera and scrolls its
     * horizontally seamless world map from side to side. A translucent
     * terrestrial cloud map adds atmospheric depth without introducing
     * gameplay nebula terrain. A mission background is rendered in
     * battlefield coordinates, which makes a finite texture read as a distant
     * object as the camera moves or zooms. This layer instead recalculates its
     * world-space dimensions from the visible viewport every frame.
     */
    private static final class AtmosphericBackdrop
            extends BaseCombatLayeredRenderingPlugin {
        private static final float OVERSCAN = 1.02f;
        private static final float SCROLL_CYCLES_PER_SECOND = 1f / 180f;
        private static final float CLOUD_ALPHA = 0.34f;

        private final SpriteAPI sprite;
        private final SpriteAPI clouds;
        private final float sourceWidth;
        private final float sourceHeight;
        private float scrollPhase;

        private AtmosphericBackdrop() {
            try {
                Global.getSettings().loadTexture(BACKGROUND);
                Global.getSettings().loadTexture(ATMOSPHERIC_CLOUDS);
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Unable to load Gan Eden atmospheric textures.",
                        ex);
            }
            sprite = Global.getSettings().getSprite(BACKGROUND);
            clouds = Global.getSettings().getSprite(ATMOSPHERIC_CLOUDS);
            sourceWidth = Math.max(1f, sprite.getWidth());
            sourceHeight = Math.max(1f, sprite.getHeight());
            sprite.setNormalBlend();
            sprite.setColor(Color.WHITE);
            sprite.setAlphaMult(1f);
            sprite.setAngle(0f);
            clouds.setNormalBlend();
            clouds.setColor(new Color(225, 238, 248));
            clouds.setAlphaMult(CLOUD_ALPHA);
            clouds.setAngle(0f);
        }

        @Override
        public void advance(float amount) {
            if (amount <= 0f) return;
            scrollPhase += amount * SCROLL_CYCLES_PER_SECOND;
            scrollPhase -= (float) Math.floor(scrollPhase);
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            // BELOW_PLANETS is underneath Starsector's opaque combat
            // background fill. ABOVE_PLANETS keeps the terrain above that
            // fill and any vanilla planet scenery, while allowing atmospheric
            // cloud layers to remain visible over the surface.
            return EnumSet.of(CombatEngineLayers.ABOVE_PLANETS);
        }

        @Override
        public float getRenderRadius() {
            return Float.MAX_VALUE;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (layer != CombatEngineLayers.ABOVE_PLANETS
                    || viewport == null) {
                return;
            }

            float coverScale = Math.max(
                    viewport.getVisibleWidth() / sourceWidth,
                    viewport.getVisibleHeight() / sourceHeight);
            float width = sourceWidth * coverScale * OVERSCAN;
            float height = sourceHeight * coverScale * OVERSCAN;
            sprite.setSize(width, height);
            sprite.setCenter(width * 0.5f, height * 0.5f);
            clouds.setSize(width, height);
            clouds.setCenter(width * 0.5f, height * 0.5f);

            Vector2f center = viewport.getCenter();
            float offset = scrollPhase * width;
            for (int tile = -1; tile <= 1; tile++) {
                float x = center.x + offset + tile * width;
                sprite.renderAtCenter(x, center.y);
                clouds.renderAtCenter(x, center.y);
            }
        }
    }
}
