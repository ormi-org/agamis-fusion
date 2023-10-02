import { Organization } from "@core/models/data/organization.model"
import { UserInfo } from "@core/models/user-info.model"

export interface ExplorerState {
    userInfo?: UserInfo,
    organization?: Organization,
    ui: {
        view: {
            name: string
        }
    }
}