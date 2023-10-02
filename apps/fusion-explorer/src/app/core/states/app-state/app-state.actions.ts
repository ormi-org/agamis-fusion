import { AppConfig } from '@core/models/app-config.model'
import { UserInfo } from '@core/models/user-info.model'
import { createAction, props } from '@ngrx/store'

export const loadConfig = createAction(
    '[Config Service] Load Config',
    props<{ config: AppConfig }>()
)

export const loadUserInfo = createAction(
    '[Authentication Service] Load UserInfo',
    props<{ userInfo: UserInfo }>()
)