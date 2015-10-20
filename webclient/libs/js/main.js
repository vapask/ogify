/**
 * Created by melge on 12.07.2015.
 */

/**
 * Parse date in format dd.mm.yyyy and time hh:mm
 */
function parseDate(date, time) {
    dateItems = date.split('.');
    timeItems = time.split(':');

    return new Date(dateItems[2], dateItems[1] - 1, dateItems[0], timeItems[0], timeItems[1], 0, 0);
}