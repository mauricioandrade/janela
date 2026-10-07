export type ScoreTier = "high" | "mid" | "low"

export function scoreTier(score: number): ScoreTier {
  if (score >= 70) return "high"
  if (score >= 40) return "mid"
  return "low"
}

const TIER_TEXT: Record<ScoreTier, string> = {
  high: "text-score-high",
  mid: "text-score-mid",
  low: "text-score-low",
}

/** Colour for a tier icon; text itself stays in text tokens. */
export function scoreIconColor(score: number) {
  return TIER_TEXT[scoreTier(score)]
}

export type UvLevel = "low" | "moderate" | "high" | "veryHigh" | "extreme"

/** WHO UV index categories, as the backend gives them to the model. */
export function uvLevel(uv: number): UvLevel {
  const rounded = Math.round(uv)
  if (rounded <= 2) return "low"
  if (rounded <= 5) return "moderate"
  if (rounded <= 7) return "high"
  if (rounded <= 10) return "veryHigh"
  return "extreme"
}

/** UV worth a warning: WHO "high" and above. */
export function isHighUv(uv: number | null) {
  return uv !== null && ["high", "veryHigh", "extreme"].includes(uvLevel(uv))
}

/** Rain chance worth a warning in the tropics, where afternoon storms are the norm. */
export function isLikelyRain(chance: number | null) {
  return chance !== null && chance >= 40
}
