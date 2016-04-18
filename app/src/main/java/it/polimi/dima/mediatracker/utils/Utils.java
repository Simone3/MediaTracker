package it.polimi.dima.mediatracker.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.ScreenController;

/**
 * Misc utility methods
 */
public class Utils
{
    /**
     * Translates and iterator to a list
     * @param iterator the iterator
     * @param <T> the type of objects in the iterator
     * @return the list with all the iterator elements
     */
    public static<T> List<T> iteratorToList(Iterator<T> iterator)
    {
        List<T> copy = new ArrayList<>();
        while(iterator.hasNext()) copy.add(iterator.next());
        return copy;
    }

    /**
     * Translates two lists into an hashmap
     * @param keys list of keys
     * @param values list of values
     * @param <K> keys type
     * @param <V> values type
     * @return the hashmap
     */
    public static<K, V> HashMap<K, V> listsToHashMap(List<K> keys, List<V> values)
    {
        HashMap<K, V> result = new HashMap<>();

        if(keys==null || values==null || keys.size()!=values.size()) return result;

        for(int i=0; i<keys.size(); i++)
        {
            result.put(keys.get(i), values.get(i));
        }
        return result;
    }

    /**
     * Translates a string to a calendar instance using the provided format
     * @param dateString the string containing the date
     * @param format the format of the date in the string
     * @return the calendar representing the given date
     */
    private static Calendar parseCalendarFromString(String dateString, String format)
    {
        if(dateString==null || "".equals(dateString)) return null;

        try
        {
            Calendar calendar = Calendar.getInstance();
            Date date = (new SimpleDateFormat(format, Locale.ENGLISH)).parse(dateString);
            calendar.setTime(date);
            return calendar;
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Translates a string to a date using the provided format
     * @param dateString the string containing the date
     * @param format the format of the date in the string
     * @return the date
     */
    public static Date parseDateFromString(String dateString, String format)
    {
        Calendar calendar = parseCalendarFromString(dateString, format);
        if(calendar==null) return null;
        else return calendar.getTime();
    }

    /**
     * Similar to {@link Utils#parseDateFromString(String, String)} but returns only the year of the date
     */
    public static int parseYearFromString(String dateString, String format)
    {
        Calendar calendar = parseCalendarFromString(dateString, format);
        if(calendar==null) return 0;
        else return calendar.get(Calendar.YEAR);
    }

    /**
     * Creates a date object from year, month and day values
     * @param year the year (if 0 the returned date will be null)
     * @param month the month (can be 0 if unknown)
     * @param day the day (can be 0 if unknown)
     * @return the date
     */
    public static Date parseDateFromYearMonthDay(int year, int month, int day)
    {
        // Must have at least the year
        if(year<=0) return null;

        // Set values
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        // Return the date
        return calendar.getTime();
    }

    /**
     * Wrapper for {@link android.text.TextUtils#join(CharSequence, Object[])} that first checks if the token array is empty
     */
    public static String joinIfNotEmpty(CharSequence delimiter, Object[] tokens)
    {
        if(tokens!=null && tokens.length > 0) return TextUtils.join(delimiter, tokens);
        else return "";
    }

    /**
     * Check if string is null or empty
     * @param string the string to check
     * @return true if null or ""
     */
    public static boolean isEmpty(String string)
    {
        return string==null || string.isEmpty();
    }

    /**
     * Computes the average of the given integer array
     * @param array the array
     * @return the average value in the array
     */
    public static int arrayAverage(int[] array)
    {
        float average = 0;
        if(array!=null)
        {
            for(int v: array) average += v;
            average = average/array.length;
        }
        return Math.round(average);
    }

    /**
     * Wrapper for {@link java.lang.Integer#parseInt(String)} that returns 0 in case of exception
     */
    public static int parseInt(String s)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch(Exception e)
        {
            return 0;
        }
    }

    /**
     * Runtime permissions request for API>=23
     * @param activity the requesting activity
     * @param permission the permission
     * @param permissionRequestLayout the main layout component displayed on screen
     * @param permissionReminder the string shown as reminder if the user didn't accept the permission
     * @param requestCode the request code
     */
    public static void askPermissionsAtRuntimeIfNeeded(final Activity activity, final String permission, View permissionRequestLayout, final int permissionReminder, final int requestCode)
    {
        // If we don't already have the permission...
        if(ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED)
        {
            // If the user previously refused to grant the permission (and didn't set "Don't ask again")...
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            {
                // Show a message as a reminder
                Snackbar.make(permissionRequestLayout, activity.getString(permissionReminder, activity.getString(R.string.app_name)), Snackbar.LENGTH_INDEFINITE)
                        .setAction(activity.getString(R.string.permission_request_agree), new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                // If the user clicks on Agree, ask permission again
                                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                            }
                        }).show();
            }

            // If it's the first time we get here...
            else
            {
                // Ask permission
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        }
    }

    /**
     * Helper to manage a menu option selection
     * @param activity the activity
     * @param id the ID of the option selected by the user
     * @return true if this method managed the option, false otherwise
     */
    public static boolean manageToolbarMenuSelection(Activity activity, int id)
    {
        // Open settings
        if(id == R.id.action_settings)
        {
            Intent intent = ScreenController.getSettingsIntent(activity);
            activity.startActivity(intent);
            return true;
        }

        // Show credits
        if(id == R.id.action_credits)
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_credits, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle(R.string.credits);
            AlertDialog builder = dialogBuilder.create();
            builder.show();
            return true;
        }

        return false;
    }

    /**
     * Random integer between the two given values
     * @param min min value (included)
     * @param max max value (included)
     * @return a random integer
     */
    public static int randInt(int min, int max)
    {
        return (new Random()).nextInt((max - min) + 1) + min;
    }
}
