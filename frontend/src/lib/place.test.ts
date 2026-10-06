import { describe, expect, it } from "vitest"

import { brazilianState, placeLabel, stateFlagUrl } from "@/lib/place"

describe("brazilianState", () => {
  it.each([
    ["São Paulo", "SP"],
    ["Sao Paulo", "SP"],
    ["Estado de Bahia", "BA"],
    ["Federal District", "DF"],
    ["Rio Grande do Sul", "RS"],
  ])("maps %s to %s", (admin1, code) => {
    expect(brazilianState({ admin1, countryCode: "BR" })).toBe(code)
  })

  it("ignores places outside Brazil and unknown states", () => {
    expect(brazilianState({ admin1: "São Paulo", countryCode: "PT" })).toBeNull()
    expect(brazilianState({ admin1: "Atlantis", countryCode: "BR" })).toBeNull()
    expect(brazilianState({ admin1: null, countryCode: "BR" })).toBeNull()
  })
})

describe("placeLabel", () => {
  it("uses the state code in Brazil", () => {
    expect(placeLabel({ name: "Itobi", admin1: "São Paulo", countryCode: "BR" })).toBe("Itobi – SP")
  })

  it("uses the full region elsewhere", () => {
    expect(placeLabel({ name: "Springfield", admin1: "Missouri", countryCode: "US" })).toBe("Springfield – Missouri")
  })

  it("is just the name without a region, or when the region repeats it", () => {
    expect(placeLabel({ name: "Recife", admin1: null, countryCode: "BR" })).toBe("Recife")
    expect(placeLabel({ name: "Lisboa", admin1: "Lisboa", countryCode: "PT" })).toBe("Lisboa")
  })
})

describe("stateFlagUrl", () => {
  it("points at the bundled Brazilian state flag", () => {
    expect(stateFlagUrl({ admin1: "Pernambuco", countryCode: "BR" })).toBe("/flags/br/pe.svg")
    expect(stateFlagUrl({ admin1: "Missouri", countryCode: "US" })).toBeNull()
  })
})
