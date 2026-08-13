# Chinese content localization overlay

`rules.csv` is only one source of player-facing text. This layer covers the
mod metadata and structured data files that define industries, market
conditions, hullmods, descriptions, settings, achievements, combat chatter,
ships, variants, entities, and other UI content. Java-rendered interface text
is routed through `data/strings/strings.json`; locale copies are checked for
matching keys and format tokens before they enter an overlay.

The English files in the mod root remain authoritative. Each locale store
records the exact English source beside its independently authored target.
`tools/content_localization.ps1` refuses to build when that source has drifted,
when format tokens change, or when a target contains unsafe quotation marks.

Build either overlay from the repository root:

    powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\content_localization.ps1 -Command Check -Locale zh-Hans-CN
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\content_localization.ps1 -Command Build -Locale zh-Hans-CN

    powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\content_localization.ps1 -Command Check -Locale zh-Hant-TW
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\content_localization.ps1 -Command Build -Locale zh-Hant-TW

Outputs are written beneath `build/localization/<locale>/`. The build also
regenerates and validates that locale's `data/campaign/rules.csv`.

These are replacement overlays for the Hall of Triumph mod, intended to be
used with a Chinese Starsector installation such as FOSSIC. They do not
redistribute FOSSIC files.
