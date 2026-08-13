# Translation and review brief

## Source hierarchy

1. The current English unit and its full rule context are authoritative.
2. Locked glossary entries are authoritative for recurring terminology.
3. Existing Chinese units are consistency references, never a reason to inherit
   an error or flatten a deliberate distinction in English.

Each batch may contain `relatedReferences` selected by the local vector index.
Use them to recover recurring terminology, voice, callbacks, and previously
translated parallel lines. Their `reviewStatus` determines their authority:
`locked` and `human-reviewed` wording is strong evidence, `machine-reviewed`
wording is a checked suggestion, `draft` wording is provisional, and
`untranslated` references provide English context only. Never copy a reference
solely because its similarity score is high.

Create `zh-Hans-CN` and `zh-Hant-TW` independently from English. Do not create
Traditional by character conversion from Simplified.

## Four-pass method

For each small batch:

1. **Meaning pass:** identify speaker, literal meaning, implication, emotional
   beat, technical terms, and any ambiguity. Do this from English before drafting
   Chinese.
2. **Target pass:** translate naturally for the requested locale. Preserve voice,
   subtext, paragraph breaks, variables, numbers, option intent, and the
   distinction between narration and quoted speech.
3. **Independent audit:** without looking at English wording while composing,
   back-translate the Chinese into plain English. Then compare that back-translation
   with the source and list omissions, additions, tone shifts, or uncertain terms.
4. **Revision pass:** revise the Chinese against the original English and glossary.
   Set a candid confidence value and retain unresolved questions in `issues`.

Do not make the back-translation artificially resemble the source. Its value is
that awkward or missing meaning becomes visible to a non-fluent maintainer.

## Voice and register

- **Isa:** technically fluent, energetic, irreverent, and emotionally evasive.
  Her jokes should sound spontaneous rather than formally literary. Preserve her
  warmth and swagger without making her childish.
- **Isaac and archived records:** controlled and information-dense. Keep the
  tension between institutional language and personal material.
- **System/status lines:** compact, neutral, and immediately scannable.
- **Player options:** concise, conversational, and faithful to the response they
  trigger. Never add promises, hostility, romance, or moral judgment absent from
  English.
- **Technical prose:** prefer established Starsector/community terminology once
  locked in the glossary. Do not replace a precise mechanic with atmospheric
  language.
- **Ceremonial or religious language:** elevated only when English is elevated.
  Avoid pseudo-classical Chinese unless the line is explicitly marked for that
  treatment.

## Locale policy

### Mainland Simplified (`zh-Hans-CN`)

Use natural mainland Mandarin vocabulary and Simplified characters for normal
dialogue and gameplay text. Traditional characters may appear only as a marked
artistic choice in a small diegetic fragment. The target unit must list the exact
fragment in `traditionalArtFragments` and explain why in `artNote`.

### Taiwan Traditional (`zh-Hant-TW`)

Use Traditional characters and Taiwan-oriented Mandarin vocabulary, including UI
and technical word choices. Do not merely convert glyphs from the mainland draft.
This target is not a claim to be a Hong Kong Cantonese localization.

## Starsector and CSV constraints

- Preserve every token beginning with `$` exactly, including case.
- Preserve gameplay quantities and codes unless a documented formatting decision
  says otherwise.
- Preserve paragraph boundaries and the order of information.
- Spoken quotation marks must be straight ASCII `"`. Never use `“` or `”`.
- Do not edit commands, conditions, rule IDs, option IDs, or option priorities.
- Translate bracketed runtime lines such as `[Objective updated: ...]` as complete
  units. Their matching `SetTextHighlights` strings must use exactly the same
  target text; the builder and validator enforce this.
- Script-string targets cannot contain a literal ASCII `"`, because they are
  already quoted arguments in Starsector rule commands.

## Confidence and escalation

Use `high` only when meaning, terminology, voice, and structure are all clear.
Use `medium` for a sound draft with a debatable stylistic or terminology choice.
Use `low` whenever lore interpretation, idiom, wordplay, speaker intent, or a
technical term is uncertain. Add a short English explanation to `issues`.

Never hide uncertainty by choosing generic wording. A visible question is safer
than a confident mistranslation.
