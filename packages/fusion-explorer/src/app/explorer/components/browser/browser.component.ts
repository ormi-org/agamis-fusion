import { Component } from '@angular/core';
import { Direction } from '@shared/components/separator/models/enums/direction.enum';
import { Color, Icon } from '@shared/constants/assets';

@Component({
  selector: 'app-browser',
  templateUrl: './browser.component.html',
  styleUrls: ['./browser.component.scss'],
})
export class BrowserComponent {
  protected Direction: typeof Direction = Direction;
  protected Color: typeof Color = Color;
  protected fusionIcon: Icon = Icon.AGAMIS_FUSION_LOGO;
}
