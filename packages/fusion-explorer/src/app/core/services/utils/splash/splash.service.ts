import { Injectable } from '@angular/core';
import { CoreModule } from '@core/core.module';
import { Loading } from '@core/services/typed/loading';

@Injectable({
  providedIn: CoreModule
})
export class SplashService extends Loading {}
