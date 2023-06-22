import { Component, OnInit } from '@angular/core';
import { Profile } from '@core/models/data/profile.model';
import { ProfileService } from '@core/services/profile/profile.service';
import { Store } from '@ngrx/store';
import { Icon } from '@shared/constants/assets';
import { Ordering } from '@shared/constants/utils/ordering';
import { UserTableDatasource } from './datasources/user-table.datasource';

type UsernameTemplateComputing = (profile: Profile) => { value: string; isAlias: boolean };

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

  constructor(
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
