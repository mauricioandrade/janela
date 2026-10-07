import { useLayoutEffect, useRef, useState, type PointerEvent } from "react"
import { cn } from "cn"

import { useI18n } from "@/hooks/use-i18n"
import type { HourScore, OutdoorWindow } from "@/lib/api"
import {
  dayKey,
  formatDayParts,
  formatTime,
  hourOf,
  minutesOfDay,
} from "@/lib/format"
import { labelRows } from "@/lib/label-rows"

/** Below this hour score the scorer discards any window containing the hour. */
const WINDOW_FLOOR = 40
const TICK_EVERY_HOURS = 3
// SVG user space; stretched to the row's box, strokes stay crisp with non-scaling-stroke.
const W = 1000
const H = 100

type Point = { x: number; y: number }

type TideChartProps = {
  hours: HourScore[]
  windows: OutdoorWindow[]
}

/**
 * Each day read like a tide table: the comfort score of every daylight hour as a curve, the windows marked
 * like high tides with their times. The table below carries the same data for assistive tech.
 */
export function TideChart({ hours, windows }: TideChartProps) {
  const { t } = useI18n()
  if (hours.length === 0) return null

  const days = [...new Set(hours.map((hour) => dayKey(hour.time)))].sort()
  const first = Math.min(...hours.map((hour) => hourOf(hour.time)))
  const last = Math.max(...hours.map((hour) => hourOf(hour.time))) + 1
  const scale = { first, last }
  // Edge hours get their own labels; ticks too close to them would collide on a phone.
  const ticks = range(first + 2, last - 2).filter(
    (hour) => hour % TICK_EVERY_HOURS === 0
  )

  return (
    <section aria-labelledby="tide-heading" className="flex flex-col gap-4">
      <header className="flex flex-col gap-1">
        <h2 id="tide-heading" className="text-lg font-semibold tracking-tight">
          {t.tideHeading}
        </h2>
        <p className="max-w-[65ch] text-sm text-muted-foreground">
          {t.tideCaption}
        </p>
      </header>

      <div className="flex flex-col gap-2">
        {days.map((day, index) => (
          <DayTide
            key={day}
            day={day}
            hours={hours.filter((hour) => dayKey(hour.time) === day)}
            windows={windows}
            scale={scale}
            ticks={ticks}
            delayMs={index * 120}
          />
        ))}
        <div
          className="grid grid-cols-[3.75rem_1fr] gap-3 sm:grid-cols-[4.5rem_1fr]"
          aria-hidden
        >
          <span />
          <div className="relative h-4 text-xs text-muted-foreground font-condensed tabular-nums">
            <span className="absolute left-0">
              {String(first).padStart(2, "0")}h
            </span>
            {ticks.map((hour) => (
              <span
                key={hour}
                className="absolute -translate-x-1/2"
                style={{ left: `${xPercent(hour, scale)}%` }}
              >
                {String(hour).padStart(2, "0")}h
              </span>
            ))}
            <span className="absolute right-0">
              {String(last).padStart(2, "0")}h
            </span>
          </div>
        </div>
      </div>
    </section>
  )
}

type DayTideProps = {
  day: string
  hours: HourScore[]
  windows: OutdoorWindow[]
  scale: { first: number; last: number }
  ticks: number[]
  delayMs: number
}

function DayTide({ day, hours, windows, scale, ticks, delayMs }: DayTideProps) {
  const { t, lang } = useI18n()
  const [hovered, setHovered] = useState<HourScore | null>(null)
  const chartRef = useRef<HTMLDivElement>(null)
  const labelRefs = useRef<(HTMLSpanElement | null)[]>([])
  const [labelLayout, setLabelLayout] = useState({ rows: [] as number[], step: 0 })
  const parts = formatDayParts(day, lang)
  const dayWindows = windows
    .map((window, index) => ({ window, rank: index + 1 }))
    .filter(({ window }) => dayKey(window.start) === day)
  const windowsKey = dayWindows.map(({ window }) => window.start).join()

  // Labels of neighbouring windows (16:00–17:00 next to 17:00–18:00) are wider than their bands, so measure
  // them where they render and drop any that would cover another to a row below.
  useLayoutEffect(() => {
    const chart = chartRef.current
    if (!chart) return
    function place() {
      const labels = labelRefs.current.slice(0, windowsKey.split(",").length)
      const spans = labels.map((label) => {
        const box = label?.getBoundingClientRect()
        return { left: box?.left ?? 0, right: box?.right ?? 0 }
      })
      if (spans.every((span) => span.right === span.left)) return
      const rows = labelRows(spans)
      const step = (labels[0]?.offsetHeight ?? 0) + 2
      setLabelLayout((current) =>
        current.step === step && current.rows.join() === rows.join() ? current : { rows, step }
      )
    }
    place()
    const observer = new ResizeObserver(place)
    observer.observe(chart)
    return () => observer.disconnect()
  }, [windowsKey])

  const peak = hours.reduce(
    (best, hour) => (hour.score > best.score ? hour : best),
    hours[0]
  )

  const points: Point[] = hours.map((hour) => ({
    x: x(hourOf(hour.time) + 0.5, scale),
    y: y(hour.score),
  }))
  // Hold the first and last hour's level out to the edges of the day.
  const edged = [
    { x: x(hourOf(hours[0].time), scale), y: points[0].y },
    ...points,
    {
      x: x(hourOf(hours[hours.length - 1].time) + 1, scale),
      y: points[points.length - 1].y,
    },
  ]
  const line = smoothPath(edged)
  const area = `${line} L ${edged[edged.length - 1].x} ${H} L ${edged[0].x} ${H} Z`

  function handlePointer(event: PointerEvent<HTMLDivElement>) {
    const box = event.currentTarget.getBoundingClientRect()
    const hourAt =
      scale.first +
      ((event.clientX - box.left) / box.width) * (scale.last - scale.first)
    const nearest = hours.reduce((best, hour) =>
      Math.abs(hourOf(hour.time) + 0.5 - hourAt) <
      Math.abs(hourOf(best.time) + 0.5 - hourAt)
        ? hour
        : best
    )
    setHovered(nearest)
  }

  return (
    <div className="grid grid-cols-[3.75rem_1fr] items-stretch gap-3 sm:grid-cols-[4.5rem_1fr]">
      <p className="flex flex-col justify-center leading-tight">
        <span className="font-semibold first-letter:uppercase">
          {parts.weekday}
        </span>
        <span className="text-sm text-muted-foreground font-condensed">
          {parts.date}
        </span>
      </p>
      <div
        role="img"
        aria-label={t.tideDaySummary(
          `${parts.weekday} ${parts.date}`,
          peak.score,
          formatTime(peak.time)
        )}
        ref={chartRef}
        className="relative h-24 touch-pan-y rounded-md border bg-card sm:h-28"
        onPointerMove={handlePointer}
        onPointerDown={handlePointer}
        onPointerLeave={() => setHovered(null)}
      >
        <svg
          viewBox={`0 0 ${W} ${H}`}
          preserveAspectRatio="none"
          className="absolute inset-0 size-full overflow-visible"
          aria-hidden
        >
          {ticks.map((hour) => (
            <line
              key={hour}
              x1={x(hour, scale)}
              x2={x(hour, scale)}
              y1={0}
              y2={H}
              className="stroke-border"
              vectorEffect="non-scaling-stroke"
            />
          ))}
          {dayWindows.map(({ window, rank }) => (
            <rect
              key={window.start}
              x={x(minutesOfDay(window.start) / 60, scale)}
              width={
                x(minutesOfDay(window.end, true) / 60, scale) -
                x(minutesOfDay(window.start) / 60, scale)
              }
              y={0}
              height={H}
              // Intensity lives in fill-opacity: the entrance animation drives opacity and would override it.
              fillOpacity={rank === 1 ? 0.25 : 0.12}
              className="animate-tide-fill fill-primary"
              style={{ animationDelay: `${delayMs + 300 + rank * 80}ms` }}
            />
          ))}
          <line
            x1={0}
            x2={W}
            y1={y(WINDOW_FLOOR)}
            y2={y(WINDOW_FLOOR)}
            className="stroke-muted-foreground/50"
            strokeDasharray="4 4"
            vectorEffect="non-scaling-stroke"
          />
          <path d={area} className="fill-tide/15" />
          <path
            d={line}
            pathLength={1}
            fill="none"
            className="animate-tide-draw stroke-tide"
            strokeWidth={2}
            strokeLinejoin="round"
            vectorEffect="non-scaling-stroke"
            style={{ animationDelay: `${delayMs}ms` }}
          />
        </svg>

        {dayWindows.map(({ window, rank }, index) => {
          const row = labelLayout.rows[index] ?? 0
          const middle = xPercent(
            (minutesOfDay(window.start) + minutesOfDay(window.end, true)) / 120,
            scale
          )
          // Near an edge the label hugs the inside instead of spilling out of the chart.
          const anchor = middle < 12 ? "start" : middle > 88 ? "end" : "middle"
          return (
            <span
              key={window.start}
              ref={(label) => {
                labelRefs.current[index] = label
              }}
              aria-hidden
              className={cn(
                "absolute top-1.5 flex items-center gap-1 rounded px-1.5 py-0.5 text-xs font-semibold whitespace-nowrap font-condensed tabular-nums",
                anchor === "middle" && "-translate-x-1/2",
                rank === 1
                  ? "bg-primary text-primary-foreground"
                  : "bg-card/90 text-foreground ring-1 ring-border"
              )}
              style={{
                ...(row > 0 && { top: `calc(0.375rem + ${row * labelLayout.step}px)` }),
                ...(anchor === "start"
                  ? {
                      left: `${xPercent(minutesOfDay(window.start) / 60, scale)}%`,
                      marginLeft: 4,
                    }
                  : anchor === "end"
                    ? {
                        right: `${100 - xPercent(minutesOfDay(window.end, true) / 60, scale)}%`,
                        marginRight: 4,
                      }
                    : { left: `${middle}%` }),
              }}
            >
              <span className="opacity-70">{rank}</span>
              {formatTime(window.start)}–{formatTime(window.end)}
            </span>
          )
        })}

        {hovered && (
          <span
            aria-hidden
            className="pointer-events-none absolute inset-y-0"
            style={{ left: `${xPercent(hourOf(hovered.time) + 0.5, scale)}%` }}
          >
            <span className="absolute inset-y-0 w-px bg-foreground/30" />
            <span
              className="absolute size-2.5 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-card bg-tide"
              style={{ top: `${y(hovered.score)}%` }}
            />
            <span className="absolute bottom-1 left-2 rounded bg-foreground px-1.5 py-0.5 text-xs font-medium whitespace-nowrap text-background font-condensed tabular-nums">
              {t.tideHour(formatTime(hovered.time), hovered.score)}
            </span>
          </span>
        )}
      </div>
    </div>
  )
}

function x(hour: number, { first, last }: { first: number; last: number }) {
  return ((hour - first) / (last - first)) * W
}

function xPercent(hour: number, scale: { first: number; last: number }) {
  return (x(hour, scale) / W) * 100
}

/** Score 0–100 to the SVG's y, with just enough headroom for the window labels. */
function y(score: number) {
  return H - 3 - (score / 100) * (H - 24)
}

function range(from: number, to: number) {
  return Array.from({ length: Math.max(0, to - from + 1) }, (_, i) => from + i)
}

/** Catmull–Rom through the points, as cubic Béziers: a smooth tide that still passes through every hour. */
function smoothPath(points: Point[]) {
  if (points.length === 1) return `M ${points[0].x} ${points[0].y}`
  let d = `M ${points[0].x} ${points[0].y}`
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[i - 1] ?? points[i]
    const p1 = points[i]
    const p2 = points[i + 1]
    const p3 = points[i + 2] ?? p2
    const c1 = { x: p1.x + (p2.x - p0.x) / 6, y: p1.y + (p2.y - p0.y) / 6 }
    const c2 = { x: p2.x - (p3.x - p1.x) / 6, y: p2.y - (p3.y - p1.y) / 6 }
    d += ` C ${c1.x} ${c1.y}, ${c2.x} ${c2.y}, ${p2.x} ${p2.y}`
  }
  return d
}
