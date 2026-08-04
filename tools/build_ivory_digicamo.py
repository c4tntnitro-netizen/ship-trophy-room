"""Bake digital camouflage into the eight static Ivory Remnant sprites."""

from __future__ import annotations

import hashlib
import math
import random
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageOps


REPO = Path(__file__).resolve().parents[1]
SOURCE_DIR = REPO / "tools" / "assets" / "ivory_base"
OUTPUT_DIR = REPO / "graphics" / "ships" / "white_remnants"

SPRITES = (
    "ship_trophy_white_apex.png",
    "ship_trophy_white_brilliant.png",
    "ship_trophy_white_fulgent.png",
    "ship_trophy_white_glimmer.png",
    "ship_trophy_white_lumen.png",
    "ship_trophy_white_nova.png",
    "ship_trophy_white_radiant.png",
    "ship_trophy_white_scintilla.png",
)

# Cool military greys with a subdued blue-green cast. These colors are used as
# multiplicative tints rather than flat overlays, preserving every vent, panel
# line, highlight, and glow from the ivory source beneath the camouflage.
PALETTE = (
    (96, 116, 124, 214),
    (138, 160, 166, 198),
    (190, 207, 209, 176),
    (239, 246, 241, 148),
)

CENTER_PATTERN_STRENGTH = 0.18
FULL_STRENGTH_RADIUS = 0.90
FALLOFF_START_RADIUS = 0.12


def stable_seed(name: str) -> int:
    return int.from_bytes(
        hashlib.sha256(name.encode("utf-8")).digest()[:8], "big"
    )


def make_pattern(
    size: tuple[int, int], seed: int, mirror_horizontal: bool = False
) -> Image.Image:
    width, height = size
    rng = random.Random(seed)
    cell = max(3, min(width, height) // 20)
    grid_width = max(1, (width + cell - 1) // cell)
    grid_height = max(1, (height + cell - 1) // cell)

    pattern = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(pattern)

    # Overlapping, orthogonal cell clusters produce a recognizable digital
    # camouflage pattern rather than noise or diagonal tiger stripes.
    cluster_count = max(24, (grid_width * grid_height) // 4)
    for _ in range(cluster_count):
        x = rng.randrange(grid_width)
        y = rng.randrange(grid_height)
        cells_w = rng.choices((1, 2, 3, 4, 5), (2, 5, 6, 4, 1))[0]
        cells_h = rng.choices((1, 2, 3), (5, 4, 1))[0]
        color = PALETTE[rng.randrange(len(PALETTE))]
        draw.rectangle(
            (
                x * cell,
                y * cell,
                min(width, (x + cells_w) * cell) - 1,
                min(height, (y + cells_h) * cell) - 1,
            ),
            fill=color,
        )

        # About half the clusters get a smaller perpendicular block, giving
        # them the stepped silhouettes associated with digi-camo.
        if rng.random() < 0.55:
            step_x = max(0, min(grid_width - 1, x + rng.choice((-1, 1))))
            step_y = max(0, min(grid_height - 1, y + rng.choice((-1, 1))))
            draw.rectangle(
                (
                    step_x * cell,
                    step_y * cell,
                    min(width, (step_x + rng.randint(1, 3)) * cell) - 1,
                    min(height, (step_y + rng.randint(1, 2)) * cell) - 1,
                ),
                fill=color,
            )
    if mirror_horizontal:
        # Radiant's broad bilateral silhouette makes a fully random pattern
        # look visually lopsided. Mirror one half without changing the stepped
        # digital geometry or introducing a hard central seam.
        half_width = (width + 1) // 2
        left = pattern.crop((0, 0, half_width, height))
        mirrored = ImageOps.mirror(left)
        pattern.paste(mirrored.crop((width % 2, 0, half_width, height)),
                      (half_width, 0))
    return pattern


def make_radial_opacity_mask(size: tuple[int, int]) -> Image.Image:
    """Fade camo out toward the hull's center without changing its silhouette."""
    width, height = size
    center_x = (width - 1) * 0.5
    center_y = (height - 1) * 0.5
    half_width = max(1.0, width * 0.5)
    half_height = max(1.0, height * 0.5)
    span = FULL_STRENGTH_RADIUS - FALLOFF_START_RADIUS

    mask = Image.new("L", size)
    pixels = mask.load()
    for y in range(height):
        ny = (y - center_y) / half_height
        for x in range(width):
            nx = (x - center_x) / half_width
            radius = math.sqrt(nx * nx + ny * ny)
            t = max(0.0, min(1.0, (radius - FALLOFF_START_RADIUS) / span))
            # Smoothstep avoids visible rings in the alpha transition.
            t = t * t * (3.0 - 2.0 * t)
            strength = CENTER_PATTERN_STRENGTH + (
                1.0 - CENTER_PATTERN_STRENGTH
            ) * t
            pixels[x, y] = round(strength * 255)
    return mask


def render(source: Path, output: Path) -> None:
    ivory = Image.open(source).convert("RGBA")
    pattern = make_pattern(
        ivory.size,
        stable_seed(source.name),
        mirror_horizontal=source.name == "ship_trophy_white_radiant.png",
    )
    pattern_alpha = ImageChops.multiply(
        pattern.getchannel("A"), ivory.getchannel("A")
    )
    pattern_alpha = ImageChops.multiply(
        pattern_alpha, make_radial_opacity_mask(ivory.size)
    )
    pattern.putalpha(pattern_alpha)

    # A normal alpha composite replaces the hull texture with flat blocks and
    # makes the ships look washed out. Multiplication changes local value and
    # hue while retaining the source pixel's mechanical detail.
    tinted_rgb = ImageChops.multiply(
        ivory.convert("RGB"), pattern.convert("RGB")
    )
    tinted = Image.merge("RGBA", (*tinted_rgb.split(), ivory.getchannel("A")))
    result = Image.composite(tinted, ivory, pattern_alpha)
    result.putalpha(ivory.getchannel("A"))
    output.parent.mkdir(parents=True, exist_ok=True)
    result.save(output, optimize=True)


def main() -> None:
    for name in SPRITES:
        render(SOURCE_DIR / name, OUTPUT_DIR / name)
        print(f"generated {name}")


if __name__ == "__main__":
    main()
