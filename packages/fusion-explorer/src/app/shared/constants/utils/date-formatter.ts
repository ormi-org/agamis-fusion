const MS_PER_MIN = 60 * 1000
const MS_PER_HOUR = 60 * MS_PER_MIN
const MS_PER_DAY = 24 * MS_PER_HOUR

export class DateFormatter {
    static formatToTimeDiffLimit(date: Date): string {
        const currentDate = new Date()
        const diff = currentDate.getTime() - date.getTime()
        // more than a year
        if (diff > 365 * MS_PER_DAY) {
            return $localize`:@@ui.classic.shared.const.date.more-than-a-year:more than a year`
        }
        // count in month
        if (diff > 365/12 * MS_PER_DAY - 1) {
            const nbrOfMonths = Math.round(diff / (365/12 * MS_PER_DAY))
            // round to year
            if (nbrOfMonths === 12) {
                return $localize`:@@ui.classic.shared.const.date.one-year-ago:1 year ago`
            }
            if (nbrOfMonths === 1) {
                return $localize`:@@ui.classic.shared.const.date.a-month-ago:last month`
            }
            return $localize`:@@ui.classic.shared.const.date.n-months-ago:%n-months% months ago`
            .replace('%n-months%', nbrOfMonths.toString())
        }
        // count in weeks
        if (diff > 365/52 * MS_PER_DAY) {
            const nbrOfWeeks = Math.round(diff / (365/52 * MS_PER_DAY))
            if (nbrOfWeeks === 1) {
                return $localize`:@@ui.classic.shared.const.date.a-week-ago:last week`
            }
            return $localize`:@@ui.classic.shared.const.date.n-weeks-ago:%n-weeks% weeks ago`
            .replace('%n-weeks%', nbrOfWeeks.toString())
        }
        // count in days
        if (diff >= MS_PER_DAY) {
            const nbrOfDays = Math.round(diff / MS_PER_DAY)
            if (nbrOfDays === 1) {
                return $localize`:@@ui.classic.shared.const.date.a-day-ago:yesterday`
            }
            return $localize`:@@ui.classic.shared.const.date.n-days-ago:%n-days% days ago`
            .replace('%n-days%', nbrOfDays.toString())
        }
        // count in hours
        if (diff >= MS_PER_HOUR) {
            const nbrOfHours = Math.round(diff / MS_PER_HOUR)
            if (nbrOfHours === 1) {
                return $localize`:@@ui.classic.shared.const.date.an-hour-ago:an hour ago`
            }
            return $localize`:@@ui.classic.shared.const.date.n-hours-ago:%n-hours% hours ago`
            .replace('%n-hours%', nbrOfHours.toString())
        }
        // count in min
        if (diff > MS_PER_MIN) {
            const nbrOfMinutes = Math.round(diff / MS_PER_MIN)
            return $localize`:@@ui.classic.shared.const.date.n-minutes-ago:%n-minutes% minutes ago`
            .replace('%n-minutes%', nbrOfMinutes.toString())
        }
        // else 'a minute ago'
        return $localize`:@@ui.classic.shared.const.date.a-minute-ago:a minute ago`
    }
}