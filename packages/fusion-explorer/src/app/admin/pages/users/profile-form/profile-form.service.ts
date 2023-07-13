import { Profile } from '@core/models/data/profile.model'
import { Observable, ReplaySubject, Subject } from 'rxjs'

export class ProfileFormService {

  private profileSource: ReplaySubject<Profile> = new ReplaySubject<Profile>(1)
  private profileOut: Subject<Profile> = new Subject<Profile>()

  pushSource(profile: Profile) {
    this.profileSource.next(profile)
  }

  getSourceMutationEvent(): Observable<Profile> {
    return this.profileSource.asObservable()
  }

  pushOutput(profile: Profile) {
    this.profileOut.next(profile)
  }

  getOutput(): Observable<Profile> {
    return this.profileOut.asObservable()
  }
}
