import type { Lang } from "@/lib/api"

const LOCALES: Record<Lang, string> = { pt: "pt-BR", en: "en-US" }

/** "2026-10-07T05:00" → its calendar date, without shifting it to the browser's timezone. */
export function dayKey(localDateTime: string) {
  return localDateTime.slice(0, 10)
}

export function formatDay(localDateTime: string, lang: Lang, weekday: "long" | "short" = "long") {
  const [year, month, day] = dayKey(localDateTime).split("-").map(Number)
  return new Intl.DateTimeFormat(LOCALES[lang], {
    weekday,
    day: "numeric",
    month: "short",
    timeZone: "UTC",
  }).format(new Date(Date.UTC(year, month - 1, day)))
}

export function formatTime(localDateTime: string) {
  return localDateTime.slice(11, 16)
}

/** Minutes since midnight; an `end` on the next day's 00:00 counts as 24:00. */
export function minutesOfDay(localDateTime: string, isEnd = false) {
  const [hours, minutes] = formatTime(localDateTime).split(":").map(Number)
  const total = hours * 60 + minutes
  return isEnd && total === 0 ? 24 * 60 : total
}

/** "qui." and "8 de out." (or "Thu" and "Oct 8"), for compact day labels. */
export function formatDayParts(localDateTime: string, lang: Lang) {
  const [year, month, day] = dayKey(localDateTime).split("-").map(Number)
  const date = new Date(Date.UTC(year, month - 1, day))
  const locale = LOCALES[lang]
  return {
    weekday: new Intl.DateTimeFormat(locale, { weekday: "short", timeZone: "UTC" }).format(date),
    date: new Intl.DateTimeFormat(locale, { day: "numeric", month: "short", timeZone: "UTC" }).format(date),
  }
}

/** The hour of a local "yyyy-MM-ddTHH:mm" timestamp, as a number. */
export function hourOf(localDateTime: string) {
  return Number(localDateTime.slice(11, 13))
}

/** Feels-like temperature for the reader: Fahrenheit in English, Celsius in Portuguese (the API sends Celsius). */
export function formatTemperature(celsius: number, lang: Lang) {
  return lang === "en" ? `${Math.round((celsius * 9) / 5 + 32)}°F` : `${Math.round(celsius)}°C`
}
