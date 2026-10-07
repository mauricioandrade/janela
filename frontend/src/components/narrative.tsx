import { CpuIcon, FileTextIcon } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { useI18n } from "@/hooks/use-i18n"
import type { WindowsResponse } from "@/lib/api"

type NarrativeProps = Pick<WindowsResponse, "narrative" | "aiGenerated" | "model">

/** The recommendation in the model's words, with who wrote it stated plainly. */
export function Narrative({ narrative, aiGenerated, model }: NarrativeProps) {
  const { t } = useI18n()
  return (
    <section aria-labelledby="narrative-heading" className="flex flex-col gap-4 rounded-xl border bg-card p-5 sm:p-6">
      <h2 id="narrative-heading" className="sr-only">
        {t.recommendation}
      </h2>
      <Badge variant={aiGenerated ? "default" : "outline"} className="h-6 px-2.5 text-sm">
        {aiGenerated ? <CpuIcon data-icon="inline-start" /> : <FileTextIcon data-icon="inline-start" />}
        {aiGenerated ? (
          <>
            {t.byGemma} <span translate="no">{model ?? "Gemma"}</span>
          </>
        ) : (
          t.byTemplate
        )}
      </Badge>
      <p className="max-w-[65ch] text-[1.0625rem] leading-relaxed text-pretty whitespace-pre-line">{narrative}</p>
    </section>
  )
}
