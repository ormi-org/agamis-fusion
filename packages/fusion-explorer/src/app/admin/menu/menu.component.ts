import { AfterContentInit, AfterViewInit, Component, ContentChildren, ElementRef, Input, OnInit, QueryList, ViewChild, ViewChildren, forwardRef } from '@angular/core';
import { Icon } from '@shared/constants/assets';
import { Path } from '@shared/constants/paths';
import { ItemComponent } from './item/item.component';
import { startWith } from 'rxjs';

@Component({
  selector: 'admin-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
})
export class MenuComponent implements AfterViewInit {
  icons: typeof Icon = Icon;
  iconRelativePath: string = [Path.ASSETS, Path.ICONS, Icon.USER_LOCK].join('/');
  tailIconRelativePath: string = [Path.ASSETS, Path.ICONS, Icon.ARROW].join('/');

  @Input() text: string = "Undefined text";

  @Input() isCollapsed?: boolean = true;

  @ViewChild('menuBody')
  menuBody!: ElementRef<HTMLElement>;

  @ViewChildren('menuItem')
  menuItems!: QueryList<ItemComponent>;

  ngAfterViewInit(): void {
    this.menuBody.nativeElement.style.maxHeight = this.menuItems.toArray().reduce((acc, i) => acc + i.getHeight(), 0) + 'px';
  }

  protected toggleCollapse(): void {
    this.isCollapsed = !this.isCollapsed;
  }
}
