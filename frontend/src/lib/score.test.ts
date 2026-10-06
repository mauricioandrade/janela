import { describe, expect, it } from "vitest"

import { scoreFill, scoreTier } from "@/lib/score"

describe("score tiers", () => {
  it.each([
    [100, "high"],
    [70, "high"],
    [69, "mid"],
    [40, "mid"],
    [39, "low"],
    [0, "low"],
  ] as const)("%i is %s", (score, tier) => {
    expect(scoreTier(score)).toBe(tier)
  })

  it("maps tiers to the score tokens", () => {
    expect(scoreFill(92)).toBe("bg-score-high")
    expect(scoreFill(10)).toBe("bg-score-low")
  })
})
