import { AfterViewInit, Component, Inject, LOCALE_ID, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Profile } from '@core/models/data/profile.model';
import { ProfileService } from '@core/services/profile/profile.service';
import { LoadingService } from '@explorer/utils/loading/loading.service';
import { Store } from '@ngrx/store';
import { DynamicTableComponent } from '@shared/components/dynamic-table/dynamic-table.component';
import LoadingQuery from '@shared/components/dynamic-table/typed/data-source/typed/loading-query.interface';
import { Icon } from '@shared/constants/assets';
import { DateFormatter } from '@shared/constants/utils/date-formatter';
import { Ordering } from '@shared/constants/utils/ordering';
import { Subject } from 'rxjs';
import { UserTableDatasource } from './datasources/user-table.datasource';

type TemplateComputing = (profile: Profile) => { value: string };
type UsernameTemplateComputing = (profile: Profile) => { value: string; isAlias: boolean };
type ActiveTemplateComputing = (profile: Profile) => { value: string; isActive: boolean };

@Component({
  selector: 'admin-page-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements OnInit, OnDestroy, AfterViewInit {
  protected Ordering = Ordering;
  protected Icon: typeof Icon = Icon;
  
  protected datasourceQuerySubject: Subject<LoadingQuery> = new Subject();
  protected tableDatasource: UserTableDatasource;

  @ViewChild(DynamicTableComponent)
  private dynTable!: DynamicTableComponent<Profile>;

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
      value: $localize`:@@ui.classic.const.word.yes:yes`,
      isActive: true
    } : {
      value: $localize`:@@ui.classic.const.word.no:no`,
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
    private loadingService: LoadingService,
    private profileService: ProfileService,
    private readonly _: Store
  ) {
    this.tableDatasource = new UserTableDatasource(_, this.profileService);
  }

  ngOnInit(): void {
    this.tableDatasource.$loading.subscribe((isLoading) => {
      if (isLoading) {
        this.loadingService.reset();
      } else {
        this.loadingService.next();
        setTimeout(() => this.loadingService.complete(), 600);
      }
    });
    // subscribe to loadingQuery for fetching
    this.datasourceQuerySubject.subscribe((query) => {
      this.tableDatasource.load(query);
    });
    // initial fetch
    this.datasourceQuerySubject.next({
      filters: [],
      sorting: {
        field: 'lastLogin',
        direction: Ordering.DESC
      },
      pageIndex: 1,
      pageSize: 25
    });
  }

  ngAfterViewInit(): void {
    this.dynTable.getSelectEvent().subscribe((updatedValue) => {
      console.log('Selected profile: %s', updatedValue.id);
    });
    this.dynTable.getSortEvent().subscribe((updatedValue) => {
      console.log('Sorted profiles by: %s, %s', updatedValue.field, updatedValue.direction);
    })
  }
  
  ngOnDestroy(): void {
    this.tableDatasource.disconnect();
    this.datasourceQuerySubject.complete();
  }
}
