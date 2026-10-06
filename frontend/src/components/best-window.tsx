import { CloudRainIcon, SunIcon, ThermometerIcon, WindIcon } from "lucide-react"

import { ScoreBadge, ScoreMeter } from "@/components/score"
import { StatTile } from "@/components/stat-tile"
import { useI18n } from "@/hooks/use-i18n"
import type { OutdoorWindow } from "@/lib/api"
import { formatDay, formatTime } from "@/lib/format"
import { uvLevel } from "@/lib/score"

/** The answer to "when should I go?": the best window, its time as the page's one big number, and why. */
export function BestWindow({ window }: { window: OutdoorWindow }) {
  const { t, lang } = useI18n()

  return (
    <section
      aria-labelledby="best-window-title"
      className="flex flex-col gap-5 rounded-2xl border bg-[color-mix(in_oklch,var(--sky)_22%,var(--card))] p-5 shadow-sm sm:p-6"
    >
      <div className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1">
        <h2
          id="best-window-title"
          className="text-sm font-medium text-muted-foreground"
        >
          {t.best}
        </h2>
        <ScoreBadge score={window.score} />
      </div>

      <div className="flex flex-col gap-1">
        <p className="font-heading text-lg font-semibold first-letter:uppercase sm:text-xl">
          {formatDay(window.start, lang)}
        </p>
        <p className="text-5xl font-semibold tracking-tighter sm:text-6xl">
          {formatTime(window.start)}
          <span className="mx-[0.12em] font-normal text-muted-foreground">–</span>
          {formatTime(window.end)}
        </p>
      </div>

      <ScoreMeter score={window.score} />

      <dl className="grid grid-cols-2 gap-2 2xl:grid-cols-4">
        {window.apparentTempC !== null && (
          <StatTile
            icon={ThermometerIcon}
            label={t.feelsLikeLong}
            value={`${Math.round(window.apparentTempC)}°C`}
          />
        )}
        {window.maxUv !== null && (
          <StatTile
            icon={SunIcon}
            label={t.uvLong}
            value={String(Math.round(window.maxUv))}
            note={t.uvLevels[uvLevel(window.maxUv)]}
          />
        )}
        {window.maxRainProbability !== null && (
          <StatTile
            icon={CloudRainIcon}
            label={t.rainLong}
            value={`${window.maxRainProbability}%`}
          />
        )}
        {window.maxWindKmh !== null && (
          <StatTile
            icon={WindIcon}
            label={t.windLong}
            value={`${Math.round(window.maxWindKmh)} km/h`}
          />
        )}
      </dl>
    </section>
  )
}
