import { Profile } from '@core/models/data/profile.model';
import { Observable, Subject } from 'rxjs';

export class ProfileFormService {

  private profileSource: Subject<Profile> = new Subject<Profile>();
  private profileOut: Subject<Profile> = new Subject<Profile>();

  pushSource(profile: Profile) {
    this.profileSource.next(profile);
  }

  getSourceMutationEvent(): Observable<Profile> {
    return this.profileSource.asObservable();
  }

  pushOutput(profile: Profile) {
    this.profileOut.next(profile);
  }

  getOutput(): Observable<Profile> {
    return this.profileOut.asObservable();
  }
}
