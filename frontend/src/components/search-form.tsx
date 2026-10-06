import { useState, type FormEvent } from "react"
import { SearchIcon } from "lucide-react"

import { ACTIVITIES, ACTIVITY_ICONS } from "@/components/activity-icons"
import { Button } from "@/components/ui/button"
import { Field, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import type { Activity, WindowsParams } from "@/lib/api"
import type { Messages } from "@/lib/i18n"

const DURATIONS = [30, 45, 60, 90, 120, 180, 240]
const DAY_OPTIONS = [1, 2, 3]

export type SearchValues = Omit<WindowsParams, "lang">

type SearchFormProps = {
  t: Messages
  isPending: boolean
  /** Server-side errors keyed by request parameter name. */
  serverErrors: Partial<Record<keyof SearchValues, string>>
  onSearch: (values: SearchValues) => void
}

export function SearchForm({ t, isPending, serverErrors, onSearch }: SearchFormProps) {
  const [city, setCity] = useState("")
  const [activity, setActivity] = useState<Activity>("WALK")
  const [durationMinutes, setDurationMinutes] = useState(60)
  const [days, setDays] = useState(2)
  const [cityRequired, setCityRequired] = useState(false)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const trimmed = city.trim()
    setCityRequired(trimmed === "")
    if (trimmed === "") return
    onSearch({ city: trimmed, activity, durationMinutes, days })
  }

  const cityError = cityRequired ? t.cityRequired : serverErrors.city

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FieldGroup>
        <Field data-invalid={cityError ? true : undefined}>
          <FieldLabel htmlFor="city">{t.city}</FieldLabel>
          <Input
            id="city"
            name="city"
            autoComplete="address-level2"
            placeholder={t.cityPlaceholder}
            value={city}
            onChange={(event) => setCity(event.target.value)}
            aria-invalid={cityError ? true : undefined}
          />
          {cityError && <FieldError>{cityError}</FieldError>}
        </Field>

        <FieldSet>
          <FieldLegend variant="label">{t.activity}</FieldLegend>
          <ToggleGroup
            variant="outline"
            className="flex-wrap"
            value={[activity]}
            onValueChange={(value) => value.length > 0 && setActivity(value[0] as Activity)}
          >
            {ACTIVITIES.map((option) => {
              const Icon = ACTIVITY_ICONS[option]
              return (
                <ToggleGroupItem key={option} value={option}>
                  <Icon data-icon="inline-start" />
                  {t.activities[option]}
                </ToggleGroupItem>
              )
            })}
          </ToggleGroup>
        </FieldSet>

        <div className="grid gap-6 sm:grid-cols-2">
          <Field data-invalid={serverErrors.durationMinutes ? true : undefined}>
            <FieldLabel htmlFor="duration">{t.duration}</FieldLabel>
            <Select
              items={DURATIONS.map((minutes) => ({ value: minutes, label: t.durationOption(minutes) }))}
              value={durationMinutes}
              onValueChange={(value) => value !== null && setDurationMinutes(value)}
            >
              <SelectTrigger id="duration" className="w-full" aria-invalid={serverErrors.durationMinutes ? true : undefined}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {DURATIONS.map((minutes) => (
                    <SelectItem key={minutes} value={minutes}>
                      {t.durationOption(minutes)}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
            {serverErrors.durationMinutes && <FieldError>{serverErrors.durationMinutes}</FieldError>}
          </Field>

          <FieldSet>
            <FieldLegend variant="label">{t.days}</FieldLegend>
            <ToggleGroup
              variant="outline"
              value={[String(days)]}
              onValueChange={(value) => value.length > 0 && setDays(Number(value[0]))}
            >
              {DAY_OPTIONS.map((option) => (
                <ToggleGroupItem key={option} value={String(option)}>
                  {t.dayOption(option)}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>
          </FieldSet>
        </div>

        <Button type="submit" size="lg" disabled={isPending} className="w-full sm:w-fit">
          {isPending ? <Spinner data-icon="inline-start" /> : <SearchIcon data-icon="inline-start" />}
          {isPending ? t.submitting : t.submit}
        </Button>
      </FieldGroup>
    </form>
  )
}
