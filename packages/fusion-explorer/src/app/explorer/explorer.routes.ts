import { Route } from '@angular/router';
import { ExplorerComponent } from './explorer.component';

export const explorerRoutes: Route[] = [
  {
    path: '',
    component: ExplorerComponent
  },
  // {
  //   path: 'admin',
  //   loadChildren: () => import('@admin/admin.module').then((module) => module.AdminModule),
  // },
  // {
  // Save widget
  // path: 'save',
  // },
  // {
  //   // Open widget
  //   // path: 'open',
  // },
];
