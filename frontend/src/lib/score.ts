export type ScoreTier = "high" | "mid" | "low"

export function scoreTier(score: number): ScoreTier {
  if (score >= 70) return "high"
  if (score >= 40) return "mid"
  return "low"
}

const TIER_FILL: Record<ScoreTier, string> = {
  high: "bg-score-high",
  mid: "bg-score-mid",
  low: "bg-score-low",
}

/** Background class for a score: the `score-*` tokens in index.css. */
export function scoreFill(score: number) {
  return TIER_FILL[scoreTier(score)]
}
