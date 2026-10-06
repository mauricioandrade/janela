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
| `lib/score.ts` | Score tiers, their `score-*` fill/track/icon classes, WHO UV categories |
| `lib/sky.ts` | Dawn / day / dusk for a window's start — sets `<html data-sky>`, which tints the page's sky |
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
| `Results` | Chooses the state to show: loading, error, empty, no windows, or results — in the order answer, why, context, alternatives |
| `BestWindow` | The answer: day, the time as the page's one hero number, score, and four `StatTile`s in a `dl` |
| `Narrative` | The model's recommendation and its "written by Gemma / template" badge |
| `DayStrip` | Visual band of daylight hours with the windows cut out — decorative, `aria-hidden` |
| `Alternatives` | The runner-up windows as an ordered list of quieter cards |
| `ScoreBadge` / `ScoreMeter` | A score as status (icon + word + number, never colour alone) and as a meter whose track is a lighter step of its fill |
| `StatTile` | One labelled weather number (`dt`/`dd`), with an optional qualifier such as the UV category |
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
- **Tokens, not colors.** Use semantic classes (`bg-primary`, `text-muted-foreground`) and the Janela tokens in
  `src/index.css`: `score-high`, `score-mid`, `score-low`, `night`. Dark mode comes from the `.dark` tokens — no
  `dark:` color overrides.
- **Spacing** with `flex`/`grid` + `gap-*`, never `space-y-*`. Equal sizes with `size-*`.
- **Type:** Inter Variable for text (optical sizing on), Bricolage Grotesque (`font-heading`) for the wordmark
  and window titles only. Times use `tabular-nums`.
- **Copy** goes in `lib/i18n.ts` for both languages, in sentence case. Errors say what happened and what to do.
- **Motion:** windows "open" in the day strip, and the sky eases to its new colour; nothing else moves.
  Both respect `prefers-reduced-motion`.
- **Layout:** one column on phones and tablets; from `lg` the form is a sticky sidebar and results take the
  rest. Check 390, 820 and 1440 px.

## Accessibility checklist

- Landmarks: one `header`, one `main`, one `footer` (Open-Meteo's CC BY 4.0 attribution must stay there); result blocks are `section`s with an `h2` (visually hidden when the
  layout already makes the role obvious); each window is an `article` with an `h3`.
- A visually hidden status line (`aria-live="polite"`) announces loading and "3 windows for Itobi – SP";
  errors announce themselves (`Alert` and `FieldError` are `role="alert"`).
- Every control has a visible label (`FieldLabel` or `FieldLegend`); errors use `data-invalid` + `aria-invalid`
  and focus moves to the city field when it is the problem.
- Decorative visuals (`DayStrip`, flags, icons) are `aria-hidden`; the same information is in text nearby.
- Brand and model names carry `translate="no"`.

## Adding a component

1. Check `components/ui` and the registry: `pnpm dlx shadcn@latest search -q <name>` / `add <name>`.
2. Read the added source — it is ours now — and keep it close to upstream.
3. Put display rules (labels, formatting) in `lib/`, not in the component.
4. Read copy with `useI18n()`; add it for both languages.
5. Check both themes and a 390 px viewport, then `pnpm test && pnpm lint && pnpm build`.
