# Janela — frontend

Single-page React app for [Janela](../README.md): search form, Gemma narrative, day strip and ranked windows.

```bash
pnpm install
pnpm dev      # http://localhost:5173, expects the backend on http://localhost:8080 (override with VITE_API_URL)
pnpm lint
pnpm build
```

- `src/lib/api.ts` — typed client for `GET /api/windows` and its problem-detail errors
- `src/lib/i18n.ts` — pt/en interface copy (the narrative language comes from the backend)
- `src/lib/url-state.ts` — shareable `?city=&activity=&duration=&days=&lang=` links
- `src/components/` — app components; `src/components/ui/` holds shadcn/ui (base-nova) sources
- Theme tokens (palette, score tiers, the window-open animation) live in `src/index.css`
