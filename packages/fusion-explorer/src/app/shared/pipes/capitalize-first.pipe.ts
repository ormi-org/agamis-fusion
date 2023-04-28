import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: 'capitalizeFirst'
})
export class CapitalizeFirstPipe implements PipeTransform {
    transform(value: string, ...args: any[]) {
        if (typeof value != 'string') return 'Value is not a string';
        if (value === undefined || value === null) return 'Value not assigned';
        return value.charAt(0).toUpperCase() + value.slice(1);
    }
}