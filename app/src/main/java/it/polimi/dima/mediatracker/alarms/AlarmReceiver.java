package it.polimi.dima.mediatracker.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import it.polimi.dima.mediatracker.controllers.NotificationsManager;

/**
 * Receives all alarms set up by the application and Android's BootCompleted alarm to restart them
 */
public class AlarmReceiver extends BroadcastReceiver
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Get wakelock (need to keep device awake)
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getSimpleName());
        try
        {
            wakeLock.acquire();

            // If we have an action...
            if(intent.getAction()!=null)
            {
                // If the device just restarted...
                if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
                {
                    // Start all alarms
                    AlarmScheduler.getInstance(context).startAllAlarms();
                }

                // If it's the new releases alarm...
                else if(intent.getAction().equals(AlarmScheduler.NEW_RELEASES_NOTIFICATIONS_ALARM_ACTION))
                {
                    // Send notifications if needed
                    NotificationsManager.getInstance(context).sendNewReleasesNotifications();
                }
            }
        }
        finally
        {
            // Release wakelock
            wakeLock.release();
        }
    }
}
