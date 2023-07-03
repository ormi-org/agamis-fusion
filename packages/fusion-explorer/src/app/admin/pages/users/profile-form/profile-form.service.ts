import { Profile } from '@core/models/data/profile.model';
import { Observable, ReplaySubject } from 'rxjs';

export class ProfileFormService {

  private profileSource: ReplaySubject<Profile> = new ReplaySubject<Profile>();

  pushProfile(profile: Profile) {
    this.profileSource.next(profile);
  }

  getProfileMutationEvent(): Observable<Profile> {
    return this.profileSource.asObservable();
  }
}
