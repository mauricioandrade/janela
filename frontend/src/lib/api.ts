export type Activity = "RUN" | "WALK" | "BIKE" | "PICNIC" | "GARDENING"
export type Lang = "pt" | "en"

/** A geocoded place; `admin1` is the state or province. Only `name` and coordinates are always present. */
export type Place = {
  id: number | null
  name: string
  admin1: string | null
  country: string | null
  countryCode: string | null
}

export type City = Place & { latitude: number; longitude: number }

export type WindowsParams = {
  city: string
  /** Pins the exact place picked from the suggestions; without it the city name's best match is used. */
  cityId?: number
  activity: Activity
  durationMinutes: number
  days: number
  lang: Lang
}

/** A window in the location's local time ("yyyy-MM-ddTHH:mm"); `end` is exclusive. */
export type OutdoorWindow = {
  start: string
  end: string
  score: number
  apparentTempC: number | null
  maxUv: number | null
  maxRainProbability: number | null
  maxWindKmh: number | null
}

/** A search as the form submits it; the language is added by the page. */
export type SearchValues = Omit<WindowsParams, "lang">

export type WindowsResponse = {
  location: City & { timezone: string }
  activity: Activity
  windows: OutdoorWindow[]
  narrative: string
  aiGenerated: boolean
  model: string | null
}

export class ApiError extends Error {
  readonly status: number
  readonly fields: string[]

  constructor(status: number, detail: string, fields: string[] = []) {
    super(detail)
    this.status = status
    this.fields = fields
  }
}

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080"

export async function fetchWindows(params: WindowsParams, signal?: AbortSignal): Promise<WindowsResponse> {
  const query = new URLSearchParams({
    city: params.city,
    activity: params.activity,
    durationMinutes: String(params.durationMinutes),
    days: String(params.days),
    lang: params.lang,
  })
  if (params.cityId) query.set("cityId", String(params.cityId))
  return getJson(`/api/v1/windows?${query}`, signal)
}

export async function fetchCities(q: string, lang: Lang, signal?: AbortSignal): Promise<City[]> {
  return getJson(`/api/v1/cities?${new URLSearchParams({ q, lang })}`, signal)
}

async function getJson<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, { signal })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}))
    throw new ApiError(response.status, problem.detail ?? response.statusText, problem.fields ?? [])
  }
  return response.json()
}
