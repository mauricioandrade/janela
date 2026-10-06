import { CircleAlertIcon, CircleCheckIcon, CircleMinusIcon } from "lucide-react"
import { Progress } from "@base-ui/react/progress"
import { cn } from "cn"

import { ProgressIndicator, ProgressTrack } from "@/components/ui/progress"
import { useI18n } from "@/hooks/use-i18n"
import { scoreFill, scoreIconColor, scoreTier, scoreTrack } from "@/lib/score"

const TIER_ICONS = {
  high: CircleCheckIcon,
  mid: CircleMinusIcon,
  low: CircleAlertIcon,
}

/** A score as status: icon, word and number, so the tier never rests on colour alone. */
export function ScoreBadge({
  score,
  className,
}: {
  score: number
  className?: string
}) {
  const { t } = useI18n()
  const tier = scoreTier(score)
  const Icon = TIER_ICONS[tier]
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 text-sm font-medium",
        className
      )}
      title={t.scoreOf(score)}
    >
      <Icon className={cn("size-4", scoreIconColor(score))} aria-hidden />
      {t.tiers[tier]}
      <span className="text-muted-foreground tabular-nums">{score}</span>
    </span>
  )
}

/** The score as a meter; the track is a lighter step of the fill's hue. */
export function ScoreMeter({
  score,
  className,
}: {
  score: number
  className?: string
}) {
  const { t } = useI18n()
  return (
    <Progress.Root
      value={score}
      aria-label={t.score}
      className={cn("w-full", className)}
    >
      <ProgressTrack className={cn("h-1.5", scoreTrack(score))}>
        <ProgressIndicator className={scoreFill(score)} />
      </ProgressTrack>
    </Progress.Root>
  )
}
