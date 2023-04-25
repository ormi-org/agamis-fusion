import { Component, Input } from '@angular/core';
import { Icon } from '@shared/constants/assets';
import { Path } from '@shared/constants/paths';

@Component({
  selector: 'admin-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
})
export class MenuComponent {
  icons: typeof Icon = Icon;
  iconRelativePath: string = [Path.ASSETS, Path.ICONS, Icon.USER_LOCK].join('/');
  tailIconRelativePath: string = [Path.ASSETS, Path.ICONS, Icon.ARROW].join('/');

  @Input() text: string = "Undefined text";

  isCollapsed: boolean = true;

  protected toggleCollapse(): void {
    this.isCollapsed = !this.isCollapsed;
    console.log(this.isCollapsed);
  }
}
