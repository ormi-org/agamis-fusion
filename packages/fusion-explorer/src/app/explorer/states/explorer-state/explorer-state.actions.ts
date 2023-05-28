import { createAction, props } from '@ngrx/store';

export const setOrganization = createAction(
    '[Explorer Component] Set Organization',
    props<{ organization: {
        id: string,
        label: string
    } }>()
);