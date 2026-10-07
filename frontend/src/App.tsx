import { useEffect, useRef, useState } from "react"

import { I18nProvider } from "@/components/i18n-provider"
import { Results } from "@/components/results"
import { SearchForm } from "@/components/search-form"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { useWindowsSearch } from "@/hooks/use-windows-search"
import type { Lang, SearchValues } from "@/lib/api"
import { messages } from "@/lib/i18n"
import { placeLabel } from "@/lib/place"
import { completeSearch, readSearchFromUrl, writeLangToUrl, writeSearchToUrl } from "@/lib/url-state"

const LANG_KEY = "janela.lang"

function initialLang(fromUrl?: Lang): Lang {
  if (fromUrl) return fromUrl
  try {
    const stored = localStorage.getItem(LANG_KEY)
    if (stored === "pt" || stored === "en") return stored
  } catch {
    // Storage can be unavailable (private mode); fall back to the browser language.
  }
  const preferred = navigator.languages.find((tag) => /^(pt|en)\b/i.test(tag))
  return preferred?.toLowerCase().startsWith("pt") ? "pt" : "en"
}

export function App() {
  const [fromUrl] = useState(() => readSearchFromUrl())
  const [lang, setLang] = useState<Lang>(() => initialLang(fromUrl.lang))
  // A shared link with a full search runs it right away.
  const [search, setSearch] = useState<SearchValues | null>(() => completeSearch(fromUrl))
  const windows = useWindowsSearch(search, lang)
  const resultsRef = useRef<HTMLDivElement>(null)
  const t = messages[lang]

  useEffect(() => {
    document.documentElement.lang = lang === "pt" ? "pt-BR" : "en"
    try {
      localStorage.setItem(LANG_KEY, lang)
    } catch {
      // Remembering the language is a convenience only.
    }
  }, [lang])

  // On a phone the form sits above the answer: bring the answer into view when it lands.
  const answeredAt = windows.isFetching ? 0 : windows.dataUpdatedAt
  useEffect(() => {
    if (!answeredAt || !search || window.matchMedia("(min-width: 1024px)").matches) return
    const smooth = !window.matchMedia("(prefers-reduced-motion: reduce)").matches
    resultsRef.current?.scrollIntoView({ behavior: smooth ? "smooth" : "auto", block: "start" })
  }, [answeredAt, search])

  function handleSearch(values: SearchValues) {
    setSearch(values)
    writeSearchToUrl({ ...values, lang })
  }

  function handleLangChange(next: Lang) {
    setLang(next)
    if (search) writeLangToUrl(next)
  }

  // One short sentence for screen readers; errors announce themselves (Alert and FieldError are role="alert").
  const status = windows.isFetching
    ? t.loadingGemma
    : windows.data
      ? t.resultsReady(placeLabel(windows.data.location), windows.data.windows.length)
      : ""

  return (
    <I18nProvider lang={lang}>
      <div className="flex min-h-svh flex-col">
        <header className="bg-shell text-shell-foreground">
          <div className="mx-auto flex w-full max-w-6xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
            <div className="flex min-w-0 items-center gap-3 sm:gap-4">
              <h1 translate="no" className="flex items-center gap-2.5 font-wide text-3xl font-extrabold tracking-tight">
                <WindowMark />
                janela
              </h1>
              <p className="hidden max-w-xs border-l border-shell-foreground/20 pl-4 text-sm leading-snug text-shell-muted md:block">
                {t.tagline}
              </p>
            </div>
            <ToggleGroup
              variant="outline"
              size="sm"
              spacing={0}
              aria-label={t.language}
              value={[lang]}
              onValueChange={(value) => value.length > 0 && handleLangChange(value[0] as Lang)}
              className="[&>button]:border-shell-foreground/25 [&>button]:text-shell-foreground [&>button:hover]:bg-shell-foreground/10"
            >
              <ToggleGroupItem value="pt" aria-label="Português">
                PT
              </ToggleGroupItem>
              <ToggleGroupItem value="en" aria-label="English">
                EN
              </ToggleGroupItem>
            </ToggleGroup>
          </div>
        </header>

        <main className="mx-auto grid w-full max-w-6xl flex-1 gap-8 px-4 py-6 sm:px-6 sm:py-8 lg:grid-cols-[minmax(0,22rem)_minmax(0,1fr)] lg:items-start lg:gap-12 lg:px-8 lg:py-10">
          <div className="flex flex-col gap-3 lg:sticky lg:top-6">
            <p className="text-sm text-muted-foreground md:hidden">{t.tagline}</p>
            <SearchForm
              initialValues={fromUrl}
              resolvedPlace={windows.data?.location ?? null}
              isPending={windows.isFetching}
              fieldProblems={windows.fieldProblems}
              onSearch={handleSearch}
            />
          </div>
          <p className="sr-only" aria-live="polite">
            {status}
          </p>
          <div ref={resultsRef} className="min-w-0 scroll-mt-4">
            <Results
              data={windows.data}
              error={windows.error}
              isFetching={windows.isFetching}
              hasSearched={search !== null}
              onRetry={() => windows.refetch()}
            />
          </div>
        </main>

        <footer className="border-t">
          <p className="mx-auto w-full max-w-6xl px-4 py-6 text-sm text-muted-foreground sm:px-6 lg:px-8">
            {t.weatherBy}{" "}
            <a href="https://open-meteo.com" className="underline hover:text-foreground" translate="no">
              Open-Meteo.com
            </a>{" "}
            (
            <a href="https://creativecommons.org/licenses/by/4.0/" className="underline hover:text-foreground">
              CC BY 4.0
            </a>
            ). {t.narrativeBy}
          </p>
        </footer>
      </div>
    </I18nProvider>
  )
}

/** The favicon's window: four panes, one lit by the sun. */
function WindowMark() {
  return (
    <svg viewBox="0 0 32 32" className="size-8 shrink-0" aria-hidden>
      <rect x="3" y="3" width="26" height="26" rx="6" className="fill-tide" />
      <path d="M16 6v20M6 16h20" className="stroke-shell" strokeWidth="2.5" />
      <rect x="5.5" y="5.5" width="9.25" height="9.25" rx="2" className="fill-sun" />
    </svg>
  )
}

export default App
