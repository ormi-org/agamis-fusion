import { Pipe, PipeTransform } from '@angular/core';
import { Icon } from '@shared/constants/assets';
import { Path } from '@shared/constants/paths';

@Pipe({
  name: 'iconify'
})
export class IconifyPipe implements PipeTransform {

  transform(value: Icon): string {
    return [Path.ASSETS, Path.ICONS, value].join('/');
  }

}
