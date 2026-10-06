import { CpuIcon, FileTextIcon } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { useI18n } from "@/hooks/use-i18n"
import type { WindowsResponse } from "@/lib/api"

type NarrativeProps = Pick<
  WindowsResponse,
  "narrative" | "aiGenerated" | "model"
>

/** The model's recommendation is the headline of the results, set apart by type rather than a box. */
export function Narrative({ narrative, aiGenerated, model }: NarrativeProps) {
  const { t } = useI18n()
  return (
    <section className="flex flex-col gap-4 border-l-4 border-primary pl-5">
      <h2 className="sr-only">{t.recommendation}</h2>
      <p className="text-lg leading-relaxed font-[450] tracking-tight text-pretty whitespace-pre-line sm:text-xl">
        {narrative}
      </p>
      <Badge variant={aiGenerated ? "default" : "outline"}>
        {aiGenerated ? (
          <CpuIcon data-icon="inline-start" />
        ) : (
          <FileTextIcon data-icon="inline-start" />
        )}
        {aiGenerated ? (
          <>
            {t.byGemma} <span translate="no">{model ?? "Gemma"}</span>
          </>
        ) : (
          t.byTemplate
        )}
      </Badge>
    </section>
  )
}
