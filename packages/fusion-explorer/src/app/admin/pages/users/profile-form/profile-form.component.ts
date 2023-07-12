import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Profile } from '@core/models/data/profile.model'
import { ProfilePicService } from '@core/services/profile/picture/profile-pic.service'
import { ProfileService } from '@core/services/profile/profile.service'
import { selectOrganization } from '@explorer/states/explorer-state/explorer-state.selectors'
import { Store } from '@ngrx/store'
import { Direction } from '@shared/components/separator/models/enums/direction.enum'
import { Color, Icon } from '@shared/constants/assets'
import { BehaviorSubject, combineLatest, map, of, skipWhile, take, tap, zip } from 'rxjs'
import { ProfileFormService } from './profile-form.service'

@Component({
  selector: 'admin-page-users-profile-form',
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.scss'],
})
export class ProfileFormComponent implements OnInit, AfterViewInit {
  protected Color: typeof Color = Color
  protected Direction: typeof Direction = Direction
  protected Icon: typeof Icon = Icon
  protected loadingSubject = new BehaviorSubject<boolean>(false)

  protected profile!: Profile
  protected profilePicUrl: string | undefined
  protected selectedFile: File | null = null
  protected maxAllowedFileSize = 0

  @ViewChild('uploadInput', {read: ElementRef})
  private uploadInput!: ElementRef

  constructor(
    private readonly store: Store,
    private readonly activatedRoute: ActivatedRoute,
    private readonly profileFormService: ProfileFormService,
    private readonly profileService: ProfileService,
    private readonly profilePicService: ProfilePicService
  ) {}

  ngOnInit(): void {
    this.profileFormService.getSourceMutationEvent() // detect change in form model
      .subscribe((p) => {
        this.profile = p
        // TODO: extract profilePicFile ID
        this.profilePicUrl = this.profilePicService.fetchProfilePicUrl()
        // reset picture input on new profile provided
        if (<HTMLInputElement>this.uploadInput?.nativeElement !== undefined) {
          (<HTMLInputElement>this.uploadInput?.nativeElement).value = '';
          (<HTMLInputElement>this.uploadInput?.nativeElement).dispatchEvent(new Event('change'))
        }
    })
    // initial load if profile not provided
    combineLatest([
      // get orgId and profileId first before making GET request
      this.activatedRoute.paramMap.pipe(
        skipWhile(params => !params),
        map(params => params.get('id'))
      ),
      this.store.select(selectOrganization).pipe(
        skipWhile(org => !org),
        map(org => org?.id)
      ),
    ])
    .subscribe(([ profileId, orgId ]) => {
      if (this.profile === undefined) {
        if (profileId && orgId) {
          this.profileService.fetchProfileById(orgId, profileId)
          .subscribe((p) => {
            this.profileFormService.pushSource(p)
          })
        }
      }
    })
  }

  ngAfterViewInit(): void {
    return
  }

  protected deleteProfilePicture(): void {
    return
  }

  protected onFileSelected(event: Event): void {
    const fileList: FileList | null = (<HTMLInputElement>event.currentTarget).files
    if (fileList && fileList.item(0)) {
      this.selectedFile = fileList.item(0)
    } else {
      this.selectedFile = null
    }
  }

  protected onUsernameChange(event: Event): void {
    this.profile.alias = (<HTMLInputElement>event.currentTarget).value
  }

  protected onFirstnameChange(event: Event): void {
    this.profile.firstName = (<HTMLInputElement>event.currentTarget).value
  }

  protected onLastnameChange(event: Event): void {
    this.profile.lastName = (<HTMLInputElement> event.currentTarget).value
  }

  protected onActiveStateChange(state: boolean): void {
    this.profile.isActive = state
  }

  protected onFormSubmit(): void {
    zip(
      [
        of(this.profile.firstName)
        .pipe(
          map((fn) => {
            if (!fn) {
              const msg = "firstname is not set"
              // display err msg
              throw new Error(msg)
            }
            return fn
          })
        ),
        of(this.profile.lastName)
        .pipe(
          tap((ln) => {
            if (!ln) {
              const msg = "lastname is not set"
              // display err msg
              throw new Error(msg)
            }
            return ln
          })
        ),
      ]
    ).subscribe({
      next: () => {
        this.store.select(selectOrganization).pipe(
          take(1),
          skipWhile(org => !org),
          map(org => org?.id)
        ).subscribe((orgId) => {
          if (orgId) {
            // start loading state
            this.loadingSubject.next(true)
            this.profileService.updateProfile(orgId, this.profile)
            .subscribe((p) => {
              // stop loading state
              this.loadingSubject.next(false)
              // push profile to both source and output to update
              // both form and table
              this.profileFormService.pushSource(p)
              this.profileFormService.pushOutput(p)
            })
          }
        })
      },
      error: (err) => {
        console.error(err)
      }
    })
  }
}
