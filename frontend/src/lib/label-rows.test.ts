import { describe, expect, it } from "vitest"

import { labelRows } from "@/lib/label-rows"

describe("labelRows", () => {
  it("keeps labels that fit side by side on the top row", () => {
    expect(labelRows([{ left: 0, right: 100 }, { left: 200, right: 300 }])).toEqual([0, 0])
  })

  it("drops a label that would cover another to the next row", () => {
    // Adjacent one-hour windows: 16:00–17:00 and 17:00–18:00, labels wider than the bands.
    expect(labelRows([{ left: 500, right: 610 }, { left: 450, right: 560 }])).toEqual([0, 1])
  })

  it("treats labels closer than the gap as overlapping", () => {
    expect(labelRows([{ left: 0, right: 100 }, { left: 102, right: 200 }])).toEqual([0, 1])
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
