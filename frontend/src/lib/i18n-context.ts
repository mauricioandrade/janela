import { createContext } from "react"

import type { Lang } from "@/lib/api"
import type { Messages } from "@/lib/i18n"

export type I18n = { lang: Lang; t: Messages }

export const I18nContext = createContext<I18n | null>(null)
