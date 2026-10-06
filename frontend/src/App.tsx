import { useEffect, useState } from "react"

import { I18nProvider } from "@/components/i18n-provider"
import { Results } from "@/components/results"
import { SearchForm } from "@/components/search-form"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { useWindowsSearch } from "@/hooks/use-windows-search"
import type { Lang, SearchValues } from "@/lib/api"
import { messages } from "@/lib/i18n"
import { placeLabel } from "@/lib/place"
import { skyFor } from "@/lib/sky"
import {
  completeSearch,
  readSearchFromUrl,
  writeLangToUrl,
  writeSearchToUrl,
} from "@/lib/url-state"

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
  const [search, setSearch] = useState<SearchValues | null>(() =>
    completeSearch(fromUrl)
  )
  const windows = useWindowsSearch(search, lang)
  const t = messages[lang]

  useEffect(() => {
    document.documentElement.lang = lang === "pt" ? "pt-BR" : "en"
    try {
      localStorage.setItem(LANG_KEY, lang)
    } catch {
      // Remembering the language is a convenience only.
    }
  }, [lang])

  // The page's sky takes the colour of the best window's time of day.
  const best = windows.data?.windows[0]
  const sky = best && !windows.isFetching ? skyFor(best.start) : null
  useEffect(() => {
    if (sky) document.documentElement.dataset.sky = sky
    else delete document.documentElement.dataset.sky
  }, [sky])

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
      ? t.resultsReady(
          placeLabel(windows.data.location),
          windows.data.windows.length
        )
      : ""

  return (
    <I18nProvider lang={lang}>
      <div className="mx-auto flex min-h-svh w-full max-w-2xl flex-col gap-10 px-4 py-8 sm:px-6 sm:py-12 lg:max-w-6xl lg:px-8">
        <header className="flex items-start justify-between gap-4">
          <div className="flex flex-col gap-2">
            <h1
              translate="no"
              className="flex items-center gap-3 font-heading text-5xl font-bold tracking-tighter sm:text-6xl"
            >
              <WindowMark />
              janela
            </h1>
            <p className="max-w-md text-lg text-pretty text-muted-foreground">
              {t.tagline}
            </p>
          </div>
          <ToggleGroup
            variant="outline"
            size="sm"
            spacing={0}
            aria-label={t.language}
            value={[lang]}
            onValueChange={(value) =>
              value.length > 0 && handleLangChange(value[0] as Lang)
            }
          >
            <ToggleGroupItem value="pt" aria-label="Português">
              PT
            </ToggleGroupItem>
            <ToggleGroupItem value="en" aria-label="English">
              EN
            </ToggleGroupItem>
          </ToggleGroup>
        </header>

        <main className="grid gap-10 lg:grid-cols-[minmax(0,26rem)_minmax(0,1fr)] lg:items-start lg:gap-12">
          <div className="lg:sticky lg:top-8">
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
          <div className="min-w-0">
            <Results
              data={windows.data}
              error={windows.error}
              isFetching={windows.isFetching}
              hasSearched={search !== null}
              onRetry={() => windows.refetch()}
            />
          </div>
        </main>

        <footer className="mt-auto border-t pt-6 text-sm text-muted-foreground">
          {t.weatherBy}{" "}
          <a
            href="https://open-meteo.com"
            className="underline underline-offset-4 hover:text-foreground"
            translate="no"
          >
            Open-Meteo.com
          </a>{" "}
          (
          <a
            href="https://creativecommons.org/licenses/by/4.0/"
            className="underline underline-offset-4 hover:text-foreground"
          >
            CC BY 4.0
          </a>
          ). {t.narrativeBy}
        </footer>
      </div>
    </I18nProvider>
  )
}

/** The favicon's window: four panes, one lit by the sun. */
function WindowMark() {
  return (
    <svg
      viewBox="0 0 32 32"
      className="size-10 shrink-0 sm:size-12"
      aria-hidden
    >
      <rect
        x="3"
        y="3"
        width="26"
        height="26"
        rx="6"
        className="fill-primary"
      />
      <path
        d="M16 6v20M6 16h20"
        className="stroke-primary-foreground"
        strokeWidth="2.5"
      />
      <rect
        x="5.5"
        y="5.5"
        width="9.25"
        height="9.25"
        rx="2"
        className="fill-score-mid"
      />
    </svg>
  )
}

export default App
