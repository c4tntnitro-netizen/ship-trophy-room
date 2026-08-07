package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
/**
 * Legacy save-compatibility marker for older Lahat variants.
 *
 * The Flaming Sword no longer applies a maneuverability penalty, but this
 * hullmod remains registered so variants serialized by earlier releases can
 * still be loaded safely.
 */
public class FlamingSwordBurden extends BaseHullMod {
}
