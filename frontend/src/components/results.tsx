import { CloudOffIcon, SearchXIcon, ServerCrashIcon } from "lucide-react"

import { Answer } from "@/components/answer"
import { Narrative } from "@/components/narrative"
import { TideChart } from "@/components/tide-chart"
import { WindowsTable } from "@/components/windows-table"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Spinner } from "@/components/ui/spinner"
import { useI18n } from "@/hooks/use-i18n"
import { ApiError, type WindowsResponse } from "@/lib/api"

type ResultsProps = {
  data: WindowsResponse | undefined
  error: Error | null
  isFetching: boolean
  hasSearched: boolean
  onRetry: () => void
}

/** Answer first, then why, then each day's comfort, then every option. */
export function Results({ data, error, isFetching, hasSearched, onRetry }: ResultsProps) {
  const { t } = useI18n()
  if (isFetching) return <LoadingState />

  if (error && !isShownInForm(error)) {
    const weatherDown = error instanceof ApiError && error.status === 503
    const invalid = error instanceof ApiError && error.status === 400
    return (
      <Alert variant="destructive">
        {weatherDown ? <CloudOffIcon /> : <ServerCrashIcon />}
        <AlertTitle>{weatherDown ? t.weatherDownTitle : invalid ? t.invalidTitle : t.networkTitle}</AlertTitle>
        <AlertDescription className="flex flex-col items-start gap-3">
          {weatherDown ? t.weatherDownDescription : invalid ? error.message : t.networkDescription}
          <Button variant="outline" size="sm" onClick={onRetry}>
            {t.retry}
          </Button>
        </AlertDescription>
      </Alert>
    )
  }

  if (!data || !hasSearched) return <EmptyState />

  const best = data.windows[0]
  return (
    <div className="flex flex-col gap-10">
      {best ? (
        <Answer window={best} activity={data.activity} place={data.location} />
      ) : (
        <section aria-labelledby="no-windows" className="flex items-start gap-3">
          <SearchXIcon className="mt-1 size-5 shrink-0 text-muted-foreground" aria-hidden />
          <div className="flex flex-col gap-1">
            <h2 id="no-windows" className="text-xl font-semibold tracking-tight">
              {t.noWindowsTitle}
            </h2>
            <p className="text-muted-foreground">{t.noWindowsDescription}</p>
          </div>
        </section>
      )}
      <Narrative narrative={data.narrative} aiGenerated={data.aiGenerated} model={data.model} />
      <TideChart hours={data.hours} windows={data.windows} />
      {data.windows.length > 0 && <WindowsTable windows={data.windows} />}
    </div>
  )
}

/** Before any search: what the page will answer, drawn as an example tide. */
function EmptyState() {
  const { t } = useI18n()
  return (
    <section aria-labelledby="empty-heading" className="flex flex-col gap-5">
      <div className="flex flex-col gap-2">
        <h2 id="empty-heading" className="text-2xl font-semibold tracking-tight text-balance sm:text-3xl">
          {t.emptyTitle}
        </h2>
        <p className="max-w-[55ch] text-muted-foreground">{t.emptyDescription}</p>
      </div>
      <figure className="flex flex-col gap-2" aria-hidden>
        <div className="relative h-28 rounded-md border bg-card">
          <svg viewBox="0 0 1000 100" preserveAspectRatio="none" className="absolute inset-0 size-full">
            <rect x="80" y="0" width="160" height="100" className="fill-primary opacity-25" />
            <rect x="760" y="0" width="120" height="100" className="fill-primary opacity-12" />
            <line x1="0" x2="1000" y1="70" y2="70" className="stroke-muted-foreground/50" strokeDasharray="4 4" vectorEffect="non-scaling-stroke" />
            <path
              d="M0 40 C 80 22, 160 18, 240 28 S 420 80, 520 84 S 700 60, 800 38 S 940 46, 1000 52 L 1000 100 L 0 100 Z"
              className="fill-tide/15"
            />
            <path
              d="M0 40 C 80 22, 160 18, 240 28 S 420 80, 520 84 S 700 60, 800 38 S 940 46, 1000 52"
              fill="none"
              className="stroke-tide"
              strokeWidth="2"
              vectorEffect="non-scaling-stroke"
            />
          </svg>
          <span className="absolute top-1.5 left-[16%] -translate-x-1/2 rounded bg-primary px-1.5 py-0.5 font-condensed text-xs font-semibold text-primary-foreground">
            06:00–08:00
          </span>
        </div>
        <figcaption className="text-xs text-muted-foreground">{t.example}</figcaption>
      </figure>
    </section>
  )
}

/** City-not-found and invalid fields are shown inline next to the field they concern. */
function isShownInForm(error: Error) {
  return error instanceof ApiError && (error.status === 404 || (error.status === 400 && error.fields.length > 0))
}

function LoadingState() {
  const { t } = useI18n()
  return (
    <div className="flex flex-col gap-10">
      <p className="flex items-center gap-2 text-sm text-muted-foreground">
        <Spinner role="presentation" aria-label={undefined} aria-hidden />
        {t.loadingGemma}
      </p>
      <div className="flex flex-col gap-3">
        <Skeleton className="h-7 w-2/3" />
        <Skeleton className="h-24 w-4/5" />
        <Skeleton className="h-16 w-full" />
      </div>
      <Skeleton className="h-36 w-full rounded-xl" />
      <div className="flex flex-col gap-2">
        <Skeleton className="h-20 w-full" />
        <Skeleton className="h-20 w-full" />
      </div>
    </div>
  )
}
