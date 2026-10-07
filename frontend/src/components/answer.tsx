import { CalculatorIcon } from "lucide-react"

import { PlaceFlags } from "@/components/place-flags"
import { ScoreBadge } from "@/components/score"
import { RainWarning, UvWarning } from "@/components/warnings"
import { useI18n } from "@/hooks/use-i18n"
import type { Activity, City, OutdoorWindow } from "@/lib/api"
import { formatDay, formatTemperature, formatTime } from "@/lib/format"
import { placeLabel } from "@/lib/place"
import { uvLevel } from "@/lib/score"

type AnswerProps = {
  window: OutdoorWindow
  activity: Activity
  place: City
}

/** When to go, as one heading: the activity and day, then the time at the size of a tide-table high water. */
export function Answer({ window, activity, place }: AnswerProps) {
  const { t, lang } = useI18n()
  const readings = [
    { label: t.feelsLikeLong, value: window.apparentTempC !== null ? formatTemperature(window.apparentTempC, lang) : null },
    {
      label: t.uvLong,
      value: window.maxUv !== null ? String(Math.round(window.maxUv)) : null,
      note: window.maxUv !== null ? t.uvLevels[uvLevel(window.maxUv)] : undefined,
      warning: <UvWarning uv={window.maxUv} />,
    },
    {
      label: t.rainLong,
      value: window.maxRainProbability !== null ? `${window.maxRainProbability}%` : null,
      warning: <RainWarning chance={window.maxRainProbability} />,
    },
    { label: t.windLong, value: window.maxWindKmh !== null ? `${Math.round(window.maxWindKmh)} km/h` : null },
  ].filter((reading) => reading.value !== null)

  return (
    <section aria-labelledby="answer-heading" className="flex flex-col gap-5">
      <h2 id="answer-heading" className="flex flex-col gap-1 text-balance">
        <span className="text-xl font-semibold tracking-tight first-letter:uppercase sm:text-2xl">
          {t.answerLead(activity, formatDay(window.start, lang))},{" "}
          <span className="font-normal text-muted-foreground">
            {t.inPlace(placeLabel(place))}
            <PlaceFlags place={place} className="ml-2 align-[-0.1em]" />
          </span>
        </span>
        <span className="font-condensed text-[clamp(4rem,14vw,7.5rem)] leading-[0.9] font-bold tracking-[-0.02em]">
          {formatTime(window.start)}
          <span className="mx-[0.06em] font-light text-muted-foreground">–</span>
          {formatTime(window.end)}
        </span>
      </h2>

      <dl className="grid grid-cols-2 border-y sm:grid-cols-5 sm:divide-x [&>div]:py-3 sm:[&>div]:px-4 sm:[&>div:first-child]:pl-0">
        <div className="col-span-2 flex flex-col gap-1 border-b sm:col-span-1 sm:border-b-0">
          <dt className="text-xs text-muted-foreground">{t.score}</dt>
          <dd className="text-lg">
            <ScoreBadge score={window.score} />
          </dd>
        </div>
        {readings.map(({ label, value, note, warning }, index) => (
          <div key={label} className={index % 2 === 1 ? "border-l pl-4 sm:pl-4" : "sm:pl-4"}>
            <dt className="text-xs text-muted-foreground">{label}</dt>
            <dd className="flex items-baseline gap-1.5">
              <span className="font-condensed text-2xl font-semibold">{value}</span>
              {note && <span className="text-sm text-muted-foreground">{note}</span>}
              {warning}
            </dd>
          </div>
        ))}
      </dl>
      <p className="flex items-start gap-2 text-xs text-muted-foreground">
        <CalculatorIcon className="mt-px size-3.5 shrink-0" aria-hidden />
        {t.computedBy}
      </p>
    </section>
  )
}
