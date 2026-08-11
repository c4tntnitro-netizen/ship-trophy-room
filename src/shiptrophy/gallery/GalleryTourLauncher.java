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
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
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
    private static final float EXHIBIT_GAP = 190f;
    private static final float EXHIBIT_Y = 450f;
    private static final float KITE_START_Y = -750f;

    private GalleryTourLauncher() {
    }

    static void launch(
            InteractionDialogAPI dialog,
            List<FleetMemberAPI> currentWindow) {
        if (dialog == null || currentWindow == null || currentWindow.isEmpty()) {
            return;
        }

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

        FleetMemberAPI kite = Global.getFactory().createFleetMember(
                FleetMemberType.SHIP, KITE_VARIANT);
        if (kite == null) return;
        kite.setShipName("Gallery Shuttle");
        kite.setOwner(0);
        kite.setFlagship(true);
        kite.getRepairTracker().setMothballed(false);
        kite.getRepairTracker().setCR(1f);
        shuttle.getFleetData().addFleetMember(kite);
        shuttle.getFleetData().setFlagship(kite);
        // CampaignState recrews factory fleets before combat. Supplying the
        // shuttle's minimum crew keeps the sole Kite deployable through that
        // otherwise automatic synchronization step.
        shuttle.getCargo().addCrew((int) Math.ceil(kite.getMinCrew()));

        Session session = new Session(exhibits, kite);
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
        dialog.setPlugin(new TourDialogPlugin(
                dialog, delegate, context, hall));
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
        private final FleetMemberAPI kite;
        private final float displayScale;
        private final float mapWidth;
        private final float mapHeight;

        private Session(List<Exhibit> exhibits, FleetMemberAPI kite) {
            this.exhibits = exhibits;
            this.kite = kite;

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
            loader.addFleetMember(FleetSide.PLAYER, session.kite);
            loader.initMap(
                    -session.mapWidth * 0.5f,
                    session.mapWidth * 0.5f,
                    -session.mapHeight * 0.5f,
                    session.mapHeight * 0.5f);
            loader.setBackgroundSpriteName(BACKGROUND);
            loader.setBackgroundGlowColor(new Color(22, 70, 78, 70));
            loader.setHyperspaceMode(false);
            loader.addPlugin(new TourCombatPlugin(session));
        }

        @Override
        public void afterDefinitionLoad(CombatEngineAPI engine) {
            engine.setDoNotEndCombat(true);
            engine.setCustomExit(
                    "Leave gallery tour", "Return to Isa?");
            engine.setRenderStarfield(false);
            engine.setBackgroundColor(new Color(3, 7, 9));
            engine.setMaxFleetPoints(FleetSide.PLAYER, 9999);
            engine.setMaxFleetPoints(FleetSide.ENEMY, 9999);
            engine.getFleetManager(FleetSide.PLAYER)
                    .setSuppressDeploymentMessages(true);
            engine.getFleetManager(FleetSide.ENEMY)
                    .setSuppressDeploymentMessages(true);
            engine.addLayeredRenderingPlugin(new ExhibitRenderer(session));
        }
    }

    private static final class TourCombatPlugin
            extends BaseEveryFrameCombatPlugin {
        private final Session session;
        private CombatEngineAPI engine;
        private boolean positioned;

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
            ShipAPI kite = engine.getPlayerShip();
            if (kite == null) {
                kite = engine.getFleetManager(FleetSide.PLAYER)
                        .getShipFor(session.kite);
                if (kite != null) engine.setPlayerShipExternal(kite);
            }
            if (kite == null) return;

            if (!positioned) {
                kite.getLocation().set(0f, KITE_START_Y);
                kite.getVelocity().set(0f, 0f);
                kite.setFacing(90f);
                positioned = true;
            }
            makeInvulnerable(kite);
            kite.setCurrentCR(1f);
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
            float width = Math.max(150f, shipWidth + 70f);
            float height = Math.max(150f, shipHeight + 70f);
            float left = centerX - width * 0.5f;
            float bottom = centerY - height * 0.5f;
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(0.03f, 0.25f, 0.29f, 0.22f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(left, bottom);
            GL11.glVertex2f(left + width, bottom);
            GL11.glVertex2f(left + width, bottom + height);
            GL11.glVertex2f(left, bottom + height);
            GL11.glEnd();
            GL11.glLineWidth(2f);
            GL11.glColor4f(0.80f, 0.59f, 0.18f, 0.62f);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2f(left, bottom);
            GL11.glVertex2f(left + width, bottom);
            GL11.glVertex2f(left + width, bottom + height);
            GL11.glVertex2f(left, bottom + height);
            GL11.glEnd();
            GL11.glPopAttrib();
        }
    }

    /**
     * Defers startBattle until the custom Gallery modal has finished closing,
     * then restores Isa's original rules dialog after the disposable combat.
     */
    private static final class TourDialogPlugin
            implements InteractionDialogPlugin {
        private final InteractionDialogAPI dialog;
        private final InteractionDialogPlugin delegate;
        private final BattleCreationContext context;
        private final CampaignFleetAPI hall;
        private final long previousBattleTimestamp;
        private final boolean previousBattleWon;
        private final long previousBattleSeed;
        private boolean started;
        private boolean cleaned;

        private TourDialogPlugin(
                InteractionDialogAPI dialog,
                InteractionDialogPlugin delegate,
                BattleCreationContext context,
                CampaignFleetAPI hall) {
            this.dialog = dialog;
            this.delegate = delegate;
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
            // The delegate is already initialized; do not reset Isa's dialog.
        }

        @Override
        public void optionSelected(String optionText, Object optionData) {
            if (delegate != null) delegate.optionSelected(optionText, optionData);
        }

        @Override
        public void optionMousedOver(String optionText, Object optionData) {
            if (delegate != null) delegate.optionMousedOver(optionText, optionData);
        }

        @Override
        public void advance(float amount) {
            if (!started) {
                started = true;
                try {
                    dialog.startBattle(context);
                } catch (Throwable ex) {
                    cleanup();
                    dialog.getTextPanel().addPara(
                            "The gallery shuttle fails to launch.");
                }
                return;
            }
            if (delegate != null) delegate.advance(amount);
        }

        @Override
        public void backFromEngagement(
                com.fs.starfarer.api.combat.EngagementResultAPI result) {
            cleanup();
            if (delegate != null) delegate.backFromEngagement(result);
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

        private void cleanup() {
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
            if (delegate != null) dialog.setPlugin(delegate);
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
