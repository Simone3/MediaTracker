package it.polimi.dima.mediatracker;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.DatePicker;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.polimi.dima.mediatracker.activities.HomeActivity;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaType;
import it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils;
import it.polimi.dima.mediatracker.utils.Utils;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.PickerActions.setDate;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.clickChildViewWithId;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.getRandomName;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.recyclerViewAtPosition;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.withCategoryName;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.withGridSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Espresso UI Test: tests the normal application flow: creation of categories, creation of media items, management of media items list, remove media items, remove categories
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ApplicationFlowUITest
{
    private static String categoryName;

    private static final int N = 3;
    private static String[] mediaItemsNames = new String[N];
    private static ImportanceLevel[] mediaItemsImportanceLevels = new ImportanceLevel[]{ImportanceLevel.MEDIUM, ImportanceLevel.HIGH, ImportanceLevel.MEDIUM};
    private static Date[] mediaItemsReleaseDates = new Date[N];

    @Rule
    public ActivityTestRule<HomeActivity> activityRule = new ActivityTestRule<>(HomeActivity.class);

    @BeforeClass
    public static void beforeClass()
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, 1);

        c.set(Calendar.YEAR, 1990);
        mediaItemsReleaseDates[0] = c.getTime();

        c.set(Calendar.YEAR, 2016);
        mediaItemsReleaseDates[1] = c.getTime();

        c.set(Calendar.YEAR, 2050);
        mediaItemsReleaseDates[2] = c.getTime();
    }

    /**
     * Single test that covers the whole application flow
     */
    @Test
    public void testApplicationFlow()
    {
        Context context = InstrumentationRegistry.getTargetContext();

        // Get categories
        List<Category> categories = Utils.iteratorToList(CategoriesController.getInstance().getAllCategories());
        int expectedCategoriesNumber = categories.size();


        /********************************* ADD NEW CATEGORY **********************/

        // Get random name
        categoryName = "C-"+InstrumentationTestUtils.getRandomName();

        // Perform two tests
        for(int i=0; i<=1; i++)
        {
            // Click on the home FAB
            onView(ViewMatchers.withId(R.id.home_fab)).perform(click());

            // Check if we are in the form
            onView(withId(R.id.form_container)).check(matches(isDisplayed()));

            // Set name
            onView(withId(R.id.form_title_input)).perform(replaceText(categoryName));

            // Open media type picker
            onView(withId(R.id.form_media_type_button)).perform(click());

            // Select "movies"
            onView(withText(MediaType.MOVIES.getNamePlural(context))).perform(click());

            // Click save
            onView(withId(R.id.action_save)).perform(click());

            // If it's the first time...
            if(i==0)
            {
                expectedCategoriesNumber++;

                // Check if we are back in the home
                onView(withId(R.id.categories_grid)).check(matches(isDisplayed()));

                // Check if the new element is present
                onView(withId(R.id.categories_grid)).check(matches(withGridSize(expectedCategoriesNumber)));
            }

            // If it's the second time...
            else if(i==1)
            {
                // Check that we are still in the form (same name error)
                onView(withId(R.id.form_title_input)).check(matches(isDisplayed()));

                // Press back
                closeSoftKeyboard();
                pressBack();

                // Check that the "unsaved changes" error is shown
                onView(withText(R.string.form_exit_error)).check(matches(isDisplayed()));

                // Click on "keep"
                onView(withText(R.string.form_exit_error_keep)).perform(click());

                // Check that we are still in the form
                onView(withId(R.id.form_title_input)).check(matches(isDisplayed()));

                // Press back again
                pressBack();

                // Now click on "discard"
                onView(withText(R.string.form_exit_error_discard)).perform(click());

                // Check if we are back in the home
                onView(withId(R.id.categories_grid)).check(matches(isDisplayed()));

                // Check that no other category was added
                onView(withId(R.id.categories_grid)).check(matches(withGridSize(expectedCategoriesNumber)));
            }
        }


        /********************************* ADD NEW MEDIA ITEMS **********************/

        // Click on the previously created category
        onView(allOf(withId(R.id.category_item), hasDescendant(withText(categoryName)))).perform(click());

        // Check if we are in the media items list
        onView(withId(R.id.media_items_list)).check(matches(isDisplayed()));

        // Create N media items
        Calendar c = Calendar.getInstance();
        for(int i=0; i<N; i++)
        {
            // Click on the list FAB
            onView(withId(R.id.items_fab)).perform(click());

            // Check if we are in the form
            onView(withId(R.id.form_container)).check(matches(isDisplayed()));

            // Name
            mediaItemsNames[i] = "MI-"+getRandomName();

            // Set name
            onView(withId(R.id.form_title_input)).perform(replaceText(mediaItemsNames[i]));

            // Open release date picker
            onView(withId(R.id.form_release_date_button)).perform(click());

            // Set date
            c.setTime(mediaItemsReleaseDates[i]);
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH)));

            // Close date picker
            onView(withText(android.R.string.ok)).perform(click());

            // Open importance level picker
            onView(withId(R.id.form_importance_level_button)).perform(scrollTo(), click());

            // Select option
            onView(withText(mediaItemsImportanceLevels[i].getName(context))).perform(click());

            // Save media item
            onView(withId(R.id.action_save)).perform(click());
        }


        /********************************* CHECK LIST SECTIONS **********************/

        // I should have section-item-section-item-section-item
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(0, hasDescendant(withText(ImportanceLevel.HIGH.getName(context))))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(1, hasDescendant(withText(mediaItemsNames[1])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(2, hasDescendant(withText(ImportanceLevel.MEDIUM.getName(context))))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(3, hasDescendant(withText(mediaItemsNames[0])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(4, hasDescendant(withText(R.string.upcoming)))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(5, hasDescendant(withText(mediaItemsNames[2])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(6, not(isDisplayed()))));

        /********************************* CHANGE MEDIA ITEMS DIRECTLY IN LIST **********************/

        // Click a list element options button
        onView(withId(R.id.media_items_list)).perform(actionOnItemAtPosition(3, clickChildViewWithId(R.id.item_options_button)));

        // Click on "set as owned"
        try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}
        onView(withText(R.string.set_as_owned)).perform(click());

        // Click again on that media item options
        try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}
        onView(withId(R.id.media_items_list)).perform(actionOnItemAtPosition(3, clickChildViewWithId(R.id.item_options_button)));

        // Click on "set as watching"
        try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}
        onView(withText(R.string.set_as_doing_movie)).perform(click());


        /********************************* CHECK AGAIN LIST SECTIONS **********************/

        // I should have section-item-section-item-section-item
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(0, hasDescendant(withText(R.string.doing_now_movie)))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(1, hasDescendant(withText(mediaItemsNames[0])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(2, hasDescendant(withText(ImportanceLevel.HIGH.getName(context))))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(3, hasDescendant(withText(mediaItemsNames[1])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(4, hasDescendant(withText(R.string.upcoming)))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(5, hasDescendant(withText(mediaItemsNames[2])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(6, not(isDisplayed()))));

        /********************************* SET ITEM AS COMPLETED **********************/

        // Swipe on a section (shouldn't do anything)
        onView(withId(R.id.media_items_list)).perform(actionOnItemAtPosition(2, swipeLeft()));
        try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}

        // Swipe on a non-upcoming element (should remove it and its section)
        onView(withId(R.id.media_items_list)).perform(actionOnItemAtPosition(1, swipeLeft()));
        try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}

        /********************************* CHECK AGAIN LIST SECTIONS **********************/

        // I should have section-item-section-item
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(0, hasDescendant(withText(ImportanceLevel.HIGH.getName(context))))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(1, hasDescendant(withText(mediaItemsNames[1])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(2, hasDescendant(withText(R.string.upcoming)))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(3, hasDescendant(withText(mediaItemsNames[2])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(4, not(isDisplayed()))));

        /********************************* DELETE MEDIA ITEM **********************/

        // Perform two tests
        for(int j=0; j<=1; j++)
        {
            // Click a list element options button
            onView(withId(R.id.media_items_list)).perform(actionOnItemAtPosition(3, clickChildViewWithId(R.id.item_options_button)));

            // Click on "delete"
            try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}
            onView(withText(R.string.delete)).perform(click());

            // Check if we have the confirm dialog
            onView(withText(R.string.delete_media_item_confirm_message)).check(matches(isDisplayed()));

            // First click "no"
            if(j==0)
            {
                // Click no
                onView(withText(android.R.string.no)).perform(click());

                // I should have section-item-section-item
                onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(0, hasDescendant(withText(ImportanceLevel.HIGH.getName(context))))));
                onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(1, hasDescendant(withText(mediaItemsNames[1])))));
                onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(2, hasDescendant(withText(R.string.upcoming)))));
                onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(3, hasDescendant(withText(mediaItemsNames[2])))));
                onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(4, not(isDisplayed()))));
            }

            // Then click "yes"
            else if(j==1)
            {
                // Click yes
                onView(withText(android.R.string.yes)).perform(click());
                try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}
            }
        }


        /********************************* CHECK AGAIN LIST SECTIONS **********************/

        // I should have section-item
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(0, hasDescendant(withText(ImportanceLevel.HIGH.getName(context))))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(1, hasDescendant(withText(mediaItemsNames[1])))));
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(2, not(isDisplayed()))));

        /********************************* DELETE THE PREVIOUSLY CREATED CATEGORY **********************/

        // Go back to the home
        pressBack();

        // Perform two tests
        for(int j=0; j<=1; j++)
        {
            // Long click on the previously created category
            onData(withCategoryName(categoryName)).perform(longClick());

            // Check context menu
            try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();}
            onView(withText(context.getString(R.string.delete))).check(matches(isDisplayed()));

            // Click on delete
            onView(withText(context.getString(R.string.delete))).perform(click());

            // Check if we have the confirm dialog
            onView(withText(R.string.delete_category_confirm_message)).check(matches(isDisplayed()));

            // First click "no"
            if(j==0)
            {
                // Click no
                onView(withText(android.R.string.no)).perform(click());

                // Check that the category is still there
                onView(withId(R.id.categories_grid)).check(matches(withGridSize(expectedCategoriesNumber)));
            }

            // Then click "yes"
            else if(j==1)
            {
                expectedCategoriesNumber--;

                // Click yes
                onView(withText(android.R.string.yes)).perform(click());

                // Check that the category is missing
                onView(withId(R.id.categories_grid)).check(matches(withGridSize(expectedCategoriesNumber)));
            }
        }
    }
}
