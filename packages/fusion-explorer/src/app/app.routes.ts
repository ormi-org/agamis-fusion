import { Route } from '@angular/router';

export const appRoutes: Route[] = [
    {
        path: '',
        children: [
            {
                path: 'explorer',
                children: [
                    {
                        // Classic explorer
                        path: '',
                        children: [
                            {
                                path: 'admin',
                                loadChildren: () => import('./admin/admin.module').then(module => module.AdminModule)
                            }
                        ]
                    },
                    // {
                    //     // Save widget
                    //     path: 'save'
                    // },
                    // {
                    //     // Open widget
                    //     path: 'open'
                    // },
                ],
            },
        ],
    }
];
