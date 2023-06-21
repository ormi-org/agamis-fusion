import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, Input, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { UserTableDatasource } from './datasources/user-table.datasource';
import { Ordering } from '@shared/constants/utils/ordering';
import { Store } from '@ngrx/store';
import { ProfileService } from '@core/services/profile/profile.service';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'admin-page-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements OnInit {
  protected Ordering = Ordering;
  
  protected tableDatasource: UserTableDatasource;

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
