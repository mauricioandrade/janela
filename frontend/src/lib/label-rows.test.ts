import { describe, expect, it } from "vitest"

import { labelRows, placeLabels } from "@/lib/label-rows"

describe("labelRows", () => {
  it("keeps labels that fit side by side on the top row", () => {
    expect(
      labelRows([
        { left: 0, right: 100 },
        { left: 200, right: 300 },
      ])
    ).toEqual([0, 0])
  })

  it("drops a label that would cover another to the next row", () => {
    // Adjacent one-hour windows: 16:00–17:00 and 17:00–18:00, labels wider than the bands.
    expect(
      labelRows([
        { left: 500, right: 610 },
        { left: 450, right: 560 },
      ])
    ).toEqual([0, 1])
  })

  it("treats labels closer than the gap as overlapping", () => {
    expect(
      labelRows([
        { left: 0, right: 100 },
        { left: 102, right: 200 },
      ])
    ).toEqual([0, 1])
  })

  it("reuses the top row when a later label clears it", () => {
    expect(
      labelRows([
        { left: 0, right: 100 },
        { left: 50, right: 150 },
        { left: 300, right: 400 },
      ])
    ).toEqual([0, 1, 0])
  })
})

describe("placeLabels", () => {
  it("centres a label on its band when it fits", () => {
    expect(placeLabels([500], [100], 1000)).toEqual([{ left: 450, row: 0 }])
  })

  it("keeps labels at the edges inside the chart", () => {
    // Bands at the first and last hour of a 390 px phone chart.
    expect(placeLabels([15, 375], [110, 110], 390)).toEqual([
      { left: 4, row: 0 },
      { left: 276, row: 0 },
    ])
  })

  it("stacks neighbours after clamping them", () => {
    // 17:00–18:00 and 16:00–17:00 at the end of a narrow chart: both pushed in, then on two rows.
    expect(placeLabels([360, 330], [110, 110], 390)).toEqual([
      { left: 276, row: 0 },
      { left: 275, row: 1 },
    ])
  })

  it("pins a label wider than the chart to the left inset", () => {
    expect(placeLabels([100], [300], 200)).toEqual([{ left: 4, row: 0 }])
  })
})
