import { Route } from '@angular/router'
import { GroupsComponent } from './pages/groups/groups.component'
import { OrganizationComponent } from './pages/organization/organization.component'
import { PermissionsComponent } from './pages/permissions/permissions.component'
import { UsersComponent } from './pages/users/users.component'
import { ProfileFormComponent } from './pages/users/profile-form/profile-form.component'

export const adminRoutes: Route[] = [
    {
        path: 'users',
        pathMatch: 'prefix',
        component: UsersComponent,
        children: [
            {
                path: ':id',
                pathMatch: 'full',
                component: ProfileFormComponent
            }
        ]
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
]
