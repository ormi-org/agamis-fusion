import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Icon } from '@shared/constants/assets'
import { ItemComponent } from './item/item.component'

@Component({
  selector: 'admin-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
})
export class MenuComponent implements AfterViewInit {
  protected readonly icons: typeof Icon = Icon
  protected isCollapsed = true

  @Input() text = 'Undefined text'

  @Input()
  height = 0

  @ViewChild('menuBody')
  menuBody!: ElementRef<HTMLElement>

  @ViewChildren(ItemComponent)
  menuItems!: QueryList<ItemComponent>

  constructor(route: ActivatedRoute) {
    const currentRoute = route.toString()
    this.isCollapsed = currentRoute.includes('/admin')
  }

  ngAfterViewInit(): void {
    this.menuBody.nativeElement.style.maxHeight =
      this.menuItems.toArray().reduce((acc, i) => acc + i.getHeight(), 0) +
      this.height +
      'px'
  }

  protected toggleCollapse(): void {
    this.isCollapsed = !this.isCollapsed
  }
}
