export type Span = { left: number; right: number }

/**
 * Stacks labels that would overlap: each one takes the first row where it clears every label already placed
 * there by `gap`. Labels come in priority order, so the first one always keeps the top row.
 */
export function labelRows(spans: Span[], gap = 4): number[] {
  const placed: Span[][] = []
  return spans.map((span) => {
    let row = placed.findIndex((others) =>
      others.every(
        (other) => span.right + gap <= other.left || other.right + gap <= span.left
      )
    )
    if (row === -1) row = placed.push([]) - 1
    placed[row].push(span)
    return row
  })
}
