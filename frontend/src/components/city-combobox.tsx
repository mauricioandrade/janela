import type { Ref } from "react"
import { useQuery } from "@tanstack/react-query"
import { MapPinIcon } from "lucide-react"

import { PlaceFlags } from "@/components/place-flags"
import {
  Combobox,
  ComboboxContent,
  ComboboxEmpty,
  ComboboxInput,
  ComboboxItem,
  ComboboxList,
} from "@/components/ui/combobox"
import { InputGroupAddon } from "@/components/ui/input-group"
import { Spinner } from "@/components/ui/spinner"
import { useDebouncedValue } from "@/hooks/use-debounced-value"
import { fetchCities, type Lang, type Place } from "@/lib/api"
import type { Messages } from "@/lib/i18n"
import { placeLabel } from "@/lib/place"

const MIN_QUERY_LENGTH = 2
const DEBOUNCE_MS = 300

type CityComboboxProps = {
  id: string
  t: Messages
  lang: Lang
  inputRef: Ref<HTMLInputElement>
  inputValue: string
  onInputValueChange: (value: string) => void
  selected: Place | null
  onSelectedChange: (place: Place | null) => void
  invalid: boolean
}

/**
 * Free-text city field with suggestions. Picking one pins the exact place (state and country included);
 * typing something else and searching still works with the name's best match.
 */
export function CityCombobox({
  id,
  t,
  lang,
  inputRef,
  inputValue,
  onInputValueChange,
  selected,
  onSelectedChange,
  invalid,
}: CityComboboxProps) {
  const term = useDebouncedValue(inputValue.trim(), DEBOUNCE_MS)
  const showingSelection = selected !== null && inputValue === placeLabel(selected)
  const suggestions = useQuery({
    queryKey: ["cities", term, lang],
    queryFn: ({ signal }) => fetchCities(term, lang, signal),
    enabled: term.length >= MIN_QUERY_LENGTH && !showingSelection,
    staleTime: 24 * 60 * 60 * 1000,
  })
  const items: Place[] = suggestions.data ?? (selected ? [selected] : [])

  return (
    <Combobox<Place>
      items={items}
      value={selected}
      onValueChange={onSelectedChange}
      inputValue={inputValue}
      onInputValueChange={onInputValueChange}
      itemToStringLabel={placeLabel}
      isItemEqualToValue={(item, value) => item.id === value.id}
      filter={null}
    >
      <ComboboxInput
        id={id}
        ref={inputRef}
        name="city"
        autoComplete="off"
        spellCheck={false}
        placeholder={t.cityPlaceholder}
        aria-invalid={invalid || undefined}
        showTrigger={false}
        className="h-9 w-full"
      >
        <InputGroupAddon>
          {selected && showingSelection ? (
            <PlaceFlags place={selected} />
          ) : (
            <MapPinIcon aria-hidden />
          )}
        </InputGroupAddon>
      </ComboboxInput>
      <ComboboxContent aria-busy={suggestions.isFetching || undefined}>
        {suggestions.isFetching && (
          <p className="flex items-center gap-2 px-2.5 py-2 text-sm text-muted-foreground">
            <Spinner role="presentation" aria-label={undefined} aria-hidden />
            {t.citySearching}
          </p>
        )}
        {!suggestions.isFetching && <ComboboxEmpty>{t.cityNoMatches}</ComboboxEmpty>}
        <ComboboxList>
          {(city: Place) => (
            <ComboboxItem key={city.id} value={city} className="gap-2.5 py-1.5">
              <PlaceFlags place={city} className="w-[46px]" />
              <span className="flex min-w-0 flex-col">
                <span className="truncate font-medium">{placeLabel(city)}</span>
                <span className="truncate text-xs text-muted-foreground">
                  {[city.admin1, city.country].filter(Boolean).join(", ")}
                </span>
              </span>
            </ComboboxItem>
          )}
        </ComboboxList>
      </ComboboxContent>
    </Combobox>
  )
}
