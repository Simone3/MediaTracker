package it.polimi.dima.mediatracker.activities;

import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.adapters.drawer.DrawerAdapter;
import it.polimi.dima.mediatracker.adapters.drawer.DrawerElement;
import it.polimi.dima.mediatracker.adapters.drawer.DrawerSubElement;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.ScreenController;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.Subcategory;

/**
 * Abstract activity that manages the navigation drawer. All subclasses just need to call "setupDrawer" to build it
 */
public abstract class DrawerAbstractActivity extends AppCompatActivity implements DrawerAdapter.DrawerListener
{
    private List<Category> categories;

    private Category currentCategory;
    private Subcategory currentSubcategory;
    private int firstSelectedItem = 0;
    private int firstSelectedSubItem = -1;
    private List<DrawerElement> drawerElements;

    private DrawerLayout drawerLayout;
    private RecyclerView drawerRecyclerView;
    private DrawerAdapter drawerAdapter;

    /**
     * Called by subclasses during onCreate to build the navigation drawer
     * @param toolbar the activity toolbar
     * @param currentCategory the current category (possibly null)
     * @param currentSubcategory the current subcategory (possibly null)
     */
    void setupDrawer(Toolbar toolbar, Category currentCategory, Subcategory currentSubcategory)
    {
        this.currentCategory = currentCategory;
        this.currentSubcategory = currentSubcategory;

        // Load categories and build items
        loadCategoriesFromDatabase();
        buildDrawerElements();

        // Get drawer recycler view
        drawerRecyclerView = (RecyclerView) findViewById(R.id.navigation_drawer);

        // Set the drawer adapter
        drawerAdapter = new DrawerAdapter(this, drawerElements, firstSelectedItem, firstSelectedSubItem, this);
        drawerRecyclerView.setAdapter(drawerAdapter);

        // Set the layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        drawerRecyclerView.setLayoutManager(layoutManager);

        // Setup the DrawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Close drawer when the user presses "back"
     */
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    /**
     * Redirects the user to a category page
     * @param category the selected category
     * @param subcategory the selected subcategory (possibly null)
     */
    void goToCategoryPage(Category category, Subcategory subcategory)
    {
        Intent intent = ScreenController.getCategoryPageIntent(this, category, subcategory);
        startActivity(intent);
    }

    /**
     * Reloads the drawer from the database, calling {@link DrawerAbstractActivity#loadCategoriesFromDatabase()}
     */
    void refreshDrawer()
    {
        buildDrawerElements();
        drawerAdapter.notifyDataSetChanged();
    }

    /**
     * Creates the drawer items (home, every category, etc.)
     */
    private void buildDrawerElements()
    {
        if(drawerElements==null) drawerElements = new ArrayList<>();
        else drawerElements.clear();
        List<DrawerSubElement> drawerSubElements;

        // Home
        drawerElements.add(new DrawerElement(getString(R.string.navigation_drawer_home), R.drawable.ic_home, android.R.color.black, null));

        // All categories
        Subcategory[] subcategories;
        for(Category category: categories)
        {
            // Build sub-items (sub-categories), if any
            subcategories = category.getMediaType().getController().getMediaTypeSubcategories();
            if(subcategories!=null && subcategories.length > 0)
            {
                drawerSubElements = new ArrayList<>();
                for(Subcategory subcategory: subcategories)
                {
                    drawerSubElements.add(new DrawerSubElement(subcategory.getName(this), subcategory.getIcon(), android.R.color.black));
                }
            }
            else
            {
                drawerSubElements = null;
            }

            // Create and add the drawer item
            drawerElements.add(new DrawerElement(category.getName(), category.getMediaType().getIcon(), category.getColor(this), drawerSubElements));
        }

        // Set current selected item values
        if(currentCategory!=null)
        {
            firstSelectedItem = categories.indexOf(currentCategory) + 1;
            if(currentSubcategory!=null)
            {
                Subcategory[] currentSubcategories = currentCategory.getMediaType().getController().getMediaTypeSubcategories();
                firstSelectedSubItem = Arrays.asList(currentSubcategories).indexOf(currentSubcategory);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerElementSelected(int selectedElementIndex, int selectedSubElementIndex)
    {
        // Close drawer
        drawerLayout.closeDrawer(drawerRecyclerView);

        // Home
        if(selectedElementIndex ==0)
        {
            Intent intent = ScreenController.getHomeIntent(this);
            startActivity(intent);
        }

        // Categories
        else
        {
            Category selectedCategory = categories.get(selectedElementIndex -1);
            Subcategory selectedSubcategory = selectedSubElementIndex >=0 ? selectedCategory.getMediaType().getController().getMediaTypeSubcategories()[selectedSubElementIndex] : null;
            goToCategoryPage(selectedCategory, selectedSubcategory);
        }
    }

    /**
     * Helper to (re)load the categories from the database
     */
    void loadCategoriesFromDatabase()
    {
        if(categories==null) categories = new ArrayList<>();
        else categories.clear();
        Iterator<Category> allCategoriesIterator = CategoriesController.getInstance().getAllCategories();
        while(allCategoriesIterator.hasNext())
        {
            categories.add(allCategoriesIterator.next());
        }
    }

    /**
     * Getter for all categories
     * @return all categories
     */
    List<Category> getAllCategories()
    {
        return categories;
    }
}
