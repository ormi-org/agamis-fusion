import { Route } from '@angular/router';
import { ErrorComponent } from '@shared/pages/error/error.component';

export const appRoutes: Route[] = [
    {
        path: 'explorer',
        loadChildren: () => import('./explorer/explorer.module').then(module => module.ExplorerModule)
    },
    {
        path: 'error',
        component: ErrorComponent,
        title: 'Error'
    },
    {
        path: '**',
        title: 'Not found',
        component: ErrorComponent,
        data: {
            code: 404,
            title: 'Not found',
            text: 'Resource was not found'
        }
    },
];
