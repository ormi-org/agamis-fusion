import { ApplicationRef } from '@angular/core';
import '@angular/localize/init';
import { enableDebugTools } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { environment } from '@environments/environment';
import { AppModule } from './app/app.module';

platformBrowserDynamic()
  .bootstrapModule(AppModule).then(moduleRef => {
    if (environment.production === false) {
      const applicationRef = moduleRef.injector.get(ApplicationRef);
      const componentRef = applicationRef.components[0];
      enableDebugTools(componentRef);
    }
  })
  .catch((err) => console.error(err));
