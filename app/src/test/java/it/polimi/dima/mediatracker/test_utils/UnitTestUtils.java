package it.polimi.dima.mediatracker.test_utils;

import java.util.Calendar;

/**
 * Some utilities for unit testing
 */
public class UnitTestUtils
{
    /**
     * Util to get a random name
     */
    public static String getRandomName()
    {
        // Take the current time in milliseconds, should be unique...!
        return String.valueOf(Calendar.getInstance().getTimeInMillis());
    }
}
