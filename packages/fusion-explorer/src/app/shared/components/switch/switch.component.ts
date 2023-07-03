import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Color } from '@shared/constants/assets';

@Component({
  selector: 'shared-switch',
  templateUrl: './switch.component.html',
  styleUrls: ['./switch.component.scss'],
})
export class SwitchComponent {
  @Input()
  trueValue = '';
  @Input()
  trueColor = Color.SUCCESS;
  @Input()
  falseValue = '';
  @Input()
  falseColor = Color.ERROR;
  @Input()
  value = false;

  @Output()
  // eslint-disable-next-line @angular-eslint/no-output-native
  change = new EventEmitter<boolean>();

  onSwitch(): void {
    this.value = !this.value;
    this.change.emit(this.value);
  }
}
