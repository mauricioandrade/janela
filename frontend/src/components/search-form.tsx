import { useEffect, useRef, useState, type FormEvent } from "react"
import { MapPinIcon, SearchIcon } from "lucide-react"

import { ACTIVITIES, ACTIVITY_ICONS } from "@/components/activity-icons"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
  FieldLegend,
  FieldSet,
} from "@/components/ui/field"
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@/components/ui/input-group"
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import type { Activity, WindowsParams } from "@/lib/api"
import type { Messages } from "@/lib/i18n"

const DURATIONS = [30, 45, 60, 90, 120, 180, 240]
const DAY_OPTIONS = [1, 2, 3]

export type SearchValues = Omit<WindowsParams, "lang">

type SearchFormProps = {
  t: Messages
  initialValues: Partial<SearchValues>
  isPending: boolean
  /** Server-side errors keyed by request parameter name. */
  serverErrors: Partial<Record<keyof SearchValues, string>>
  onSearch: (values: SearchValues) => void
}

export function SearchForm({
  t,
  initialValues,
  isPending,
  serverErrors,
  onSearch,
}: SearchFormProps) {
  const [city, setCity] = useState(initialValues.city ?? "")
  const [activity, setActivity] = useState<Activity>(
    initialValues.activity ?? "WALK"
  )
  const [durationMinutes, setDurationMinutes] = useState(
    DURATIONS.includes(initialValues.durationMinutes ?? 0)
      ? initialValues.durationMinutes!
      : 60
  )
  const [days, setDays] = useState(initialValues.days ?? 2)
  const [cityRequired, setCityRequired] = useState(false)
  const cityInput = useRef<HTMLInputElement>(null)

  // Move focus to the city field when the server can't find it.
  useEffect(() => {
    if (serverErrors.city) cityInput.current?.focus()
  }, [serverErrors.city])

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const trimmed = city.trim()
    setCityRequired(trimmed === "")
    if (trimmed === "") {
      cityInput.current?.focus()
      return
    }
    onSearch({ city: trimmed, activity, durationMinutes, days })
  }

  const cityError = cityRequired ? t.cityRequired : serverErrors.city

  return (
    <Card>
      <CardContent>
        <form onSubmit={handleSubmit} noValidate>
          <FieldGroup>
            <Field data-invalid={cityError ? true : undefined}>
              <FieldLabel htmlFor="city">{t.city}</FieldLabel>
              <InputGroup className="h-9">
                <InputGroupAddon>
                  <MapPinIcon aria-hidden />
                </InputGroupAddon>
                <InputGroupInput
                  ref={cityInput}
                  id="city"
                  name="city"
                  autoComplete="address-level2"
                  placeholder={t.cityPlaceholder}
                  value={city}
                  onChange={(event) => setCity(event.target.value)}
                  aria-invalid={cityError ? true : undefined}
                />
              </InputGroup>
              {cityError && <FieldError>{cityError}</FieldError>}
            </Field>

            <FieldSet>
              <FieldLegend variant="label">{t.activity}</FieldLegend>
              <ToggleGroup
                variant="outline"
                size="lg"
                className="flex-wrap"
                value={[activity]}
                onValueChange={(value) =>
                  value.length > 0 && setActivity(value[0] as Activity)
                }
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
              <Field
                data-invalid={serverErrors.durationMinutes ? true : undefined}
              >
                <FieldLabel htmlFor="duration">{t.duration}</FieldLabel>
                <Select
                  items={DURATIONS.map((minutes) => ({
                    value: minutes,
                    label: t.durationOption(minutes),
                  }))}
                  value={durationMinutes}
                  onValueChange={(value) =>
                    value !== null && setDurationMinutes(value)
                  }
                >
                  <SelectTrigger
                    id="duration"
                    className="w-full data-[size=default]:h-9"
                    aria-invalid={
                      serverErrors.durationMinutes ? true : undefined
                    }
                  >
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
                {serverErrors.durationMinutes && (
                  <FieldError>{serverErrors.durationMinutes}</FieldError>
                )}
              </Field>

              <FieldSet>
                <FieldLegend variant="label">{t.days}</FieldLegend>
                <ToggleGroup
                  variant="outline"
                  size="lg"
                  className="w-full *:flex-1"
                  value={[String(days)]}
                  onValueChange={(value) =>
                    value.length > 0 && setDays(Number(value[0]))
                  }
                >
                  {DAY_OPTIONS.map((option) => (
                    <ToggleGroupItem key={option} value={String(option)}>
                      {t.dayOption(option)}
                    </ToggleGroupItem>
                  ))}
                </ToggleGroup>
              </FieldSet>
            </div>

            <Button
              type="submit"
              size="lg"
              disabled={isPending}
              className="h-10 w-full"
            >
              {isPending ? (
                <Spinner data-icon="inline-start" />
              ) : (
                <SearchIcon data-icon="inline-start" />
              )}
              {isPending ? t.submitting : t.submit}
            </Button>
          </FieldGroup>
        </form>
      </CardContent>
    </Card>
  )
}
