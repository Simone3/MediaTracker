package it.polimi.dima.mediatracker.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.controllers.ScreenController;
import it.polimi.dima.mediatracker.fragments.ContentAbstractFragment;
import it.polimi.dima.mediatracker.fragments.FormAbstractFragment;
import it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment;
import it.polimi.dima.mediatracker.fragments.MediaItemsListFragment;
import it.polimi.dima.mediatracker.listeners.MediaItemListener;
import it.polimi.dima.mediatracker.listeners.OnSetupDrawerListener;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.Subcategory;
import it.polimi.dima.mediatracker.utils.GlobalConstants;

/**
 * Activity that contains:
 * - on tablets: both the "content" of a category page (media items list, suggestions) and the details (form) in a double-pane layout
 * - on phones: only the "content" of a category page
 *
 * For this reason on tablets the activity manages both a {@link ContentAbstractFragment} and a {@link FormMediaItemAbstractFragment},
 * while on phones it manages only the former (the latter is managed by {@link OnlyMediaItemFormActivity})
 */
public class CategoryActivity extends DrawerAbstractActivity implements MediaItemListener, FormAbstractFragment.OnObjectSaveListener, OnSetupDrawerListener
{
    private final static String CONTENT_FRAGMENT_TAG = "CONTENT_FRAGMENT_TAG";
    private final static String DETAILS_FRAGMENT_TAG = "DETAILS_FRAGMENT_TAG";

    private FragmentManager fragmentManager;

    private ContentAbstractFragment contentFragment;
    private FormMediaItemAbstractFragment detailsFragment;

    private boolean isMultiPaneLayout;

    private Long categoryId;
    private Category category;
    private Subcategory subcategory;
    private Long mediaItemId;

    private MediaItemsAbstractController controller;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Intent intent = getIntent();

        // Get category parameter (always from the intent)
        categoryId = intent.getLongExtra(GlobalConstants.CATEGORY_ID_INTENT_PARAMETER_NAME, 0);
        category = CategoriesController.getInstance().getCategoryById(categoryId);
        controller = category.getMediaType().getController();

        // Get subcategory parameter (from intent or saved instance)
        if(savedInstanceState!=null && savedInstanceState.containsKey(GlobalConstants.MEDIA_ITEM_ID_INTENT_PARAMETER_NAME))
        {
            subcategory = (Subcategory) savedInstanceState.getSerializable(GlobalConstants.SUBCATEGORY_INTENT_PARAMETER_NAME);
        }
        else
        {
            subcategory = (Subcategory) intent.getSerializableExtra(GlobalConstants.SUBCATEGORY_INTENT_PARAMETER_NAME);
        }

        // Get media item parameter (from intent or saved instance)
        if(savedInstanceState!=null && savedInstanceState.containsKey(GlobalConstants.MEDIA_ITEM_ID_INTENT_PARAMETER_NAME))
        {
            mediaItemId = savedInstanceState.getLong(GlobalConstants.MEDIA_ITEM_ID_INTENT_PARAMETER_NAME, 0);
        }
        else
        {
            mediaItemId = intent.getLongExtra(GlobalConstants.MEDIA_ITEM_ID_INTENT_PARAMETER_NAME, 0);
        }

        // Check if we are on a multi-pane layout (tablet) or not (phone)
        isMultiPaneLayout = getResources().getBoolean(R.bool.is_multi_pane_layout);

        // Get fragment manager
        fragmentManager = getSupportFragmentManager();

        // Retrieve or create the content fragment
        if(savedInstanceState!=null)
        {
            // Get saved instance and reset the listeners
            contentFragment = (ContentAbstractFragment) getSupportFragmentManager().findFragmentByTag(CONTENT_FRAGMENT_TAG);
            setContentFragmentListeners();
        }
        else if(contentFragment == null)
        {
            // Initialize and display the content fragment
            setContentFragmentBasedOnCurrentSubcategory();
        }

        // If we are in a multi-pane layout...
        if(isMultiPaneLayout)
        {
            // Retrieve or create the details fragment
            if(savedInstanceState!=null)
            {
                // Get saved instance
                detailsFragment = (FormMediaItemAbstractFragment) getSupportFragmentManager().findFragmentByTag(DETAILS_FRAGMENT_TAG);
                setDetailsFragmentListeners();
            }
            else if(detailsFragment == null)
            {
                // Initialize and display the details fragment
                setDetailsFragmentBasedOnCurrentMediaItem(true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed()
    {
        // If we are in a multi-pane layout...
        if(isMultiPaneLayout)
        {
            // Manage form
            detailsFragment.manageExitFromForm(new FormAbstractFragment.FormExitListener()
            {
                @Override
                public void onExit(boolean canExit)
                {
                    if(canExit) CategoryActivity.super.onBackPressed();
                }
            });
        }
        else
        {
            super.onBackPressed();
        }
    }

    /**
     * Helper to set the listeners in the content fragment
     */
    private void setContentFragmentListeners()
    {
        contentFragment.setMediaItemListener(this);
    }

    /**
     * Helper to set the listeners in the details fragment
     */
    private void setDetailsFragmentListeners()
    {
        detailsFragment.setOnObjectSaveListener(this);
    }

    /**
     * Helper to set the content fragment based on the current subcategory field
     */
    private void setContentFragmentBasedOnCurrentSubcategory()
    {
        // If it's a list...
        if(subcategory==null || subcategory.equals(Subcategory.COMPLETED))
        {
            // Create list fragment
            contentFragment = MediaItemsListFragment.newInstance(categoryId, !(subcategory==null));
        }

        // Otherwise...
        else
        {
            // Create suggestions fragment
            contentFragment = controller.getMediaItemSuggestionsFragment(categoryId);
        }

        // Add listeners
        setContentFragmentListeners();

        // Display the fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_fragment, contentFragment, CONTENT_FRAGMENT_TAG);
        transaction.commit();
    }

    /**
     * Helper to set the details fragment based on the current subcategory field
     * @param isEmpty true if the fragment should show only a blank screen with toolbar
     */
    private void setDetailsFragmentBasedOnCurrentMediaItem(boolean isEmpty)
    {
        // Create form fragment and set listeners
        detailsFragment = controller.getMediaItemFormFragment(categoryId, mediaItemId, isEmpty);
        setDetailsFragmentListeners();

        // Display the fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.details_fragment, detailsFragment, DETAILS_FRAGMENT_TAG);
        transaction.commit();
    }

    /**
     * Save the currently selected media item ID and subcategory (otherwise in the multi-pane layout if the device rotates we loose them!)
     * @param outState the state before the application is recreated
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if(isMultiPaneLayout)
        {
            if(mediaItemId!=null) outState.putLong(GlobalConstants.MEDIA_ITEM_ID_INTENT_PARAMETER_NAME, mediaItemId);
            if(subcategory!=null) outState.putSerializable(GlobalConstants.SUBCATEGORY_INTENT_PARAMETER_NAME, subcategory);
        }
    }

    /**
     * Called by both list and suggestions fragments when the user clicks on a media item
     * {@inheritDoc}
     */
    @Override
    public void onMediaItemSelected(final MediaItem mediaItem)
    {
        // If we are in a multi-pane layout...
        if(isMultiPaneLayout)
        {
            // First check if the current form needs to be saved...
            detailsFragment.manageExitFromForm(new FormAbstractFragment.FormExitListener()
            {
                @Override
                public void onExit(boolean canExit)
                {
                    if(canExit)
                    {
                        // Update the parameter
                        mediaItemId = mediaItem==null ? null : mediaItem.getId();

                        // Update the form fragment
                        setDetailsFragmentBasedOnCurrentMediaItem(false);
                    }
                }
            });
        }

        // Otherwise...
        else
        {
            // Update the parameter
            mediaItemId = mediaItem==null ? null : mediaItem.getId();

            // Start the form activity
            startFormActivity(mediaItem);
        }
    }

    /**
     * Called by list fragment when the user removes (deletes, sets as completed, etc.) a media item from the list
     * {@inheritDoc}
     */
    @Override
    public void onMediaItemRemoved(MediaItem mediaItem)
    {
        // If the removed media item is the currently displayed one in the details fragment...
        if(isMultiPaneLayout && mediaItem!=null && mediaItemId!=null && mediaItemId.equals(mediaItem.getId()))
        {
            // Reload the fragment setting it as empty
            setDetailsFragmentBasedOnCurrentMediaItem(true);
        }
    }

    /**
     * Helper called if we are on a phone (= no multi-pane layout) to start the form activity instead of displaying it in the second fragment
     * @param mediaItem the media item to display
     */
    private void startFormActivity(MediaItem mediaItem)
    {
        Intent intent = ScreenController.getMediaItemFormIntent(this, category, mediaItem);
        startActivityForResult(intent, 0);
    }

    /**
     * Called after returning from the media item form (media item added or updated) when we DON'T have a multi-pane layout
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Do this only if we actually have data
        if(data!=null)
        {
            // Do the same as the method below
            onObjectSaved();
        }
    }

    /**
     * Called after returning from the media item form (media item added or updated) when we DO have a multi-pane layout
     * {@inheritDoc}
     */
    @Override
    public void onObjectSaved()
    {
        // If the content is the list, reload it
        if(contentFragment instanceof MediaItemsListFragment)
        {
            ((MediaItemsListFragment) contentFragment).refreshMediaItemsListFromDatabase();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSetupDrawer(Toolbar toolbar)
    {
        setupDrawer(toolbar, category, subcategory);
    }
}
