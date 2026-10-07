import { CircleAlertIcon, CircleCheckIcon, CircleMinusIcon } from "lucide-react"
import { cn } from "cn"

import { useI18n } from "@/hooks/use-i18n"
import { scoreIconColor, scoreTier } from "@/lib/score"

const TIER_ICONS = { high: CircleCheckIcon, mid: CircleMinusIcon, low: CircleAlertIcon }

/** A score as status: icon, number and word, so the tier never rests on colour alone. */
export function ScoreBadge({ score, className }: { score: number; className?: string }) {
  const { t } = useI18n()
  const tier = scoreTier(score)
  const Icon = TIER_ICONS[tier]
  return (
    <span className={cn("inline-flex items-center gap-1.5", className)} title={t.scoreOf(score)}>
      <Icon className={cn("size-4 shrink-0", scoreIconColor(score))} aria-hidden />
      <span className="font-condensed font-semibold tabular-nums">{score}</span>
      <span className="text-muted-foreground">{t.tiers[tier]}</span>
    </span>
  )
}
