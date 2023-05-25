import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { appRoutes } from './app.routes';
import { CoreModule } from '@core/core.module';
import { StoreModule, provideStore } from '@ngrx/store';
import { APP_BASE_HREF } from '@angular/common';
import { SharedModule } from '@shared/shared.module';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    RouterModule.forRoot(appRoutes, { initialNavigation: 'enabledBlocking' })
  ],
  providers: [
    {
      provide: APP_BASE_HREF,
      useValue: '/app/native/fusion-explorer'
    }
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
