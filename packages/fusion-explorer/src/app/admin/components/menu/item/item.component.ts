import { AfterViewInit, Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Icon } from '@shared/constants/assets';

@Component({
  selector: 'admin-menu-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss'],
})
export class ItemComponent implements AfterViewInit {
  @Input()
  text = "undefined text";
  @Input()
  icon: {
    key: Icon,
    height: string
  } = {
    key: Icon.QUESTION_LINE,
    height: '16px',
  };
  @Input()
  link?: string[];

  private nativeElement: HTMLElement;

  constructor(element: ElementRef) {
    this.nativeElement = element.nativeElement;
  }

  @ViewChild('icon')
  iconInstance!: ElementRef<HTMLElement>;

  ngAfterViewInit(): void {
    this.iconInstance.nativeElement.style.height = this.icon.height;
  }

  getHeight(): number {
    return this.nativeElement.offsetHeight;
  }

  protected getIcon(): Icon {
    return this.icon.key;
  }
}
