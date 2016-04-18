package it.polimi.dima.mediatracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.fragments.FormAbstractFragment;
import it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.utils.GlobalConstants;

/**
 * Activity that only contains a {@link FormMediaItemAbstractFragment} when we are on a phone (on a tablet content and form are displayed in a multi-pane layout
 * by {@link CategoryActivity}, on a phone in two different activities)
 */
public class OnlyMediaItemFormActivity extends AppCompatActivity implements FormAbstractFragment.OnObjectSaveListener
{
    private final static String FORM_FRAGMENT_TAG = "FORM_FRAGMENT_TAG";

    private FormMediaItemAbstractFragment formMediaItemAbstractFragment;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_only_form);

        // Get the caller's intent
        Intent intent = getIntent();

        // Get category and media item controller
        Long categoryId = intent.getLongExtra(GlobalConstants.CATEGORY_ID_FORM_PARAMETER_NAME, -1);
        Category category = CategoriesController.getInstance().getCategoryById(categoryId);
        MediaItemsAbstractController controller = category.getMediaType().getController();

        // Get current media item (available only if we are editing one)
        Long mediaItemId = intent.getLongExtra(GlobalConstants.MEDIA_ITEM_ID_FORM_PARAMETER_NAME, -1);

        // Retrieve or create the form fragment
        if(savedInstanceState!=null)
        {
            // Get saved instance and reset the listeners
            formMediaItemAbstractFragment = (FormMediaItemAbstractFragment) getSupportFragmentManager().findFragmentByTag(FORM_FRAGMENT_TAG);
        }
        else
        {
            // Initialize and display the content fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            formMediaItemAbstractFragment = controller.getMediaItemFormFragment(categoryId, mediaItemId, false);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.details_fragment, formMediaItemAbstractFragment, FORM_FRAGMENT_TAG);
            transaction.commit();
        }
        formMediaItemAbstractFragment.setOnObjectSaveListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onObjectSaved()
    {
        // Return result to the caller's activity
        Intent result = new Intent();
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed()
    {
        // Manage exit
        formMediaItemAbstractFragment.manageExitFromForm(new FormAbstractFragment.FormExitListener()
        {
            @Override
            public void onExit(boolean canExit)
            {
                if(canExit) OnlyMediaItemFormActivity.super.onBackPressed();
            }
        });
    }
}
