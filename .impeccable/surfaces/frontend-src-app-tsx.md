---
version: 1
slug: "frontend-src-app-tsx"
primary_target: "frontend/src/App.tsx"
related_targets: ["frontend/src/components"]
---

Scope: the Janela single page (search, result, empty/loading/error states). Mode: Operate.
Audience and job: people in Brazilian tropical cities, on phone and laptop equally, deciding when in the next 1–3 days to go outside; DEV judges evaluating Best Use of Gemma.
Must not: feel heavy or slow on phones, lose the clarity of the answer, look like a corporate dashboard.
Memorable moment: the answer arriving — when to go — read off the day's comfort tide.

## Direction contract

THESIS: Each day is read like a tide table: the hourly comfort score drawn as a tide curve, the good windows marked like high tides with their times. Refuses the weather app's stack of identical icon cards.

OWN-WORLD: Marine navy shell bar, cool sea-mist ground (never cream), tide teal for comfort and primary actions, sun amber for heat and UV warnings, storm slate-violet for rain. Archivo throughout: condensed width for times and numbers, normal width for interface text. Hairline curves, tabular columns, 6–10px corners.

STORY: The visitor sees when to go first, then why (Gemma, labelled as local), then how comfort rises and falls across each day, then the other options in a timetable; changes the search in the side panel.

FIRST VIEWPORT: Navy top bar with mark, lowercase wordmark and PT/EN. Desktop: search panel left (~360px); right, the best window — day, huge condensed time, score and four readings — then the narrative directly below it (the time needs the column's full width, so the narrative moved from beside to below), then one tide row per day. Mobile: bar, search, then the answer.

FORM: Tábua de marés, position 4 on the ordered list, seed key 2cccc6f0.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
