package shiptrophy.campaign.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * A non-damaging traffic-control field around Gan Eden's forced-perspective
 * triangular aperture. The backdrop and the collision boundary share the same
 * normalized geometry, so fleets rebound from the structure they can see.
 */
public class AltitudeWarningTerrainPlugin extends BaseRingTerrain {
    private static final Color WARNING_COLOR = new Color(255, 105, 55);
    private static final float SURFACE_RETURN_SPEED = 90f;
    private static final float DEEP_RETURN_SPEED = 220f;
    private static final float WARNING_DEPTH = 430f;
    private static final float EMERGENCY_BACKSTOP_DEPTH = 520f;
    private static final float BACKDROP_WIDTH = 6200f;
    private static final float BACKDROP_HEIGHT = 3485f;
    private static final float BLACK_BACKDROP_SIZE = 20000f;

    // The original reference placed the sun at approximately (0.16, 0.68).
    // Offset the cleaned plate so Starsector's real star, at world origin,
    // occupies that cleared location.
    private static final float STAR_U = 0.16f;
    private static final float STAR_V = 0.70f;
    private static final float BACKDROP_OFFSET_X =
            (0.5f - STAR_U) * BACKDROP_WIDTH;
    private static final float BACKDROP_OFFSET_Y =
            (STAR_V - 0.5f) * BACKDROP_HEIGHT;

    /**
     * Clockwise in image coordinates; the Y conversion below turns this into
     * a counter-clockwise world polygon. Extra vertices follow the rounded
     * left and lower shell edges while retaining the reference's triangular
     * silhouette.
     */
    private static final float[][] APERTURE_UV = new float[][] {
            {0.27f, 0.015f},
            {0.70f, 0.015f},
            {0.755f, 0.81f},
            {0.70f, 0.87f},
            {0.58f, 0.925f},
            {0.12f, 0.925f},
            {0.06f, 0.87f},
            {0.07f, 0.66f},
            {0.14f, 0.33f}
    };

    private static final String WARNING_RECENT_KEY =
            "$shipTrophyGanEdenAltitudeWarningRecent";

    private transient SpriteAPI backdropTexture;

    public void reconfigure(SectorEntityToken center, float width, float middle) {
        if (params == null) {
            params = new RingParams(width, middle, center, "Altitude Warning");
        } else {
            params.bandWidthInEngine = width;
            params.middleRadius = middle;
            params.relatedEntity = center;
            params.name = "Altitude Warning";
        }
        name = "Altitude Warning";
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (Global.getSector() == null
                || Global.getSector().getPlayerFleet() == null) {
            return;
        }
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (containsEntity(player)
                && !player.getMemoryWithoutUpdate()
                        .getBoolean(WARNING_RECENT_KEY)) {
            player.addFloatingText(
                    "ALTITUDE WARNING", WARNING_COLOR, 0.8f, true);
            player.getMemoryWithoutUpdate().set(
                    WARNING_RECENT_KEY, true, 2f);
        }
    }

    @Override
    public boolean containsEntity(SectorEntityToken token) {
        if (token == null) return false;
        return containsPoint(token.getLocation(), token.getRadius());
    }

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        BoundarySample sample = sampleBoundary(point);
        if (sample == null) return false;
        return !sample.inside
                || sample.distance <= WARNING_DEPTH + Math.max(0f, radius);
    }

    @Override
    public void applyEffect(SectorEntityToken token, float amount) {
        if (!(token instanceof CampaignFleetAPI)) return;

        BoundarySample sample = sampleBoundary(token.getLocation());
        if (sample == null
                || (sample.inside && sample.distance > WARNING_DEPTH)) {
            return;
        }

        CampaignFleetAPI fleet = (CampaignFleetAPI) token;
        Vector2f inward = computeInwardDirection(
                fleet.getLocation(), sample);
        float approach = sample.inside
                ? smoothStep(clamp(
                        (WARNING_DEPTH - sample.distance) / WARNING_DEPTH))
                : 1f;
        float penetration = sample.inside
                ? 0f
                : smoothStep(clamp(
                        sample.distance / EMERGENCY_BACKSTOP_DEPTH));

        Vector2f velocity = fleet.getVelocity();
        float outwardVelocity =
                -(velocity.x * inward.x + velocity.y * inward.y);
        float desiredOutwardVelocity = -(
                SURFACE_RETURN_SPEED * approach
                        + (DEEP_RETURN_SPEED - SURFACE_RETURN_SPEED)
                                * penetration);

        if (outwardVelocity > desiredOutwardVelocity) {
            float responsePerSecond =
                    0.55f + 3.25f * approach + 4.2f * penetration;
            float blend = 1f - (float) Math.exp(
                    -responsePerSecond * Math.max(0f, amount));
            float correction =
                    (outwardVelocity - desiredOutwardVelocity) * blend;
            fleet.setVelocity(
                    velocity.x + inward.x * correction,
                    velocity.y + inward.y * correction);
        }

        // Extreme modded campaign speeds can cross the soft field in one
        // frame. Put the fleet just inside the visible frame and preserve the
        // same spring-like inward motion instead of letting it escape beneath
        // the painted shell.
        if (!sample.inside
                && sample.distance > EMERGENCY_BACKSTOP_DEPTH) {
            fleet.setLocation(
                    sample.closest.x + inward.x * 12f,
                    sample.closest.y + inward.y * 12f);

            Vector2f corrected = fleet.getVelocity();
            float correctedOutward =
                    -(corrected.x * inward.x + corrected.y * inward.y);
            if (correctedOutward > -DEEP_RETURN_SPEED) {
                float correction = correctedOutward + DEEP_RETURN_SPEED;
                fleet.setVelocity(
                        corrected.x + inward.x * correction,
                        corrected.y + inward.y * correction);
            }
        }
    }

    @Override
    public String getEffectCategory() {
        return "ship_trophy_altitude_warning";
    }

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_1);
    }

    @Override
    public float getRenderRange() {
        return 7000f;
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (layer != CampaignEngineLayers.TERRAIN_1
                || params == null
                || params.relatedEntity == null) {
            return;
        }

        Vector2f star = params.relatedEntity.getLocation();
        if (!viewport.isNearViewport(star, getRenderRange())) return;
        ensureTexture();

        Vector2f plateCenter = getBackdropCenter(star);
        float alpha = viewport.getAlphaMult();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            renderSolidQuad(
                    star,
                    BLACK_BACKDROP_SIZE,
                    BLACK_BACKDROP_SIZE,
                    new Color(0, 0, 0, 255),
                    alpha);
            renderCenteredSprite(
                    backdropTexture,
                    plateCenter,
                    BACKDROP_WIDTH,
                    BACKDROP_HEIGHT,
                    new Color(255, 255, 255, 255),
                    alpha);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void ensureTexture() {
        if (backdropTexture == null) {
            backdropTexture = Global.getSettings().getSprite(
                    "ship_trophy_gan_eden", "triangle_backdrop");
        }
    }

    private BoundarySample sampleBoundary(Vector2f point) {
        if (point == null
                || params == null
                || params.relatedEntity == null) {
            return null;
        }

        Vector2f star = params.relatedEntity.getLocation();
        Vector2f plateCenter = getBackdropCenter(star);
        boolean inside = false;
        float bestDistanceSquared = Float.MAX_VALUE;
        Vector2f closest = null;

        int count = APERTURE_UV.length;
        for (int i = 0, previous = count - 1; i < count; previous = i++) {
            Vector2f a = toWorld(APERTURE_UV[previous], plateCenter);
            Vector2f b = toWorld(APERTURE_UV[i], plateCenter);

            if ((a.y > point.y) != (b.y > point.y)) {
                float intersectionX = (b.x - a.x)
                        * (point.y - a.y) / (b.y - a.y) + a.x;
                if (point.x < intersectionX) inside = !inside;
            }

            Vector2f edgePoint = closestPointOnSegment(point, a, b);
            float dx = point.x - edgePoint.x;
            float dy = point.y - edgePoint.y;
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared < bestDistanceSquared) {
                bestDistanceSquared = distanceSquared;
                closest = edgePoint;
            }
        }

        return new BoundarySample(
                inside,
                (float) Math.sqrt(bestDistanceSquared),
                closest,
                polygonCentroid(plateCenter));
    }

    private static Vector2f computeInwardDirection(
            Vector2f point,
            BoundarySample sample) {
        float dx;
        float dy;
        if (sample.inside) {
            dx = point.x - sample.closest.x;
            dy = point.y - sample.closest.y;
        } else {
            dx = sample.closest.x - point.x;
            dy = sample.closest.y - point.y;
        }

        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001f) {
            dx = sample.centroid.x - point.x;
            dy = sample.centroid.y - point.y;
            length = (float) Math.sqrt(dx * dx + dy * dy);
        }
        if (length < 0.001f) return new Vector2f(0f, 1f);
        return new Vector2f(dx / length, dy / length);
    }

    private static Vector2f closestPointOnSegment(
            Vector2f point,
            Vector2f a,
            Vector2f b) {
        float edgeX = b.x - a.x;
        float edgeY = b.y - a.y;
        float lengthSquared = edgeX * edgeX + edgeY * edgeY;
        if (lengthSquared <= 0f) return new Vector2f(a);

        float t = ((point.x - a.x) * edgeX
                + (point.y - a.y) * edgeY) / lengthSquared;
        t = clamp(t);
        return new Vector2f(a.x + edgeX * t, a.y + edgeY * t);
    }

    private static Vector2f polygonCentroid(Vector2f plateCenter) {
        float x = 0f;
        float y = 0f;
        for (float[] uv : APERTURE_UV) {
            Vector2f vertex = toWorld(uv, plateCenter);
            x += vertex.x;
            y += vertex.y;
        }
        return new Vector2f(x / APERTURE_UV.length, y / APERTURE_UV.length);
    }

    private static Vector2f getBackdropCenter(Vector2f star) {
        return new Vector2f(
                star.x + BACKDROP_OFFSET_X,
                star.y + BACKDROP_OFFSET_Y);
    }

    private static Vector2f toWorld(float[] uv, Vector2f plateCenter) {
        return new Vector2f(
                plateCenter.x + (uv[0] - 0.5f) * BACKDROP_WIDTH,
                plateCenter.y + (0.5f - uv[1]) * BACKDROP_HEIGHT);
    }

    private static void renderCenteredSprite(
            SpriteAPI texture,
            Vector2f center,
            float width,
            float height,
            Color color,
            float alphaMult) {
        if (texture == null) return;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        texture.bindTexture();
        setColor(color, alphaMult);

        float texX = texture.getTexX();
        float texY = texture.getTexY();
        float texWidth = texture.getTexWidth();
        float texHeight = texture.getTexHeight();
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(texX, texY + texHeight);
        GL11.glVertex2f(center.x - halfWidth, center.y - halfHeight);
        GL11.glTexCoord2f(texX + texWidth, texY + texHeight);
        GL11.glVertex2f(center.x + halfWidth, center.y - halfHeight);
        GL11.glTexCoord2f(texX + texWidth, texY);
        GL11.glVertex2f(center.x + halfWidth, center.y + halfHeight);
        GL11.glTexCoord2f(texX, texY);
        GL11.glVertex2f(center.x - halfWidth, center.y + halfHeight);
        GL11.glEnd();
    }

    private static void renderSolidQuad(
            Vector2f center,
            float width,
            float height,
            Color color,
            float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        setColor(color, alphaMult);
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(center.x - halfWidth, center.y - halfHeight);
        GL11.glVertex2f(center.x + halfWidth, center.y - halfHeight);
        GL11.glVertex2f(center.x + halfWidth, center.y + halfHeight);
        GL11.glVertex2f(center.x - halfWidth, center.y + halfHeight);
        GL11.glEnd();
    }

    private static void setColor(Color color, float alphaMult) {
        GL11.glColor4f(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f * alphaMult);
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public boolean isTooltipExpandable() {
        return false;
    }

    @Override
    public float getTooltipWidth() {
        return 360f;
    }

    @Override
    public String getTerrainName() {
        return "Altitude Warning";
    }

    @Override
    public String getNameForTooltip() {
        return "Altitude Warning";
    }

    @Override
    public Color getNameColor() {
        return WARNING_COLOR;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addTitle("Altitude Warning", WARNING_COLOR);
        tooltip.addPara(
                "The inhabited aperture ends at Gan Eden's exposed shell. "
                        + "Automated traffic controls redirect approaching "
                        + "fleets into the visible interior.",
                10f);
        tooltip.addPara(
                "The field causes no hull, combat readiness, or crew damage.",
                10f);
    }

    @Override
    public boolean canPlayerHoldStationIn() {
        return false;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float smoothStep(float value) {
        return value * value * (3f - 2f * value);
    }

    private static final class BoundarySample {
        private final boolean inside;
        private final float distance;
        private final Vector2f closest;
        private final Vector2f centroid;

        private BoundarySample(
                boolean inside,
                float distance,
                Vector2f closest,
                Vector2f centroid) {
            this.inside = inside;
            this.distance = distance;
            this.closest = closest;
            this.centroid = centroid;
        }
    }
}
