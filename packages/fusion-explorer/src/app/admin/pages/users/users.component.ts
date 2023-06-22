import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import { Profile } from '@core/models/data/profile.model';
import { ProfileService } from '@core/services/profile/profile.service';
import { Store } from '@ngrx/store';
import { Icon } from '@shared/constants/assets';
import { DateFormatter } from '@shared/constants/utils/date-formatter';
import { Ordering } from '@shared/constants/utils/ordering';
import { UserTableDatasource } from './datasources/user-table.datasource';

type TemplateComputing = (profile: Profile) => { value: string };
type UsernameTemplateComputing = (profile: Profile) => { value: string; isAlias: boolean };
type ActiveTemplateComputing = (profile: Profile) => { value: string; isActive: boolean };

@Component({
  selector: 'admin-page-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements OnInit {
  protected Ordering = Ordering;
  protected Icon: typeof Icon = Icon;
  
  protected tableDatasource: UserTableDatasource;

  protected usernameValueComputing: UsernameTemplateComputing = ((profile: Profile) => {
    return profile.alias ? {
      value: profile.alias.toString(),
      isAlias: true
    } : {
      value: (profile.user?.username || '').toString(),
      isAlias: false
    }
  });

  protected activeValueComputing: ActiveTemplateComputing = ((profile: Profile) => {
    return profile.isActive ? {
      value: 'yes',
      isActive: true
    } : {
      value: 'no',
      isActive: false
    }
  });

  protected lastSeenComputing: TemplateComputing = ((profile: Profile) => {
    return {
      value: DateFormatter.formatToTimeDiffLimit(new Date(profile.lastLogin)),
    }
  });

  protected dateTimeComputing: TemplateComputing = ((profile: Profile) => {
    return {
      value: new Date(profile.lastLogin).toLocaleString(this.locale),
    }
  });

  constructor(
    @Inject(LOCALE_ID) private locale: string,
    private profileService: ProfileService,
    private readonly _: Store
  ) {
    this.tableDatasource = new UserTableDatasource(_, this.profileService);
  }

  ngOnInit(): void {
    // initial fetching
    this.tableDatasource.load(
      [],
      {
        field: "lastLogin",
        direction: Ordering.DESC
      },
      1,
      25
    )
  }
}
