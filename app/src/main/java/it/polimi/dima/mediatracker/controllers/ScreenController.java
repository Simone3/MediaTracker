package it.polimi.dima.mediatracker.controllers;

import android.content.Context;
import android.content.Intent;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.activities.CategoryActivity;
import it.polimi.dima.mediatracker.activities.HomeActivity;
import it.polimi.dima.mediatracker.activities.OnlyCategoryFormActivity;
import it.polimi.dima.mediatracker.activities.OnlyMediaItemFormActivity;
import it.polimi.dima.mediatracker.activities.SettingsActivity;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.Subcategory;
import it.polimi.dima.mediatracker.utils.GlobalConstants;

/**
 * Manages the creation of intents (with appropriate parameters) for every "screen" of the application
 */
public class ScreenController
{
    /**
     * Getter
     * @param context the context
     * @return intent to reach the home page (no parameters)
     */
    public static Intent getHomeIntent(Context context)
    {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    /**
     * Getter
     * @param context the context
     * @return intent to reach the settings page (no parameters)
     */
    public static Intent getSettingsIntent(Context context)
    {
        return new Intent(context, SettingsActivity.class);
    }

    /**
     * Getter
     * @param context the context
     * @param category the category to update or null if new category
     * @return intent to reach a category form
     */
    public static Intent getCategoryFormIntent(Context context, Category category)
    {
        Intent intent = new Intent(context, OnlyCategoryFormActivity.class);
        intent.putExtra(GlobalConstants.CATEGORY_ID_FORM_PARAMETER_NAME, (category==null ? null : category.getId()));
        return intent;
    }

    /**
     * Getter
     * @param context the context
     * @param category the category of the page
     * @param subcategory the subcategory of the page (if null it brings to the main category page)
     * @return intent to reach a category page (e.g. tracked items list)
     */
    public static Intent getCategoryPageIntent(Context context, Category category, Subcategory subcategory)
    {
        return getFullMediaItemPageIntent(context, category, subcategory, null);
    }

    /**
     * Getter
     * @param context the context
     * @param category the category of the media item
     * @param mediaItem the media item to edit or null if new media item
     * @return intent to reach a media item form
     */
    public static Intent getMediaItemFormIntent(Context context, Category category, MediaItem mediaItem)
    {
        // If we are in a multi-pane layout...
        if(context.getResources().getBoolean(R.bool.is_multi_pane_layout))
        {
            // Return the category page with this item in the details fragment
            return getFullMediaItemPageIntent(context, category, null, mediaItem);
        }

        // Otherwise...
        else
        {
            // Return the form-only activity
            Intent intent = new Intent(context, OnlyMediaItemFormActivity.class);
            intent.putExtra(GlobalConstants.CATEGORY_ID_FORM_PARAMETER_NAME, category.getId());
            intent.putExtra(GlobalConstants.MEDIA_ITEM_ID_FORM_PARAMETER_NAME, (mediaItem==null ? null : mediaItem.getId()));
            return intent;
        }

    }

    /**
     * Helper to build a {@link CategoryActivity} intent that links to a category page (list, suggestions) with optionally a selected media item (in tablets the media item details are shown on the left side of the multi-pane layout)
     * @param context the context
     * @param category the category of the media item
     * @param subcategory the subcategory of the page (if null it brings to the main category page)
     * @param mediaItem the media item to edit or null if new media item
     * @return intent to reach the CategoryActivity
     */
    private static Intent getFullMediaItemPageIntent(Context context, Category category, Subcategory subcategory, MediaItem mediaItem)
    {
        Intent intent = new Intent(context, CategoryActivity.class);
        intent.putExtra(GlobalConstants.CATEGORY_ID_INTENT_PARAMETER_NAME, category.getId());
        intent.putExtra(GlobalConstants.SUBCATEGORY_INTENT_PARAMETER_NAME, subcategory);
        intent.putExtra(GlobalConstants.MEDIA_ITEM_ID_INTENT_PARAMETER_NAME, (mediaItem==null ? null : mediaItem.getId()));
        return intent;
    }
}
