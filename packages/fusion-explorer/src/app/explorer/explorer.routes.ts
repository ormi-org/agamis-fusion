import { Route } from '@angular/router'
import { ExplorerComponent } from './explorer.component'
import { BrowserComponent } from './components/browser/browser.component'
import { preparationDataResolver } from '@core/resolvers/preparation-data/preparation-data.resolver'

export const explorerRoutes: Route[] = [
  {
    path: '',
    component: ExplorerComponent,
    resolve: { prep: preparationDataResolver },
    children: [
      {
        path: '',
        component: BrowserComponent,
        children: [
          {
            path: 'admin',
            pathMatch: 'prefix',
            loadChildren: () => import('@admin/admin.module').then((module) => module.AdminModule)
          },
        ]
      }
    ]
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
]
