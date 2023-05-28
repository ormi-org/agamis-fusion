import { ExplorerState } from "@explorer/models/states/explorer-state.model";
import { createFeatureSelector, createSelector } from "@ngrx/store";

export const selectExplorerState = createFeatureSelector<ExplorerState>('explorer');

export const selectOrganization = createSelector(
    selectExplorerState,
    (exp) => {
        return exp.organization;
    }
);