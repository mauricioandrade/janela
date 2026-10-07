import { describe, expect, it } from "vitest"

import { completeSearch, readSearchFromUrl } from "@/lib/url-state"

describe("readSearchFromUrl", () => {
  it("reads a full shared search", () => {
    expect(readSearchFromUrl("?city=Itobi&cityId=3460543&activity=bike&duration=120&days=3&lang=en")).toEqual({
      city: "Itobi",
      cityId: 3460543,
      activity: "BIKE",
      durationMinutes: 120,
      days: 3,
      lang: "en",
    })
  })

  it("accepts the outdoor workout", () => {
    expect(readSearchFromUrl("?activity=workout").activity).toBe("WORKOUT")
  })

  it("drops values it can't trust", () => {
    expect(readSearchFromUrl("?city=%20&cityId=-4&activity=SWIM&duration=abc&days=9&lang=fr")).toEqual({})
  })
})

describe("completeSearch", () => {
  it("needs city, activity, duration and days", () => {
    expect(completeSearch({ city: "Recife", activity: "RUN", durationMinutes: 60 })).toBeNull()
    expect(completeSearch({ city: "Recife", activity: "RUN", durationMinutes: 60, days: 2, lang: "pt" })).toEqual({
      city: "Recife",
      cityId: undefined,
      activity: "RUN",
      durationMinutes: 60,
      days: 2,
    })
  })
})
