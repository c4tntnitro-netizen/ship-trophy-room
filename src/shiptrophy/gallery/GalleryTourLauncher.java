package shiptrophy.gallery;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.ShipTrophyL10n;

/** Launches the disposable, read-only combat view of a curated Gallery hall. */
public final class GalleryTourLauncher {
    private static final String MARKER_KEY =
            "$ship_trophy_gallery_tour_fleet";
    private static final String SESSION_KEY =
            "$ship_trophy_gallery_tour_session";
    private static final String BACKGROUND =
            "graphics/backgrounds/ship_trophy_gallery_tour_topdown_graded.png";
    private static final String NATIVE_BACKGROUND =
            "graphics/backgrounds/wormhole_dest_black.jpg";
    private static final String STATUS_ICON =
            "graphics/icons/industry/trophy_room.png";
    private static final String KITE_VARIANT = "kite_original_Stock";
    private static final String INVULNERABLE_ID =
            "ship_trophy_gallery_tour_invulnerable";
    private static final String GALLERY_MUSIC_SET =
            "ship_trophy_time_leads_to_the_end";
    private static final String GALLERY_MUSIC_FILE =
            "ship_trophy_time_leads_to_the_end.ogg";
    // Sample-true duration of the game-ready Ogg Vorbis stream.
    private static final long GALLERY_MUSIC_DURATION_NANOS =
            (long) (134.747062d * 1_000_000_000d);
    private static final long GALLERY_MUSIC_END_TOLERANCE_NANOS =
            (long) (2d * 1_000_000_000d);
    private static final long GALLERY_MUSIC_SILENCE_NANOS =
            (long) (20d * 1_000_000_000d);
    private static final long GALLERY_MUSIC_RETRY_NANOS =
            (long) (0.75d * 1_000_000_000d);
    private static boolean galleryMusicActive;

    private static final float MIN_MAP_WIDTH = 7000f;
    private static final float MAX_MAP_WIDTH = 20000f;
    private static final float MIN_MAP_HEIGHT = 6000f;
    private static final float MAX_MAP_HEIGHT = 12000f;
    private static final float MAP_MARGIN = 2400f;
    private static final float EXHIBIT_GAP = 320f;
    private static final float AISLE_HALF_WIDTH = 780f;
    private static final float MAX_EXHIBIT_WIDTH = 980f;
    private static final float MAX_EXHIBIT_HEIGHT = 860f;
    private static final float HALL_WORLD_SCALE = 0.8f;
    private static final float BACKGROUND_OVERSCAN = 500f;
    private static final float BERTH_X_NORMALIZED = 0.373f;
    private static final float[] BERTH_Y_NORMALIZED = {
        -0.351f, -0.219f, -0.093f, 0.038f, 0.167f, 0.295f, 0.423f
    };
    static final int MAX_EXHIBITS = BERTH_Y_NORMALIZED.length * 2;
    private static final float INTRO_CAMERA_SECONDS = 0.35f;
    private static final float INTRO_VIEW_MULT = 1.1f;

    private GalleryTourLauncher() {
    }

    static void launch(
            final InteractionDialogAPI dialog,
            final List<FleetMemberAPI> selectedExhibits,
            final FleetMemberAPI selectedShuttle) {
        if (dialog == null) return;

        SectorAPI sector = Global.getSector();
        if (sector == null) return;

        // customDialogConfirm() runs before Starsector clears the Gallery
        // modal. Defer the direct launch until the following campaign frame,
        // then close the colony interaction through the supported return path.
        sector.addTransientScript(new LaunchTourAfterGalleryScript(
                dialog,
                selectedExhibits == null
                        ? Collections.<FleetMemberAPI>emptyList()
                        : new ArrayList<FleetMemberAPI>(selectedExhibits),
                selectedShuttle));
    }

    private static final class LaunchTourAfterGalleryScript
            implements EveryFrameScript {
        private final InteractionDialogAPI dialog;
        private final List<FleetMemberAPI> selectedExhibits;
        private final FleetMemberAPI selectedShuttle;
        private boolean waitedOneFrame;
        private boolean done;

        private LaunchTourAfterGalleryScript(
                InteractionDialogAPI dialog,
                List<FleetMemberAPI> selectedExhibits,
                FleetMemberAPI selectedShuttle) {
            this.dialog = dialog;
            this.selectedExhibits = selectedExhibits;
            this.selectedShuttle = selectedShuttle;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }

        @Override
        public void advance(float amount) {
            if (!waitedOneFrame) {
                waitedOneFrame = true;
                return;
            }
            done = true;
            if (!ShipGalleryData.isTourShuttleEligible(selectedShuttle)) {
                dialog.getTextPanel().addPara(
                        ShipTrophyL10n.get("gallery_no_shuttle_launch"),
                        Misc.getNegativeHighlightColor());
                return;
            }
            if (selectedExhibits.isEmpty()) {
                dialog.getTextPanel().addPara(
                        ShipTrophyL10n.get("gallery_empty_tour_launch"));
                return;
            }
            launchSelected(dialog, selectedExhibits, selectedShuttle);
        }
    }

    private static void launchSelected(
            InteractionDialogAPI dialog,
            List<FleetMemberAPI> selectedExhibits,
            FleetMemberAPI selected) {

        List<Exhibit> exhibits = captureExhibits(selectedExhibits);
        if (exhibits.isEmpty()) {
            dialog.getTextPanel().addPara(
                    ShipTrophyL10n.get("gallery_no_display_images"));
            return;
        }

        CampaignFleetAPI shuttle = Global.getFactory().createEmptyFleet(
                Factions.PLAYER, ShipTrophyL10n.get("gallery_shuttle_fleet"), false);
        CampaignFleetAPI hall = Global.getFactory().createEmptyFleet(
                Factions.NEUTRAL, ShipTrophyL10n.get("gallery_exhibits_fleet"), false);
        if (shuttle == null || hall == null) return;

        FleetMemberAPI shuttleMember = cloneForTour(selected);
        if (shuttleMember == null) return;
        shuttleMember.setOwner(0);
        shuttleMember.setFlagship(true);
        shuttleMember.getRepairTracker().setMothballed(false);
        shuttleMember.getRepairTracker().setCR(1f);
        shuttle.getFleetData().addFleetMember(shuttleMember);
        shuttle.getFleetData().setFlagship(shuttleMember);
        // CampaignState recrews factory fleets before combat. Supplying the
        // shuttle's minimum crew keeps the selected ship deployable through that
        // otherwise automatic synchronization step.
        shuttle.getCargo().addCrew(
                (int) Math.ceil(shuttleMember.getMinCrew()));

        // Some optional campaign plugins inspect every proposed opponent
        // before Starsector chooses a BattleCreationPlugin, and assume that a
        // CampaignFleet always has a flagship. Keep this harmless proxy in
        // the detached campaign fleet, but deliberately do not add it to the
        // MissionDefinition; it never appears in the Gallery combat map.
        FleetMemberAPI hallIndex = Global.getFactory().createFleetMember(
                FleetMemberType.SHIP, KITE_VARIANT);
        if (hallIndex == null) return;
        hallIndex.setShipName(ShipTrophyL10n.get("gallery_exhibit_index"));
        hallIndex.setOwner(1);
        hallIndex.setFlagship(true);
        hallIndex.getRepairTracker().setMothballed(false);
        hallIndex.getRepairTracker().setCR(1f);
        hall.getFleetData().addFleetMember(hallIndex);
        hall.getFleetData().setFlagship(hallIndex);
        hall.getCargo().addCrew((int) Math.ceil(hallIndex.getMinCrew()));

        Session session = new Session(exhibits, shuttleMember);
        hall.getMemoryWithoutUpdate().set(MARKER_KEY, true);
        hall.getMemoryWithoutUpdate().set(SESSION_KEY, session);

        BattleCreationContext context = new BattleCreationContext(
                shuttle, FleetGoal.ATTACK, hall, FleetGoal.ATTACK);
        context.objectivesAllowed = false;
        context.forceObjectivesOnMap = false;
        context.enemyDeployAll = true;
        context.aiRetreatAllowed = false;
        context.fightToTheLast = false;
        context.setInitialDeploymentBurnDuration(0f);
        context.setNormalDeploymentBurnDuration(0f);
        context.setStandoffRange(0f);

        InteractionDialogPlugin delegate = dialog.getPlugin();
        SectorEntityToken target = dialog.getInteractionTarget();
        PersonAPI activePerson = target == null
                ? null : target.getActivePerson();
        TourDialogPlugin tour = new TourDialogPlugin(
                delegate, target, activePerson, context, hall);

        // A comm-directory contact is not CampaignState's encounter dialog.
        // Close it first, then open a real encounter dialog to own the battle
        // and the return callback. Starting combat from the comm dialog leaves
        // CampaignState.encounterDialog null and crashes on return.
        dialog.dismiss();
        SectorAPI sector = Global.getSector();
        if (sector != null) {
            sector.addTransientScript(new OpenTourDialogScript(
                    tour, target, activePerson));
        }
    }

    private static FleetMemberAPI cloneForTour(FleetMemberAPI source) {
        if (!ShipGalleryData.isTourShuttleEligible(source)) return null;
        try {
            ShipVariantAPI variant = source.getVariant().clone();
            FleetMemberAPI copy = Global.getFactory().createFleetMember(
                    FleetMemberType.SHIP, variant);
            if (copy == null) return null;
            copy.setShipName(source.getShipName());
            String spriteOverride = source.getSpriteOverride();
            if (spriteOverride != null && !spriteOverride.trim().isEmpty()) {
                copy.setSpriteOverride(spriteOverride);
            }
            Vector2f overrideSize = source.getOverrideSpriteSize();
            if (overrideSize != null) {
                copy.setOverrideSpriteSize(new Vector2f(overrideSize));
            }
            return copy;
        } catch (Throwable ex) {
            System.err.println(
                    "[Hall of Triumph] Could not clone gallery shuttle.");
            ex.printStackTrace(System.err);
            return null;
        }
    }

    /** Called by the already-registered campaign battle picker. */
    public static BattleCreationPlugin pickBattleCreationPlugin(
            CampaignFleetAPI opponent) {
        if (opponent == null
                || !opponent.getMemoryWithoutUpdate().getBoolean(MARKER_KEY)) {
            return null;
        }
        Object value = opponent.getMemoryWithoutUpdate().get(SESSION_KEY);
        if (!(value instanceof Session)) return null;
        return new TourBattle((Session) value);
    }

    private static List<Exhibit> captureExhibits(
            List<FleetMemberAPI> members) {
        List<Exhibit> result = new ArrayList<Exhibit>();
        for (FleetMemberAPI member : members) {
            Exhibit exhibit = Exhibit.capture(member);
            if (exhibit != null) result.add(exhibit);
            if (result.size() >= MAX_EXHIBITS) break;
        }
        return Collections.unmodifiableList(result);
    }

    private static final class Exhibit {
        private final String spriteName;
        private final float nativeWidth;
        private final float nativeHeight;
        private float x;
        private float y;
        private float angle;

        private Exhibit(
                String spriteName, float nativeWidth, float nativeHeight) {
            this.spriteName = spriteName;
            this.nativeWidth = nativeWidth;
            this.nativeHeight = nativeHeight;
        }

        private static Exhibit capture(FleetMemberAPI member) {
            if (member == null || member.getHullSpec() == null) return null;
            String spriteName = safe(member.getSpriteOverride());
            if (spriteName.isEmpty()) {
                spriteName = safe(member.getHullSpec().getSpriteName());
            }
            if (spriteName.isEmpty()) return null;

            try {
                SpriteAPI sprite = Global.getSettings().getSprite(spriteName);
                float width = sprite.getWidth();
                float height = sprite.getHeight();
                Vector2f override = member.getOverrideSpriteSize();
                if (override != null && override.x > 0f && override.y > 0f) {
                    width = override.x;
                    height = override.y;
                }
                if (width <= 0f || height <= 0f) return null;
                return new Exhibit(spriteName, width, height);
            } catch (Throwable ex) {
                return null;
            }
        }
    }

    private static final class Session {
        private final List<Exhibit> exhibits;
        private final FleetMemberAPI shuttle;
        private final float displayScale;
        private final float mapWidth;
        private final float mapHeight;
        private final float hallWidth;
        private final float hallHeight;
        private final float shuttleStartY;

        private Session(List<Exhibit> exhibits, FleetMemberAPI shuttle) {
            this.exhibits = exhibits;
            this.shuttle = shuttle;

            float nativeWidth = 0f;
            float nativeHeight = 0f;
            for (Exhibit exhibit : exhibits) {
                nativeWidth = Math.max(nativeWidth, exhibit.nativeWidth);
                nativeHeight = Math.max(nativeHeight, exhibit.nativeHeight);
            }
            float widthScale = nativeWidth <= MAX_EXHIBIT_WIDTH
                    ? 1f : Math.max(0.1f, MAX_EXHIBIT_WIDTH / nativeWidth);
            float heightScale = nativeHeight <= MAX_EXHIBIT_HEIGHT
                    ? 1f : Math.max(0.1f, MAX_EXHIBIT_HEIGHT / nativeHeight);
            float unscaledDisplayScale = Math.min(widthScale, heightScale);
            displayScale = unscaledDisplayScale * HALL_WORLD_SCALE;
            float displayedWidth = nativeWidth * unscaledDisplayScale;
            float displayedHeight = nativeHeight * unscaledDisplayScale;
            int rows = (exhibits.size() + 1) / 2;
            float rowSpacing = Math.max(680f, displayedHeight + EXHIBIT_GAP);
            float rowSpan = Math.max(0, rows - 1) * rowSpacing;
            float unscaledMapWidth = Math.max(MIN_MAP_WIDTH,
                    Math.min(MAX_MAP_WIDTH,
                            (AISLE_HALF_WIDTH + displayedWidth + 440f) * 2f));
            float unscaledMapHeight = Math.max(MIN_MAP_HEIGHT,
                    Math.min(MAX_MAP_HEIGHT, rowSpan + MAP_MARGIN));
            mapWidth = unscaledMapWidth * HALL_WORLD_SCALE;
            mapHeight = unscaledMapHeight * HALL_WORLD_SCALE;
            hallWidth = (unscaledMapWidth + BACKGROUND_OVERSCAN)
                    * HALL_WORLD_SCALE;
            hallHeight = (unscaledMapHeight + BACKGROUND_OVERSCAN)
                    * HALL_WORLD_SCALE;
            shuttleStartY = (-unscaledMapHeight * 0.5f + 700f)
                    * HALL_WORLD_SCALE;

            for (int index = 0; index < exhibits.size(); index++) {
                Exhibit exhibit = exhibits.get(index);
                boolean leftSide = index % 2 == 0;
                exhibit.x = (leftSide ? -1f : 1f)
                        * hallWidth * BERTH_X_NORMALIZED;
                exhibit.y = hallHeight
                        * BERTH_Y_NORMALIZED[index / 2];
                exhibit.angle = leftSide ? -90f : 90f;
            }
        }
    }

    private static final class TourBattle implements BattleCreationPlugin {
        private final Session session;

        private TourBattle(Session session) {
            this.session = session;
        }

        @Override
        public void initBattle(
                BattleCreationContext context,
                MissionDefinitionAPI loader) {
            loader.initFleet(
                    FleetSide.PLAYER, "HOT", FleetGoal.ATTACK, false, 0);
            loader.initFleet(
                    FleetSide.ENEMY, "", FleetGoal.ATTACK, true, 0);
            loader.addFleetMember(FleetSide.PLAYER, session.shuttle);
            loader.initMap(
                    -session.mapWidth * 0.5f,
                    session.mapWidth * 0.5f,
                    -session.mapHeight * 0.5f,
                    session.mapHeight * 0.5f);
            // The native combat background is distant scenery and becomes
            // nearly invisible at gallery-map scale. Keep it neutral and draw
            // the Hall floor explicitly in battlefield coordinates instead.
            loader.setBackgroundSpriteName(NATIVE_BACKGROUND);
            loader.setBackgroundGlowColor(new Color(18, 55, 62, 55));
            loader.setHyperspaceMode(false);
            loader.addPlugin(new TourCombatPlugin(session));
        }

        @Override
        public void afterDefinitionLoad(CombatEngineAPI engine) {
            engine.setDoNotEndCombat(true);
            engine.setCustomExit(
                    ShipTrophyL10n.get("gallery_leave_tour"),
                    ShipTrophyL10n.get("gallery_return_to_colony"));
            engine.setRenderStarfield(false);
            engine.setBackgroundColor(new Color(5, 9, 11));
            engine.setMaxFleetPoints(FleetSide.PLAYER, 9999);
            engine.setMaxFleetPoints(FleetSide.ENEMY, 9999);
            engine.getFleetManager(FleetSide.PLAYER)
                    .setSuppressDeploymentMessages(true);
            engine.getFleetManager(FleetSide.ENEMY)
                    .setSuppressDeploymentMessages(true);
            engine.addLayeredRenderingPlugin(new HallBackdrop(session));
            engine.addLayeredRenderingPlugin(new ExhibitRenderer(session));
            if (beginGalleryMusic()) {
                // Vanilla queues combat music after this callback. Claim the
                // channel here, then start the authored track from a live
                // combat frame once battle construction has finished.
                engine.addPlugin(new GalleryMusic(engine));
            }
        }
    }

    private static boolean beginGalleryMusic() {
        galleryMusicActive = true;
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
            return true;
        } catch (RuntimeException ex) {
            System.err.println(
                    "Hall of Triumph: failed to suspend default music for "
                            + "the Gallery tour.");
            ex.printStackTrace(System.err);
            restoreGalleryMusic();
            return false;
        }
    }

    /** Releases the process-wide music channel owned by the Gallery tour. */
    private static void restoreGalleryMusic() {
        if (!galleryMusicActive) return;
        galleryMusicActive = false;

        try {
            Global.getSoundPlayer().pauseCustomMusic();
        } catch (RuntimeException ex) {
            logGalleryMusicFailure("stop custom music", ex);
        }
        try {
            Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
        } catch (RuntimeException ex) {
            logGalleryMusicFailure(
                    "release the default-music suspension", ex);
        }
        try {
            Global.getSoundPlayer().restartCurrentMusic();
        } catch (RuntimeException ex) {
            logGalleryMusicFailure("restart normal music", ex);
        }
    }

    private static void logGalleryMusicFailure(
            String operation, RuntimeException ex) {
        System.err.println(
                "Hall of Triumph: failed to " + operation
                        + " after the Gallery tour.");
        ex.printStackTrace(System.err);
    }

    /** Plays the full Gallery theme, rests for 20 seconds, then repeats. */
    private static final class GalleryMusic
            extends BaseEveryFrameCombatPlugin {
        private final CombatEngineAPI engine;
        private boolean trackRequested;
        private boolean trackActive;
        private boolean silent;
        private boolean restored;
        private long trackStartedAt = -1L;
        private long silenceStartedAt = -1L;
        private long lastMusicRequestAt = -1L;

        private GalleryMusic(CombatEngineAPI engine) {
            this.engine = engine;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (restored) return;
            if (!galleryMusicActive) {
                // An explicit G-key exit may release the channel before the
                // combat engine reports itself over. Do not let a final live
                // frame reclaim music after that handoff.
                restored = true;
                return;
            }
            if (engine == null || engine.isCombatOver()) {
                restoreDefaultMusic();
                return;
            }

            // Music keeps advancing while combat is paused, so every phase is
            // measured against wall-clock time instead of simulation time.
            long now = System.nanoTime();
            if (silent) {
                if (now - silenceStartedAt < GALLERY_MUSIC_SILENCE_NANOS) {
                    return;
                }
                silent = false;
                trackRequested = false;
                trackActive = false;
                trackStartedAt = -1L;
            }

            if (!trackRequested) {
                trackRequested = true;
                requestMusic(now);
                return;
            }

            if (!trackActive) {
                if (isCurrentGalleryMusic()) {
                    trackActive = true;
                    trackStartedAt = now;
                } else {
                    retryMusic(now);
                }
                return;
            }

            long elapsed = now - trackStartedAt;
            if (elapsed >= GALLERY_MUSIC_DURATION_NANOS) {
                beginSilence(now);
                return;
            }

            if (isCurrentGalleryMusic()) return;

            // Starsector can release a non-looping stream shortly before its
            // sample-true endpoint. Near the known end, treat that as the end
            // of the song instead of accidentally restarting it from zero.
            if (elapsed >= GALLERY_MUSIC_DURATION_NANOS
                    - GALLERY_MUSIC_END_TOLERANCE_NANOS) {
                beginSilence(now);
                return;
            }

            // A music-switcher mod or late encounter callback replaced the
            // stream early. Reassert it at a bounded rate and restart timing
            // only after the audio thread confirms the requested OGG.
            trackActive = false;
            trackStartedAt = -1L;
            retryMusic(now);
        }

        private void beginSilence(long now) {
            try {
                Global.getSoundPlayer().pauseCustomMusic();
                Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
            } catch (RuntimeException ex) {
                System.err.println(
                        "Hall of Triumph: failed to begin the Gallery "
                                + "music rest.");
                ex.printStackTrace(System.err);
                restoreDefaultMusic();
                return;
            }
            silent = true;
            silenceStartedAt = now;
            trackRequested = false;
            trackActive = false;
            trackStartedAt = -1L;
        }

        private void retryMusic(long now) {
            if (isCurrentGalleryMusic()) return;
            if (lastMusicRequestAt >= 0L
                    && now - lastMusicRequestAt
                            < GALLERY_MUSIC_RETRY_NANOS) {
                return;
            }
            requestMusic(now);
        }

        private void requestMusic(long now) {
            lastMusicRequestAt = now;
            try {
                Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
                Global.getSoundPlayer().playCustomMusic(
                        0, 0, GALLERY_MUSIC_SET, false);
            } catch (RuntimeException ex) {
                System.err.println(
                        "Hall of Triumph: failed to start Gallery tour "
                                + "music.");
                ex.printStackTrace(System.err);
                restoreDefaultMusic();
            }
        }

        private boolean isCurrentGalleryMusic() {
            try {
                return GALLERY_MUSIC_FILE.equals(
                        Global.getSoundPlayer().getCurrentMusicId());
            } catch (RuntimeException ex) {
                return false;
            }
        }

        private void restoreDefaultMusic() {
            restored = true;
            restoreGalleryMusic();
        }
    }

    private static final class TourCombatPlugin
            extends BaseEveryFrameCombatPlugin {
        private final Session session;
        private CombatEngineAPI engine;
        private boolean positioned;
        private float introCameraRemaining;

        private TourCombatPlugin(Session session) {
            this.session = session;
        }

        @Override
        public void init(CombatEngineAPI engine) {
            this.engine = engine;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (engine == null) return;
            ShipAPI shuttle = engine.getPlayerShip();
            if (shuttle == null) {
                shuttle = engine.getFleetManager(FleetSide.PLAYER)
                        .getShipFor(session.shuttle);
                if (shuttle != null) engine.setPlayerShipExternal(shuttle);
            }
            if (shuttle == null) return;

            if (!positioned) {
                shuttle.getLocation().set(0f, session.shuttleStartY);
                shuttle.getVelocity().set(0f, 0f);
                shuttle.setFacing(90f);
                ViewportAPI viewport = engine.getViewport();
                if (viewport != null) {
                    viewport.setExternalControl(true);
                    viewport.setCenter(new Vector2f(shuttle.getLocation()));
                    viewport.setViewMult(INTRO_VIEW_MULT);
                    introCameraRemaining = INTRO_CAMERA_SECONDS;
                }
                // A regular campaign battle opens paused for deployment. This
                // scene has only one pre-deployed shuttle, so start the tour.
                if (engine.isPaused()) engine.setPaused(false);
                positioned = true;
            }
            if (introCameraRemaining > 0f) {
                ViewportAPI viewport = engine.getViewport();
                if (viewport != null) {
                    viewport.setCenter(new Vector2f(shuttle.getLocation()));
                    viewport.setViewMult(INTRO_VIEW_MULT);
                }
                introCameraRemaining -= Math.max(0f, amount);
                if (introCameraRemaining <= 0f && viewport != null) {
                    viewport.setExternalControl(false);
                }
            }
            makeInvulnerable(shuttle);
            shuttle.setCurrentCR(1f);
            engine.maintainStatusForPlayerShip(
                    "ship_trophy_gallery_tour_status",
                    STATUS_ICON,
                    ShipTrophyL10n.get("gallery_tour_status"),
                    ShipTrophyL10n.get("gallery_tour_instructions"),
                    false);
        }

        @Override
        public void processInputPreCoreControls(
                float amount, List<InputEventAPI> events) {
            if (engine == null || events == null) return;
            for (InputEventAPI event : events) {
                if (event == null || event.isConsumed()
                        || !event.isKeyboardEvent()
                        || !event.isKeyDownEvent()
                        || event.getEventValue() != Keyboard.KEY_G) {
                    continue;
                }
                event.consume();
                restoreGalleryMusic();
                engine.setDoNotEndCombat(false);
                engine.endCombat(0f, FleetSide.PLAYER);
                return;
            }
        }

        private static void makeInvulnerable(ShipAPI ship) {
            MutableShipStatsAPI stats = ship.getMutableStats();
            stats.getHullDamageTakenMult().modifyMult(INVULNERABLE_ID, 0f);
            stats.getArmorDamageTakenMult().modifyMult(INVULNERABLE_ID, 0f);
            stats.getShieldDamageTakenMult().modifyMult(INVULNERABLE_ID, 0f);
            stats.getEmpDamageTakenMult().modifyMult(INVULNERABLE_ID, 0f);
            stats.getEngineDamageTakenMult().modifyMult(INVULNERABLE_ID, 0f);
            stats.getWeaponDamageTakenMult().modifyMult(INVULNERABLE_ID, 0f);
        }
    }

    /** Draws the generated Hall interior as the physical floor of the map. */
    private static final class HallBackdrop
            extends BaseCombatLayeredRenderingPlugin {
        private final Session session;
        private final SpriteAPI floor;

        private HallBackdrop(Session session) {
            this.session = session;
            SpriteAPI loaded = null;
            try {
                Global.getSettings().loadTexture(BACKGROUND);
                loaded = Global.getSettings().getSprite(BACKGROUND);
            } catch (Throwable ignored) {
            }
            floor = loaded;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
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
            if (layer != CombatEngineLayers.ABOVE_PLANETS || floor == null) return;
            SpriteAPI sprite = floor;
            float oldWidth = 0f;
            float oldHeight = 0f;
            float oldCenterX = 0f;
            float oldCenterY = 0f;
            float oldAngle = 0f;
            float oldAlpha = 1f;
            Color oldColor = Color.WHITE;
            int oldBlendSource = GL11.GL_SRC_ALPHA;
            int oldBlendDestination = GL11.GL_ONE_MINUS_SRC_ALPHA;
            boolean captured = false;
            try {
                oldWidth = sprite.getWidth();
                oldHeight = sprite.getHeight();
                oldCenterX = sprite.getCenterX();
                oldCenterY = sprite.getCenterY();
                oldAngle = sprite.getAngle();
                oldAlpha = sprite.getAlphaMult();
                oldColor = sprite.getColor();
                oldBlendSource = sprite.getBlendSrc();
                oldBlendDestination = sprite.getBlendDest();
                captured = true;

                float width = session.hallWidth;
                float height = session.hallHeight;
                sprite.setSize(width, height);
                sprite.setCenter(width * 0.5f, height * 0.5f);
                sprite.setAngle(0f);
                sprite.setColor(new Color(218, 228, 232));
                sprite.setAlphaMult(1f);
                sprite.setNormalBlend();
                sprite.renderAtCenter(0f, 0f);
            } catch (Throwable ignored) {
                // The neutral native background remains as a safe fallback.
            } finally {
                if (sprite != null && captured) {
                    try {
                        sprite.setSize(oldWidth, oldHeight);
                        sprite.setCenter(oldCenterX, oldCenterY);
                        sprite.setAngle(oldAngle);
                        sprite.setColor(oldColor == null ? Color.WHITE : oldColor);
                        sprite.setAlphaMult(oldAlpha);
                        sprite.setBlendFunc(oldBlendSource, oldBlendDestination);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

    private static final class ExhibitRenderer
            extends BaseCombatLayeredRenderingPlugin {
        private final Session session;

        private ExhibitRenderer(Session session) {
            this.session = session;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.BELOW_SHIPS_LAYER);
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
            if (layer != CombatEngineLayers.BELOW_SHIPS_LAYER) return;
            for (Exhibit exhibit : session.exhibits) {
                renderExhibit(exhibit, session.displayScale);
            }
        }

        private static void renderExhibit(Exhibit exhibit, float scale) {
            float width = exhibit.nativeWidth * scale;
            float height = exhibit.nativeHeight * scale;
            drawBerthClamps(exhibit.x, exhibit.y, height, width);

            SpriteAPI sprite = null;
            float oldWidth = 0f;
            float oldHeight = 0f;
            float oldCenterX = 0f;
            float oldCenterY = 0f;
            float oldAngle = 0f;
            float oldAlpha = 1f;
            Color oldColor = Color.WHITE;
            int oldBlendSource = GL11.GL_SRC_ALPHA;
            int oldBlendDestination = GL11.GL_ONE_MINUS_SRC_ALPHA;
            boolean captured = false;
            try {
                sprite = Global.getSettings().getSprite(exhibit.spriteName);
                oldWidth = sprite.getWidth();
                oldHeight = sprite.getHeight();
                oldCenterX = sprite.getCenterX();
                oldCenterY = sprite.getCenterY();
                oldAngle = sprite.getAngle();
                oldAlpha = sprite.getAlphaMult();
                oldColor = sprite.getColor();
                oldBlendSource = sprite.getBlendSrc();
                oldBlendDestination = sprite.getBlendDest();
                captured = true;

                sprite.setSize(width, height);
                sprite.setCenter(width * 0.5f, height * 0.5f);
                sprite.setAngle(exhibit.angle);
                sprite.setColor(Color.WHITE);
                sprite.setAlphaMult(0.96f);
                sprite.setNormalBlend();
                sprite.renderAtCenter(exhibit.x, exhibit.y);
            } catch (Throwable ex) {
                // One broken third-party sprite must not blank the whole row.
            } finally {
                if (sprite != null && captured) {
                    try {
                        sprite.setSize(oldWidth, oldHeight);
                        sprite.setCenter(oldCenterX, oldCenterY);
                        sprite.setAngle(oldAngle);
                        sprite.setColor(oldColor == null ? Color.WHITE : oldColor);
                        sprite.setAlphaMult(oldAlpha);
                        sprite.setBlendFunc(oldBlendSource, oldBlendDestination);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        private static void drawBerthClamps(
                float centerX, float centerY, float shipWidth, float shipHeight) {
            float halfWidth = Math.max(72f, shipWidth * 0.56f + 26f);
            float halfHeight = Math.max(72f, shipHeight * 0.56f + 26f);
            float bracket = Math.max(24f,
                    Math.min(58f, Math.min(halfWidth, halfHeight) * 0.34f));
            float left = centerX - halfWidth;
            float right = centerX + halfWidth;
            float bottom = centerY - halfHeight;
            float top = centerY + halfHeight;

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(2f);
            GL11.glColor4f(0.92f, 0.64f, 0.20f, 0.58f);
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(left, bottom);
            GL11.glVertex2f(left + bracket, bottom);
            GL11.glVertex2f(left, bottom);
            GL11.glVertex2f(left, bottom + bracket);
            GL11.glVertex2f(right, bottom);
            GL11.glVertex2f(right - bracket, bottom);
            GL11.glVertex2f(right, bottom);
            GL11.glVertex2f(right, bottom + bracket);
            GL11.glVertex2f(left, top);
            GL11.glVertex2f(left + bracket, top);
            GL11.glVertex2f(left, top);
            GL11.glVertex2f(left, top - bracket);
            GL11.glVertex2f(right, top);
            GL11.glVertex2f(right - bracket, top);
            GL11.glVertex2f(right, top);
            GL11.glVertex2f(right, top - bracket);
            GL11.glEnd();
            GL11.glPopAttrib();
        }
    }

    /** Waits for the comm-directory contact screen to finish closing. */
    private static final class OpenTourDialogScript
            implements EveryFrameScript {
        private final TourDialogPlugin plugin;
        private final SectorEntityToken target;
        private final PersonAPI activePerson;
        private boolean done;

        private OpenTourDialogScript(
                TourDialogPlugin plugin,
                SectorEntityToken target,
                PersonAPI activePerson) {
            this.plugin = plugin;
            this.target = target;
            this.activePerson = activePerson;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }

        @Override
        public void advance(float amount) {
            SectorAPI sector = Global.getSector();
            if (sector == null) {
                done = true;
                return;
            }
            if (sector.getCampaignUI().getCurrentInteractionDialog() != null) {
                return;
            }
            if (target != null && activePerson != null) {
                target.setActivePerson(activePerson);
            }
            done = sector.getCampaignUI().showInteractionDialog(
                    plugin, target);
        }
    }

    /** Owns both the disposable battle and its supported campaign return. */
    private static final class TourDialogPlugin
            implements InteractionDialogPlugin {
        private static final String CLOSE_OPTION =
                "ship_trophy_gallery_tour_close";

        private final InteractionDialogPlugin delegate;
        private final SectorEntityToken target;
        private final PersonAPI activePerson;
        private final BattleCreationContext context;
        private final CampaignFleetAPI hall;
        private final long previousBattleTimestamp;
        private final boolean previousBattleWon;
        private final long previousBattleSeed;
        private InteractionDialogAPI dialog;
        private boolean started;
        private boolean cleaned;

        private TourDialogPlugin(
                InteractionDialogPlugin delegate,
                SectorEntityToken target,
                PersonAPI activePerson,
                BattleCreationContext context,
                CampaignFleetAPI hall) {
            this.delegate = delegate;
            this.target = target;
            this.activePerson = activePerson;
            this.context = context;
            this.hall = hall;
            SectorAPI sector = Global.getSector();
            previousBattleTimestamp = sector == null
                    ? 0L : sector.getLastPlayerBattleTimestamp();
            previousBattleWon = sector != null && sector.isLastPlayerBattleWon();
            previousBattleSeed = sector == null
                    ? 0L : sector.getPlayerBattleSeed();
        }

        @Override
        public void init(InteractionDialogAPI dialog) {
            this.dialog = dialog;
        }

        @Override
        public void optionSelected(String optionText, Object optionData) {
            if (CLOSE_OPTION.equals(optionData) && dialog != null) {
                dialog.dismiss();
            }
        }

        @Override
        public void optionMousedOver(String optionText, Object optionData) {
        }

        @Override
        public void advance(float amount) {
            if (!started) {
                started = true;
                try {
                    dialog.startBattle(context);
                } catch (Throwable ex) {
                    System.err.println(
                            "[Hall of Triumph] Gallery tour launch failed.");
                    ex.printStackTrace(System.err);
                    cleanupBattleState();
                    showFallback(ShipTrophyL10n.get("gallery_launch_failed"));
                }
            }
        }

        @Override
        public void backFromEngagement(
                com.fs.starfarer.api.combat.EngagementResultAPI result) {
            cleanupBattleState();
            if (target != null && activePerson != null) {
                target.setActivePerson(activePerson);
            }
            if (dialog != null && delegate != null) {
                try {
                    dialog.setPlugin(delegate);
                    delegate.init(dialog);
                    return;
                } catch (Throwable ex) {
                    System.err.println(
                            "[Hall of Triumph] Could not restore the colony dialog.");
                    ex.printStackTrace(System.err);
                    dialog.setPlugin(this);
                }
            }
            showFallback(ShipTrophyL10n.get("gallery_return_failed"));
        }

        @Override
        public Object getContext() {
            return delegate == null ? null : delegate.getContext();
        }

        @Override
        public Map<String, MemoryAPI> getMemoryMap() {
            return delegate == null
                    ? Collections.<String, MemoryAPI>emptyMap()
                    : delegate.getMemoryMap();
        }

        private void cleanupBattleState() {
            restoreGalleryMusic();
            if (cleaned) return;
            cleaned = true;
            if (hall != null) {
                hall.getMemoryWithoutUpdate().unset(MARKER_KEY);
                hall.getMemoryWithoutUpdate().unset(SESSION_KEY);
            }
            SectorAPI sector = Global.getSector();
            if (sector != null) {
                sector.setLastPlayerBattleTimestamp(previousBattleTimestamp);
                sector.setLastPlayerBattleWon(previousBattleWon);
                sector.setPlayerBattleSeed(previousBattleSeed);
                sector.addTransientScript(new RestoreBattleStateScript(
                        previousBattleTimestamp,
                        previousBattleWon,
                        previousBattleSeed));
            }
        }

        private void showFallback(String message) {
            if (dialog == null) return;
            dialog.getTextPanel().addPara(message);
            dialog.getOptionPanel().clearOptions();
            String close = ShipTrophyL10n.get("gallery_close_channel");
            dialog.getOptionPanel().addOption(close, CLOSE_OPTION);
            dialog.setOptionOnEscape(close, CLOSE_OPTION);
        }
    }

    /** CampaignState writes its battle timestamp after backFromEngagement. */
    private static final class RestoreBattleStateScript
            implements EveryFrameScript {
        private final long timestamp;
        private final boolean won;
        private final long seed;
        private boolean done;

        private RestoreBattleStateScript(
                long timestamp, boolean won, long seed) {
            this.timestamp = timestamp;
            this.won = won;
            this.seed = seed;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }

        @Override
        public void advance(float amount) {
            SectorAPI sector = Global.getSector();
            if (sector != null) {
                sector.setLastPlayerBattleTimestamp(timestamp);
                sector.setLastPlayerBattleWon(won);
                sector.setPlayerBattleSeed(seed);
            }
            done = true;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
