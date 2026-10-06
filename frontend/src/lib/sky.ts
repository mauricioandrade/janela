/** The part of the day a window starts in; it tints the page's sky. */
export type Sky = "dawn" | "day" | "dusk"

export function skyFor(localDateTime: string): Sky {
  const hour = Number(localDateTime.slice(11, 13))
  if (hour < 9) return "dawn"
  if (hour < 16) return "day"
  return "dusk"
}
