import type { Activity, Lang, WindowsParams } from "@/lib/api"

const ACTIVITY_VALUES: Activity[] = ["RUN", "WALK", "BIKE", "PICNIC", "GARDENING"]

/** Reads a shareable search from the URL (`?city=Recife&activity=RUN&duration=60&days=2&lang=pt`). */
export function readSearchFromUrl(): Partial<WindowsParams> {
  const query = new URLSearchParams(window.location.search)
  const search: Partial<WindowsParams> = {}
  const city = query.get("city")?.trim()
  if (city) search.city = city
  const activity = query.get("activity")?.toUpperCase() as Activity | undefined
  if (activity && ACTIVITY_VALUES.includes(activity)) search.activity = activity
  const duration = Number(query.get("duration"))
  if (Number.isInteger(duration) && duration > 0) search.durationMinutes = duration
  const days = Number(query.get("days"))
  if ([1, 2, 3].includes(days)) search.days = days
  const lang = query.get("lang")
  if (lang === "pt" || lang === "en") search.lang = lang
  return search
}

export function writeSearchToUrl(params: WindowsParams) {
  const query = new URLSearchParams({
    city: params.city,
    activity: params.activity,
    duration: String(params.durationMinutes),
    days: String(params.days),
    lang: params.lang,
  })
  window.history.replaceState(null, "", `?${query}`)
}

export function writeLangToUrl(lang: Lang) {
  const query = new URLSearchParams(window.location.search)
  query.set("lang", lang)
  window.history.replaceState(null, "", `?${query}`)
}
