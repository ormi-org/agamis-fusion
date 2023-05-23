import { Route } from '@angular/router';

export const appRoutes: Route[] = [
    {
        path: '',
        children: [
            {
                path: 'explorer',
                loadChildren: () => import('./explorer/explorer.module').then(module => module.ExplorerModule)
            },
        ],
    }
];
