import { Organization } from '@core/models/data/organization.model';
import { createAction, props } from '@ngrx/store';

export const setOrganization = createAction(
    '[Explorer Component] Set Organization',
    props<{ organization: Organization }>()
);