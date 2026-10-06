import { CpuIcon, FileTextIcon } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import type { WindowsResponse } from "@/lib/api"
import type { Messages } from "@/lib/i18n"

type NarrativeProps = Pick<WindowsResponse, "narrative" | "aiGenerated" | "model"> & { t: Messages }

/** The model's recommendation is the headline of the results, set apart by type rather than a box. */
export function Narrative({ narrative, aiGenerated, model, t }: NarrativeProps) {
  return (
    <section className="flex flex-col gap-4 border-l-4 border-primary pl-5">
      <h2 className="sr-only">{t.recommendation}</h2>
      <p className="text-xl leading-relaxed font-[450] tracking-tight text-pretty whitespace-pre-line sm:text-[1.375rem]">{narrative}</p>
      <Badge variant={aiGenerated ? "default" : "outline"}>
        {aiGenerated ? <CpuIcon data-icon="inline-start" /> : <FileTextIcon data-icon="inline-start" />}
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
