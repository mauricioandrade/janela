import { useMemo, type ReactNode } from "react"

import type { Lang } from "@/lib/api"
import { messages } from "@/lib/i18n"
import { I18nContext } from "@/lib/i18n-context"

/** Makes the current language and its interface copy available to every component through useI18n(). */
export function I18nProvider({ lang, children }: { lang: Lang; children: ReactNode }) {
  const value = useMemo(() => ({ lang, t: messages[lang] }), [lang])
  return <I18nContext value={value}>{children}</I18nContext>
}
