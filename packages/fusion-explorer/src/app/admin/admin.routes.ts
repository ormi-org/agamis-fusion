import { Route } from '@angular/router';
import { GroupsComponent } from './pages/groups/groups.component';
import { OrganizationComponent } from './pages/organization/organization.component';
import { PermissionsComponent } from './pages/permissions/permissions.component';
import { UsersComponent } from './pages/users/users.component';

export const adminRoutes: Route[] = [
    {
        path: 'users',
        pathMatch: 'full',
        component: UsersComponent
    },
    {
        path: 'groups',
        pathMatch: 'full',
        component: GroupsComponent
    },
    {
        path: 'permissions',
        pathMatch: 'full',
        component: PermissionsComponent
    },
    {
        path: 'organization',
        pathMatch: 'full',
        component: OrganizationComponent
    },
];
