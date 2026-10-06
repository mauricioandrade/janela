import { CloudRainIcon, SunIcon, ThermometerIcon, WindIcon, type LucideIcon } from "lucide-react"
import { Progress } from "@base-ui/react/progress"
import { cn } from "cn"

import { Badge } from "@/components/ui/badge"
import { Card, CardContent } from "@/components/ui/card"
import { ProgressIndicator, ProgressTrack } from "@/components/ui/progress"
import { Separator } from "@/components/ui/separator"
import { useI18n } from "@/hooks/use-i18n"
import type { OutdoorWindow } from "@/lib/api"
import { formatDay, formatTime } from "@/lib/format"
import { scoreFill } from "@/lib/score"

type WindowListProps = {
  windows: OutdoorWindow[]
}

/** The ranked windows as an ordered list, best first: the accessible counterpart of the day strip. */
export function WindowList({ windows }: WindowListProps) {
  const { t } = useI18n()
  return (
    <Card>
      <h2 className="sr-only">{t.windowsHeading}</h2>
      <CardContent>
        <ol className="flex flex-col gap-5">
          {windows.map((window, index) => (
            <li key={window.start} className="flex flex-col gap-5">
              {index > 0 && <Separator />}
              <WindowRow window={window} rank={index + 1} />
            </li>
          ))}
        </ol>
      </CardContent>
    </Card>
  )
}

function WindowRow({ window, rank }: { window: OutdoorWindow; rank: number }) {
  const { t, lang } = useI18n()
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
            <ProgressIndicator className={scoreFill(window.score)} />
          </ProgressTrack>
          <span className="w-7 text-right text-sm font-semibold tabular-nums">{window.score}</span>
        </Progress.Root>
      </div>
    </article>
  )
}
