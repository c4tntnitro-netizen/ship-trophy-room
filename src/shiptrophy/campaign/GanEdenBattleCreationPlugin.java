package shiptrophy.campaign;

import java.awt.Color;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;

/** Gives the Gan Eden guardians a normal battle framed by the inner world. */
public final class GanEdenBattleCreationPlugin extends BaseCampaignPlugin {
    public static final String ID = "ship_trophy_gan_eden_battle_creation";
    private static final String BACKGROUND =
            "graphics/backgrounds/ship_trophy_gan_eden_atmosphere.png";

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
            loader.setBackgroundSpriteName(BACKGROUND);
            loader.setBackgroundGlowColor(new Color(82, 142, 176, 70));
        }

        @Override
        public void afterDefinitionLoad(CombatEngineAPI engine) {
            super.afterDefinitionLoad(engine);
            engine.setRenderStarfield(false);
            engine.setBackgroundColor(new Color(8, 20, 43));
            engine.setBackgroundGlowColor(new Color(95, 157, 184, 70));
            engine.setBackgroundGlowColorNonAdditive(true);
        }
    }
}
