package it.polimi.dima.mediatracker.activities;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;

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
import it.polimi.dima.mediatracker.controllers.ScreenController;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.MediaType;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.getRandomName;
import static it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils.recyclerViewAtPosition;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Espresso UI Test: tests several operations on the Media Items List page, e.g. click on a media item, search, etc.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class MediaItemsListActivityUITest
{
    private static Context context;

    private static Category currentCategory;
    private static Category createdFakeCategory;

    private static List<MediaItem> mediaItems;
    private static MediaItem createdFakeMediaItem;

    @Rule
    public ActivityTestRule<CategoryActivity> activityRule = new ActivityTestRule<CategoryActivity>(CategoryActivity.class)
    {
        @Override
        protected Intent getActivityIntent()
        {
            return ScreenController.getCategoryPageIntent(context, currentCategory, null);
        }
    };

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
        currentCategory = categoriesIterator.next();

        // Get items in the category
        mediaItems = currentCategory.getMediaType().getController().getTrackedMediaItemsInCategory(0, currentCategory);

        // Add fake media item if empty
        if(mediaItems==null || mediaItems.size()<=0)
        {
            createdFakeMediaItem = currentCategory.getMediaType().getController().initializeEmptyMediaItem();
            createdFakeMediaItem.setTitle(getRandomName());
            createdFakeMediaItem.setCategory(currentCategory.getId());
            createdFakeMediaItem.setImportanceLevel(ImportanceLevel.HIGH);
            System.out.println(createdFakeMediaItem.getTitle());
            currentCategory.getMediaType().getController().saveMediaItem(createdFakeMediaItem);
            System.out.println(createdFakeMediaItem.getId());
            mediaItems = currentCategory.getMediaType().getController().getTrackedMediaItemsInCategory(0, currentCategory);
            System.out.println(mediaItems);
        }
    }

    @AfterClass
    public static void afterClass()
    {
        // Delete fake media item if needed
        if(createdFakeMediaItem!=null)
        {
            currentCategory.getMediaType().getController().deleteMediaItem(createdFakeMediaItem);
        }

        // Delete fake category if needed
        if(createdFakeCategory!=null)
        {
            CategoriesController.getInstance().deleteCategory(createdFakeCategory);
        }
    }

    /**
     * Tests click on a media item
     */
    @Test
    public void testMediaItemClick()
    {
        // Get first media item
        MediaItem mediaItemToSelect = mediaItems.get(0);

        // Get first list element clickable part
        ViewInteraction element = onView(allOf(withId(R.id.item_clickable_part), hasDescendant(withText(mediaItemToSelect.getTitle()))));

        // Check if it exists
        element.check(matches(isDisplayed()));

        // Click on it
        element.perform(click());

        // Check if we are in the form
        onView(withId(R.id.form_container)).check(matches(isDisplayed()));

        // Check name input
        onView(withId(R.id.form_title_input)).check(matches(withText(mediaItemToSelect.getTitle())));
    }

    /**
     * Tests click on edit option on a media item
     */
    @Test
    public void testMediaItemEditOption()
    {
        // Get first media item
        MediaItem mediaItemToSelect = mediaItems.get(0);

        // Click on first list element options button
        onView(allOf(withId(R.id.item_options_button), isDescendantOfA(hasSibling(allOf(withId(R.id.item_clickable_part), hasDescendant(withText(mediaItemToSelect.getTitle()))))))).perform(click());

        // Check if the option menu is shown
        onView(withText(context.getString(R.string.edit))).check(matches(isDisplayed()));

        // Click on edit
        onView(withText(context.getString(R.string.edit))).perform(click());

        // Check if we are in the form
        onView(withId(R.id.form_container)).check(matches(isDisplayed()));

        // Check name input
        onView(withId(R.id.form_title_input)).check(matches(withText(mediaItemToSelect.getTitle())));
    }

    /**
     * Tests search in list
     */
    @Test
    public void testSearch()
    {
        // Get first media item
        MediaItem mediaItemToSelect = mediaItems.get(0);
        String name = mediaItemToSelect.getTitle();

        // Check that first element is a section (= not searching)
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(0, hasDescendant(withId(R.id.list_section_title)))));

        // Write part of the name in the search
        String partOfName = name.length()<=2 ? name.substring(0,1) : name.substring(1,name.length()-2);
        onView(withId(R.id.action_search)).perform(click());
        onView(withId(R.id.search_src_text)).perform(typeText(partOfName), pressKey(KeyEvent.KEYCODE_ENTER));

        // Check that first element is not a section (= searching)
        onView(withId(R.id.media_items_list)).check(matches(recyclerViewAtPosition(0, hasDescendant(withId(R.id.item_clickable_part)))));

        // Check that the media item is present
        onView(allOf(withId(R.id.item_options_button), isDescendantOfA(hasSibling(allOf(withId(R.id.item_clickable_part), hasDescendant(withText(mediaItemToSelect.getTitle()))))))).check(matches(isDisplayed()));
    }

    /**
     * Tests action button to add new media item
     */
    @Test
    public void testListFAB()
    {
        // Click on the FAB
        onView(withId(R.id.items_fab)).perform(click());

        // Check if we are in the form
        onView(withId(R.id.form_container)).check(matches(isDisplayed()));

        // Check name is empty
        onView(withId(R.id.form_title_input)).check(matches(withText("")));
    }
}