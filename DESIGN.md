---
name: janela
description: When to go outside, read off each day's comfort tide.
colors:
  sea-mist: "oklch(0.965 0.009 215)"
  chart-paper: "oklch(0.99 0.004 215)"
  marine-navy: "oklch(0.27 0.05 240)"
  tide-teal: "oklch(0.52 0.09 195)"
  tide-teal-ring: "oklch(0.6 0.09 195)"
  shallow-water: "oklch(0.91 0.03 195)"
  mist-secondary: "oklch(0.925 0.016 215)"
  mist-muted: "oklch(0.935 0.012 215)"
  slate-ink-muted: "oklch(0.46 0.035 235)"
  hairline: "oklch(0.875 0.016 220)"
  input-stroke: "oklch(0.85 0.02 220)"
  shell-foreground: "oklch(0.97 0.01 215)"
  shell-muted: "oklch(0.78 0.03 225)"
  sun-amber: "oklch(0.72 0.14 70)"
  storm-violet: "oklch(0.5 0.08 285)"
  coral-low: "oklch(0.56 0.16 30)"
  destructive: "oklch(0.55 0.17 28)"
  night-sea: "oklch(0.19 0.03 238)"
  night-paper: "oklch(0.23 0.032 238)"
  night-shell: "oklch(0.14 0.03 240)"
  night-ink: "oklch(0.95 0.01 215)"
  night-ink-muted: "oklch(0.74 0.03 225)"
  night-tide: "oklch(0.74 0.1 190)"
  night-sun: "oklch(0.8 0.13 75)"
  night-storm: "oklch(0.7 0.08 285)"
  night-coral: "oklch(0.7 0.15 30)"
  night-hairline: "oklch(1 0 0 / 11%)"
typography:
  wordmark:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.875rem"
    fontWeight: 800
    letterSpacing: "-0.025em"
    fontVariation: "'wdth' 118"
  display-time:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "clamp(4rem, 14vw, 7.5rem)"
    fontWeight: 700
    lineHeight: 0.9
    letterSpacing: "-0.02em"
    fontVariation: "'wdth' 72"
  headline:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 600
    lineHeight: 1.33
    letterSpacing: "-0.025em"
  title:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 600
    lineHeight: 1.55
    letterSpacing: "-0.025em"
  reading:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 600
    lineHeight: 1.33
    fontFeature: "'tnum'"
    fontVariation: "'wdth' 72"
  narrative:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.0625rem"
    fontWeight: 400
    lineHeight: 1.625
  body:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.43
  label:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 400
    lineHeight: 1.33
  tick:
    fontFamily: "Archivo Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 600
    fontFeature: "'tnum'"
    fontVariation: "'wdth' 72"
rounded:
  flag: "2px"
  label: "4px"
  sm: "4.8px"
  md: "6.4px"
  lg: "8px"
  xl: "11.2px"
  pill: "20.8px"
spacing:
  hair: "4px"
  xs: "8px"
  sm: "12px"
  md: "16px"
  lg: "20px"
  xl: "32px"
  section: "40px"
  column: "48px"
components:
  shell-bar:
    backgroundColor: "{colors.marine-navy}"
    textColor: "{colors.shell-foreground}"
    padding: "16px 32px"
  button-primary:
    backgroundColor: "{colors.tide-teal}"
    textColor: "{colors.chart-paper}"
    rounded: "{rounded.lg}"
    padding: "0 10px"
    height: "40px"
  button-outline:
    backgroundColor: "{colors.sea-mist}"
    textColor: "{colors.marine-navy}"
    rounded: "{rounded.lg}"
    height: "32px"
  toggle-outline:
    textColor: "{colors.marine-navy}"
    rounded: "{rounded.lg}"
    height: "36px"
  toggle-outline-pressed:
    backgroundColor: "{colors.tide-teal}"
    textColor: "{colors.chart-paper}"
    rounded: "{rounded.lg}"
  input-field:
    textColor: "{colors.marine-navy}"
    rounded: "{rounded.lg}"
    height: "36px"
  panel:
    backgroundColor: "{colors.chart-paper}"
    textColor: "{colors.marine-navy}"
    rounded: "{rounded.xl}"
    padding: "24px"
  authorship-badge:
    backgroundColor: "{colors.tide-teal}"
    textColor: "{colors.chart-paper}"
    rounded: "{rounded.pill}"
    typography: "{typography.body}"
    height: "24px"
    padding: "0 10px"
  tide-row:
    backgroundColor: "{colors.chart-paper}"
    rounded: "{rounded.md}"
    height: "112px"
  tide-window-label-best:
    backgroundColor: "{colors.tide-teal}"
    textColor: "{colors.chart-paper}"
    typography: "{typography.tick}"
    rounded: "{rounded.label}"
    padding: "2px 6px"
  tide-window-label:
    backgroundColor: "{colors.chart-paper}"
    textColor: "{colors.marine-navy}"
    typography: "{typography.tick}"
    rounded: "{rounded.label}"
    padding: "2px 6px"
---

# Design System: janela

## Overview

**Creative North Star: "The Tide Table"**

janela reads each day the way a harbour tide table reads the sea: a curve of comfort rising and falling across the daylight hours, the good windows marked like high water with their times set in tall condensed figures. The world is maritime and printed rather than glossy: a marine-navy shell bar, a cool sea-mist ground, chart-paper panels, and one tide-teal colour that means comfort and action at once. Heat and storms each get their own colour (sun amber, storm violet), and those colours only ever show up as icons with labels.

The page answers before it explains. The best window is a heading whose time is set at high-water size, then the four readings in a ruled row, then the model's recommendation in a quiet panel, then one tide row per day, then a timetable of every window. The layout stays dense and tabular, as on a printed table: hairline rules, columns of tabular numbers, no decorative fill. It has to stay light on a phone and must never look like a corporate dashboard.

Explicitly rejected: the weather app's stack of identical icon cards, a hero of stat tiles, cream or warm-paper grounds, grain, and gradients.

**Key Characteristics:**
- One typeface, Archivo Variable, played across its width axis: condensed (72%) for every time and number, normal for interface text, wide (118%) only for the wordmark.
- Tide teal is the single accent: comfort curve, best window, primary actions, pressed toggles, focus ring.
- Flat, ruled surfaces: hairline borders and 1px rings, no drop shadows on panels.
- Status is never colour alone: score = icon + number + word; warnings = icon with an accessible label.
- One authored motion moment: the tides draw in when results land.

## Colors

A cool, low-chroma sea palette (hue 215–240) carries the ground and ink. One mid-chroma teal does the work, and two signal hues are kept for weather warnings. Values are OKLCH and come from `frontend/src/index.css`. The `night-*` tokens are the `.dark` theme ("night sea"), which uses the same ramps stepped for a dark room.

### Primary
- **Tide Teal** (`tide-teal`): the comfort curve and its 15% fill, the best window's band (25% opacity) and its label, the primary button, pressed toggles, the Gemma authorship badge, the high score tier icon, the rank-1 row tint (6%), and the logo's frame. `primary`, `tide` and `score-high` share this value. **Night Tide** (`night-tide`) plays the same role in dark mode, where it is lifted so it reads on navy.
- **Tide Teal Ring** (`tide-teal-ring`): focus rings, always at 50% alpha.
- **Shallow Water** (`shallow-water`): the `accent` surface for highlighted menu items in comboboxes and selects.

### Secondary
- **Sun Amber** (`sun-amber`; night `night-sun`): high UV (WHO "high" and above), the mid score tier icon, and the lit pane in the window mark. It fails text contrast on sea-mist, so it is used only for icons and fills, never for text.
- **Storm Violet** (`storm-violet`; night `night-storm`): likely rain (40% chance or more), as an icon only.

### Tertiary
- **Coral Low** (`coral-low`; night `night-coral`): the low score tier icon (below 40).
- **Destructive** (`destructive`): error alerts and invalid fields.

### Neutral
- **Sea Mist** (`sea-mist`; night `night-sea`): the page ground. It is cool, never cream.
- **Chart Paper** (`chart-paper`; night `night-paper`): panels, tide rows, popovers, and text on teal.
- **Marine Navy** (`marine-navy`): body ink and the shell bar. Ink and shell share the hue, so the page reads as one printed sheet. **Night Shell** (`night-shell`) darkens the bar below the night ground.
- **Slate Ink Muted** (`slate-ink-muted`; night `night-ink-muted`): labels, dates, captions, tier words, units, and the window floor line (at 50%).
- **Hairline** (`hairline`; night `night-hairline`, white at 11%): every border, table rule, divider, and tide tick line.
- **Input Stroke** (`input-stroke`): field and toggle outlines.
- **Mist Secondary / Mist Muted** (`mist-secondary`, `mist-muted`): hover fills and skeletons.
- **Shell Foreground / Shell Muted** (`shell-foreground`, `shell-muted`): the wordmark and the tagline on the navy bar.

### Named Rules
**The One Tide Rule.** Teal means comfort or action, and nothing else. Do not introduce a second accent; a new state reuses teal at an opacity step (6%, 12%, 15%, 25%) or a neutral.

**The Signal-Not-Text Rule.** Sun amber, storm violet and coral are icon and fill colours. Text always stays in ink or muted ink, because amber on sea-mist fails contrast.

**The Cool Ground Rule.** The ground is sea-mist (hue 215), never cream, beige or warm paper.

## Typography

**Display Font:** Archivo Variable (with ui-sans-serif, system-ui fallback), loaded from `@fontsource-variable/archivo/wdth.css`
**Body Font:** Archivo Variable
**Label/Mono Font:** Archivo Variable, condensed (`font-stretch: 72%`), with tabular figures

**Character:** a single grotesque used like a printed table. Width does the job a second typeface would do. Condensed figures stack tight and tall like tide-table times, normal width carries interface prose, and the wide cut (`font-stretch: 118%`) belongs to the wordmark alone.

### Hierarchy
- **Wordmark** (800, 1.875rem, wide 118%, tracking-tight): "janela", lowercase, on the shell bar next to the window mark. Used nowhere else.
- **Display Time** (700, clamp(4rem, 14vw, 7.5rem), line-height 0.9, -0.02em, condensed): the best window's start and end, inside the answer heading. The dash between them is weight 300 in muted ink. There is one per page.
- **Headline** (600, 1.25rem to 1.5rem, tracking-tight): the answer lead ("Run on Thursday, 8 Oct, in …", with the place in 400 muted), plus the empty-state and no-windows titles.
- **Title** (600, 1.125rem, tracking-tight): section headings such as "Comfort across the days" and "All windows".
- **Reading** (600, 1.5rem, condensed, tabular): the four readings under the answer. Timetable cells use the same treatment at 1rem, with the time column at 1.125rem.
- **Narrative** (400, 1.0625rem, line-height 1.625, max 65ch, text-pretty): the model's recommendation.
- **Body** (400, 0.875rem): form, captions, footer, tagline.
- **Label** (400, 0.75rem, muted, sentence case): reading labels, table headers, axis ticks (condensed), and the "computed in Java" provenance line.

### Named Rules
**The Width-Is-Meaning Rule.** Every time, score and measurement is condensed and tabular. Interface words stay at normal width, and only the wordmark is wide.

**The No-Eyebrow Rule.** Labels are sentence case and sit directly over their value. There are no uppercase tracked kickers above headings.

## Layout

The content sits in one centred column, max 72rem, with side gutters of 16, 24 and 32px at the base, sm and lg breakpoints. From lg (1024px) it splits into two columns: the search panel on the left (up to 22rem, sticky at 24px from the top) and the results on the right, separated by a 48px gap. Below lg the column stacks into bar, tagline, search, then answer. When results land on a phone, the page scrolls them into view (smoothly, unless reduced motion is on).

Results stack in reading order with 40px between blocks: the answer, then the narrative, then the tide rows, then the timetable. Each tide row is a two-column grid: a day label column (3.75rem, 4.5rem from sm) and the chart (96px tall, 112px from sm). A shared hour axis sits under the last row. The four readings sit in a ruled `dl`: 2 columns on a phone and 5 from sm, each cell divided by a vertical hairline. The timetable is a real table from sm; below that it becomes an ordered list of ruled rows.

Spacing follows Tailwind's 4px step. The values in use are 4, 8, 12, 16, 20, 24, 32, 40 and 48px.

## Elevation & Depth

The system is flat. Panels have no drop shadow. Depth comes from tone (chart-paper panels on a sea-mist ground) and from hairlines: a 1px border, or a 1px ring at foreground/10 on the search card. Only floating layers (popovers, select and combobox menus, which come from shadcn/ui primitives) carry their library elevation. The one other "shadow" in the build is a 1px ring drawn around flag images at foreground/15, which acts as a border.

### Named Rules
**The Ruled-Not-Raised Rule.** A surface is separated by a hairline or a tonal step, never by a shadow. If a new panel seems to need a shadow, use a border instead.

## Shapes

The corners are moderate, set from a 0.5rem base radius. Controls (buttons, inputs, toggles) use 8px. Tide rows and the empty-state example chart use 6.4px. The search card and the narrative panel use 11.2px. Tide window labels and the hover readout use 4px, flags use 2px, and only the authorship badge is a full pill. Lines are hairline: the tide curve is a 2px non-scaling stroke with round joins, tick lines are 1px, and the window floor (score 40) is a 4/4 dashed line. The window mark repeats the system's geometry: a rounded square split into four panes, with one pane lit in sun amber.

## Components

### Buttons
- **Shape:** gently rounded (8px).
- **Primary:** tide teal fill and chart-paper text, 14px, weight 500, with a leading 16px icon. The search submit is 40px tall and full width.
- **Hover / Focus:** the fill drops to 80% on hover. Focus shows a 3px tide-ring at 50%. On press (`:active`) the button moves down 1px.
- **Outline:** sea-mist fill with a hairline border and muted hover. Used for Retry.

### Chips (toggle groups)
- **Style:** outline toggles with an input-stroke border on a transparent fill. Pressed toggles fill with tide teal and chart-paper text.
- **State:** activity choices are stacked icon-over-label tiles (3 columns, 5 at sm, back to 3 inside the lg side panel). Days are equal-width segments. The PT/EN switch in the shell bar uses shell-foreground strokes at 25%.

### Cards / Containers
- **Corner Style:** 11.2px.
- **Background:** chart paper.
- **Shadow Strategy:** none (see Elevation & Depth).
- **Border:** a 1px hairline, or on the search card a 1px ring at foreground/10.
- **Internal Padding:** 16px on the search card, 20px on the narrative (24px from sm).
- Only two containers exist: the search panel and the narrative panel. Results are never put in cards.

### Inputs / Fields
- **Style:** input-stroke outline, 8px radius, 36px tall, transparent on light and input/30 on dark. The city field has a map-pin or place-flags addon.
- **Focus:** the border turns ring and gains a 3px ring at 50%.
- **Error:** destructive border and a 3px destructive/20 ring. The field error appears below the field, and focus moves to it.

### Navigation (shell bar)
- Marine navy, full bleed. On the left: the window mark (32px), the wide wordmark, and from md a tagline separated by a 20% hairline. On the right: the PT/EN switch. There are no nav links. The footer is a plain hairline-topped line that credits Open-Meteo (CC BY 4.0) and says where the text comes from.

### Answer (signature)
The best window is a single `h2`: the lead line names the activity, day and place (with flags), and the time follows at display size. Under it comes the ruled readings row: the score first, then feels-like, UV (with its WHO word), rain and wind. Each label is muted and 12px, and each value is condensed, 24px and weight 600. A 12px provenance line with a calculator icon closes it, stating that the times and scores were computed in Java.

### Tide Chart (signature)
There is one row per day. The day label shows the weekday in 600 and the condensed muted date. The chart is a chart-paper box with a hairline border. Inside it: 3-hourly tick lines, window bands in teal (25% for the best window, 12% for the others), a dashed floor at score 40, the area filled at tide/15, and a smooth Catmull–Rom curve stroked in tide teal. Each window has a condensed label pinned to the top of the row: the best window's label is filled teal, the others are chart paper with a 1px ring, and each begins with its rank number at 70% opacity. Pointer hover shows a 1px vertical guide, a teal dot ringed in chart paper, and an ink readout chip. The row is `role="img"` with a spoken summary, and the timetable carries the same data for assistive tech.

### Timetable
The heading is "All windows". It has rank, day, time, score, and then right-aligned feels-like, UV, rain and wind. The body is condensed and tabular, with rows separated by hairlines. The best row is tinted primary/6 and set in 600. UV and rain values are preceded by their warning icon when it applies.

### Score
Every score is shown the same way: a tier icon (check, minus or alert in teal, amber or coral), then the number (condensed, 600, tabular), then the tier word in muted ink. Tiers: 70 and up is high, 40 and up is mid, anything lower is low.

### Warnings
Two warnings exist. A sun icon in sun amber flags high UV, and a cloud-rain icon in storm violet flags a likely rain chance. Each is 16px, `role="img"`, and has a translated label. The values next to them stay in ink.

### Authorship Badge
A 24px pill at the top of the narrative panel. It is filled teal with a CPU icon and reads "Written locally by <model>" when Gemma wrote the text. It switches to an outline badge with a file icon when the template wrote it.

### Motion
The page has one authored moment. When results land, each day's tide curve draws in (`tide-draw`, 900ms, cubic-bezier(0.16, 1, 0.3, 1)), staggered 120ms per day. Then its window bands rise from the bottom (`tide-fill`, 700ms, same easing), 300ms in plus 80ms per rank. Under `prefers-reduced-motion: reduce` both animations are turned off. Everything else is library state transitions only.

## Do's and Don'ts

### Do:
- **Do** set every time, score and measurement in condensed Archivo (72%) with tabular figures.
- **Do** make the best window's time the largest thing on the page, inside the answer heading.
- **Do** separate surfaces with hairlines (`hairline`, or white at 11% in dark) and tonal steps between sea-mist and chart paper.
- **Do** show a score as icon + number + word, and a warning as an icon with its own accessible label.
- **Do** reuse tide teal at opacity steps (6, 12, 15, 25%) for new comfort or selection states instead of adding a hue.
- **Do** define every new colour in both `:root` and `.dark`, using the same role names.
- **Do** keep a reduced-motion path for any new animation, and keep the tide draw the only authored moment.

### Don't:
- **Don't** colour text with sun amber, storm violet or coral; they fail text contrast on sea-mist.
- **Don't** add eyebrow labels or uppercase tracked kickers above headings.
- **Don't** build a hero of stat tiles or a stack of identical weather cards; readings stay in one ruled row and windows stay in a timetable.
- **Don't** add drop shadows to panels, grain, textures or gradients.
- **Don't** use a cream, beige or warm-paper ground.
- **Don't** use the wide (118%) width anywhere but the wordmark.
- **Don't** put results inside cards; only the search panel and the narrative panel are containers.
