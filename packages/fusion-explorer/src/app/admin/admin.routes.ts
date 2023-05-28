import { Route } from '@angular/router';
import { UsersComponent } from './pages/users/users.component';
import { ErrorComponent } from '@shared/pages/error/error.component';

export const adminRoutes: Route[] = [
    {
        path: 'users',
        pathMatch: 'full',
        component: UsersComponent
    },
];
