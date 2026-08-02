package shiptrophy.campaign;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;

/** Gives the Gan Eden guardians a normal battle framed by the inner world. */
public final class GanEdenBattleCreationPlugin extends BaseCampaignPlugin {
    public static final String ID = "ship_trophy_gan_eden_battle_creation";
    private static final String BACKGROUND =
            "graphics/planets/terran_eccentric.jpg";

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
        if (!(opponent instanceof CampaignFleetAPI)
                || !opponent.getMemoryWithoutUpdate()
                        .getBoolean(GanEdenAmbushScript.FLEET_KEY)) {
            return null;
        }
        return new PluginPick<BattleCreationPlugin>(
                new AtmosphericBattle(), PickPriority.MOD_SPECIFIC);
    }

    private static final class AtmosphericBattle
            extends BattleCreationPluginImpl {
        @Override
        public void initBattle(
                BattleCreationContext context,
                MissionDefinitionAPI loader) {
            // Retain vanilla deployment, objectives, map size, and terrain.
            super.initBattle(context, loader);
            loader.setBackgroundGlowColor(new Color(82, 142, 176, 70));
        }

        @Override
        public void afterDefinitionLoad(CombatEngineAPI engine) {
            super.afterDefinitionLoad(engine);
            engine.setRenderStarfield(false);
            engine.setBackgroundColor(new Color(8, 20, 43));
            engine.setBackgroundGlowColor(new Color(95, 157, 184, 70));
            engine.setBackgroundGlowColorNonAdditive(true);
            engine.addLayeredRenderingPlugin(new AtmosphericBackdrop());
        }
    }

    /**
     * Keeps the Gan Eden terrain locked to the combat camera and scales it as
     * an aspect-preserving cover image. A mission background is rendered in
     * battlefield coordinates, which makes a finite texture read as a distant
     * object as the camera moves or zooms. This layer instead recalculates its
     * world-space dimensions from the visible viewport every frame.
     */
    private static final class AtmosphericBackdrop
            extends BaseCombatLayeredRenderingPlugin {
        private static final float OVERSCAN = 1.02f;

        private final SpriteAPI sprite;
        private final float sourceWidth;
        private final float sourceHeight;

        private AtmosphericBackdrop() {
            sprite = Global.getSettings().getSprite(BACKGROUND);
            sourceWidth = Math.max(1f, sprite.getWidth());
            sourceHeight = Math.max(1f, sprite.getHeight());
            sprite.setNormalBlend();
            sprite.setColor(Color.WHITE);
            sprite.setAlphaMult(1f);
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

            Vector2f center = viewport.getCenter();
            sprite.renderAtCenter(center.x, center.y);
        }
    }
}
