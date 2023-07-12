import { AfterViewInit, Component, Inject, LOCALE_ID, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'
import { Profile } from '@core/models/data/profile.model'
import { ProfileService } from '@core/services/profile/profile.service'
import { LoadingService } from '@explorer/utils/loading/loading.service'
import { Store } from '@ngrx/store'
import { DynamicTableComponent } from '@shared/components/dynamic-table/dynamic-table.component'
import LoadingQuery from '@shared/components/dynamic-table/typed/data-source/typed/loading-query.interface'
import { Icon } from '@shared/constants/assets'
import { DateFormatter } from '@shared/constants/utils/date-formatter'
import { Ordering } from '@shared/constants/utils/ordering'
import { Subject, auditTime, filter, withLatestFrom } from 'rxjs'
import { UserTableDatasource } from './datasources/user-table.datasource'
import { ProfileFormService } from './profile-form/profile-form.service'

type TemplateComputing = (profile: Profile) => { value: string }
type UsernameTemplateComputing = (profile: Profile) => { value: string; isAlias: boolean }
type ActiveTemplateComputing = (profile: Profile) => { value: string; isActive: boolean }

@Component({
  selector: 'admin-page-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements OnInit, OnDestroy, AfterViewInit {
  protected Ordering = Ordering
  protected Icon: typeof Icon = Icon
  
  protected datasourceQuerySubject: Subject<[LoadingQuery, boolean]> = new Subject()
  protected tableDatasource: UserTableDatasource

  private query: LoadingQuery = {
    filters: [],
    sorting: {
      field: 'lastLogin',
      direction: Ordering.DESC
    },
    pageIndex: 1,
    pageSize: 50
  }

  @ViewChild(DynamicTableComponent)
  private dynTable!: DynamicTableComponent<Profile>

  protected usernameValueComputing: UsernameTemplateComputing = ((profile: Profile) => {
    return profile.alias ? {
      value: profile.alias.toString(),
      isAlias: true
    } : {
      value: (profile.user?.username || '').toString(),
      isAlias: false
    }
  })

  protected activeValueComputing: ActiveTemplateComputing = ((profile: Profile) => {
    return profile.isActive ? {
      value: $localize`:@@ui.classic.const.word.yes:yes`,
      isActive: true
    } : {
      value: $localize`:@@ui.classic.const.word.no:no`,
      isActive: false
    }
  })

  protected lastSeenComputing: TemplateComputing = ((profile: Profile) => {
    return {
      value: DateFormatter.formatToTimeDiffLimit(new Date(profile.lastLogin)),
    }
  })

  protected creationDateTimeComputing: TemplateComputing = ((profile: Profile) => {
    return {
      value: new Date(profile.createdAt).toLocaleString(this.locale),
    }
  })

  protected updateDateTimeComputing: TemplateComputing = ((profile: Profile) => {
    return {
      value: new Date(profile.updatedAt).toLocaleString(this.locale),
    }
  })

  constructor(
    @Inject(LOCALE_ID) private readonly locale: string,
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute,
    private readonly loadingService: LoadingService,
    private readonly profileService: ProfileService,
    private readonly profileFormService: ProfileFormService,
    private readonly _store: Store
  ) {
    this.tableDatasource = new UserTableDatasource(_store, this.profileService)
  }

  ngOnInit(): void {
    this.tableDatasource.$loading.subscribe((isLoading) => {
      if (isLoading) {
        this.loadingService.reset()
      } else {
        this.loadingService.next()
        setTimeout(() => this.loadingService.complete(), 600)
      }
    })
    // subscribe to loadingQuery for fetching
    this.datasourceQuerySubject.subscribe(([query, stack]) => {
      this.tableDatasource.load(query, stack)
    })
    // initial fetch
    this.datasourceQuerySubject.next([this.query, false])
    // bind refresher for table on profile update
    this.tableDatasource.bindUnaryRefresher(this.profileFormService.getOutput())
  }

  ngAfterViewInit(): void {
    this.dynTable.getSelectEvent().subscribe((selectedProfile) => {
      this.router.navigate([selectedProfile.id], {relativeTo: this.activatedRoute}).then(() => {
        this.profileFormService.pushSource(selectedProfile)
      })
    })
    this.dynTable.getSortEvent().subscribe((updatedValue) => {
      this.query.sorting = updatedValue
      this.query.pageIndex = 1
      this.datasourceQuerySubject.next([this.query, false])
    })
    // check on scroll event for table to load next chunk of data
    this.dynTable.getBodyScrollEvent()
    .pipe(
      auditTime(500),
      withLatestFrom(this.loadingService.$loading),
      filter(([_, loading]) => loading === false)
    ).subscribe(([_, loading]) => {
      // trigger next page only if not already loading
      if (!loading) {
        // itterate pageIndex by 1 then next & stack
        this.query.pageIndex += 1
        this.datasourceQuerySubject.next([this.query, true])
      }
    })
  }
  
  ngOnDestroy(): void {
    this.tableDatasource.disconnect()
    this.datasourceQuerySubject.complete()
  }
}
