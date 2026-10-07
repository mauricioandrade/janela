import {
  BikeIcon,
  DumbbellIcon,
  FootprintsIcon,
  SandwichIcon,
  ShovelIcon,
  ZapIcon,
  type LucideIcon,
} from "lucide-react"

import type { Activity } from "@/lib/api"

export const ACTIVITIES: Activity[] = [
  "RUN",
  "WALK",
  "BIKE",
  "PICNIC",
  "GARDENING",
  "WORKOUT",
]

export const ACTIVITY_ICONS: Record<Activity, LucideIcon> = {
  RUN: ZapIcon,
  WALK: FootprintsIcon,
  BIKE: BikeIcon,
  PICNIC: SandwichIcon,
  GARDENING: ShovelIcon,
  WORKOUT: DumbbellIcon,
}
