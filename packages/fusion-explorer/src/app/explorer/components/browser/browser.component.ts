import { Component, OnInit } from '@angular/core';
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
    fill: 70,
  },
  {
    fill: 90,
    autoNext: 100,
    fillDuration: 5000
  },
  {
    fill: 100
  }
];

@Component({
  selector: 'explorer-browser',
  templateUrl: './browser.component.html',
  styleUrls: ['./browser.component.scss'],
})
export class BrowserComponent implements OnInit {
  protected Direction: typeof Direction = Direction;
  protected Color: typeof Color = Color;
  protected fusionIcon: Icon = Icon.AGAMIS_FUSION_LOGO;
  protected loadingBarStages: Stage[] = LOADING_BAR_STAGES;

  constructor(protected readonly loadingService: LoadingService) {}

  ngOnInit(): void {
    // throw new Error('Method not implemented.');
  }
}
