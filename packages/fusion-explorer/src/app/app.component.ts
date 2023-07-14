import { Component, OnInit, enableProdMode } from '@angular/core'
import { environment } from '@environments/environment'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {

  ngOnInit(): void {
    if (environment.production === true) {
      enableProdMode()
    }
  }
}
