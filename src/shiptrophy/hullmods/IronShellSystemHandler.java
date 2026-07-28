package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import shiptrophy.TrophyNetwork;

public class IronShellSystemHandler extends BaseHullMod {
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null || stats.getVariant() == null) return;

        boolean shouldUseIaido = stats.getVariant().hasHullMod(IronShellDiscipline.HULLMOD_ID)
                && TrophyNetwork.isSubtypeUnlocked(IronShellDiscipline.SUBTYPE_ID)
                && IronShellDiscipline.isIronShellHull(stats.getVariant().getHullSpec());

        if (shouldUseIaido) {
            IronShellDiscipline.installIaido(stats);
        } else {
            IronShellDiscipline.restoreOriginalSystemIfNeeded(stats);
        }
    }
}
