import { CloudRainIcon, SunIcon } from "lucide-react"

import { useI18n } from "@/hooks/use-i18n"
import { isHighUv, isLikelyRain } from "@/lib/score"

/** Sun amber for UV, storm violet for rain: an icon with its own label, never colour alone. */
export function UvWarning({ uv }: { uv: number | null }) {
  const { t } = useI18n()
  if (!isHighUv(uv)) return null
  return <SunIcon className="inline size-4 shrink-0 self-center align-[-0.125em] text-sun" role="img" aria-label={t.highUv} />
}

export function RainWarning({ chance }: { chance: number | null }) {
  const { t } = useI18n()
  if (!isLikelyRain(chance)) return null
  return <CloudRainIcon
      className="inline size-4 shrink-0 self-center align-[-0.125em] text-storm"
      role="img"
      aria-label={t.likelyRain}
    />
}
