import { Fragment } from "react"
import { CloudRainIcon, SunIcon, ThermometerIcon, WindIcon, type LucideIcon } from "lucide-react"
import { cn } from "cn"

import { Badge } from "@/components/ui/badge"
import { Card, CardContent } from "@/components/ui/card"
import { ProgressIndicator, ProgressTrack } from "@/components/ui/progress"
import { Progress } from "@base-ui/react/progress"
import { Separator } from "@/components/ui/separator"
import type { Lang, OutdoorWindow } from "@/lib/api"
import { formatDay, formatTime, scoreTier } from "@/lib/format"
import type { Messages } from "@/lib/i18n"

const TIER_FILL = {
  high: "bg-score-high",
  mid: "bg-score-mid",
  low: "bg-score-low",
} as const

type WindowListProps = {
  windows: OutdoorWindow[]
  lang: Lang
  t: Messages
}

export function WindowList({ windows, lang, t }: WindowListProps) {
  return (
    <Card>
      <h2 className="sr-only">{t.windowsHeading}</h2>
      <CardContent className="flex flex-col gap-5">
        {windows.map((window, index) => (
          <Fragment key={window.start}>
            {index > 0 && <Separator />}
            <WindowRow window={window} rank={index + 1} lang={lang} t={t} />
          </Fragment>
        ))}
      </CardContent>
    </Card>
  )
}

function WindowRow({ window, rank, lang, t }: { window: OutdoorWindow; rank: number; lang: Lang; t: Messages }) {
  const metrics: { icon: LucideIcon; label: string; value: string }[] = []
  if (window.apparentTempC !== null)
    metrics.push({ icon: ThermometerIcon, label: t.feelsLike, value: `${Math.round(window.apparentTempC)}°C` })
  if (window.maxUv !== null) metrics.push({ icon: SunIcon, label: t.uv, value: `UV ${Math.round(window.maxUv)}` })
  if (window.maxRainProbability !== null)
    metrics.push({ icon: CloudRainIcon, label: t.rain, value: `${window.maxRainProbability}%` })
  if (window.maxWindKmh !== null)
    metrics.push({ icon: WindIcon, label: t.wind, value: `${Math.round(window.maxWindKmh)} km/h` })

  return (
    <article className="flex flex-col gap-3">
      <div className="flex flex-col gap-0.5">
        <span className="text-sm text-muted-foreground">{rank === 1 ? t.best : t.rank(rank)}</span>
        <h3 className={cn("font-heading font-semibold tracking-tight", rank === 1 ? "text-3xl" : "text-xl")}>
          <span className="inline-block first-letter:uppercase">{formatDay(window.start, lang)}</span>,{" "}
          <span className="tabular-nums">
            {formatTime(window.start)}–{formatTime(window.end)}
          </span>
        </h3>
      </div>
      <div className="flex flex-wrap items-center justify-between gap-x-6 gap-y-3">
        <ul className="flex flex-wrap gap-2">
          {metrics.map(({ icon: Icon, label, value }) => (
            <li key={label}>
              <Badge variant="secondary">
                <Icon data-icon="inline-start" aria-hidden />
                <span className="sr-only">{label}:</span>
                {value}
              </Badge>
            </li>
          ))}
        </ul>
        <Progress.Root value={window.score} className="flex w-40 items-center gap-3" aria-label={t.score}>
          <ProgressTrack className="h-2">
            <ProgressIndicator className={TIER_FILL[scoreTier(window.score)]} />
          </ProgressTrack>
          <span className="w-7 text-right text-sm font-semibold tabular-nums">{window.score}</span>
        </Progress.Root>
      </div>
    </article>
  )
}
