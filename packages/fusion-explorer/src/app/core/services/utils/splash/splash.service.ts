import { Injectable } from '@angular/core'
import { CoreModule } from '@core/core.module'
import { Loading } from '@core/services/typed/loading'
import { BehaviorSubject, Observable, Subject } from 'rxjs'

@Injectable({
  providedIn: CoreModule
})
export class SplashService extends Loading {
  protected override loadingSubject: Subject<boolean> = new BehaviorSubject(true)

  override $loading: Observable<boolean> = this.loadingSubject.asObservable()
}
