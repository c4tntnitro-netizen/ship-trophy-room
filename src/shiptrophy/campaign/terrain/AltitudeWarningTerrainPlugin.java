package shiptrophy.campaign.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import shiptrophy.campaign.GanEdenGenerator;

/**
 * A non-damaging traffic-control field along Gan Eden's inhabited inner
 * surface. It arrests outward motion and redirects fleets toward the star.
 */
public class AltitudeWarningTerrainPlugin extends BaseRingTerrain {
    private static final Color WARNING_COLOR = new Color(255, 105, 55);
    private static final float SURFACE_RETURN_SPEED = 90f;
    private static final float DEEP_RETURN_SPEED = 220f;
    private static final float EMERGENCY_BACKSTOP_RADIUS = 2525f;
    private static final int RENDER_SEGMENTS = 96;
    private static final int SPHERE_RADIAL_SEGMENTS = 48;
    private static final float SPHERE_LONGITUDE_OFFSET = 0.25f;
    private static final int SPHERE_VERTEX_STRIDE = 4;
    private static final int SPHERE_VERTICES_PER_BAND = (RENDER_SEGMENTS + 1) * 2;
    private static final float[] INWARD_SPHERE_MESH = buildInwardSphereMesh();
    private static final float BLACK_BACKDROP_RADIUS = 16000f;
    private static final String WARNING_RECENT_KEY =
            "$shipTrophyGanEdenAltitudeWarningRecent";

    private transient SpriteAPI innerSurfaceTexture;

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

        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        boolean inside = containsEntity(player);
        if (inside && !player.getMemoryWithoutUpdate().getBoolean(WARNING_RECENT_KEY)) {
            player.addFloatingText("ALTITUDE WARNING", WARNING_COLOR, 0.8f, true);
            player.getMemoryWithoutUpdate().set(WARNING_RECENT_KEY, true, 0.5f);
        }
    }

    @Override
    public void applyEffect(SectorEntityToken token, float amount) {
        if (!(token instanceof CampaignFleetAPI) || params == null || params.relatedEntity == null) return;

        CampaignFleetAPI fleet = (CampaignFleetAPI) token;
        Vector2f center = params.relatedEntity.getLocation();
        Vector2f location = fleet.getLocation();
        float dx = location.x - center.x;
        float dy = location.y - center.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= 0f) return;

        float outwardX = dx / distance;
        float outwardY = dy / distance;
        float approach = smoothStep(clamp(
                (distance - GanEdenGenerator.WARNING_INNER_RADIUS)
                        / (GanEdenGenerator.HARD_SURFACE_RADIUS
                                - GanEdenGenerator.WARNING_INNER_RADIUS)));
        float penetration = smoothStep(clamp(
                (distance - GanEdenGenerator.HARD_SURFACE_RADIUS)
                        / (EMERGENCY_BACKSTOP_RADIUS
                                - GanEdenGenerator.HARD_SURFACE_RADIUS)));

        Vector2f velocity = fleet.getVelocity();
        float radialVelocity = velocity.x * outwardX + velocity.y * outwardY;
        float desiredRadialVelocity = -(
                SURFACE_RETURN_SPEED * approach
                        + (DEEP_RETURN_SPEED - SURFACE_RETURN_SPEED) * penetration);

        if (radialVelocity > desiredRadialVelocity) {
            // A frame-rate-independent spring response. Near the warning's
            // inner edge it only feathers off outward momentum; the response
            // becomes firmer as the fleet enters the visible atmosphere and
            // the fleet naturally rebounds toward the star.
            float responsePerSecond = 0.55f + 3.25f * approach + 4.2f * penetration;
            float blend = 1f - (float) Math.exp(-responsePerSecond * Math.max(0f, amount));
            float correction = (radialVelocity - desiredRadialVelocity) * blend;
            fleet.setVelocity(
                    velocity.x - outwardX * correction,
                    velocity.y - outwardY * correction);
        }

        // This is unreachable during ordinary flight: it is a last-resort
        // guard for extreme modded campaign speeds, buried well beneath the
        // visible surface instead of forming the apparent boundary.
        if (distance > EMERGENCY_BACKSTOP_RADIUS) {
            float safeRadius = EMERGENCY_BACKSTOP_RADIUS - 12f;
            fleet.setLocation(
                    center.x + outwardX * safeRadius,
                    center.y + outwardY * safeRadius);

            Vector2f corrected = fleet.getVelocity();
            float correctedRadial = corrected.x * outwardX + corrected.y * outwardY;
            if (correctedRadial > -DEEP_RETURN_SPEED) {
                float correction = correctedRadial + DEEP_RETURN_SPEED;
                fleet.setVelocity(
                        corrected.x - outwardX * correction,
                        corrected.y - outwardY * correction);
            }
        }
    }

    @Override
    public String getEffectCategory() {
        // BaseTerrain.advance() requires this to group overlapping instances
        // before it calls applyEffect(). Gan Eden has one boundary, but using
        // a stable category also prevents duplicate effects if a save ever
        // contains more than one copy of the terrain.
        return "ship_trophy_altitude_warning";
    }

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_1);
    }

    @Override
    public float getRenderRange() {
        return GanEdenGenerator.SURFACE_OUTER_RADIUS + 2500f;
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (layer != CampaignEngineLayers.TERRAIN_1
                || params == null
                || params.relatedEntity == null) {
            return;
        }

        Vector2f center = params.relatedEntity.getLocation();
        if (!viewport.isNearViewport(center, getRenderRange())) return;

        ensureTextures();
        float alpha = viewport.getAlphaMult();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // Starsector's planet maps are 2:1 equirectangular textures. Lift
            // this circular projection onto the back hemisphere and sample it
            // by longitude and latitude, i.e. view the planetary surface from
            // its inward-facing side. The geography itself now curves into the
            // horizon instead of meeting a separately stretched ring texture.
            renderInwardSphere(
                    innerSurfaceTexture,
                    center,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS,
                    new Color(255, 255, 255, 255),
                    alpha);

            // The location's official background is also vanilla black. This
            // mask guarantees a clean circular aperture while keeping custom
            // planet imagery out of Starsector's title-screen background cache.
            renderSolidAnnulus(
                    center,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS,
                    BLACK_BACKDROP_RADIUS,
                    new Color(0, 0, 0, 255),
                    alpha);

            // Looking increasingly edge-on through the inner atmosphere
            // shifts the white haze toward nitrogen blue at the horizon.
            renderGradientAnnulus(
                    center,
                    GanEdenGenerator.WARNING_INNER_RADIUS,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS,
                    new Color(242, 248, 255, 8),
                    new Color(82, 164, 255, 138),
                    alpha);

            renderSolidAnnulus(
                    center,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS - 20f,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS + 20f,
                    new Color(184, 220, 255, 150),
                    alpha);

            renderSolidAnnulus(
                    center,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS + 20f,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS + 36f,
                    new Color(52, 64, 68, 150),
                    alpha);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void ensureTextures() {
        if (innerSurfaceTexture == null) {
            innerSurfaceTexture = Global.getSettings().getSprite(
                    "ship_trophy_gan_eden", "inner_surface");
        }
    }

    private static void renderInwardSphere(
            SpriteAPI texture,
            Vector2f center,
            float radius,
            Color color,
            float alphaMult) {
        if (texture == null) return;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        texture.bindTexture();
        setColor(color, alphaMult);

        float texX = texture.getTexX();
        float texY = texture.getTexY();
        float texWidth = texture.getTexWidth();
        float texHeight = texture.getTexHeight();
        int cursor = 0;

        for (int radial = 0; radial < SPHERE_RADIAL_SEGMENTS; radial++) {
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (int vertex = 0; vertex < SPHERE_VERTICES_PER_BAND; vertex++) {
                float diskX = INWARD_SPHERE_MESH[cursor++];
                float diskY = INWARD_SPHERE_MESH[cursor++];
                float u = INWARD_SPHERE_MESH[cursor++];
                float v = INWARD_SPHERE_MESH[cursor++];

                GL11.glTexCoord2f(
                        texX + texWidth * u,
                        texY + texHeight * v);
                GL11.glVertex2f(
                        center.x + radius * diskX,
                        center.y + radius * diskY);
            }
            GL11.glEnd();
        }
    }

    private static float[] buildInwardSphereMesh() {
        float[] mesh = new float[
                SPHERE_RADIAL_SEGMENTS
                        * SPHERE_VERTICES_PER_BAND
                        * SPHERE_VERTEX_STRIDE];
        int cursor = 0;

        for (int radial = 0; radial < SPHERE_RADIAL_SEGMENTS; radial++) {
            float inner = (float) radial / SPHERE_RADIAL_SEGMENTS;
            float outer = (float) (radial + 1) / SPHERE_RADIAL_SEGMENTS;

            for (int angular = 0; angular <= RENDER_SEGMENTS; angular++) {
                double angle = Math.PI * 2.0 * angular / RENDER_SEGMENTS;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                cursor = putInwardSphereVertex(mesh, cursor, inner, cos, sin);
                cursor = putInwardSphereVertex(mesh, cursor, outer, cos, sin);
            }
        }
        return mesh;
    }

    private static int putInwardSphereVertex(
            float[] mesh,
            int cursor,
            float diskRadius,
            float cos,
            float sin) {
        float diskX = diskRadius * cos;
        float diskY = diskRadius * sin;

        // An equidistant 180-degree interior view: screen radius represents
        // angular distance from the camera's inward viewing axis. Unlike the
        // orthographic projection used for an exterior planet, this does not
        // crush the surface into a thin strip at the circular horizon.
        float viewAngle = (float) Math.PI * 0.5f * diskRadius;
        float sinViewAngle = (float) Math.sin(viewAngle);
        float sphereX = -sinViewAngle * cos;
        float sphereY = sinViewAngle * sin;
        float sphereZ = -(float) Math.cos(viewAngle);

        float longitude = (float) Math.atan2(sphereZ, sphereX);
        float latitude = (float) Math.asin(Math.max(
                -1f, Math.min(1f, sphereY)));
        float u = 0.5f + longitude / ((float) Math.PI * 2f)
                + SPHERE_LONGITUDE_OFFSET;
        float v = 0.5f - latitude / (float) Math.PI;

        mesh[cursor++] = diskX;
        mesh[cursor++] = diskY;
        mesh[cursor++] = u;
        mesh[cursor++] = v;
        return cursor;
    }

    private static void renderGradientAnnulus(
            Vector2f center,
            float innerRadius,
            float outerRadius,
            Color innerColor,
            Color outerColor,
            float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= RENDER_SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / RENDER_SEGMENTS;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            setColor(innerColor, alphaMult);
            GL11.glVertex2f(
                    center.x + cos * innerRadius,
                    center.y + sin * innerRadius);
            setColor(outerColor, alphaMult);
            GL11.glVertex2f(
                    center.x + cos * outerRadius,
                    center.y + sin * outerRadius);
        }
        GL11.glEnd();
    }

    private static void renderSolidAnnulus(
            Vector2f center,
            float innerRadius,
            float outerRadius,
            Color color,
            float alphaMult) {
        renderGradientAnnulus(
                center, innerRadius, outerRadius, color, color, alphaMult);
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
                "The inner surface of Gan Eden is dangerously close. "
                        + "Automated traffic controls redirect approaching fleets toward the star.",
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
}
