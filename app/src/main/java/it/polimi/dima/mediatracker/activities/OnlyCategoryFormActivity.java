package it.polimi.dima.mediatracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.fragments.FormAbstractFragment;
import it.polimi.dima.mediatracker.fragments.FormCategoryFragment;
import it.polimi.dima.mediatracker.utils.GlobalConstants;

/**
 * Activity that only contains a {@link FormCategoryFragment}
 */
public class OnlyCategoryFormActivity extends AppCompatActivity implements FormAbstractFragment.OnObjectSaveListener
{
    private final static String FORM_FRAGMENT_TAG = "FORM_FRAGMENT_TAG";

    private FormCategoryFragment formCategoryFragment;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_only_form);

        // Get category parameter
        Intent intent = getIntent();
        Long categoryId = intent.getLongExtra(GlobalConstants.CATEGORY_ID_FORM_PARAMETER_NAME, -1);

        // Retrieve or create the form fragment
        if(savedInstanceState!=null)
        {
            // Get saved instance and reset the listeners
            formCategoryFragment = (FormCategoryFragment) getSupportFragmentManager().findFragmentByTag(FORM_FRAGMENT_TAG);
        }
        else
        {
            // Initialize and display the content fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            formCategoryFragment = FormCategoryFragment.newInstance(categoryId);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.details_fragment, formCategoryFragment, FORM_FRAGMENT_TAG);
            transaction.commit();
        }
        formCategoryFragment.setOnObjectSaveListener(this);
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
        formCategoryFragment.manageExitFromForm(new FormAbstractFragment.FormExitListener()
        {
            @Override
            public void onExit(boolean canExit)
            {
                if(canExit) OnlyCategoryFormActivity.super.onBackPressed();
            }
        });
    }
}

