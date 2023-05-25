import { AfterViewInit, Component, OnDestroy, OnInit, enableProdMode, isDevMode } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderTitleItem } from '@core/models/header-title-item.model';
import { JwtAuthenticationService } from '@core/services/authentication/jwt/jwt-authentication.service';
import { ConfigService } from '@core/services/config/config.service';
import { selectOrganization } from '@core/states/app-state/app-state.selectors';
import { environment } from '@environments/environment';
import { Store } from '@ngrx/store';
import { BehaviorSubject, Subject, catchError, throwError, zip } from 'rxjs';

@Component({
  selector: 'app-explorer',
  templateUrl: './explorer.component.html',
  styleUrls: ['./explorer.component.scss'],
})
export class ExplorerComponent implements OnInit, OnDestroy, AfterViewInit {

  private loadingSubject = new BehaviorSubject<boolean>(true);
  protected $loading: boolean = this.loadingSubject.value;
  protected lbNextStageSubject = new Subject<void>();

  private headerTitleSubject = new BehaviorSubject<HeaderTitleItem[]>([
    {
      text: 'fusion',
      style: {
        weight: 800
      }
    },
    {
      text: '',
      style: {
        weight: 800
      }
    }
  ]);
  protected headerTitle = this.headerTitleSubject.value;

  constructor(
    private readonly router: Router,
    private readonly store: Store,
    private readonly configService: ConfigService,
    private readonly jwtAuthService: JwtAuthenticationService
  ) {}

  private setHeaderTitle(items: HeaderTitleItem[]): void {
    this.headerTitleSubject.next(this.headerTitle.slice(0, 1).concat(items));
  }

  private onOrgNameUpdate(orgName: string): void {
    let newTitle = this.headerTitle
    newTitle[1] = {
      ...newTitle[1],
      text: orgName
    }
    this.headerTitleSubject.next(newTitle);
  }

  ngOnInit(): void {
    this.store.select(selectOrganization).subscribe((org) => {
      if (org?.label) {
        this.onOrgNameUpdate(org.label);
      } else if (org?.id) {
        this.onOrgNameUpdate(org.id);
      }
      this.headerTitleSubject.next([
        this.headerTitle[0],
        {
          ...this.headerTitle[1],
          text: ''
        }
      ])
    });
  }

  ngOnDestroy(): void {
    this.loadingSubject.complete();
  }

  ngAfterViewInit(): void {
    // 1. Loading App Global Configuration configuration
    this.lbNextStageSubject.next();
    this.configService.load()
    .pipe(
      catchError((err) => {
        // nav to error page
        this.router.navigateByUrl('/error', { state: { title: 'fatal error', text: 'an irrecoverable error occured' } } );
        this.$loading = false;
        return throwError(() => new Error(err));
      })
    )
    .subscribe((_) => {
      zip(
        // 2. Authentication + Load profile data
        this.jwtAuthService.getUserInfo()
        // 3. Load organization data
        // ...
      )
      .pipe(
        catchError((err) => {
          this.router.navigateByUrl('/error', { state: { title: 'fatal error', text: 'an irrecoverable error occured' } } );
          this.$loading = false;
          return throwError(() => new Error("testError"))
        })
      )
      .subscribe(([userInfo]) => {
        this.lbNextStageSubject.next();
        this.$loading = false;
      })
    });
  }
}
