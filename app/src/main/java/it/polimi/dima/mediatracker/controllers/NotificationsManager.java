package it.polimi.dima.mediatracker.controllers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Iterator;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Manages all the application notifications
 */
public class NotificationsManager
{
    private Context context;
    private NotificationManager notificationManager;
    private SettingsManager settingsManager;

    private static NotificationsManager instance;

    /**
     * Private constructor
     * @param context the context
     */
    private NotificationsManager(Context context)
    {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        settingsManager = SettingsManager.getInstance(context);
    }

    /**
     * Singleton pattern
     */
    public static synchronized NotificationsManager getInstance(Context context)
    {
        if(instance==null) instance = new NotificationsManager(context);
        return instance;
    }

    /**
     * Queries the database for media items released today and, if any, sends a notification for each media type
     */
    public void sendNewReleasesNotifications()
    {
        Category category;
        List<MediaItem> releasedToday;
        String[] itemTitles;
        String content;
        PendingIntent notificationClickPendingIntent;
        Intent notificationClickIntent;

        // Loop all categories
        Iterator<Category> categories = CategoriesController.getInstance().getAllCategories();
        while(categories.hasNext())
        {
            category = categories.next();

            // Get media items in this category that were released today
            MediaItemsAbstractController controller = category.getMediaType().getController();
            releasedToday = controller.getMediaItemsReleasedToday(category);

            // If we have media items...
            if(releasedToday!=null && releasedToday.size()>0)
            {
                // Get all their titles
                itemTitles = new String[releasedToday.size()];
                for(int i=0; i<itemTitles.length; i++) itemTitles[i] = releasedToday.get(i).getTitle();
                content = context.getString(R.string.new_releases_notification_content, Utils.joinIfNotEmpty(", ", itemTitles), context.getResources().getQuantityString(R.plurals.released_today, releasedToday.size(), releasedToday.size()));

                // If only one media item notification click brings to its form, otherwise to the category list
                if(releasedToday.size()==1)
                {
                    notificationClickIntent = ScreenController.getMediaItemFormIntent(context, category, releasedToday.get(0));
                }
                else
                {
                    notificationClickIntent = ScreenController.getCategoryPageIntent(context, category, null);
                }

                // Build pending intent
                notificationClickPendingIntent = PendingIntent.getActivity(context, category.getId().intValue(), notificationClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Send notification
                sendNotification(category.getId().intValue(), category.getMediaType().getIcon(), category.getName(), content, notificationClickPendingIntent, Notification.PRIORITY_DEFAULT);
            }
        }
    }

    /**
     * Helper to send a notification
     * @param notificationID the ID
     * @param smallIcon the icon
     * @param title the title
     * @param content the content
     * @param notificationClickIntent the click intent
     * @param priority the priority
     */
    private void sendNotification(int notificationID, int smallIcon, String title, String content, PendingIntent notificationClickIntent, int priority)
    {
        // Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(priority)
                .setSound(settingsManager.getNotificationsSound())
                .setAutoCancel(true)
                .setVibrate(settingsManager.getNotificationsVibratePattern());

        // Set on click action
        builder.setContentIntent(notificationClickIntent);

        // Send notification
        notificationManager.notify(notificationID, builder.build());
    }
}
