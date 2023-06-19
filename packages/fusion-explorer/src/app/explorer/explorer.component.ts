import { Component } from '@angular/core';
import { SplashService } from '@core/services/utils/splash/splash.service';

@Component({
  selector: 'explorer',
  templateUrl: './explorer.component.html',
  styleUrls: ['./explorer.component.scss'],
})
export class ExplorerComponent {

  constructor(protected readonly splashService: SplashService) {
    // reset loading-bar
    splashService.reset();
  }
}
