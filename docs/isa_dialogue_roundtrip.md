# Isa Dialogue Round Trip

Isa's editable dialogue draft lives at:

```text
dialogue/isa.ink
```

Use this file for rewriting her voice. It is not loaded by Starsector yet; it is a writer-facing source file that maps back to the current Java and `rules.csv` dialogue.

The `VAR` values at the top are preview defaults for Inky/Ink. They are not final game values; Starsector will still supply real colony, progress, and hullmod data when the dialogue is imported back into Java.

Keep these pieces intact while editing:

- `=== knot_names ===`
- The preview entry line: `-> isa_bar_prompt`
- `// hook:` comments
- `// subtype:` comments
- Dynamic placeholders such as `{market_name}`, `{current_dp}`, `{unlock_dp}`, `{hullmod_name}`, and `{remaining_dp}`
- `SYSTEM:` and `STATUS:` lines, unless you want their UI wording changed too

When you are done, ask Codex to import `dialogue/isa.ink` back into the mod. The current targets are:

- `src/shiptrophy/IsaBarEvent.java`
- `src/shiptrophy/IsaContactDialogPlugin.java`
- `data/campaign/rules.csv`

The import step should rebuild `jars/ShipTrophyRoom.jar`, package the mod, and reinstall the clean `ShipTrophyRoom` folder.
