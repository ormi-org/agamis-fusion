import { AppState } from "@core/models/states/app-state.model";
import { combineReducers, createReducer, on } from "@ngrx/store";
import * as AppStateActions from './app-state.actions';

export const initialState: AppState = {};

export const appStateCoreFeatureKey = 'app';

export const appStateReducer = createReducer(
    initialState,
    on(AppStateActions.loadConfig, (state, action) => ({ ...state, config: action.config}))
);