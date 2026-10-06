import type { LucideIcon } from "lucide-react"

type StatTileProps = {
  icon: LucideIcon
  label: string
  value: string
  /** A word that qualifies the value, e.g. the UV category. */
  note?: string
}

/** One weather number with its label; meant to sit in a <dl>. */
export function StatTile({ icon: Icon, label, value, note }: StatTileProps) {
  return (
    <div className="flex min-w-0 flex-col gap-1 rounded-lg bg-background/60 p-3">
      <dt className="flex items-center gap-1.5 text-xs text-muted-foreground">
        <Icon className="size-3.5 shrink-0" aria-hidden />
        <span className="truncate">{label}</span>
      </dt>
      <dd className="flex items-baseline gap-1.5">
        <span className="text-xl font-semibold tracking-tight">{value}</span>
        {note && (
          <span className="truncate text-xs text-muted-foreground">{note}</span>
        )}
      </dd>
    </div>
  )
}
