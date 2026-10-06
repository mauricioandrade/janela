import { useEffect, useState } from "react"
import { useQuery } from "@tanstack/react-query"

import { Results } from "@/components/results"
import { SearchForm, type SearchValues } from "@/components/search-form"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { ApiError, fetchWindows, type Lang } from "@/lib/api"
import { messages } from "@/lib/i18n"

const LANG_KEY = "janela.lang"

function initialLang(): Lang {
  try {
    const stored = localStorage.getItem(LANG_KEY)
    if (stored === "pt" || stored === "en") return stored
  } catch {
    // Storage can be unavailable (private mode); fall back to the browser language.
  }
  return navigator.language.toLowerCase().startsWith("pt") ? "pt" : "en"
}

export function App() {
  const [lang, setLang] = useState<Lang>(initialLang)
  const [search, setSearch] = useState<SearchValues | null>(null)
  const t = messages[lang]

  useEffect(() => {
    document.documentElement.lang = lang === "pt" ? "pt-BR" : "en"
    try {
      localStorage.setItem(LANG_KEY, lang)
    } catch {
      // Remembering the language is a convenience only.
    }
  }, [lang])

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
          <h1 className="font-heading text-5xl font-bold tracking-tighter sm:text-6xl">janela</h1>
          <p className="max-w-sm text-pretty text-muted-foreground">{t.tagline}</p>
        </div>
        <ToggleGroup
          variant="outline"
          size="sm"
          spacing={0}
          aria-label={t.language}
          value={[lang]}
          onValueChange={(value) => value.length > 0 && setLang(value[0] as Lang)}
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
        <SearchForm t={t} isPending={query.isFetching} serverErrors={serverErrors} onSearch={setSearch} />
        <Results
          t={t}
          lang={lang}
          data={query.data}
          error={query.error}
          isFetching={query.isFetching}
          hasSearched={search !== null}
          onRetry={() => query.refetch()}
        />
      </main>
    </div>
  )
}

export default App
