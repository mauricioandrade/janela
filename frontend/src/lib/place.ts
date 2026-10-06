import type { Place } from "@/lib/api"

/** Brazilian states by normalized name (no accents, lowercase), including the spellings Open-Meteo uses. */
const BRAZIL_STATES: Record<string, string> = {
  acre: "AC",
  alagoas: "AL",
  amapa: "AP",
  amazonas: "AM",
  bahia: "BA",
  ceara: "CE",
  "distrito federal": "DF",
  "federal district": "DF",
  "espirito santo": "ES",
  goias: "GO",
  maranhao: "MA",
  "mato grosso": "MT",
  "mato grosso do sul": "MS",
  "minas gerais": "MG",
  para: "PA",
  paraiba: "PB",
  parana: "PR",
  pernambuco: "PE",
  piaui: "PI",
  "rio de janeiro": "RJ",
  "rio grande do norte": "RN",
  "rio grande do sul": "RS",
  rondonia: "RO",
  roraima: "RR",
  "santa catarina": "SC",
  "sao paulo": "SP",
  sergipe: "SE",
  tocantins: "TO",
}

function normalize(name: string) {
  return name
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/^(estado d[eoa]s?|state of)\s+/, "")
    .trim()
}

/** "SP" for São Paulo, Brazil; null elsewhere or when the state is unknown. */
export function brazilianState(place: Pick<Place, "admin1" | "countryCode">) {
  if (place.countryCode !== "BR" || !place.admin1) return null
  return BRAZIL_STATES[normalize(place.admin1)] ?? null
}

/** "Itobi – SP" in Brazil, "Springfield – Missouri" elsewhere, just the name when there is no state. */
export function placeLabel(place: Pick<Place, "name" | "admin1" | "countryCode">) {
  const region = brazilianState(place) ?? (place.admin1 !== place.name ? place.admin1 : null)
  return region ? `${place.name} – ${region}` : place.name
}

// Loaded on demand: each flag is fetched only when a place from that country is shown.
const countryFlags = import.meta.glob<string>("/node_modules/country-flag-icons/3x2/*.svg", {
  query: "?url",
  import: "default",
})

export function loadCountryFlag(countryCode: string): Promise<string> | null {
  const loader = countryFlags[`/node_modules/country-flag-icons/3x2/${countryCode.toUpperCase()}.svg`]
  return loader ? loader() : null
}

/** State flags live in public/flags/br (public-domain SVGs from Wikimedia Commons). */
export function stateFlagUrl(place: Pick<Place, "admin1" | "countryCode">) {
  const state = brazilianState(place)
  return state ? `/flags/br/${state.toLowerCase()}.svg` : null
}
