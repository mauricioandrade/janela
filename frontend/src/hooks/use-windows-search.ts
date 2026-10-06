import { useQuery } from "@tanstack/react-query"

import { ApiError, fetchWindows, type Lang, type SearchValues } from "@/lib/api"

/** Why the server rejected a form field; the form turns it into copy. */
export type FieldProblem = "notFound" | "invalid"
export type FieldProblems = Partial<Record<keyof SearchValues, FieldProblem>>

/**
 * The windows query for a submitted search (none until the first one), plus the server's complaints mapped
 * to form fields: 404 means the city, 400 lists its fields.
 */
export function useWindowsSearch(search: SearchValues | null, lang: Lang) {
  const params = search && { ...search, lang }
  const query = useQuery({
    queryKey: ["windows", params],
    queryFn: ({ signal }) => fetchWindows(params!, signal),
    enabled: params !== null,
  })
  return { ...query, fieldProblems: fieldProblems(query.error) }
}

function fieldProblems(error: Error | null): FieldProblems {
  if (!(error instanceof ApiError)) return {}
  if (error.status === 404) return { city: "notFound" }
  if (error.status === 400) {
    return Object.fromEntries(error.fields.map((field) => [field, "invalid"])) as FieldProblems
  }
  return {}
}
