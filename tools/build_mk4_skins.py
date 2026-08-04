"""Pre-render the finite Mk IV hull-skin set used by Hall of Triumph.

This is a development-time asset builder. The game never generates or paints
Mk IV hull sprites at runtime.
"""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageChops


REPO = Path(__file__).resolve().parents[1]
CORE = Path(r"C:\Program Files (x86)\Fractal Softworks\Starsector\starsector-core")
OUTPUT_SPRITES = REPO / "graphics" / "ships" / "mk4"
OUTPUT_SKINS = REPO / "data" / "hulls" / "skins"


COMMON = {
    "restoreToBaseHull": False,
    "removeWeaponSlots": [],
    "removeEngineSlots": [],
    "removeBuiltInMods": [],
    "removeBuiltInWeapons": [],
    "builtInWeapons": {},
}


SPECS = [
    {
        "source": "graphics/ships/vanguard/vanguard_pirate.png",
        "overlay": "pirate",
        "skin": {
            **COMMON,
            "baseHullId": "vanguard",
            "skinHullId": "ship_trophy_mk4_pirate_vanguard",
            "hullName": "Vanguard (P)",
            "descriptionId": "vanguard",
            "descriptionPrefix": "A Pirate Mk IV refit carrying improvised tiger-stripe field paint, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex", "special_allows_system_use", "system_allows_special_use"],
            "tech": "Pirate",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/manticore/manticore_pirate.png",
        "overlay": "pirate",
        "skin": {
            **COMMON,
            "baseHullId": "manticore",
            "skinHullId": "ship_trophy_mk4_pirate_manticore",
            "hullName": "Manticore (P)",
            "descriptionId": "manticore",
            "descriptionPrefix": "A Pirate Mk IV refit carrying improvised tiger-stripe field paint, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Pirate",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/falcon/falcon_p.png",
        "overlay": "pirate",
        "skin": {
            **COMMON,
            "baseHullId": "falcon",
            "skinHullId": "ship_trophy_mk4_pirate_falcon",
            "hullName": "Falcon (P)",
            "descriptionId": "falcon",
            "descriptionPrefix": "A Pirate Mk IV refit carrying improvised tiger-stripe field paint, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Pirate",
            "suppliesToRecover": 20,
            "suppliesPerMonth": 20,
            "baseValueMult": 1.25,
            "removeWeaponSlots": ["WS 003", "WS 004"],
            "weaponSlotChanges": {
                "WS 005": {"type": "COMPOSITE"},
                "WS 006": {"type": "COMPOSITE"},
                "WS 007": {"type": "MISSILE"},
                "WS 008": {"type": "MISSILE"},
            },
            "builtInMods": ["augmentedengines", "unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/eradicator/eradicator_pirate.png",
        "overlay": "pirate",
        "skin": {
            **COMMON,
            "baseHullId": "eradicator",
            "skinHullId": "ship_trophy_mk4_pirate_eradicator",
            "hullName": "Eradicator (P)",
            "descriptionId": "eradicator",
            "descriptionPrefix": "A Pirate Mk IV refit carrying improvised tiger-stripe field paint, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Pirate",
            "systemId": "burndrive",
            "suppliesToRecover": 18,
            "suppliesPerMonth": 18,
            "baseValueMult": 0.75,
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/atlas/atlas_mk2_base.png",
        "overlay": "pirate",
        "skin": {
            **COMMON,
            "baseHullId": "atlas2",
            "skinHullId": "ship_trophy_mk4_pirate_atlas2",
            "hullName": "Atlas Mk.II",
            "descriptionId": "atlas2",
            "descriptionPrefix": "A Pirate Mk IV refit carrying improvised tiger-stripe field paint, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Pirate",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/enforcer/enforcer_base.png",
        "overlay": "pather",
        "skin": {
            **COMMON,
            "baseHullId": "enforcer",
            "skinHullId": "ship_trophy_mk4_pather_enforcer",
            "hullName": "Enforcer",
            "descriptionId": "enforcer",
            "descriptionPrefix": "A Luddic Path Mk IV refit marked by dried blood, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Luddic Path",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/hammerhead/hammerhead_base.png",
        "overlay": "pather",
        "skin": {
            **COMMON,
            "baseHullId": "hammerhead",
            "skinHullId": "ship_trophy_mk4_pather_hammerhead",
            "hullName": "Hammerhead",
            "descriptionId": "hammerhead",
            "descriptionPrefix": "A Luddic Path Mk IV refit marked by dried blood, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Luddic Path",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/manticore/manticore_pather.png",
        "overlay": "pather",
        "skin": {
            **COMMON,
            "baseHullId": "manticore",
            "skinHullId": "ship_trophy_mk4_pather_manticore",
            "hullName": "Manticore (LP)",
            "descriptionId": "manticore",
            "descriptionPrefix": "A Luddic Path Mk IV refit marked by dried blood, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Luddic Path",
            "suppliesToRecover": 14,
            "suppliesPerMonth": 14,
            "weaponSlotChanges": {
                "WS 000": {"type": "MISSILE"},
                "WS 004": {"type": "BALLISTIC"},
                "WS 005": {"type": "BALLISTIC"},
            },
            "removeBuiltInMods": ["ballistic_rangefinder"],
            "builtInMods": ["safetyoverrides", "ill_advised", "unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/eradicator/eradicator_base.png",
        "overlay": "pather",
        "skin": {
            **COMMON,
            "baseHullId": "eradicator",
            "skinHullId": "ship_trophy_mk4_pather_eradicator",
            "hullName": "Eradicator",
            "descriptionId": "eradicator",
            "descriptionPrefix": "A Luddic Path Mk IV refit marked by dried blood, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Luddic Path",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/sunder/sunder.png",
        "overlay": "pather",
        "skin": {
            **COMMON,
            "baseHullId": "sunder",
            "skinHullId": "ship_trophy_mk4_pather_sunder",
            "hullName": "Sunder",
            "descriptionId": "sunder",
            "descriptionPrefix": "A Luddic Path Mk IV refit marked by dried blood, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Luddic Path",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
    {
        "source": "graphics/ships/prometheus/prometheus2_base.png",
        "overlay": "pather",
        "skin": {
            **COMMON,
            "baseHullId": "prometheus2",
            "skinHullId": "ship_trophy_mk4_pather_prometheus2",
            "hullName": "Prometheus Mk.II",
            "descriptionId": "prometheus2",
            "descriptionPrefix": "A Luddic Path Mk IV refit marked by dried blood, reinforced armor, and dangerously overdriven engines.",
            "tags": ["hide_in_codex"],
            "tech": "Luddic Path",
            "builtInMods": ["unstable_injector", "heavyarmor"],
        },
    },
]


def render_sprite(source: Path, overlay: Path, output: Path) -> None:
    base = Image.open(source).convert("RGBA")
    paint = Image.open(overlay).convert("RGBA").resize(
        base.size, Image.Resampling.LANCZOS
    )
    paint.putalpha(ImageChops.multiply(paint.getchannel("A"), base.getchannel("A")))
    rendered = Image.alpha_composite(base, paint)
    rendered.putalpha(base.getchannel("A"))
    output.parent.mkdir(parents=True, exist_ok=True)
    rendered.save(output, optimize=True)


def main() -> None:
    overlays = {
        "pirate": REPO / "graphics" / "ships" / "mk4_overlays" / "ship_trophy_mk4_pirate_tiger.png",
        "pather": REPO / "graphics" / "ships" / "mk4_overlays" / "ship_trophy_mk4_pather_blood.png",
    }
    for spec in SPECS:
        skin = dict(spec["skin"])
        skin_id = skin["skinHullId"]
        faction = spec["overlay"]
        relative_sprite = Path("graphics") / "ships" / "mk4" / faction / f"{skin_id}.png"
        skin["spriteName"] = relative_sprite.as_posix()
        render_sprite(
            CORE / spec["source"],
            overlays[faction],
            REPO / relative_sprite,
        )
        skin_path = OUTPUT_SKINS / f"{skin_id}.skin"
        skin_path.write_text(json.dumps(skin, indent=4) + "\n", encoding="utf-8")
        print(f"generated {skin_id}")


if __name__ == "__main__":
    main()
