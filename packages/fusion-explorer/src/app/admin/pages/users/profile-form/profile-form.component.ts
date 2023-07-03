import { Component, OnInit } from '@angular/core';
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
export class ProfileFormComponent implements OnInit {
  protected Color: typeof Color = Color;
  protected Direction: typeof Direction = Direction;
  protected Icon: typeof Icon = Icon;

  protected profile!: Profile;
  protected selectedFile!: File | null;
  protected maxAllowedFileSize = 0;

  constructor(
    private readonly store: Store,
    private readonly activatedRoute: ActivatedRoute,
    private readonly profileFormService: ProfileFormService,
    private readonly profileService: ProfileService
  ) {}

  ngOnInit(): void {
    this.profileFormService.getProfileMutationEvent().subscribe((p) => {
      this.profile = p;
    });
    if (this.profile === undefined) {
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

  deleteProfilePicture(): void {
    return;
  }

  onFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    const fileList: FileList | null = element.files;
    if (fileList && fileList.item(0)) {
      this.selectedFile = fileList.item(0);
    }
  }

  onUsernameChange(event: Event): void {
    console.log(event);
  }

  onFirstnameChange(event: Event): void {
    console.log(event);
  }

  onLastnameChange(event: Event): void {
    console.log(event);
  }

  onActiveStateChange(state: boolean): void {
    this.profile.isActive = state;
  }

  onFormSubmit(): void {
    console.log("submit");
  }
}
