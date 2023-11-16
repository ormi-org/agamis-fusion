import { Injectable } from '@angular/core'
import { CoreModule } from '@core/core.module'

@Injectable({
  providedIn: CoreModule
})
export class ProfilePicService {

  // constructor() { }
  // TODO: implement
  fetchProfilePicUrl(): string {
    return 'https://picsum.photos/64/64'
  }
}
