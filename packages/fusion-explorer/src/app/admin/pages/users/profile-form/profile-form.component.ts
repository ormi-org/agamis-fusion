import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Profile } from '@core/models/data/profile.model';
import { Direction } from '@shared/components/separator/models/enums/direction.enum';
import { Color, Icon } from '@shared/constants/assets';
import { ProfileFormService } from './profile-form.service';
import { ProfileService } from '@core/services/profile/profile.service';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { combineLatest, map, skipWhile } from 'rxjs';
import { selectOrganization } from '@explorer/states/explorer-state/explorer-state.selectors';

@Component({
  selector: 'admin-page-users-profile-form',
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.scss'],
})
export class ProfileFormComponent implements OnInit, AfterViewInit {
  protected Color: typeof Color = Color;
  protected Direction: typeof Direction = Direction;
  protected Icon: typeof Icon = Icon;

  protected profile!: Profile;
  protected selectedFile!: File | null;
  protected maxAllowedFileSize = 0;

  @ViewChild('uploadInput', {read: ElementRef})
  private uploadInput!: ElementRef;

  constructor(
    private readonly store: Store,
    private readonly activatedRoute: ActivatedRoute,
    private readonly profileFormService: ProfileFormService,
    private readonly profileService: ProfileService
  ) {}

  ngOnInit(): void {
    // initial load if profile not provided
    if (this.profile === undefined) {
      // get orgId and profileId first before making GET request
      const initialProfileFetchSub = combineLatest([
        this.activatedRoute.paramMap.pipe(
          skipWhile(params => !params),
          map(params => params.get('id'))
        ),
        this.store.select(selectOrganization).pipe(
          skipWhile(org => !org),
          map(org => org?.id)
        )
      ]).subscribe(([ profileId, orgId ]) => {
        if (profileId && orgId) {
          this.profileService.fetchProfileById(orgId, profileId)
          .subscribe((p) => {
            this.profile = p;
          });
        }
        initialProfileFetchSub.unsubscribe();
      });
    }
  }

  ngAfterViewInit(): void {
    // detect change in form model
    this.profileFormService.getProfileMutationEvent().subscribe((p) => {
      this.profile = p;
      // reset input
      (<HTMLInputElement>this.uploadInput.nativeElement).value = '';
      (<HTMLInputElement>this.uploadInput.nativeElement).dispatchEvent(new Event('change'));
    });
  }

  protected deleteProfilePicture(): void {
    return;
  }

  protected onFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    const fileList: FileList | null = element.files;
    if (fileList && fileList.item(0)) {
      this.selectedFile = fileList.item(0);
    } else {
      this.selectedFile = null;
    }
  }

  protected onUsernameChange(event: Event): void {
    console.log(event);
  }

  protected onFirstnameChange(event: Event): void {
    console.log(event);
  }

  protected onLastnameChange(event: Event): void {
    console.log(event);
  }

  protected onActiveStateChange(state: boolean): void {
    this.profile.isActive = state;
  }

  protected onFormSubmit(): void {
    console.log("submit");
  }
}
