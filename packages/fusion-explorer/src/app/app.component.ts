import { Component, OnInit, enableProdMode, isDevMode } from '@angular/core';
import { environment } from '@environments/environment';
import { v1 as mockRestServerV1 } from '@mocks/server/rest';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  title = 'Fusion explorer';

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
  }
}
