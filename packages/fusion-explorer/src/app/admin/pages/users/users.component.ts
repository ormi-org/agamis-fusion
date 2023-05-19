import { Component } from '@angular/core';
import { UserDatasource } from './datasources/user.datasource';
import { UserService } from '@core/services/user/user.service';
import { Ordering } from '@shared/constants/utils/ordering';

@Component({
  selector: 'admin-page-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent {
  protected Ordering = Ordering;
  
  private userService: UserService;
  
  protected userDatasource: UserDatasource;

  constructor(userService: UserService) {
    this.userService = userService;
    this.userDatasource = new UserDatasource(this.userService);
  }
}
