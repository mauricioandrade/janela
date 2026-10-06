import { CloudOffIcon, MapPinIcon, SearchXIcon, ServerCrashIcon, SunIcon } from "lucide-react"

import { DayStrip } from "@/components/day-strip"
import { Narrative } from "@/components/narrative"
import { WindowList } from "@/components/window-list"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Skeleton } from "@/components/ui/skeleton"
import { Spinner } from "@/components/ui/spinner"
import { ApiError, type Lang, type WindowsResponse } from "@/lib/api"
import type { Messages } from "@/lib/i18n"

type ResultsProps = {
  t: Messages
  lang: Lang
  data: WindowsResponse | undefined
  error: Error | null
  isFetching: boolean
  hasSearched: boolean
  onRetry: () => void
}

export function Results({ t, lang, data, error, isFetching, hasSearched, onRetry }: ResultsProps) {
  if (isFetching) return <LoadingState t={t} />

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

  if (!data || !hasSearched) {
    return (
      <Empty className="border">
        <EmptyHeader>
          <EmptyMedia>
            <StripPreview />
          </EmptyMedia>
          <EmptyTitle>{t.emptyTitle}</EmptyTitle>
          <EmptyDescription>{t.emptyDescription}</EmptyDescription>
        </EmptyHeader>
      </Empty>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <p className="flex items-center gap-1.5 text-sm text-muted-foreground">
        <MapPinIcon className="size-4" aria-hidden />
        {data.location.name}
      </p>
      <Narrative narrative={data.narrative} aiGenerated={data.aiGenerated} model={data.model} t={t} />
      {data.windows.length === 0 ? (
        <Empty className="border">
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <SearchXIcon />
            </EmptyMedia>
            <EmptyTitle>{t.noWindowsTitle}</EmptyTitle>
            <EmptyDescription>{t.noWindowsDescription}</EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : (
        <>
          <DayStrip windows={data.windows} lang={lang} label={t.dayStrip} />
          <WindowList windows={data.windows} lang={lang} t={t} />
        </>
      )}
    </div>
  )
}

/** A ghost of the day strip: hints at what a search returns before there is one. */
function StripPreview() {
  return (
    <div aria-hidden className="flex w-64 flex-col gap-2">
      <div className="relative h-9 overflow-hidden rounded-md bg-night">
        <span className="absolute inset-y-0 left-1/3 w-px bg-foreground/10" />
        <span className="absolute inset-y-0 left-2/3 w-px bg-foreground/10" />
        <div className="animate-window-open absolute inset-y-1.5 left-[12%] flex w-[16%] items-center justify-center rounded-sm bg-score-high text-primary-foreground">
          <SunIcon className="size-3.5" />
        </div>
        <div className="absolute inset-y-1.5 left-[70%] w-[12%] rounded-sm bg-score-mid/50" />
      </div>
      <div className="flex justify-between text-xs text-muted-foreground tabular-nums">
        <span>06h</span>
        <span>12h</span>
        <span>18h</span>
      </div>
    </div>
  )
}

/** City-not-found and invalid fields are shown inline next to the field they concern. */
function isShownInForm(error: Error) {
  return error instanceof ApiError && (error.status === 404 || (error.status === 400 && error.fields.length > 0))
}

function LoadingState({ t }: { t: Messages }) {
  return (
    <div className="flex flex-col gap-8">
      <p className="flex items-center gap-2 text-sm text-muted-foreground">
        <Spinner role="presentation" aria-label={undefined} aria-hidden />
        {t.loadingGemma}
      </p>
      <div className="flex flex-col gap-3 border-l-4 border-muted pl-5">
        <Skeleton className="h-6 w-full" />
        <Skeleton className="h-6 w-11/12" />
        <Skeleton className="h-6 w-2/3" />
      </div>
      <Skeleton className="h-24 w-full" />
      <Skeleton className="h-48 w-full" />
    </div>
  )
}
