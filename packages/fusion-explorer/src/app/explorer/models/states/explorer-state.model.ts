import { UserInfo } from "@core/models/user-info.model"

export interface ExplorerState {
    userInfo?: UserInfo,
    organization?: {
        id?: string,
        label?: string
    },
    ui: {
        view: {
            name: string
        }
    }
}