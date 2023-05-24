import { Component, Input } from '@angular/core';
import { Color, Icon } from '@shared/constants/assets';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'shared-splash',
  templateUrl: './splash.component.html',
  styleUrls: ['./splash.component.scss'],
})
export class SplashComponent {
  fusionIcon: Icon = Icon.AGAMIS_FUSION_LOGO;
  Color: typeof Color = Color;

  @Input()
  nextStageSignalSubject!: BehaviorSubject<void>;
}
