import { CommonModule } from '@angular/common'
import { provideHttpClient, withInterceptors } from '@angular/common/http'
import { NgModule } from '@angular/core'
import { StoreModule } from '@ngrx/store'
import { authInterceptor } from './interceptors/auth-interceptor/auth-interceptor.interceptor'
import { JwtAuthenticationService } from './services/authentication/jwt/jwt-authentication.service'
import { LocalStorageService } from './services/utils/local-storage/local-storage.service'
import { appStateCoreFeatureKey, appStateReducer } from './states/app-state/app-state.reducers'

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature(
      appStateCoreFeatureKey,
      appStateReducer
    )
  ],
  providers: [
    JwtAuthenticationService,
    LocalStorageService,
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
})
export class CoreModule { }
