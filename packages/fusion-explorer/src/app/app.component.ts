import { Component, OnDestroy, OnInit, enableProdMode, isDevMode } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderTitleItem } from '@core/models/header-title-item.model';
import { ConfigService } from '@core/services/config/config.service';
import { selectOrganization } from '@core/states/app-state/app-state.selectors';
import { environment } from '@environments/environment';
import { v1 as mockRestServerV1 } from '@mocks/server/rest';
import { Store } from '@ngrx/store';
import { BehaviorSubject, Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'fusion-explorer';

  private loadingSubject = new BehaviorSubject<boolean>(true);
  protected $loading: boolean = this.loadingSubject.value;

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
  ])
  protected headerTitle = this.headerTitleSubject.value;

  constructor(
    private readonly router: Router,
    private readonly store: Store,
    private readonly configService: ConfigService
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
    if (environment.production === true) {
      enableProdMode();
    }
    if (isDevMode()) {
      if (environment.enableMock === true) {
        // enable mocking rest server
        mockRestServerV1();
      }
    }
    const originalRoute = this.router.url;
    this.store.select(selectOrganization).subscribe((org) => {
      if (org?.name) {
        this.onOrgNameUpdate(org.name);
      }
      if (org?.id) {
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
    this.configService.load().then(
      () => {
        // nav to original ui; after successful config retrieval
        this.router.navigate([originalRoute]);
      },
      (err: Error) => {
        // nav to error page
        this.router.navigate(['/error']);
      }
    );
  }

  ngOnDestroy(): void {
    this.loadingSubject.complete();
  }
}
