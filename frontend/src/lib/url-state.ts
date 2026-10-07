import type { Activity, Lang, SearchValues, WindowsParams } from "@/lib/api"

const ACTIVITY_VALUES: Activity[] = ["RUN", "WALK", "BIKE", "PICNIC", "GARDENING", "WORKOUT"]

/** Reads a shareable search from the URL (`?city=Itobi&cityId=3460543&activity=RUN&duration=60&days=2&lang=pt`). */
export function readSearchFromUrl(queryString = window.location.search): Partial<WindowsParams> {
  const query = new URLSearchParams(queryString)
  const search: Partial<WindowsParams> = {}
  const city = query.get("city")?.trim()
  if (city) search.city = city
  const cityId = Number(query.get("cityId"))
  if (Number.isInteger(cityId) && cityId > 0) search.cityId = cityId
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

/** The search a link describes, when it describes a complete one. */
export function completeSearch(fromUrl: Partial<WindowsParams>): SearchValues | null {
  const { city, cityId, activity, durationMinutes, days } = fromUrl
  return city && activity && durationMinutes && days ? { city, cityId, activity, durationMinutes, days } : null
}

export function writeSearchToUrl(params: WindowsParams) {
  const query = new URLSearchParams({
    city: params.city,
    activity: params.activity,
    duration: String(params.durationMinutes),
    days: String(params.days),
    lang: params.lang,
  })
  if (params.cityId) query.set("cityId", String(params.cityId))
  window.history.replaceState(null, "", `?${query}`)
}

export function writeLangToUrl(lang: Lang) {
  const query = new URLSearchParams(window.location.search)
  query.set("lang", lang)
  window.history.replaceState(null, "", `?${query}`)
}
