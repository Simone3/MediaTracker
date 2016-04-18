package it.polimi.dima.mediatracker.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.adapters.HomeCategoriesAdapter;
import it.polimi.dima.mediatracker.alarms.AlarmScheduler;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.ScreenController;
import it.polimi.dima.mediatracker.controllers.SettingsManager;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * The application's main activity, shows the list of all categories
 */
public class HomeActivity extends DrawerAbstractActivity
{
    private SettingsManager settingsManager;

    private GridView gridView;
    private HomeCategoriesAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get settings manager
        settingsManager = SettingsManager.getInstance(this);

        // If it's the first time the application is run, perform some actions
        if(settingsManager.isFirstRun()) performFirstRunOperations();

        // Setup toolbar and drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupDrawer(toolbar, null, null);

        // Manage floating action button that allows to create a new category
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.home_fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                goToCategoryForm(null);
            }
        });

        // Setup the category grid
        gridView = (GridView) findViewById(R.id.categories_grid);
        gridAdapter = new HomeCategoriesAdapter(this, R.layout.grid_categories_item, getAllCategories());
        gridView.setAdapter(gridAdapter);

        // If the user clicks on a grid element, he/she goes to the linked category page
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                goToCategoryPage(getAllCategories().get(position), null);
            }
        });

        // Empty view
        gridView.setEmptyView(findViewById(R.id.home_empty_view));

        // Register for the context menu on the grid view
        registerForContextMenu(gridView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.action_bar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here
        int id = item.getItemId();
        return Utils.manageToolbarMenuSelection(this, id) || super.onOptionsItemSelected(item);
    }

    /**
     * Called after returning from the category form (category added or updated)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Do this only if we actually have data
        if(data!=null)
        {
            // Reload both grid and drawer
            loadCategoriesFromDatabase();
            refreshDrawer();
            refreshGrid();
        }
    }

    /**
     * Helper to reload the categories grid
     */
    private void refreshGrid()
    {
        // Update grid
        gridAdapter.notifyDataSetChanged();
        gridView.invalidateViews();
    }

    /**
     * Context menu allows to edit, delete, etc. the categories from the grid
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_categories, menu);
    }

    /**
     * Context menu allows to edit, delete, etc. the categories from the grid
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        // Get selected category
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int selectedPosition = info.position;

        switch(item.getItemId())
        {
            // To edit we go to the category form
            case R.id.edit_category:
                goToCategoryForm(getAllCategories().get(selectedPosition));
                return true;

            // To delete...
            case R.id.delete_category:

                // We first show a confirmation message
                new AlertDialog.Builder(this)
                    .setMessage(R.string.delete_category_confirm_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            // Delete category
                            CategoriesController.getInstance().deleteCategory(getAllCategories().get(selectedPosition));
                            getAllCategories().remove(selectedPosition);

                            // Reload grid and drawer
                            loadCategoriesFromDatabase();
                            refreshDrawer();
                            refreshGrid();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    })
                    .show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Called to perform some operations if it's the first run
     */
    private void performFirstRunOperations()
    {
        // Initialize database
        CategoriesController.getInstance().addDefaultCategoriesIfEmpty(this);

        // Start alarms
        AlarmScheduler.getInstance(this).startAllAlarms();

        // Change first run flag
        settingsManager.setFirstRun(false);
    }

    /**
     * Helper to go to the category form
     * @param category null if adding new category, the category to edit otherwise
     */
    private void goToCategoryForm(Category category)
    {
        Intent intent = ScreenController.getCategoryFormIntent(HomeActivity.this, category);
        startActivityForResult(intent, 0);
    }
}
