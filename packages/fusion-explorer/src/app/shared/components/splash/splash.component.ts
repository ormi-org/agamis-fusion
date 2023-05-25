import { Component, Input } from '@angular/core';
import { Color, Icon } from '@shared/constants/assets';
import { BehaviorSubject, Subject } from 'rxjs';
import { Stage as LoadingBarStage } from '../loading-bar/models/stage.model';

@Component({
  selector: 'shared-splash',
  templateUrl: './splash.component.html',
  styleUrls: ['./splash.component.scss'],
})
export class SplashComponent {
  protected fusionIcon: Icon = Icon.AGAMIS_FUSION_LOGO;
  protected loadingBarStages: LoadingBarStage[] = [
    {
      fill: 0,
      autoNext: 500
    },
    {
      fill: 13,
      fillDuration: 100
    },
    {
      fill: 87,
      fillDuration: 5000
    },
    {
      fill: 100,
      fillDuration: 300
    }
  ];

  @Input()
  nextStageSignalSubject!: Subject<void>;
}
