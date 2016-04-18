package it.polimi.dima.mediatracker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.DatePicker;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.MediaType;
import it.polimi.dima.mediatracker.model.TVShow;
import it.polimi.dima.mediatracker.utils.GlobalConstants;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.PickerActions.setDate;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.getRandomName;
import static it.polimi.dima.mediatracker.test_utils.OrientationChangeAction.orientationLandscape;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MediaItemsFormUITest
{
    private static Context context;

    private static Category createdFakeCategory;

    private CountDownLatch latch;
    private Activity activity;
    private ActivityLifecycleMonitor lifecycleMonitor;

    @Rule
    public ActivityTestRule<OnlyMediaItemFormActivity> activityRule = new ActivityTestRule<OnlyMediaItemFormActivity>(OnlyMediaItemFormActivity.class)
    {
        @Override
        protected Intent getActivityIntent()
        {
            Intent intent = new Intent(context, OnlyMediaItemFormActivity.class);
            intent.putExtra(GlobalConstants.CATEGORY_ID_FORM_PARAMETER_NAME, createdFakeCategory.getId());
            return intent;
        }
    };

    @BeforeClass
    public static void beforeClass()
    {
        // Context
        context = InstrumentationRegistry.getTargetContext();

        // Add fake category
        createdFakeCategory = new Category(context, getRandomName(), R.color.colorPrimary, MediaType.TV_SHOWS);
        CategoriesController.getInstance().saveCategory(createdFakeCategory);
    }

    @AfterClass
    public static void afterClass()
    {
        // Delete fake category
        if(createdFakeCategory!=null)
        {
            CategoriesController.getInstance().deleteCategory(createdFakeCategory);
        }
    }

    /**
     * Tests if the form works and if the media item is correctly stored in the database
     */
    @Test
    public void testInsertMediaItem()
    {
        // Title
        String title = getRandomName();
        onView(withId(R.id.form_title_input)).perform(scrollTo(), replaceText(title));

        // Creators
        String creators = "TheCreator1, TheCreator2";
        onView(withId(R.id.form_creator_input)).perform(scrollTo(), replaceText(creators));

        // Genres
        String genres = "TheGenres1, TheGenres2";
        onView(withId(R.id.form_genres_input)).perform(scrollTo(), replaceText(genres));

        // Release date
        onView(withId(R.id.form_release_date_button)).perform(scrollTo(), click());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -6);
        Date releaseDate = c.getTime();
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH)));
        onView(withText(android.R.string.ok)).perform(click());

        // Episode runtime
        int runtime = 50;
        onView(withId(R.id.form_duration_input)).perform(scrollTo(), replaceText(String.valueOf(runtime)));

        // Summary
        String description = "This is the description.";
        onView(withId(R.id.form_description_input)).perform(scrollTo(), replaceText(description));

        // Episodes
        int episodes = 200;
        onView(withId(R.id.form_episodes_number_input)).perform(scrollTo(), replaceText(String.valueOf(episodes)));

        // Seasons
        int seasons = 12;
        onView(withId(R.id.form_seasons_number_input)).perform(scrollTo(), replaceText(String.valueOf(seasons)));

        // In production
        onView(withId(R.id.form_in_production_input)).perform(scrollTo(), click());

        // Notes
        String notes = "These are my notes.";
        onView(withId(R.id.form_user_comment_input)).perform(scrollTo(), replaceText(notes));

        // Importance level
        ImportanceLevel importanceLevel = ImportanceLevel.MEDIUM;
        onView(withId(R.id.form_importance_level_button)).perform(scrollTo(), click());
        onView(withText(importanceLevel.getName(context))).perform(click());

        // Owned
        onView(withId(R.id.form_owned_input)).perform(scrollTo(), click());

        // Save
        onView(withId(R.id.action_save)).perform(click());

        // Get the new media item from the database
        MediaItemsAbstractController controller = createdFakeCategory.getMediaType().getController();
        List<MediaItem> mediaItems = controller.getTrackedMediaItemsInCategory(0, createdFakeCategory);
        assertTrue("No media items", mediaItems!=null && mediaItems.size()>0);
        TVShow tvShow = null;
        for(MediaItem mediaItem: mediaItems)
        {
            if(title.equals(mediaItem.getTitle()) && mediaItem instanceof TVShow)
            {
                tvShow = (TVShow) mediaItem;
                break;
            }
        }
        assertTrue("The new media items is not in the list", tvShow!=null);

        // Check data
        assertTrue("Wrong creators", creators.equals(tvShow.getCreatedBy()));
        assertTrue("Wrong genres", genres.equals(tvShow.getGenres()));
        Calendar c1 = Calendar.getInstance(); c1.setTime(releaseDate);
        Calendar c2 = Calendar.getInstance(); c2.setTime(tvShow.getReleaseDate());
        assertTrue("Wrong release date", c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH)==c2.get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH)==c2.get(Calendar.DAY_OF_MONTH));
        assertTrue("Wrong runtime", runtime==tvShow.getEpisodeRuntimeMin());
        assertTrue("Wrong summary", description.equals(tvShow.getDescription()));
        assertTrue("Wrong episodes", episodes==tvShow.getEpisodesNumber());
        assertTrue("Wrong seasons", seasons==tvShow.getSeasonsNumber());
        assertTrue("Wrong in production", tvShow.isInProduction());
        assertTrue("Wrong notes", notes.equals(tvShow.getUserComment()));
        assertTrue("Wrong importance level", importanceLevel.equals(tvShow.getImportanceLevel()));
        assertTrue("Wrong owned", tvShow.isOwned());
        assertFalse("Wrong upcoming", tvShow.isUpcoming());
        assertFalse("Wrong doing now", tvShow.isDoingNow());
        assertFalse("Wrong completed", tvShow.isCompleted());
    }

    /**
     * Checks if the back button asks for confirmation if there are unsaved changes
     */
    @Test
    public void testBackButton()
    {
        // Write director value
        onView(withId(R.id.form_creator_input)).perform(scrollTo(), replaceText("TheDirector"));

        // Press back
        pressBack();

        // Error should be shown
        onView(withText(R.string.form_exit_error)).check(matches(isDisplayed()));

        // Click on "keep editing"
        onView(withText(R.string.form_exit_error_keep)).perform(click());

        // We should still be in the form
        onView(withId(R.id.form_container)).check(matches(isDisplayed()));
    }

    /**
     * Tests if after onPause -> onResume (e.g. activity loses focus because of dialog) the form is still displayed correctly and the inputs don't lose their data
     */
    @Test
    public void testPauseResume()
    {
        // Get monitor and activity
        lifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance();
        activity = activityRule.getActivity();

        // Set values
        beforeEventFormSetup();

        // Need to run on UI thread
        latch = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                // Check state "Resumed"
                assertTrue(lifecycleMonitor.getActivitiesInStage(Stage.RESUMED).contains(activity));

                // Pause
                getInstrumentation().callActivityOnPause(activity);

                // Check state "Paused"
                assertTrue(lifecycleMonitor.getActivitiesInStage(Stage.PAUSED).contains(activity));

                // Resume
                getInstrumentation().callActivityOnResume(activity);

                // Check state "Resumed"
                assertTrue(lifecycleMonitor.getActivitiesInStage(Stage.RESUMED).contains(activity));

                // Synchronize with the test thread
                latch.countDown();
            }
        });
        try
        {
            latch.await();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        // Check values
        afterEventFromCheck();
    }

    /**
     * Tests if after onPause -> onStop -> onRestart -> onStart -> onResume (e.g. activity is moved to the background and then restored) the form is still displayed correctly and the inputs don't lose their data
     */
    @Test
    public void testStopRestart()
    {
        // Get monitor and activity
        lifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance();
        activity = activityRule.getActivity();

        // Set values
        beforeEventFormSetup();

        // Need to run on UI thread
        latch = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                // Check state "Resumed"
                assertTrue(lifecycleMonitor.getActivitiesInStage(Stage.RESUMED).contains(activity));

                // Pause and then stop
                getInstrumentation().callActivityOnPause(activity);
                getInstrumentation().callActivityOnStop(activity);

                // Check state "Stopped"
                assertTrue(lifecycleMonitor.getActivitiesInStage(Stage.STOPPED).contains(activity));

                // Resume
                getInstrumentation().callActivityOnRestart(activity);
                getInstrumentation().callActivityOnStart(activity);
                getInstrumentation().callActivityOnResume(activity);

                // Check state "Resumed"
                assertTrue(lifecycleMonitor.getActivitiesInStage(Stage.RESUMED).contains(activity));

                // Synchronize with the test thread
                latch.countDown();
            }
        });
        try
        {
            latch.await();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        // Check values
        afterEventFromCheck();
    }

    /**
     * Tests if after a device orientation change the form is still displayed correctly and the inputs don't lose their data
     */
    @Test
    public void testOrientationChange()
    {
        // Set values
        beforeEventFormSetup();

        // Rotate device
        onView(isRoot()).perform(orientationLandscape());

        // Check values
        afterEventFromCheck();
    }



    /************************************** HELPERS ******************************************/

    /**
     * Sets some fields in the form
     */
    private void beforeEventFormSetup()
    {
        // Write director value
        onView(withId(R.id.form_creator_input)).perform(scrollTo(), replaceText("Test creator"));

        // Set owned
        onView(withId(R.id.form_owned_input)).perform(scrollTo(), click());

        // Select importance level
        onView(withId(R.id.form_importance_level_button)).perform(scrollTo(), click());
        onView(withText(ImportanceLevel.HIGH.getName(context))).perform(click());
    }

    /**
     * Checks some fields in the form
     */
    private void afterEventFromCheck()
    {
        // Check director value
        onView(withId(R.id.form_creator_input)).check(matches(withText("Test creator")));

        // Check importance level button text
        onView(withId(R.id.form_importance_level_button)).perform(scrollTo()).check(matches(withText(ImportanceLevel.HIGH.getName(context))));

        // Check owned
        onView(withId(R.id.form_owned_input)).perform(scrollTo()).check(matches(isChecked()));

        // Check importance level selected value
        onView(withId(R.id.form_importance_level_button)).perform(scrollTo(), click());
        onView(withText(ImportanceLevel.HIGH.getName(context))).check(matches(isChecked()));
    }
}
