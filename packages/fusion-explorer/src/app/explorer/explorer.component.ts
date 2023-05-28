import { AfterViewInit, ChangeDetectorRef, Component, Input, OnChanges, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { JwtAuthenticationService } from '@core/services/authentication/jwt/jwt-authentication.service';
import { ConfigService } from '@core/services/config/config.service';
import { loadConfig, loadUserInfo } from '@core/states/app-state/app-state.actions';
import { selectOrganization } from '@explorer/states/explorer-state/explorer-state.selectors';
import { Store } from '@ngrx/store';
import { BehaviorSubject, Subject, catchError, tap, throwError, zip } from 'rxjs';

@Component({
  selector: 'explorer',
  templateUrl: './explorer.component.html',
  styleUrls: ['./explorer.component.scss'],
})
export class ExplorerComponent implements AfterViewInit {
  
  protected $loading: boolean = true;
  protected lbNextStageSubject = new Subject<void>();

  constructor(
    private readonly cd: ChangeDetectorRef,
    private readonly router: Router,
    private readonly store: Store,
    private readonly configService: ConfigService,
    private readonly jwtAuthService: JwtAuthenticationService
  ) {}

  ngAfterViewInit(): void {
    this.lbNextStageSubject.next();
    // 1. Loading App Global Configuration configuration
    this.configService.load()
    .pipe(
      catchError((err) => {
        // nav to error page
        this.router.navigateByUrl('/error', { state: { title: 'fatal error', text: 'an irrecoverable error occured' } } );
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
        // timeout the splash disapearance for smoothing loading bar animation
        setTimeout(() => {
          this.$loading = false;
        }, 600);
      })
    });
  }
}
