import {
  CloudRainIcon,
  SunIcon,
  ThermometerIcon,
  WindIcon,
  type LucideIcon,
} from "lucide-react"

import { ScoreBadge, ScoreMeter } from "@/components/score"
import { useI18n } from "@/hooks/use-i18n"
import type { OutdoorWindow } from "@/lib/api"
import { formatDay, formatTime } from "@/lib/format"

/** The runner-up windows, in rank order, quieter than the best one. */
export function Alternatives({ windows }: { windows: OutdoorWindow[] }) {
  const { t } = useI18n()
  return (
    <section
      aria-labelledby="alternatives-title"
      className="flex flex-col gap-3"
    >
      <h2
        id="alternatives-title"
        className="text-sm font-medium text-muted-foreground"
      >
        {t.alternatives}
      </h2>
      <ol className="grid gap-3 sm:grid-cols-2">
        {windows.map((window, index) => (
          <li key={window.start}>
            <Alternative window={window} rank={index + 2} />
          </li>
        ))}
      </ol>
    </section>
  )
}

function Alternative({
  window,
  rank,
}: {
  window: OutdoorWindow
  rank: number
}) {
  const { t, lang } = useI18n()
  const metrics: { icon: LucideIcon; label: string; value: string }[] = []
  if (window.apparentTempC !== null)
    metrics.push({
      icon: ThermometerIcon,
      label: t.feelsLike,
      value: `${Math.round(window.apparentTempC)}°C`,
    })
  if (window.maxUv !== null)
    metrics.push({
      icon: SunIcon,
      label: t.uv,
      value: `UV ${Math.round(window.maxUv)}`,
    })
  if (window.maxRainProbability !== null)
    metrics.push({
      icon: CloudRainIcon,
      label: t.rain,
      value: `${window.maxRainProbability}%`,
    })
  if (window.maxWindKmh !== null)
    metrics.push({
      icon: WindIcon,
      label: t.wind,
      value: `${Math.round(window.maxWindKmh)} km/h`,
    })

  return (
    <article className="flex h-full flex-col gap-3 rounded-xl border bg-card/70 p-4">
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs text-muted-foreground">{t.rank(rank)}</span>
        <ScoreBadge score={window.score} />
      </div>
      <h3 className="flex flex-col">
        <span className="font-heading text-sm font-semibold first-letter:uppercase">
          {formatDay(window.start, lang)}
        </span>
        <span className="text-2xl font-semibold tracking-tight">
          {formatTime(window.start)}–{formatTime(window.end)}
        </span>
      </h3>
      <ScoreMeter score={window.score} />
      <ul className="mt-auto flex flex-wrap gap-x-3 gap-y-1 text-sm text-muted-foreground">
        {metrics.map(({ icon: Icon, label, value }) => (
          <li key={label} className="flex items-center gap-1">
            <Icon className="size-3.5" aria-hidden />
            <span className="sr-only">{label}:</span>
            {value}
          </li>
        ))}
      </ul>
    </article>
  )
}
