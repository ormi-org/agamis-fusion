import { Icon } from "@shared/constants/assets";

export interface HeaderTitleItem {
    text: string,
    icon?: {
        value: Icon,
        backgroundColor: string,
    },
    style: {
        weight: number,
        emphasis?: string
    }
}