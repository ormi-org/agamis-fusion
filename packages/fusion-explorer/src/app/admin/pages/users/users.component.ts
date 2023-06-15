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
export class UsersComponent implements OnInit, OnDestroy, AfterViewInit {
  protected Ordering = Ordering;
  
  protected tableDatasource: UserTableDatasource;
  protected resizeObs!: ResizeObserver;
  protected width: BehaviorSubject<number> = new BehaviorSubject(0);

  @ViewChild('pageContainer', {read: ElementRef})
  private pageContainerRef!: ElementRef;

  constructor(
    private profileService: ProfileService,
    private readonly _: Store
  ) {
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

  ngAfterViewInit(): void {
    this.width.next(this.pageContainerRef.nativeElement.offsetWidth);
    this.resizeObs = new ResizeObserver(entries => {
      this.width.next(entries[0].contentRect.width);
    });
    this.resizeObs.observe(this.pageContainerRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.resizeObs.unobserve(this.pageContainerRef.nativeElement);
  }
}
