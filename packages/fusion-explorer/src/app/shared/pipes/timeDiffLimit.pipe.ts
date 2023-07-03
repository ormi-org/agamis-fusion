import { Pipe, PipeTransform } from "@angular/core";
import { DateFormatter } from "@shared/constants/utils/date-formatter";

@Pipe({
    name: 'timeDiffLimit'
})
export class TimeDiffLimitPipe implements PipeTransform {
    transform(value: Date) {
        return DateFormatter.formatToTimeDiffLimit(value);
    }
}