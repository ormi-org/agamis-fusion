import { DateFormatter } from "./date-formatter";

const MS_PER_MIN = 60 * 1000;
const MS_PER_HOUR = 60 * MS_PER_MIN;
const MS_PER_DAY = 24 * MS_PER_HOUR;

describe('DateFormatter', () => {

  it('formatToTimeDiffLimit(Date)#should return MTAY when number of diffday is > 365', () => {
    const expected = 'more than a year';
    const dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 366);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);
  });

  it('formatToTimeDiffLimit(Date)#should return number of months when number of diffday is <= 365 and > 30', () => {
    var expected = '1 year ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/12 * 11.6);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = '11 months ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/12 * 11.2);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = '11 months ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/12 * 11);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = '2 months ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/12 * 2);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = 'last month';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/12 * 1);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);
  });

  it('formatToTimeDiffLimit(Date)#should return number of weeks when number of diffday is < 30 and >= 7', () => {
    var expected = '4 weeks ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/52 * 4);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/52 * 3.6);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = '3 weeks ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/52 * 3.2);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = 'last week';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 365/52);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);
  });

  it('formatToTimeDiffLimit(Date)#should return number of days when number of diffday is < 7 and >= 1', () => {
    var expected = '6 days ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 6);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 6.4);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = '3 days ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY * 2.5);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = 'yesterday';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_DAY);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);
  });

  it('formatToTimeDiffLimit(Date)#should return number of hours when number of diffhour is < 24 and >= 1', () => {
    var expected = '23 hours ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_HOUR * 23);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var dateToCompare = new Date(new Date().getTime() - MS_PER_HOUR * 23.3);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = '3 hours ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_HOUR * 2.5);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = 'an hour ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_HOUR);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);
  });

  it('formatToTimeDiffLimit(Date)#should return number of minutes when number of diffmin is < 60', () => {
    var expected = '59 minutes ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_MIN * 59);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var dateToCompare = new Date(new Date().getTime() - MS_PER_MIN * 59.2);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = '10 minutes ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_MIN * 10);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);

    var expected = 'a minute ago';
    var dateToCompare = new Date(new Date().getTime() - MS_PER_MIN);

    expect(DateFormatter.formatToTimeDiffLimit(dateToCompare))
    .toBe(expected);
  });
});
