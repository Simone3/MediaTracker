package it.polimi.dima.mediatracker.activities;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaType;
import it.polimi.dima.mediatracker.utils.Utils;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.withValue;

/**
 * Espresso UI Test: tests several operations on the Home page, e.g. click on a category, open settings, etc.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class HomeActivityUITest
{
    private static List<Category> categories;
    private static Category createdFakeCategory;
    private static Context context;

    @Rule
    public ActivityTestRule<HomeActivity> activityRule = new ActivityTestRule<>(HomeActivity.class);

    @BeforeClass
    public static void beforeClass()
    {
        // Context
        context = InstrumentationRegistry.getTargetContext();

        // Get categories
        Iterator<Category> categoriesIterator = CategoriesController.getInstance().getAllCategories();

        // Add fake category if empty
        createdFakeCategory = null;
        if(!categoriesIterator.hasNext())
        {
            createdFakeCategory = new Category(context, InstrumentationTestUtils.getRandomName(), R.color.colorPrimary, MediaType.BOOKS);
            CategoriesController.getInstance().saveCategory(createdFakeCategory);
            categoriesIterator = CategoriesController.getInstance().getAllCategories();
        }

        // Set variable
        categories = Utils.iteratorToList(categoriesIterator);
    }

    @AfterClass
    public static void afterClass()
    {
        // Delete fake category if needed
        if(createdFakeCategory!=null)
        {
            CategoriesController.getInstance().deleteCategory(createdFakeCategory);
        }
    }

    /**
     * Tests click on a category in the grid
     */
    @Test
    public void testCategoryClick()
    {
        // Get first category
        Category categoryToSelect = categories.get(0);

        // Click on the first category
        onData(withValue(categoryToSelect)).perform(click());

        // Check if we are in the media items list
        onView(withId(R.id.media_items_list)).check(matches(isDisplayed()));
    }

    /**
     * Tests edit button on a category in the grid
     */
    @Test
    public void testCategoryEdit()
    {
        // Get first category
        Category categoryToSelect = categories.get(0);

        // Long click on the first category
        onData(withValue(categoryToSelect)).perform(longClick());

        // Check context menu
        onView(withText(context.getString(R.string.edit))).check(matches(isDisplayed()));

        // Click on edit
        onView(withText(context.getString(R.string.edit))).perform(click());

        // Check if we are in the form
        onView(withId(R.id.form_container)).check(matches(isDisplayed()));

        // Check name input
        onView(withId(R.id.form_title_input)).check(matches(withText(categoryToSelect.getName())));
    }

    /**
     * Tests action button to add a category
     */
    @Test
    public void testHomeFAB()
    {
        // Click on the FAB
        onView(withId(R.id.home_fab)).perform(click());

        // Check if we are in the form
        onView(withId(R.id.form_container)).check(matches(isDisplayed()));

        // Check name is empty
        onView(withId(R.id.form_title_input)).check(matches(withText("")));
    }

    /**
     * Tests link to settings
     */
    @Test
    public void testSettingsClick()
    {
        Context context = InstrumentationRegistry.getTargetContext();

        // Open overflow menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click on settings
        onView(withText(context.getString(R.string.action_settings))).perform(click());

        // Check if we are in settings
        onView(withId(R.id.settings_fragment)).check(matches(isDisplayed()));
    }
}