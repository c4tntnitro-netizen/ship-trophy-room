package shiptrophy.campaign.terrain;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
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

    private transient boolean playerWasInside;

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
