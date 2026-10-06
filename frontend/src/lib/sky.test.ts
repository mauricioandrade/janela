import { describe, expect, it } from "vitest"

import { skyFor } from "@/lib/sky"

describe("skyFor", () => {
  it.each([
    ["2026-10-08T06:00", "dawn"],
    ["2026-10-08T08:00", "dawn"],
    ["2026-10-08T09:00", "day"],
    ["2026-10-08T15:00", "day"],
    ["2026-10-08T16:00", "dusk"],
  ])("%s starts at %s", (start, sky) => {
    expect(skyFor(start)).toBe(sky)
  })
})
