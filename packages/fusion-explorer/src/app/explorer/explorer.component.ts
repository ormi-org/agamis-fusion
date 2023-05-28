import { AfterViewInit, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JwtAuthenticationService } from '@core/services/authentication/jwt/jwt-authentication.service';
import { ConfigService } from '@core/services/config/config.service';
import { OrganizationService } from '@core/services/organization/organization.service';
import { loadConfig, loadUserInfo } from '@core/states/app-state/app-state.actions';
import { Store } from '@ngrx/store';
import { Subject, catchError, of, tap, throwError, zip } from 'rxjs';
import { setOrganization } from './states/explorer-state/explorer-state.actions';
import { Organization } from '@core/models/data/organization.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'explorer',
  templateUrl: './explorer.component.html',
  styleUrls: ['./explorer.component.scss'],
})
export class ExplorerComponent implements AfterViewInit {
  
  protected $loading: boolean = true;
  protected lbNextStageSubject = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly store: Store,
    private readonly configService: ConfigService,
    private readonly jwtAuthService: JwtAuthenticationService,
    private readonly organizationService: OrganizationService
  ) {}

  ngAfterViewInit(): void {
    // 0. check if orgId is provided; otherwise redirect to bad request
    const orgId = this.route.snapshot.queryParamMap.get('orgId');
    if (orgId === null) {
      this.router.navigateByUrl('/error', { state: { code: 400, title: 'bad request', text: $localize`:@@client.messages.errors.input.no-organization-id:no organization identifier provided` } })
      return;
    }
    this.lbNextStageSubject.next();
    // 1. Loading App Global Configuration configuration
    this.configService.load()
    .pipe(
      catchError((err) => {
        // nav to error page
        this.router.navigateByUrl('/error', { state: { title: 'fatal error', text: $localize`:@@client.messages.errors.generics.unrecoverable:an irrecoverable error occured` } } );
        this.$loading = false;
        return throwError(() => new Error(err));
      }),
      tap((fetchedConfig) => {
        this.store.dispatch(loadConfig({ config: fetchedConfig }));
      })
    )
    .subscribe((_) => {
      zip(
        // 2. Authentication + Load profile data
        this.jwtAuthService.getUserInfo()
        .pipe(
          tap((userInfo) => {
            this.store.dispatch(loadUserInfo({ userInfo: userInfo }))
          })
        ),
        // 3. Load organization data
        this.organizationService.getOrganizationById(orgId)
        .pipe(
          tap((org: Organization) => {
            this.store.dispatch(setOrganization({ organization: org }))
          }),
          catchError((err: HttpErrorResponse) => {
            if (err.status === 404) {
              this.router.navigateByUrl('/error', { state: { code: 404, title: 'not found', text: $localize`:@@client.messages.errors.data.organization.not-found:organization was not found` } });
              this.$loading = false;
              return of(undefined);
            }
            return throwError(() => err);
          })
        )
        // ...
      )
      .pipe(
        catchError((err) => {
          this.router.navigateByUrl('/error', { state: { title: 'fatal error', text: $localize`:@@client.messages.errors.generics.unrecoverable:an irrecoverable error occured` } } );
          this.$loading = false;
          return throwError(() => new Error(err))
        })
      )
      .subscribe(([_]) => {
        this.lbNextStageSubject.next();
        // timeout the splash disapearance for smoothing loading bar animation
        setTimeout(() => {
          this.$loading = false;
        }, 600);
      })
    });
  }
}
