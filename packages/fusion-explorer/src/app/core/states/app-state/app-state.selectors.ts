import { AppState } from "@core/models/states/app-state.model"
import { createFeatureSelector, createSelector } from "@ngrx/store"

export const selectAppState = createFeatureSelector<AppState>('app')

export const selectAppConfig = createSelector(
    selectAppState,
    (app) => {
        return app.config
    }
)