import { Route } from '@angular/router';

export const explorerRoutes: Route[] = [
  {
    path: 'admin',
    loadChildren: () => import('@admin/admin.module').then((module) => module.AdminModule),
  },
  {
    // Save widget
    path: 'save',
  },
  {
    // Open widget
    path: 'open',
  },
];
