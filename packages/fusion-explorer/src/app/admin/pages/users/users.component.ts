import { Component, OnInit } from '@angular/core';
import { UserTableDatasource } from './datasources/user-table.datasource';
import { Ordering } from '@shared/constants/utils/ordering';
import { Store } from '@ngrx/store';
import { ProfileService } from '@core/services/profile/profile.service';
import Sorting from '@shared/components/dynamic-table/typed/data-source/typed/sorting.interface';

@Component({
  selector: 'admin-page-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements OnInit {
  protected Ordering = Ordering;
  
  protected tableDatasource: UserTableDatasource;

  constructor(private profileService: ProfileService, private readonly _: Store) {
    this.tableDatasource = new UserTableDatasource(_, this.profileService);
  }

  ngOnInit(): void {
    // this.tableDatasource.load(
    //   [],
    //   {
    //     field: 
    //   }
    // )
  }
}
