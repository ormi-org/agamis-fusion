import { Component, OnInit } from '@angular/core';
import { Profile } from '@core/models/data/profile.model';
import { Direction } from '@shared/components/separator/models/enums/direction.enum';
import { Color, Icon } from '@shared/constants/assets';
import { ProfileFormService } from './profile-form.service';

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

  constructor(private readonly profileFormService: ProfileFormService) {}

  ngOnInit(): void {
    this.profileFormService.getProfileMutationEvent().subscribe((p) => {
      this.profile = p;
    });
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
    console.log(state);
  }

  onFormSubmit(): void {
    console.log("submit");
  }
}
