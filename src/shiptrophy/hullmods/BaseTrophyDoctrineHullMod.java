package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.TrophyDoctrine;
import shiptrophy.TrophyNetwork;
import shiptrophy.ShipTrophyL10n;
import shiptrophy.TrophySubtypeRegistry;
import shiptrophy.TrophySubtypeSpec;

public abstract class BaseTrophyDoctrineHullMod extends BaseHullMod {
    protected TrophyDoctrine getDoctrine() { return null; }
    protected String getSubtypeId() {
        TrophyDoctrine doctrine = getDoctrine();
        return doctrine == null ? "" : doctrine.id;
    }
    protected String getDModCalculationNote() { return null; }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return TrophyHullModUtil.areEffectsEnabled()
                && isUnlocked() && matchesStyle(ship)
                && hasNoOtherTrophyHullMod(ship);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return TrophyHullModUtil.areEffectsEnabled() && isUnlocked();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!TrophyHullModUtil.areEffectsEnabled()) {
            return ShipTrophyL10n.get("hullmod_disabled_reason");
        }
        if (!isUnlocked()) {
            TrophySubtypeSpec subtype = getSubtype();
            String showcaseName = subtype == null
                    ? ShipTrophyL10n.get("hullmod_matching") : subtype.showcaseName;
            float unlockDp = subtype == null ? TrophyNetwork.DOCTRINE_UNLOCK_DP : subtype.unlockDp;
            return ShipTrophyL10n.format(
                    "hullmod_requires_dp", Math.round(unlockDp), showcaseName);
        }
        if (!matchesStyle(ship)) {
            TrophySubtypeSpec subtype = getSubtype();
            String style = subtype == null
                    ? ShipTrophyL10n.get("hullmod_matching") : subtype.installStyle;
            return ShipTrophyL10n.format("hullmod_style_only", style);
        }
        if (!hasNoOtherTrophyHullMod(ship)) {
            String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, getCurrentHullModId());
            return other == null
                    ? ShipTrophyL10n.get("hullmod_only_one_reason")
                    : ShipTrophyL10n.format("hullmod_incompatible", other);
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (!TrophyHullModUtil.areEffectsEnabled()) {
            removeDisabledArtifacts(stats);
            return;
        }
        if (!isUnlocked()) return;
        applyDoctrineEffects(hullSize, stats, id);
    }

    protected abstract void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id);

    protected void removeDisabledArtifacts(MutableShipStatsAPI stats) {
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (!TrophyHullModUtil.areEffectsEnabled()) {
            tooltip.addPara(ShipTrophyL10n.get("hullmod_effects_disabled"),
                    opad, Misc.getNegativeHighlightColor(),
                    ShipTrophyL10n.get("hullmod_disabled_highlight"));
        }
        String dmodNote = getDModCalculationNote();
        if (dmodNote != null) {
            tooltip.addPara(dmodNote, opad, h,
                    ShipTrophyL10n.get("hullmod_counts_dmod_highlight"));
        }
        tooltip.addPara(ShipTrophyL10n.get("hullmod_only_one"), opad, h,
                ShipTrophyL10n.get("hullmod_only_one_highlight"));
    }

    protected boolean isUnlocked() {
        return TrophyNetwork.isSubtypeUnlocked(getSubtypeId());
    }

    protected boolean matchesStyle(ShipAPI ship) {
        return TrophyNetwork.isMatchingInstallStyle(ship, getSubtypeId());
    }

    protected boolean hasNoOtherTrophyHullMod(ShipAPI ship) {
        return !TrophyHullModUtil.hasOtherTrophyHullMod(ship, getCurrentHullModId());
    }

    protected String getCurrentHullModId() {
        TrophySubtypeSpec subtype = getSubtype();
        return subtype == null ? "" : subtype.hullModId;
    }

    protected TrophySubtypeSpec getSubtype() {
        return TrophySubtypeRegistry.getSubtype(getSubtypeId());
    }

    protected void addHiddenMarker(MutableShipStatsAPI stats, String markerId) {
        ShipVariantAPI variant = stats.getVariant();
        if (variant != null && !variant.hasHullMod(markerId)) {
            variant.addMod(markerId);
        }
    }

    protected void removeHiddenMarker(MutableShipStatsAPI stats, String markerId) {
        ShipVariantAPI variant = stats.getVariant();
        if (variant != null && variant.hasHullMod(markerId)) {
            variant.removeMod(markerId);
            variant.removePermaMod(markerId);
        }
    }
}
