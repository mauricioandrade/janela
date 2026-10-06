import { describe, expect, it } from "vitest"

import { scoreFill, scoreTier, scoreTrack, uvLevel } from "@/lib/score"

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

describe("meters and UV", () => {
  it("draws the track a lighter step of the fill's hue", () => {
    expect(scoreTrack(92)).toBe("bg-score-high/20")
  })

  it.each([
    [0.4, "low"],
    [2.4, "low"],
    [3, "moderate"],
    [6, "high"],
    [8, "veryHigh"],
    [11, "extreme"],
  ] as const)("UV %s is %s (WHO)", (uv, level) => {
    expect(uvLevel(uv)).toBe(level)
  })
})
