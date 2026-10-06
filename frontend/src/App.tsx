import { useEffect, useState } from "react"
import { useQuery } from "@tanstack/react-query"

import { Results } from "@/components/results"
import { SearchForm, type SearchValues } from "@/components/search-form"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { ApiError, fetchWindows, type Lang } from "@/lib/api"
import { messages } from "@/lib/i18n"
import { readSearchFromUrl, writeLangToUrl, writeSearchToUrl } from "@/lib/url-state"

const LANG_KEY = "janela.lang"

const fromUrl = readSearchFromUrl()

function initialLang(): Lang {
  if (fromUrl.lang) return fromUrl.lang
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
  const [lang, setLang] = useState<Lang>(initialLang)
  // A shared link with a full search runs it right away.
  const [search, setSearch] = useState<SearchValues | null>(() =>
    fromUrl.city && fromUrl.activity && fromUrl.durationMinutes && fromUrl.days
      ? { city: fromUrl.city, activity: fromUrl.activity, durationMinutes: fromUrl.durationMinutes, days: fromUrl.days }
      : null
  )
  const t = messages[lang]

  useEffect(() => {
    document.documentElement.lang = lang === "pt" ? "pt-BR" : "en"
    try {
      localStorage.setItem(LANG_KEY, lang)
    } catch {
      // Remembering the language is a convenience only.
    }
  }, [lang])

  function handleSearch(values: SearchValues) {
    setSearch(values)
    writeSearchToUrl({ ...values, lang })
  }

  function handleLangChange(next: Lang) {
    setLang(next)
    if (search) writeLangToUrl(next)
  }

  const params = search && { ...search, lang }
  const query = useQuery({
    queryKey: ["windows", params],
    queryFn: ({ signal }) => fetchWindows(params!, signal),
    enabled: params !== null,
  })

  const apiError = query.error instanceof ApiError ? query.error : null
  const serverErrors: Partial<Record<keyof SearchValues, string>> = {}
  if (apiError?.status === 404) serverErrors.city = t.cityNotFound
  if (apiError?.status === 400) {
    for (const field of apiError.fields) serverErrors[field as keyof SearchValues] = t.invalidTitle
  }

  return (
    <div className="mx-auto flex min-h-svh w-full max-w-2xl flex-col gap-10 px-4 py-10 sm:py-16">
      <header className="flex items-start justify-between gap-4">
        <div className="flex flex-col gap-2">
          <h1 translate="no" className="font-heading text-5xl font-bold tracking-tighter sm:text-6xl">
            janela
          </h1>
          <p className="max-w-sm text-pretty text-muted-foreground">{t.tagline}</p>
        </div>
        <ToggleGroup
          variant="outline"
          size="sm"
          spacing={0}
          aria-label={t.language}
          value={[lang]}
          onValueChange={(value) => value.length > 0 && handleLangChange(value[0] as Lang)}
        >
          <ToggleGroupItem value="pt" aria-label="Português">
            PT
          </ToggleGroupItem>
          <ToggleGroupItem value="en" aria-label="English">
            EN
          </ToggleGroupItem>
        </ToggleGroup>
      </header>

      <main className="flex flex-col gap-12">
        <SearchForm
          t={t}
          initialValues={fromUrl}
          isPending={query.isFetching}
          serverErrors={serverErrors}
          onSearch={handleSearch}
        />
        <section aria-live="polite" aria-busy={query.isFetching}>
          <Results
          t={t}
          lang={lang}
          data={query.data}
          error={query.error}
          isFetching={query.isFetching}
          hasSearched={search !== null}
          onRetry={() => query.refetch()}
          />
        </section>
      </main>
    </div>
  )
}

export default App
