import { cn } from "cn"

import { useI18n } from "@/hooks/use-i18n"
import type { OutdoorWindow } from "@/lib/api"
import { dayKey, formatDay, formatTime, minutesOfDay } from "@/lib/format"
import { scoreFill } from "@/lib/score"

/** Daylight hours shown by default; the range widens if a window falls outside it. */
const DEFAULT_FIRST_HOUR = 5
const DEFAULT_LAST_HOUR = 21
const TICK_EVERY_HOURS = 3

type DayStripProps = {
  windows: OutdoorWindow[]
}

/**
 * Each day as a band of daylight hours, with the ranked windows cut out of it.
 * Purely visual: the window list below carries the same data for assistive tech.
 */
export function DayStrip({ windows }: DayStripProps) {
  const { t, lang } = useI18n()
  const days = [
    ...new Set(windows.map((window) => dayKey(window.start))),
  ].sort()
  const firstHour = Math.min(
    DEFAULT_FIRST_HOUR,
    ...windows.map((window) => Math.floor(minutesOfDay(window.start) / 60))
  )
  const lastHour = Math.max(
    DEFAULT_LAST_HOUR,
    ...windows.map((window) => Math.ceil(minutesOfDay(window.end, true) / 60))
  )
  const range = { from: firstHour * 60, span: (lastHour - firstHour) * 60 }
  // Edge hours are left unlabeled so their labels don't spill past the band.
  const ticks = Array.from(
    { length: lastHour - firstHour - 1 },
    (_, i) => firstHour + 1 + i
  ).filter((hour) => hour % TICK_EVERY_HOURS === 0)
  const position = (minutes: number) =>
    ((minutes - range.from) / range.span) * 100

  return (
    <figure className="flex flex-col gap-3" aria-hidden>
      <figcaption className="text-sm text-muted-foreground">
        {t.dayStrip}
      </figcaption>
      <div className="grid grid-cols-[auto_1fr] items-center gap-x-4 gap-y-3">
        {days.map((day) => (
          <DayRow
            key={day}
            label={formatDay(day, lang, "short")}
            windows={windows.filter((window) => dayKey(window.start) === day)}
            ranks={windows}
            ticks={ticks}
            position={position}
          />
        ))}
        <div />
        <div
          className="relative h-4 text-xs text-muted-foreground tabular-nums"
          aria-hidden
        >
          {ticks.map((hour) => (
            <span
              key={hour}
              className="absolute -translate-x-1/2"
              style={{ left: `${position(hour * 60)}%` }}
            >
              {String(hour).padStart(2, "0")}h
            </span>
          ))}
        </div>
      </div>
    </figure>
  )
}

type DayRowProps = {
  label: string
  windows: OutdoorWindow[]
  ranks: OutdoorWindow[]
  ticks: number[]
  position: (minutes: number) => number
}

function DayRow({ label, windows, ranks, ticks, position }: DayRowProps) {
  return (
    <>
      <span className="text-sm font-medium first-letter:uppercase">
        {label}
      </span>
      <div className="relative h-10 overflow-hidden rounded-md bg-linear-to-r from-night via-secondary to-night">
        {ticks.map((hour) => (
          <span
            key={hour}
            aria-hidden
            className="absolute inset-y-0 w-px bg-foreground/10"
            style={{ left: `${position(hour * 60)}%` }}
          />
        ))}
        {windows.map((window) => {
          const rank = ranks.indexOf(window) + 1
          const start = position(minutesOfDay(window.start))
          const end = position(minutesOfDay(window.end, true))
          return (
            <div
              key={window.start}
              title={`${formatTime(window.start)}–${formatTime(window.end)}`}
              className={cn(
                "absolute inset-y-1.5 flex min-w-5 animate-window-open items-center justify-center rounded-sm text-xs font-semibold text-primary-foreground",
                scoreFill(window.score)
              )}
              style={{
                left: `calc(${start}% + 1px)`,
                width: `calc(${end - start}% - 2px)`,
                animationDelay: `${(rank - 1) * 120}ms`,
              }}
            >
              {rank}
            </div>
          )
        })}
      </div>
    </>
  )
}
