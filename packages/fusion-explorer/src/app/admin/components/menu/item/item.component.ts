import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { Icon } from '@shared/constants/assets';

@Component({
  selector: 'admin-menu-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss'],
})
export class ItemComponent implements AfterViewInit {
  @Input()
  text: string = "undefined text";
  @Input()
  icon: {
    key: Icon,
    height: string
  } = {
    key: Icon.QUESTION_LINE,
    height: '16px',
  };

  protected isActive: boolean = false;

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
