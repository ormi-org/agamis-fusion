import { Component } from '@angular/core';
import { ActivationStart, Event, Router } from '@angular/router';
import { LoadingService } from '@explorer/utils/loading/loading.service';
import { Stage } from '@shared/components/loading-bar/models/stage.model';
import { Direction } from '@shared/components/separator/models/enums/direction.enum';
import { Color, Icon } from '@shared/constants/assets';

const LOADING_BAR_STAGES: Stage[] = [
  {
    fill: 0,
    autoNext: 100,
  },
  {
    fill: 10,
    autoNext: 300,
    fillDuration: 200,
  },
  {
    fill: 90,
    fillDuration: 5000,
  },
  {
    fill: 100,
    fillDuration: 200,
  }
];

@Component({
  selector: 'explorer-browser',
  templateUrl: './browser.component.html',
  styleUrls: ['./browser.component.scss'],
})
export class BrowserComponent {
  protected readonly Direction: typeof Direction = Direction;
  protected readonly Color: typeof Color = Color;
  protected readonly fusionIcon: Icon = Icon.AGAMIS_FUSION_LOGO;
  protected readonly loadingBarStages: Stage[] = LOADING_BAR_STAGES;

  constructor(
    router: Router,
    protected readonly loadingService: LoadingService,
  ) {
    router.events.subscribe((e: Event) => {
      if (e instanceof ActivationStart) {
        this.loadingService.complete();
      }
    });
  }
}
