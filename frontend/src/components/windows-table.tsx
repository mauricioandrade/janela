import { cn } from "cn"

import { ScoreBadge } from "@/components/score"
import { RainWarning, UvWarning } from "@/components/warnings"
import { useI18n } from "@/hooks/use-i18n"
import type { OutdoorWindow } from "@/lib/api"
import { formatDayParts, formatTemperature, formatTime, formatWind } from "@/lib/format"

/** Every window in rank order, set like a timetable: one row per window, numbers in columns. */
export function WindowsTable({ windows }: { windows: OutdoorWindow[] }) {
  const { t, lang } = useI18n()
  const rows = windows.map((window, index) => ({
    window,
    rank: index + 1,
    day: formatDayParts(window.start, lang),
    time: `${formatTime(window.start)}–${formatTime(window.end)}`,
    feelsLike: window.apparentTempC !== null ? formatTemperature(window.apparentTempC, lang) : "—",
    uv: window.maxUv !== null ? String(Math.round(window.maxUv)) : "—",
    rain: window.maxRainProbability !== null ? `${window.maxRainProbability}%` : "—",
    wind: window.maxWindKmh !== null ? formatWind(window.maxWindKmh, lang) : "—",
  }))

  return (
    <section aria-labelledby="windows-heading" className="flex flex-col gap-3">
      <h2 id="windows-heading" className="text-lg font-semibold tracking-tight">
        {t.tableHeading}
      </h2>

      <table className="hidden w-full border-collapse text-sm sm:table">
        <thead>
          <tr className="border-b text-left text-xs text-muted-foreground [&>th]:pb-2 [&>th]:font-medium">
            <th scope="col" className="w-14">{t.columns.rank}</th>
            <th scope="col">{t.columns.day}</th>
            <th scope="col">{t.columns.time}</th>
            <th scope="col">{t.columns.score}</th>
            <th scope="col" className="text-right">{t.columns.feelsLike}</th>
            <th scope="col" className="text-right">{t.columns.uv}</th>
            <th scope="col" className="text-right">{t.columns.rain}</th>
            <th scope="col" className="text-right">{t.columns.wind}</th>
          </tr>
        </thead>
        <tbody className="font-condensed text-base tabular-nums">
          {rows.map((row) => (
            <tr
              key={row.window.start}
              className={cn("border-b [&>td]:py-2.5 [&>td]:align-baseline", row.rank === 1 && "bg-primary/6 font-semibold")}
            >
              <td className="pl-2">{row.rank}</td>
              <td>
                <span className="first-letter:uppercase">{row.day.weekday}</span>{" "}
                <span className="text-muted-foreground">{row.day.date}</span>
              </td>
              <td className="text-lg">{row.time}</td>
              <td className="font-normal">
                <ScoreBadge score={row.window.score} />
              </td>
              <td className="text-right">{row.feelsLike}</td>
              <td className="text-right">
                <span className="inline-flex items-baseline justify-end gap-1">
                  <UvWarning uv={row.window.maxUv} />
                  {row.uv}
                </span>
              </td>
              <td className="text-right">
                <span className="inline-flex items-baseline justify-end gap-1">
                  <RainWarning chance={row.window.maxRainProbability} />
                  {row.rain}
                </span>
              </td>
              <td className="pr-2 text-right">{row.wind}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <ol className="flex flex-col divide-y border-y sm:hidden">
        {rows.map((row) => (
          <li key={row.window.start} className={cn("flex flex-col gap-1 py-3", row.rank === 1 && "bg-primary/6 px-2")}>
            <div className="flex items-baseline justify-between gap-3">
              <p className="font-condensed text-xl font-semibold tabular-nums">
                <span className="mr-2 text-sm text-muted-foreground">{row.rank}</span>
                {row.time}
              </p>
              <ScoreBadge score={row.window.score} className="text-sm" />
            </div>
            <p className="text-sm">
              <span className="first-letter:uppercase">{row.day.weekday}</span>{" "}
              <span className="text-muted-foreground">{row.day.date}</span>
            </p>
            <p className="flex flex-wrap items-baseline gap-x-1 font-condensed text-sm text-muted-foreground tabular-nums">
              {t.columns.feelsLike} {row.feelsLike}, <UvWarning uv={row.window.maxUv} />
              {t.columns.uv} {row.uv}, <RainWarning chance={row.window.maxRainProbability} />
              {t.columns.rain} {row.rain}, {t.columns.wind} {row.wind}
            </p>
          </li>
        ))}
      </ol>
    </section>
  )
}
