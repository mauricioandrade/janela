# Janela — frontend guide

Single-page React app for [Janela](../README.md). This guide is the map: where things live, who owns which
state, and the conventions to keep when you add something.

```bash
pnpm install
pnpm dev      # http://localhost:5173 — expects the backend on http://localhost:8080 (override with VITE_API_URL)
pnpm test     # Vitest, for the pure modules in lib/
pnpm lint
pnpm build    # type-checks, then bundles
```

## Layers

```
src/
├── main.tsx              providers: TanStack Query (no retries, 10-min stale time), theme
├── App.tsx               page shell, language, URL sync, status line, footer
├── lib/                  no React: API client, formatting, score tiers, place labels, i18n strings, URL state
├── hooks/                useWindowsSearch, useI18n, useDebouncedValue
├── components/           Janela's components
└── components/ui/        shadcn/ui sources (base-nova style, Base UI primitives) — vendored, edit sparingly
```

Dependencies point one way: `components/ui` ← `components` ← `App`, and everything may use `lib`. `lib` never
imports React components, so formatting and labels stay testable on their own (`*.test.ts` next to each
module).

| Module | Owns |
|--------|------|
| `lib/api.ts` | Types mirroring the backend (`WindowsResponse`, `SearchValues`, `City`, `Place`), `fetchWindows` and `fetchCities` against `/api/v1`, `ApiError` (status + `fields` from problem details) |
| `lib/format.ts` | Day and time formatting from the API's local `yyyy-MM-ddTHH:mm` strings (never shifted to the browser's time zone) |
| `lib/score.ts` | Score tiers and their colour classes, WHO UV categories, when UV and rain deserve a warning |
| `lib/place.ts` | "Itobi – SP" labels, Brazilian state codes, flag URLs (country flags lazy-loaded from `country-flag-icons`, state flags in `public/flags/br`) |
| `lib/i18n.ts` | All interface copy in `pt` and `en`; the narrative's language comes from the backend |
| `I18nProvider` / `useI18n()` | The current `lang` and its copy `t`, for any component — no `t`/`lang` props |
| `useWindowsSearch` | The windows query for a submitted search, plus 404/400 mapped to form-field problems |
| `lib/url-state.ts` | Shareable `?city=&cityId=&activity=&duration=&days=&lang=` links |

## Components

| Component | Role |
|-----------|------|
| `SearchForm` | Form state (city text, picked place, activity, duration, days), client-side validation, field problems turned into copy; calls `onSearch` |
| `CityCombobox` | City field with debounced suggestions (`["cities", term, lang]` query); picking one pins its id |
| `Results` | Chooses the state: loading, error, empty (an example tide), no windows, or results — answer, why, each day's comfort, every option |
| `Answer` | The answer as one heading: activity, day and place, then the time at high-water size; one timetable row of readings and the "computed in Java" line |
| `Narrative` | The model's recommendation under its byline badge (Gemma, or the offline template) |
| `TideChart` | The signature: each day's hourly comfort as a tide curve, windows as labelled high-tide bands, the 40 cut-off dashed; hover/tap reads an hour |
| `WindowsTable` | Every window as a timetable (`table` from `sm`, stacked list on phones) |
| `ScoreBadge` | A score as status: icon, number and word — never colour alone |
| `UvWarning` / `RainWarning` | Sun-amber and storm-violet icons with labels for WHO-high UV and rain ≥ 40 % |
| `PlaceFlags` | Country and state flags next to a place name (decorative) |

## State

- **Server state** lives in TanStack Query only: `["windows", params]` runs when a search is submitted (and when
  the language changes, since `lang` is part of the key); `["cities", term, lang]` backs the suggestions.
- **Form state** stays inside `SearchForm`; it reports a finished search upward and nothing else.
- **App state** is the submitted search and the language, mirrored to the URL and `localStorage` (language only).
  The shared link is read once, inside `App`.
- Errors map to places: 404 and 400-with-`fields` show inline on the field; 503 and network errors show an `Alert`.

## Conventions

- **shadcn first.** Compose existing components (`Field`, `ToggleGroup`, `Empty`, `Alert`, `Badge`, `Skeleton`)
  before writing markup. Base UI uses `render`, not `asChild`; toggle-group values are string arrays.
- **Design system.** The visual world (tide table) is recorded in [`../DESIGN.md`](../DESIGN.md); product truth in
  [`../PRODUCT.md`](../PRODUCT.md). Read them before visual work.
- **Tokens, not colors.** Use semantic classes (`bg-primary`, `text-muted-foreground`) and the Janela tokens in
  `src/index.css`: `shell`, `tide`, `sun`, `storm`, `score-high|mid|low`. Dark mode comes from the `.dark` tokens — no
  `dark:` color overrides.
- **Spacing** with `flex`/`grid` + `gap-*`, never `space-y-*`. Equal sizes with `size-*`.
- **Type:** Archivo Variable everywhere; `font-condensed` for times and numbers, `font-wide` for the wordmark.
  Columns of numbers use `tabular-nums`.
- **Copy** goes in `lib/i18n.ts` for both languages, in sentence case. Errors say what happened and what to do.
- **Motion:** one authored moment — each day's tide draws in and its windows fill when results land; nothing
  else moves. It is off under `prefers-reduced-motion`.
- **Layout:** one column on phones and tablets; from `lg` the form is a sticky sidebar and results take the
  rest. Check 390, 820 and 1440 px.

## Accessibility checklist

- Landmarks: one `header`, one `main`, one `footer` (Open-Meteo's CC BY 4.0 attribution must stay there); result blocks are `section`s with an `h2` (visually hidden when the
  layout already makes the role obvious); each window is an `article` with an `h3`.
- A visually hidden status line (`aria-live="polite"`) announces loading and "3 windows for Itobi – SP";
  errors announce themselves (`Alert` and `FieldError` are `role="alert"`).
- Every control has a visible label (`FieldLabel` or `FieldLegend`); errors use `data-invalid` + `aria-invalid`
  and focus moves to the city field when it is the problem.
- Each tide row is `role="img"` with a one-line summary; the timetable carries the same data. Flags and icons
  are `aria-hidden` unless they are the only carrier (warnings have `aria-label`).
- Brand and model names carry `translate="no"`.

## Adding a component

1. Check `components/ui` and the registry: `pnpm dlx shadcn@latest search -q <name>` / `add <name>`.
2. Read the added source — it is ours now — and keep it close to upstream.
3. Put display rules (labels, formatting) in `lib/`, not in the component.
4. Read copy with `useI18n()`; add it for both languages.
5. Check both themes and a 390 px viewport, then `pnpm test && pnpm lint && pnpm build`.
