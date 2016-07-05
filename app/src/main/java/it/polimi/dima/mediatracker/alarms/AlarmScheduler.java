package it.polimi.dima.mediatracker.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Date;

import it.polimi.dima.mediatracker.controllers.SettingsManager;
import it.polimi.dima.mediatracker.utils.GlobalConstants;

/**
 * Manages the creation of the application's alarms, e.g. for the new releases notifications
 */
public class AlarmScheduler
{
    final static String NEW_RELEASES_NOTIFICATIONS_ALARM_ACTION = "NEW_RELEASES_NOTIFICATIONS_ALARM_ACTION";
    private final static String NEW_RELEASES_NOTIFICATIONS_ALARM_NAME = "NEW_RELEASES_NOTIFICATIONS_ALARM_NAME";

    private static AlarmScheduler instance = null;
    private Context appContext;
    private SettingsManager settingsManager;

    /**
     * Private constructor
     * @param context the context
     */
    private AlarmScheduler(Context context)
    {
        this.appContext = context.getApplicationContext();
        settingsManager = SettingsManager.getInstance(context);
    }

    /**
     * Singleton pattern
     * @param context the activity context
     */
    public synchronized static AlarmScheduler getInstance(Context context)
    {
        if(instance==null) instance = new AlarmScheduler(context);
        return instance;
    }

    /**
     * Helper to setup a single (non-repeating) alarm
     * @param date the date for the alarm to schedule
     * @param action the action name (the one used by AlarmReceiver to understand the alarm type)
     * @param uniqueName the alarm unique name
     */
    private void scheduleSingleAlarm(Date date, String action, String uniqueName)
    {
        // Get alarm manager
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        // Build intent for the AlarmReceiver
        PendingIntent pendingIntent = buildPendingIntentForAlarms(action, uniqueName);

        // Schedule the alarm at the given date
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
    }

    /**
     * Helper to setup a repeating alarm
     * @param date the date for the alarm to schedule
     * @param intervalMilliseconds the interval in milliseconds between two calls of the alarm
     * @param action the action name (the one used by AlarmReceiver to understand the alarm type)
     * @param uniqueName the alarm unique name
     */
    private void scheduleRepeatingAlarm(Date date, long intervalMilliseconds, String action, String uniqueName)
    {
        // Get alarm manager
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        // Build intent for the AlarmReceiver
        PendingIntent pendingIntent = buildPendingIntentForAlarms(action, uniqueName);

        // Schedule the alarm at the given date
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), intervalMilliseconds, pendingIntent);
    }

    /**
     * Helper to remove an alarm
     * @param action the action name (the one used by AlarmReceiver to understand the alarm type)
     * @param uniqueName the alarm unique name
     */
    private void removeAlarm(String action, String uniqueName)
    {
        // Get alarm manager
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        // Build intent for the AlarmReceiver
        PendingIntent pendingIntent = buildPendingIntentForAlarms(action, uniqueName);

        // Remove any alarm that matches the pending intent
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Helper to build an alarm pending intent
     * @param action the action name (the one used by AlarmReceiver to understand the alarm type)
     * @param uniqueName the alarm unique name
     * @return the pending intent for the alarm
     */
    private PendingIntent buildPendingIntentForAlarms(String action, String uniqueName)
    {
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        intent.setType(uniqueName);
        intent.setAction(action);
        return PendingIntent.getBroadcast(appContext, 0, intent, 0);
    }

    /**
     * Starts the alarm for the new releases notifications, using the preferences given by the SettingsManager
     */
    public void startNewReleasesAlarm()
    {
        // Get time for the notifications
        SettingsManager settingsManager = SettingsManager.getInstance(appContext);
        int hour = settingsManager.getNewReleasesNotificationHour();
        int minutes = settingsManager.getNewReleasesNotificationMinutes();

        // Set calendar date to today at the given time
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        // If it's already past, set it to tomorrow
        if(now.after(calendar.getTime()))
        {
            calendar.add(Calendar.DATE, 1);
        }

        // Setup repeating alarm
        scheduleRepeatingAlarm(calendar.getTime(), GlobalConstants.MILLISECONDS_IN_DAY, NEW_RELEASES_NOTIFICATIONS_ALARM_ACTION, NEW_RELEASES_NOTIFICATIONS_ALARM_NAME);
    }

    /**
     * Stops the alarm for the new releases notifications
     */
    public void stopNewReleasesAlarm()
    {
        removeAlarm(NEW_RELEASES_NOTIFICATIONS_ALARM_ACTION, NEW_RELEASES_NOTIFICATIONS_ALARM_NAME);
    }

    /**
    * Updates the alarm for the new releases notifications, using the new preferences given by the SettingsManager
    */
    public void updateNewReleasesAlarm()
    {
        stopNewReleasesAlarm();
        startNewReleasesAlarm();
    }

    /**
     * Starts all the application's alarms, to be called e.g. at first run or after boot
     */
    public void startAllAlarms()
    {
        if(settingsManager.areNewReleasesNotificationsActive())
        {
            startNewReleasesAlarm();
        }
    }
}
