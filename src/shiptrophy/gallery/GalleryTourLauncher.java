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

/** Launches the disposable, read-only combat view of a Gallery filmstrip. */
public final class GalleryTourLauncher {
    private static final String MARKER_KEY =
            "$ship_trophy_gallery_tour_fleet";
    private static final String SESSION_KEY =
            "$ship_trophy_gallery_tour_session";
    private static final String BACKGROUND =
            "graphics/backgrounds/ship_trophy_gallery_tour.png";
    private static final String NATIVE_BACKGROUND =
            "graphics/backgrounds/wormhole_dest_black.jpg";
    private static final String STATUS_ICON =
            "graphics/icons/industry/trophy_room.png";
    private static final String KITE_VARIANT = "kite_original_Stock";
    private static final String INVULNERABLE_ID =
            "ship_trophy_gallery_tour_invulnerable";

    private static final float MIN_MAP_WIDTH = 7000f;
    private static final float MAX_MAP_WIDTH = 20000f;
    private static final float MIN_MAP_HEIGHT = 6000f;
    private static final float MAX_MAP_HEIGHT = 12000f;
    private static final float MAP_MARGIN = 2400f;
    private static final float EXHIBIT_GAP = 260f;
    private static final float EXHIBIT_Y = 350f;
    private static final float SHUTTLE_START_Y = -650f;
    private static final float INTRO_CAMERA_SECONDS = 0.35f;
    private static final float INTRO_VIEW_MULT = 1.1f;

    private GalleryTourLauncher() {
    }

    static void launch(
            final InteractionDialogAPI dialog,
            final List<FleetMemberAPI> currentWindow,
            final FleetMemberAPI selectedShuttle) {
        if (dialog == null || currentWindow == null || currentWindow.isEmpty()) {
            return;
        }

        SectorAPI sector = Global.getSector();
        if (sector == null) return;

        // customDialogConfirm() runs before Starsector clears the Gallery
        // modal. Defer the direct launch until the following campaign frame,
        // then close Isa's contact screen through the supported return path.
        sector.addTransientScript(new LaunchTourAfterGalleryScript(
                dialog,
                new ArrayList<FleetMemberAPI>(currentWindow),
                selectedShuttle));
    }

    private static final class LaunchTourAfterGalleryScript
            implements EveryFrameScript {
        private final InteractionDialogAPI dialog;
        private final List<FleetMemberAPI> currentWindow;
        private final FleetMemberAPI selectedShuttle;
        private boolean waitedOneFrame;
        private boolean done;

        private LaunchTourAfterGalleryScript(
                InteractionDialogAPI dialog,
                List<FleetMemberAPI> currentWindow,
                FleetMemberAPI selectedShuttle) {
            this.dialog = dialog;
            this.currentWindow = currentWindow;
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
                        "The Gallery has no preserved frigate with "
                                + "Civilian-grade Hull available as a tour shuttle.");
                return;
            }
            launchSelected(dialog, currentWindow, selectedShuttle);
        }
    }

    private static void launchSelected(
            InteractionDialogAPI dialog,
            List<FleetMemberAPI> currentWindow,
            FleetMemberAPI selected) {

        List<Exhibit> exhibits = captureExhibits(currentWindow);
        if (exhibits.isEmpty()) {
            dialog.getTextPanel().addPara(
                    "The gallery shuttle cannot resolve any display images for this row.");
            return;
        }

        CampaignFleetAPI shuttle = Global.getFactory().createEmptyFleet(
                Factions.PLAYER, "Gallery Shuttle", false);
        CampaignFleetAPI hall = Global.getFactory().createEmptyFleet(
                Factions.NEUTRAL, "Hall of Triumph Exhibits", false);
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
        hallIndex.setShipName("Gallery Exhibit Index");
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
        }
        return Collections.unmodifiableList(result);
    }

    private static final class Exhibit {
        private final String spriteName;
        private final float nativeWidth;
        private final float nativeHeight;
        private float x;

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

        private Session(List<Exhibit> exhibits, FleetMemberAPI shuttle) {
            this.exhibits = exhibits;
            this.shuttle = shuttle;

            float nativeWidth = 0f;
            float nativeHeight = 0f;
            for (Exhibit exhibit : exhibits) {
                nativeWidth += exhibit.nativeWidth;
                nativeHeight = Math.max(nativeHeight, exhibit.nativeHeight);
            }
            float gaps = EXHIBIT_GAP * Math.max(0, exhibits.size() - 1);
            float availableWidth = MAX_MAP_WIDTH - MAP_MARGIN - gaps;
            float availableHeight = MAX_MAP_HEIGHT - MAP_MARGIN;
            float widthScale = nativeWidth <= availableWidth
                    ? 1f : Math.max(0.1f, availableWidth / nativeWidth);
            float heightScale = nativeHeight <= availableHeight
                    ? 1f : Math.max(0.1f, availableHeight / nativeHeight);
            displayScale = Math.min(widthScale, heightScale);
            float rowWidth = nativeWidth * displayScale + gaps;
            mapWidth = Math.max(MIN_MAP_WIDTH,
                    Math.min(MAX_MAP_WIDTH, rowWidth + MAP_MARGIN));
            mapHeight = Math.max(MIN_MAP_HEIGHT,
                    Math.min(MAX_MAP_HEIGHT,
                            nativeHeight * displayScale + MAP_MARGIN));

            float cursor = -rowWidth * 0.5f;
            for (Exhibit exhibit : exhibits) {
                float width = exhibit.nativeWidth * displayScale;
                exhibit.x = cursor + width * 0.5f;
                cursor += width + EXHIBIT_GAP;
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
                    "Leave gallery tour", "Return to Isa?");
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
                shuttle.getLocation().set(0f, SHUTTLE_START_Y);
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
                    "Hall of Triumph gallery tour",
                    "Fly north to the exhibits; press G to return to Isa",
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

                float overscan = 500f;
                float width = session.mapWidth + overscan;
                float height = session.mapHeight + overscan;
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
            drawPad(exhibit.x, EXHIBIT_Y, width, height);

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
                sprite.setAngle(0f);
                sprite.setColor(Color.WHITE);
                sprite.setAlphaMult(0.96f);
                sprite.setNormalBlend();
                sprite.renderAtCenter(exhibit.x, EXHIBIT_Y);
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

        private static void drawPad(
                float centerX, float centerY, float shipWidth, float shipHeight) {
            float radiusX = Math.max(90f, shipWidth * 0.58f + 42f);
            float radiusY = Math.max(52f, shipHeight * 0.22f + 28f);
            float padY = centerY - Math.max(18f, shipHeight * 0.23f);
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // A shallow cyan hologram pool, fading toward its edge.
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4f(0.10f, 0.62f, 0.68f, 0.18f);
            GL11.glVertex2f(centerX, padY);
            GL11.glColor4f(0.05f, 0.32f, 0.38f, 0.02f);
            for (int i = 0; i <= 40; i++) {
                double angle = Math.PI * 2d * i / 40d;
                GL11.glVertex2f(
                        centerX + (float) Math.cos(angle) * radiusX,
                        padY + (float) Math.sin(angle) * radiusY);
            }
            GL11.glEnd();

            GL11.glLineWidth(1.25f);
            GL11.glColor4f(0.23f, 0.76f, 0.80f, 0.48f);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int i = 0; i < 40; i++) {
                double angle = Math.PI * 2d * i / 40d;
                GL11.glVertex2f(
                        centerX + (float) Math.cos(angle) * radiusX,
                        padY + (float) Math.sin(angle) * radiusY);
            }
            GL11.glEnd();

            // A restrained amber registration mark grounds each exhibit.
            float markerHalf = Math.min(48f, radiusX * 0.32f);
            GL11.glLineWidth(2f);
            GL11.glColor4f(0.92f, 0.64f, 0.20f, 0.68f);
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(centerX - markerHalf, padY - radiusY - 8f);
            GL11.glVertex2f(centerX + markerHalf, padY - radiusY - 8f);
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
                    showFallback(
                            "The gallery shuttle fails to launch. "
                                    + "See starsector.log for details.");
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
                            "[Hall of Triumph] Could not restore Isa's contact dialog.");
                    ex.printStackTrace(System.err);
                    dialog.setPlugin(this);
                }
            }
            showFallback(
                    "The gallery shuttle docks safely, but the comm link to Isa "
                            + "does not reopen. Close this channel to return to campaign.");
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
            dialog.getOptionPanel().addOption(
                    "Close the channel.", CLOSE_OPTION);
            dialog.setOptionOnEscape("Close the channel.", CLOSE_OPTION);
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
