import { AfterViewInit, Component, ElementRef, Input, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { Icon } from '@shared/constants/assets';
import { ItemComponent } from './item/item.component';

@Component({
  selector: 'admin-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
})
export class MenuComponent implements AfterViewInit {
  icons: typeof Icon = Icon;
  icon: Icon = Icon.USER_LOCK;
  tailIcon: Icon = Icon.ARROW;

  @Input() text: string = "Undefined text";

  @Input() isCollapsed?: boolean = true;

  @ViewChild('menuBody')
  menuBody!: ElementRef<HTMLElement>;

  @ViewChildren(ItemComponent)
  menuItems!: QueryList<ItemComponent>;

  ngAfterViewInit(): void {
    this.menuBody.nativeElement.style.maxHeight = this.menuItems.toArray().reduce((acc, i) => acc + i.getHeight(), 0) + 'px';
  }

  protected toggleCollapse(): void {
    this.isCollapsed = !this.isCollapsed;
  }
}
