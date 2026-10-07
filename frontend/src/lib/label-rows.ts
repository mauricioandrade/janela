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
        (other) =>
          span.right + gap <= other.left || other.right + gap <= span.left
      )
    )
    if (row === -1) row = placed.push([]) - 1
    placed[row].push(span)
    return row
  })
}

/**
 * Lays out labels of the given widths over a chart `chartWidth` wide: each one centred on its band's
 * `center`, pushed back inside when it would cross an edge (keeping `inset` clear), then stacked by
 * `labelRows`. All values are pixels; labels come in priority order.
 */
export function placeLabels(
  centers: number[],
  widths: number[],
  chartWidth: number,
  inset = 4
): { left: number; row: number }[] {
  const lefts = centers.map((center, i) => {
    const max = Math.max(inset, chartWidth - widths[i] - inset)
    return Math.min(Math.max(center - widths[i] / 2, inset), max)
  })
  const rows = labelRows(
    lefts.map((left, i) => ({ left, right: left + widths[i] }))
  )
  return lefts.map((left, i) => ({ left, row: rows[i] }))
}
