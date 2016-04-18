package it.polimi.dima.mediatracker.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import it.polimi.dima.mediatracker.R;

/**
 * Manages access to the application settings (it's just a wrapper for the built-in PreferenceManager)
 */
public class SettingsManager
{
    private final static int DEFAULT_NEW_RELEASES_NOTIFICATION_HOUR = 20;
    private final static int DEFAULT_NEW_RELEASES_NOTIFICATION_MINUTES = 0;
    private final static boolean DEFAULT_RECEIVE_NOTIFICATIONS = true;
    private final static boolean DEFAULT_NOTIFICATIONS_VIBRATE = false;
    private final static String DEFAULT_NOTIFICATIONS_SOUND = null;

    private static SettingsManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    /**
     * Private constructor
     * @param context the context
     */
    private SettingsManager(Context context)
    {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = this.sharedPreferences.edit();
        this.context = context;
    }

    /**
     * Singleton pattern
     */
    public static synchronized SettingsManager getInstance(Context context)
    {
        if(instance == null) instance = new SettingsManager(context);
        return instance;
    }



    /************************************************ GETTERS ************************************************/


    /**
     * Getter
     * @return true if it's the first application run
     */
    public boolean isFirstRun()
    {
        return sharedPreferences.getBoolean(context.getString(R.string.key_first_run), true);
    }

    /**
     * Getter
     * @return the hour for new releases notifications
     */
    public int getNewReleasesNotificationHour()
    {
        return sharedPreferences.getInt(context.getString(R.string.key_new_releases_notification_hour), DEFAULT_NEW_RELEASES_NOTIFICATION_HOUR);
    }

    /**
     * Getter
     * @return the minutes for new releases notifications
     */
    public int getNewReleasesNotificationMinutes()
    {
        return sharedPreferences.getInt(context.getString(R.string.key_new_releases_notification_minutes), DEFAULT_NEW_RELEASES_NOTIFICATION_MINUTES);
    }

    /**
     * Getter
     * @return true if the user wants notifications for new releases
     */
    public boolean areNewReleasesNotificationsActive()
    {
        return sharedPreferences.getBoolean(context.getString(R.string.key_receive_new_releases_notifications), DEFAULT_RECEIVE_NOTIFICATIONS);
    }

    /**
     * Getter
     * @return true if device needs to vibrate at notifications
     */
    private boolean getNotificationsVibrate()
    {
        return sharedPreferences.getBoolean(context.getString(R.string.key_notifications_vibrate), DEFAULT_NOTIFICATIONS_VIBRATE);
    }

    /**
     * Getter
     * @return the pattern for notifications vibrations (empty array if no vibration)
     */
    public long[] getNotificationsVibratePattern()
    {
        if(!getNotificationsVibrate())
        {
            return new long[0];
        }
        else
        {
            // Start immediately (0) and vibrate for 200 milliseconds, then stop
            return new long[]{0, 200};
        }
    }

    /**
     * Getter
     * @return notifications sound file Uri
     */
    public Uri getNotificationsSound()
    {
        return Uri.parse(sharedPreferences.getString(context.getString(R.string.key_notifications_sound), DEFAULT_NOTIFICATIONS_SOUND));
    }



    /************************************************ SETTERS ************************************************/


    /**
     * Setter
     * @param isFirstRun true if it's the first application run
     */
    public void setFirstRun(boolean isFirstRun)
    {
        editor.putBoolean(context.getString(R.string.key_first_run), isFirstRun);
        editor.commit();
    }

    /**
     * Setter
     * @param hour the hour for new releases notifications
     */
    public void setNewReleasesNotificationHour(int hour)
    {
        editor.putInt(context.getString(R.string.key_new_releases_notification_hour), hour);
        editor.commit();
    }

    /**
     * Setter
     * @param minutes the minutes for new releases notifications
     */
    public void setNewReleasesNotificationMinutes(int minutes)
    {
        editor.putInt(context.getString(R.string.key_new_releases_notification_minutes), minutes);
        editor.commit();
    }
}
