import { useEffect, useState } from "react"
import { cn } from "cn"

import type { Place } from "@/lib/api"
import { loadCountryFlag, stateFlagUrl } from "@/lib/place"

type PlaceFlagsProps = {
  place: Pick<Place, "admin1" | "country" | "countryCode">
  className?: string
}

/** Country flag, plus the state flag for Brazilian places. Decorative: the place name is always written out. */
export function PlaceFlags({ place, className }: PlaceFlagsProps) {
  const countryFlag = useCountryFlag(place.countryCode)
  const stateFlag = stateFlagUrl(place)
  if (!countryFlag && !stateFlag) return null

  return (
    <span
      className={cn("inline-flex shrink-0 items-center gap-1", className)}
      aria-hidden
    >
      {countryFlag && (
        <Flag src={countryFlag} title={place.country ?? undefined} />
      )}
      {stateFlag && <Flag src={stateFlag} title={place.admin1 ?? undefined} />}
    </span>
  )
}

function Flag({ src, title }: { src: string; title?: string }) {
  return (
    <img
      src={src}
      alt=""
      title={title}
      width={21}
      height={14}
      loading="lazy"
      className="h-3.5 w-[21px] rounded-[2px] object-cover shadow-[0_0_0_1px] shadow-foreground/15"
    />
  )
}

function useCountryFlag(countryCode: string | null) {
  const [loaded, setLoaded] = useState<{ code: string; url: string } | null>(
    null
  )

  useEffect(() => {
    if (!countryCode) return
    let active = true
    loadCountryFlag(countryCode)?.then(
      (url) => active && setLoaded({ code: countryCode, url })
    )
    return () => {
      active = false
    }
  }, [countryCode])

  return loaded && loaded.code === countryCode ? loaded.url : null
}
