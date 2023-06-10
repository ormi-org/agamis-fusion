import { createReducer, on } from "@ngrx/store";
import * as ExplorerStateActions from './explorer-state.actions';
import { ExplorerState } from "@explorer/models/states/explorer-state.model";

export const initialState: ExplorerState = {
    ui: {
        view: {
            name: ''
        }
    }
};

export const explorerStateExplorerFeatureKey = 'explorer';

export const explorerStateReducer = createReducer(
    initialState,
    on(ExplorerStateActions.setOrganization, (state, action) => (
        {
            ...state,
            organization: action.organization
        }
    ))
);