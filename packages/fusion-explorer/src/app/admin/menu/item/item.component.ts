import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { Icon } from '@shared/constants/assets';
import { Path } from '@shared/constants/paths';

@Component({
  selector: 'admin-menu-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss'],
})
export class ItemComponent implements OnInit, AfterViewInit {
  @Input() isActive: boolean = false;
  @Input() text: string = "undefined text";
  @Input() icon: {
    key: Icon,
    height: string
  } = {
    key: Icon.QUESTION_LINE,
    height: '16px',
  };

  iconRelativePath!: string;
  isSelected: boolean = false;
  @ViewChild('icon')
  iconInstance!: ElementRef<HTMLElement>;

  ngOnInit(): void {
    this.iconRelativePath = [Path.ASSETS, Path.ICONS, this.icon.key].join('/');
  }

  ngAfterViewInit(): void {
    this.iconInstance.nativeElement.style.height = this.icon.height;
  }
}
