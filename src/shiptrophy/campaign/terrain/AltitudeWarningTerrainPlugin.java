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
    private static final float BASE_RETURN_SPEED = 45f;
    private static final float MAX_RETURN_SPEED = 165f;
    private static final int RENDER_SEGMENTS = 160;
    private static final float BLACK_BACKDROP_RADIUS = 16000f;

    private transient boolean playerWasInside;
    private transient SpriteAPI surfaceTexture;
    private transient SpriteAPI eccentricSurfaceTexture;
    private transient SpriteAPI shellTexture;

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
        if (inside && !playerWasInside) {
            player.addFloatingText("ALTITUDE WARNING", WARNING_COLOR, 0.8f, true);
        }
        playerWasInside = inside;
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
        float depth = clamp(
                (distance - GanEdenGenerator.WARNING_INNER_RADIUS)
                        / (GanEdenGenerator.WARNING_OUTER_RADIUS
                                - GanEdenGenerator.WARNING_INNER_RADIUS));

        Vector2f velocity = fleet.getVelocity();
        float radialVelocity = velocity.x * outwardX + velocity.y * outwardY;
        float desiredRadialVelocity = -(BASE_RETURN_SPEED
                + (MAX_RETURN_SPEED - BASE_RETURN_SPEED) * depth);

        if (radialVelocity > desiredRadialVelocity) {
            float blend = Math.min(1f, Math.max(0.08f, amount * 40f));
            float correction = (radialVelocity - desiredRadialVelocity) * blend;
            fleet.setVelocity(
                    velocity.x - outwardX * correction,
                    velocity.y - outwardY * correction);
        }

        // The outer edge is a hard safety rail. It never inflicts damage; it
        // only places the fleet just inside the surface and points it inward.
        if (distance > GanEdenGenerator.HARD_SURFACE_RADIUS) {
            float safeRadius = GanEdenGenerator.HARD_SURFACE_RADIUS - 12f;
            fleet.setLocation(
                    center.x + outwardX * safeRadius,
                    center.y + outwardY * safeRadius);

            Vector2f corrected = fleet.getVelocity();
            float correctedRadial = corrected.x * outwardX + corrected.y * outwardY;
            if (correctedRadial > -BASE_RETURN_SPEED) {
                float correction = correctedRadial + BASE_RETURN_SPEED;
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

            // Campaign locations add procedural background stars independently
            // of the selected background image. A solid layer beneath all
            // entities removes them and makes the sphere read as an enclosure.
            renderSolidDisk(center, BLACK_BACKDROP_RADIUS, new Color(0, 0, 0, 255), alpha);

            renderSolidAnnulus(
                    center,
                    GanEdenGenerator.HARD_SURFACE_RADIUS,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS,
                    new Color(139, 188, 104, 255),
                    alpha);

            renderTexturedAnnulus(
                    surfaceTexture,
                    center,
                    GanEdenGenerator.HARD_SURFACE_RADIUS,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS,
                    new Color(72, 176, 54, 145),
                    alpha);

            renderTexturedAnnulus(
                    eccentricSurfaceTexture,
                    center,
                    GanEdenGenerator.HARD_SURFACE_RADIUS,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS,
                    new Color(54, 148, 42, 90),
                    alpha);

            // Faint structural tracery keeps the terrain from reading as an
            // ordinary planetary ring while preserving the geography.
            renderTexturedAnnulus(
                    shellTexture,
                    center,
                    GanEdenGenerator.HARD_SURFACE_RADIUS,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS,
                    new Color(215, 176, 118, 55),
                    alpha);

            renderGradientAnnulus(
                    center,
                    GanEdenGenerator.WARNING_INNER_RADIUS,
                    GanEdenGenerator.HARD_SURFACE_RADIUS,
                    new Color(255, 86, 42, 10),
                    new Color(255, 92, 42, 105),
                    alpha);

            renderSolidAnnulus(
                    center,
                    GanEdenGenerator.HARD_SURFACE_RADIUS - 28f,
                    GanEdenGenerator.HARD_SURFACE_RADIUS + 28f,
                    new Color(255, 176, 92, 145),
                    alpha);

            renderSolidAnnulus(
                    center,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS - 34f,
                    GanEdenGenerator.SURFACE_OUTER_RADIUS + 34f,
                    new Color(42, 218, 126, 175),
                    alpha);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void ensureTextures() {
        if (surfaceTexture == null) {
            surfaceTexture = Global.getSettings().getSprite(
                    "ship_trophy_gan_eden", "inner_surface");
        }
        if (eccentricSurfaceTexture == null) {
            eccentricSurfaceTexture = Global.getSettings().getSprite(
                    "ship_trophy_gan_eden", "inner_surface_eccentric");
        }
        if (shellTexture == null) {
            shellTexture = Global.getSettings().getSprite(
                    "ship_trophy_gan_eden", "shell_detail");
        }
    }

    private static void renderSolidDisk(
            Vector2f center, float radius, Color color, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        setColor(color, alphaMult);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(center.x, center.y);
        for (int i = 0; i <= RENDER_SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / RENDER_SEGMENTS;
            GL11.glVertex2f(
                    center.x + (float) Math.cos(angle) * radius,
                    center.y + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    private static void renderTexturedAnnulus(
            SpriteAPI texture,
            Vector2f center,
            float innerRadius,
            float outerRadius,
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

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= RENDER_SEGMENTS; i++) {
            float progress = (float) i / RENDER_SEGMENTS;
            double angle = Math.PI * 2.0 * progress;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float u = texX + texWidth * progress;

            GL11.glTexCoord2f(u, texY + texHeight);
            GL11.glVertex2f(
                    center.x + cos * innerRadius,
                    center.y + sin * innerRadius);
            GL11.glTexCoord2f(u, texY);
            GL11.glVertex2f(
                    center.x + cos * outerRadius,
                    center.y + sin * outerRadius);
        }
        GL11.glEnd();
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
}
