# Isa Dialogue Workflow

## Definitive source

Isa's in-game Hall dialogue is authored in:

```text
data/campaign/rules.csv
```

For Isa's bar event, contact conversation, unlock scenes, recruitment, and Hall-completion cinematic, `rules.csv` is both the writing master and the runtime source. Dialogue text, fixed options, branches, tooltips, highlights, sound cues, and first-view presentation belong there.

## Ink writing aid

```text
dialogue/isa.ink
```

The Ink file is a human-facing drafting and Inky preview aid. Starsector does not load it, and it must never overwrite `rules.csv` automatically. After writing in Ink, reconcile the intended wording and branch changes into the corresponding CSV rules by hand or with a reviewed conversion.

The same rule applies to the quest drafts in `dialogue/gan_eden_quest.ink`,
`dialogue/hypershunt.ink`, and `dialogue/Logs.ink`. Their canonical runtime
counterparts are the `shipTrophyIsaShatteredRing*` and `shipTrophyGanEden*`
records in `rules.csv`.

Keep these pieces intact while drafting:

- `=== knot_names ===`
- The preview entry line: `-> isa_preview_hub`
- `// hook:` comments naming corresponding `rules.csv` rows
- `// subtype:` comments
- Dynamic placeholders such as `{market_name}`, `{current_dp}`, `{unlock_dp}`, `{hullmod_name}`, and `{remaining_dp}`
- `SYSTEM:`, `STATUS:`, and `HIGHLIGHT:` lines unless their intended UI wording changes

## Runtime ownership

- `data/campaign/rules.csv` owns Isa's bar event, Hall contact menus, unlock conversations, quest dialogue, officer recruitment, and Hall-completion transmission.
- `IsaContactRulesCMD.java` only calculates dynamic ledger values, enumerates optional integrations, formats status rows, checks conditions, and performs state-changing actions requested by rules.
- `IsaBarEvent.java` only owns bar-event lifecycle, rule dispatch, portrait presentation, and hiring state.
- `HallOfTriumphCompletionDialogPlugin.java` only owns the campaign completion interaction lifecycle, illustration, and rule dispatch.
- `data/config/chatter/characters/ship_trophy_isa.json` owns Combat Chatter lines.

## Editing workflow

1. Draft or preview prose in `dialogue/isa.ink` when useful.
2. Make the authoritative wording and branch edits in `data/campaign/rules.csv`.
3. Validate CSV parsing, unique rule IDs, option targets, and trigger reachability.
4. Rebuild the JAR only when Java conditions or actions changed.
5. Package and reinstall the clean `HallOfTriumph` folder.
