export type Activity = "RUN" | "WALK" | "BIKE" | "PICNIC" | "GARDENING"
export type Lang = "pt" | "en"

export type WindowsParams = {
  city: string
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

export type WindowsResponse = {
  location: { name: string; latitude: number; longitude: number; timezone: string }
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
  const response = await fetch(`${API_URL}/api/windows?${query}`, { signal })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}))
    throw new ApiError(response.status, problem.detail ?? response.statusText, problem.fields ?? [])
  }
  return response.json()
}
