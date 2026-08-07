# Project instructions

## Starsector rules

- `data/campaign/rules.csv` is the runtime dialogue authority and must remain valid UTF-8 CSV with exactly these seven columns: `id,trigger,conditions,script,text,options,notes`.
- Never put typographic double quotation marks (`U+201C` or `U+201D`) in `rules.csv`. Starsector misparses them and may throw `NumberFormatException: For input string` during startup.
- Write spoken quotation marks as ASCII `"` characters. Inside a quoted CSV field, escape every literal ASCII quote by doubling it as `""`.
- Multiline quoted CSV fields are supported. Preserve their CSV quoting and do not process this file as independent physical lines.
- Every bracketed runtime status or quest-progression line in the `text` column must be included verbatim in `SetTextHighlights` for that rule.
- After every `rules.csv` edit, run `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\validate_rules.ps1`. Treat any validation failure as blocking.
- Before packaging, regenerate the rules-derived proofreading copies with `tools/build_gan_eden_master.ps1`, then run the normal build and package scripts.
