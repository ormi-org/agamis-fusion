import { AppConfig } from '@core/models/app-config.model';
import { createAction, props } from '@ngrx/store';

export const setOrganization = createAction(
    '[App Component] Set Organization',
    props<{ organization: {
        id: string,
        name: string
    } }>()
)

export const loadConfig = createAction(
    '[Config Service] Load Config',
    props<{ config: AppConfig }>()
)