package it.polimi.dima.mediatracker.activities;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.ScreenController;
import it.polimi.dima.mediatracker.controllers.VideogamesController;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.MediaType;
import it.polimi.dima.mediatracker.model.Subcategory;
import it.polimi.dima.mediatracker.model.Videogame;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.clickOnDialogOptionAtPosition;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.getRandomName;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Espresso UI Test: tests several suggestions results
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class SuggestionsUITest
{
    private static Category category;

    private static MediaItem v1;
    private static MediaItem v2;
    private static MediaItem v3;
    private static MediaItem v4;
    private static MediaItem v5;
    private static MediaItem v6;
    private static MediaItem v7;
    private static MediaItem v8;
    private static MediaItem v9;
    private static MediaItem v10;
    private static MediaItem v11;

    private static Context context;

    @Rule
    public ActivityTestRule<CategoryActivity> activityRule = new ActivityTestRule<CategoryActivity>(CategoryActivity.class)
    {
        @Override
        protected Intent getActivityIntent()
        {
            return ScreenController.getCategoryPageIntent(context, category, Subcategory.SUGGESTION);
        }
    };

    @BeforeClass
    public static void beforeClass()
    {
        // Context
        context = InstrumentationRegistry.getTargetContext();

        // Create category
        category = new Category(context, "CAT"+getRandomName(), R.color.colorPrimary, MediaType.VIDEOGAMES);
        CategoriesController.getInstance().saveCategory(category);

        // Create media items
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -6);
        Date d0 = c.getTime();
        c.set(Calendar.YEAR, 1990);
        Date d1 = c.getTime();
        c.set(Calendar.YEAR, 2000);
        Date d2 = c.getTime();
        c.set(Calendar.YEAR, 2100);
        Date d3 = c.getTime();

        // Completed
        v1 = initV("V1", "Horror", 100, false, d1, false, d1);
        v2 = initV("V2", "RPG, Action, Adventure", 0, true, d0, false, d1);
        v3 = initV("V3", "Sport", 1, false, d1, false, d1);

        // Upcoming
        v4 = initV("V4", null, 0, false, null, true, d3);
        v5 = initV("V5", "", 0, true, null, false, d3);

        // Non-completed, Playing now
        v6 = initV("V6", "Action, Adventure", 10, true, null, true, d1);
        v7 = initV("V7", "Sport", 20, true, null, true, d2);

        // Non-completed, Owned
        v8 = initV("V8", null, 100, true, null, false, d1);
        v9 = initV("V9", "RPG, Adventure", 99999, true, null, false, d1);

        // Non-completed, Non-owned
        v10 = initV("V10", "Action, Adventure", 70, false, null, false, d2);
        v11 = initV("V11", "Adventure", 0, false, null, false, d2);
    }

    @AfterClass
    public static void afterClass()
    {
        // Delete category (and all items in it)
        CategoriesController.getInstance().deleteCategory(category);
    }

    /**
     * Tracked
     * "RPG" genre
     * Very long
     * Owned
     * ==> 1 result
     */
    @Test
    public void testSuggestions1()
    {
        setStatus(false);
        setGenre("RPG");
        setLength(4);
        setOwned(true);

        submitAndVerify(new MediaItem[]{v9});
    }

    /**
     * Tracked
     * "RPGkkk" genre
     * Very long
     * Owned
     * ==> no result
     */
    @Test
    public void testSuggestions2()
    {
        setStatus(false);
        setGenre("NonExistingGenre");
        setLength(4);
        setOwned(true);

        submitAndVerify(null);
    }

    /**
     * Tracked
     * "Adventure" genre
     * Any duration
     * Not owned
     * ==> 2 possible results
     */
    @Test
    public void testSuggestions3()
    {
        setStatus(false);
        setGenre("Adventure");
        setLength(0);
        setOwned(false);

        submitAndVerify(new MediaItem[]{v10, v11});
    }

    /**
     * Tracked
     * "Adventure" genre
     * Long
     * Owned or not
     * ==> 1 result
     */
    @Test
    public void testSuggestions4()
    {
        setStatus(false);
        setGenre("Adventure");
        setLength(3);
        setOwned(null);

        submitAndVerify(new MediaItem[]{v10});
    }

    /**
     * Completed
     * "Action" genre
     * Any duration
     * Completed at least 3 years ago
     * ==> 1 result
     */
    @Test
    public void testSuggestions5()
    {
        setStatus(true);
        setGenre("Action");
        setLength(0);
        setCompletion(3);

        submitAndVerify(new MediaItem[]{v2});
    }

    /**
     * Completed
     * "Action" genre
     * Any duration
     * Completed at least 1 year ago
     * ==> no result
     */
    @Test
    public void testSuggestions6()
    {
        setStatus(true);
        setGenre("Action");
        setLength(0);
        setCompletion(0);

        submitAndVerify(null);
    }



    /*************************************************** HELPERS *******************************************/


    /**
     * Helper to set status input
     */
    private void setStatus(boolean completed)
    {
        // Open picker
        onView(withId(R.id.suggestions_item_status)).perform(click());

        // Select option
        clickOnDialogOptionAtPosition(completed ? 1 : 0);
    }

    /**
     * Helper to set genres input
     */
    private void setGenre(String genres)
    {
        // Write text
        onView(withId(R.id.suggestions_genres)).perform(typeText(genres));
    }

    /**
     * Helper to set duration input
     */
    private void setLength(int pos)
    {
        // Open picker
        onView(withId(R.id.suggestions_duration)).perform(click());

        // Select option
        clickOnDialogOptionAtPosition(pos);
    }

    /**
     * Helper to set owned input
     */
    private void setOwned(Boolean owned)
    {
        // Open picker
        onView(withId(R.id.suggestions_owned)).perform(click());

        // Select option
        int pos = owned==null ? 2 : (owned ? 0 : 1);
        clickOnDialogOptionAtPosition(pos);
    }

    /**
     * Helper to set completion input
     */
    private void setCompletion(int pos)
    {
        // Open picker
        onView(withId(R.id.suggestions_completion)).perform(click());

        // Select option
        clickOnDialogOptionAtPosition(pos);
    }

    /**
     * Helper to submit result
     */
    private void submit()
    {
        closeSoftKeyboard();
    }

    /**
     * Helper to reload result
     */
    private void reload()
    {
        onView(withId(R.id.suggestions_result_reload)).perform(scrollTo(), click());
    }

    /**
     * Helper to submit and verify that the result is among one of the given valid media items
     */
    private void submitAndVerify(MediaItem[] valid)
    {
        // If result should be empty, check once that the empty message is shown
        if(valid==null || valid.length<=0)
        {
            submit();
            onView(allOf(withId(R.id.suggestions_result_area), hasDescendant(withText(R.string.suggestions_result_nothing))));
            return;
        }

        // Otherwise try to submit 3*size times and check if correct
        submit();
        for(int i=0; i<valid.length*3; i++)
        {
            reload();
            boolean ok = false;
            for(MediaItem v : valid)
            {
                try
                {
                    onView(allOf(withId(R.id.suggestions_result_area), hasDescendant(withText(v.getTitle())))).check(matches(isDisplayed()));
                    ok = true;
                    break;
                }
                catch (Exception e){/**/}
            }
            assertTrue(ok);
        }
    }

    /**
     * Helper to initialize a fake videogame
     */
    private static Videogame initV(String title, String genres, int hours, boolean owned, Date completion, boolean doing, Date release)
    {
        Videogame temp = new Videogame();
        temp.setTitle(title+"-"+getRandomName());
        temp.setGenres(genres);
        temp.setAverageLengthHours(hours);
        temp.setOwned(owned);
        temp.setCompletionDate(completion);
        temp.setDoingNow(doing);
        temp.setCategory(category.getId());
        temp.setReleaseDate(release);
        VideogamesController.getInstance().saveMediaItem(temp);
        return temp;
    }
}