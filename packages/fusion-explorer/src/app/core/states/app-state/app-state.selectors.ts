import { AppState } from "@core/models/states/app-state.model";
import { createFeatureSelector, createSelector } from "@ngrx/store";

export const selectAppState = createFeatureSelector<AppState>('app');

export const selectOrganization = createSelector(
    selectAppState,
    (app) => {
        return app.organization;
    }
);

export const selectAppConfig = createSelector(
    selectAppState,
    (app) => {
        return app.config;
    }
)