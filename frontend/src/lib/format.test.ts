import { describe, expect, it } from "vitest"

import { dayKey, formatDay, formatTemperature, formatTime, formatWind, minutesOfDay } from "@/lib/format"

describe("format", () => {
  it("formats the local day without shifting it to the browser's time zone", () => {
    expect(dayKey("2026-10-07T23:00")).toBe("2026-10-07")
    expect(formatDay("2026-10-07T23:00", "en")).toBe("Wednesday, Oct 7")
    expect(formatDay("2026-10-07T23:00", "pt", "short")).toMatch(/^qua\.?, 7 de out\.?$/)
  })

  it("reads times and minutes from the local timestamp", () => {
    expect(formatTime("2026-10-07T06:30")).toBe("06:30")
    expect(minutesOfDay("2026-10-07T06:30")).toBe(390)
  })

  it("shows Fahrenheit in English and Celsius in Portuguese", () => {
    expect(formatTemperature(23.2, "pt")).toBe("23°C")
    expect(formatTemperature(23.2, "en")).toBe("74°F")
    expect(formatTemperature(-40, "en")).toBe("-40°F")
  })

  it("shows mph in English and km/h in Portuguese", () => {
    expect(formatWind(16, "pt")).toBe("16 km/h")
    expect(formatWind(16, "en")).toBe("10 mph")
  })

  it("counts a window ending at midnight as 24:00", () => {
    expect(minutesOfDay("2026-10-08T00:00", true)).toBe(24 * 60)
    expect(minutesOfDay("2026-10-08T00:00")).toBe(0)
  })
})
