import { AppConfig } from "../app-config.model"
import { UserInfo } from "../user-info.model"

export interface AppState {
    config?: AppConfig,
    userInfo?: UserInfo,
    organization?: {
        id?: string,
        label?: string
    }
}