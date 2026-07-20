package shiptrophy.hullmods;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.input.InputEventAPI;

public class HumanityTransferNotifier extends BaseEveryFrameCombatPlugin {
    private static final Color NEURAL_BLUE = new Color(0, 121, 216, 255);

    private CombatEngineAPI engine;
    private ShipAPI previousPlayerShip;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        previousPlayerShip = engine == null ? null : engine.getPlayerShip();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) return;

        ShipAPI currentPlayerShip = engine.getPlayerShip();
        if (currentPlayerShip == previousPlayerShip) return;

        ShipAPI previous = previousPlayerShip;
        previousPlayerShip = currentPlayerShip;
        if (previous == null || currentPlayerShip == null) return;
        if (!hasHumanity(previous) && !hasHumanity(currentPlayerShip)) return;

        float timeMult = Math.max(0.01f, currentPlayerShip.getMutableStats().getTimeMult().getModifiedValue());
        engine.addFloatingTextAlways(
                currentPlayerShip.getLocation(),
                "Neural transfer complete",
                NeuralLinkScript.getFloatySize(currentPlayerShip),
                NEURAL_BLUE,
                currentPlayerShip,
                4f * timeMult,
                0.8f / timeMult,
                1f / timeMult,
                0f,
                0f,
                1f);
    }

    private boolean hasHumanity(ShipAPI ship) {
        return ship != null && ship.getVariant() != null
                && ship.getVariant().hasHullMod(Humanity.HULLMOD_ID);
    }
}