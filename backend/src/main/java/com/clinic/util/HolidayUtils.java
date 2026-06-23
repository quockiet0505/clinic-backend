package com.clinic.util;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

public class HolidayUtils {
    
    // Hardcoded fixed solar holidays in Vietnam
    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
        MonthDay.of(1, 1),   // New Year's Day
        MonthDay.of(4, 30),  // Liberation Day
        MonthDay.of(5, 1),   // International Labor Day
        MonthDay.of(9, 2)    // National Day
    );

    /**
     * Checks if a given date is a fixed holiday.
     * Note: This only checks fixed solar calendar holidays.
     * Lunar holidays (e.g., Tet) need to be handled via LeaveRequest or a custom calendar.
     */
    public static boolean isHoliday(LocalDate date) {
        if (date == null) return false;
        MonthDay monthDay = MonthDay.from(date);
        return FIXED_HOLIDAYS.contains(monthDay);
    }
}
