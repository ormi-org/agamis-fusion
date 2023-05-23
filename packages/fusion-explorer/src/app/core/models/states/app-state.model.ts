import { AppConfig } from "../app-config.model"

export interface AppState {
    config?: AppConfig
    organization?: {
        id?: string,
        name?: string
    }
}