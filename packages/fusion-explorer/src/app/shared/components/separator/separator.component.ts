import { AfterContentInit, Component, HostBinding, Input, OnInit } from '@angular/core';
import { Direction } from './models/enums/direction.enum';

@Component({
  selector: 'shared-separator',
  templateUrl: './separator.component.html',
  styleUrls: ['./separator.component.scss'],
})
export class SeparatorComponent implements AfterContentInit {
  @Input()
  direction!: Direction;
  @Input()
  thickness!: number;
  @Input()
  color!: string;
  @Input()
  bordering!: number;

  protected paddingTop!: number;
  protected paddingBot!: number;
  protected paddingLeft!: number;
  protected paddingRight!: number;

  @HostBinding('style.height')
  @HostBinding('style.min-height')
  @HostBinding('style.max-height')
  protected height!: string;
  @HostBinding('style.width')
  @HostBinding('style.min-width')
  @HostBinding('style.max-width')
  protected width!: string;

  ngAfterContentInit(): void {
    switch (this.direction) {
      case Direction.VERTICAL:
        this.paddingBot = this.paddingTop = this.bordering;
        this.height = 100+'%';
        this.width = this.thickness+'px';
        break;
      case Direction.HORIZONTAL:
        this.paddingLeft = this.paddingRight = this.bordering;
        this.height = this.thickness+'px';
        this.width = 100+'%';
        break;
    } 
  }
}
