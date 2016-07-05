package it.polimi.dima.mediatracker.controllers;

import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.MediaType;
import it.polimi.dima.mediatracker.model.Movie;
import it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils;
import it.polimi.dima.mediatracker.utils.Utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the media items controller
 *
 * Note: the test for the list order is in a separate class
 */
public class MediaItemsControllerTest
{
    private static MediaItemsAbstractController controller;
    private static CategoriesController categoriesController;
    private static Category createdFakeCategory;

    private static Date lastYear;

    @BeforeClass
    public static void beforeClass()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        lastYear = calendar.getTime();
    }

    @Before
    public void before()
    {
        categoriesController = CategoriesController.getInstance();
        createdFakeCategory = new Category(InstrumentationRegistry.getTargetContext(), InstrumentationTestUtils.getRandomName(), R.color.colorPrimary, MediaType.MOVIES);
        categoriesController.saveCategory(createdFakeCategory);

        controller = createdFakeCategory.getMediaType().getController();
    }

    @After
    public void after()
    {
        if(createdFakeCategory!=null) categoriesController.deleteCategory(createdFakeCategory);
    }

    /**
     * Tests both saveMediaItem() and getMediaItemById()
     */
    @Test
    public void testSaveMediaItemAndGetMediaItemById()
    {
        MediaItem toSave = createMediaItem(null, false);

        controller.saveMediaItem(toSave);

        MediaItem saved = controller.getMediaItemById(createdFakeCategory, toSave.getId());

        assertNotNull("Saved is null", saved);

        assertEquals("Wrong media item", toSave, saved);
    }

    /**
     * Tests deleteMediaItem()
     */
    @Test
    public void testDeleteMediaItem()
    {
        MediaItem toSave = createMediaItem(null, false);

        controller.saveMediaItem(toSave);

        Long savedId = toSave.getId();

        controller.deleteMediaItem(toSave);

        MediaItem shouldBeNull = controller.getMediaItemById(createdFakeCategory, savedId);

        assertNull("Deleted is not null", shouldBeNull);
    }

    /**
     * Tests deleteAllMediaItemsInCategory()
     */
    @Test
    public void testDeleteAllMediaItemsInCategory()
    {
        initTrackedAndCompleted(12, 8);

        controller.deleteAllMediaItemsInCategory(createdFakeCategory);

        List<MediaItem> tracked = controller.getTrackedMediaItemsInCategory(0, createdFakeCategory);
        assertTrue("Tracked are not null", tracked==null || tracked.size()==0);

        List<MediaItem> completed = controller.getCompletedMediaItemsInCategory(0, createdFakeCategory);
        assertTrue("Completed are not null", completed==null || completed.size()==0);
    }

    /**
     * Tests getTrackedMediaItems()
     */
    @Test
    public void testGetTrackedMediaItems()
    {
        initTrackedAndCompleted(6, 4);

        List<MediaItem> tracked = controller.getTrackedMediaItemsInCategory(0, createdFakeCategory);

        assertNotNull("Tracked are null", tracked);

        assertEquals("Wrong tracked", 6, tracked.size());
    }

    /**
     * Tests getCompletedMediaItems()
     */
    @Test
    public void testGetCompletedMediaItems()
    {
        initTrackedAndCompleted(6, 4);

        List<MediaItem> completed = controller.getCompletedMediaItemsInCategory(0, createdFakeCategory);

        assertNotNull("Tracked are null", completed);

        assertEquals("Wrong completed", 4, completed.size());
    }

    /**
     * Tests searchMediaItemsInCategory()
     */
    @Test
    public void testSearchMediaItemsInCategory()
    {
        List<MediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(createMediaItem("QWER", false)); // 0
        mediaItems.add(createMediaItem("TYUI", true)); // 1
        mediaItems.add(createMediaItem("OPAS", false)); // 2
        mediaItems.add(createMediaItem("DFGH", false)); // 3
        mediaItems.add(createMediaItem("JKLZ", false)); // 4
        mediaItems.add(createMediaItem("XCVB", true)); // 5
        mediaItems.add(createMediaItem("AAVBAA", false)); // 6
        for(MediaItem mi: mediaItems)
        {
            controller.saveMediaItem(mi);
        }

        List<MediaItem> shouldBe = new ArrayList<>();



        shouldBe.clear();
        shouldBe.add(mediaItems.get(0));
        assertSearch("ER", false, shouldBe);

        shouldBe.clear();
        shouldBe.add(mediaItems.get(0));
        assertSearch("ER", null, shouldBe);

        shouldBe.clear();
        assertSearch("ER", true, shouldBe);




        shouldBe.clear();
        assertSearch("TYUI", false, shouldBe);

        shouldBe.clear();
        shouldBe.add(mediaItems.get(1));
        assertSearch("TYUI", null, shouldBe);

        shouldBe.clear();
        shouldBe.add(mediaItems.get(1));
        assertSearch("TYUI", true, shouldBe);





        shouldBe.clear();
        assertSearch("AAAAAAA", null, shouldBe);




        shouldBe.clear();
        shouldBe.add(mediaItems.get(6));
        assertSearch("VB", false, shouldBe);

        shouldBe.clear();
        shouldBe.add(mediaItems.get(5));
        shouldBe.add(mediaItems.get(6));
        assertSearch("VB", null, shouldBe);

        shouldBe.clear();
        shouldBe.add(mediaItems.get(5));
        assertSearch("VB", true, shouldBe);
    }

    /**
     * Tests getMediaItemsReleasedToday()
     */
    @Test
    public void testGetMediaItemsReleasedToday()
    {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date todayEnd = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        Date yesterday = calendar.getTime();

        List<MediaItem> shouldBe = new ArrayList<>();
        for(int i=0; i<10; i++)
        {
            MediaItem mi = createMediaItem(null, false);

            if(i==0 || i==1 || i==8)
            {
                mi.setReleaseDate(now);
                shouldBe.add(mi);
            }
            else if(i==3)
            {
                mi.setReleaseDate(todayStart);
                shouldBe.add(mi);
            }
            else if(i==6)
            {
                mi.setReleaseDate(todayEnd);
                shouldBe.add(mi);
            }
            else if(i==2 || i==5)
            {
                mi.setReleaseDate(tomorrow);
            }
            else if(i==4)
            {
                mi.setReleaseDate(yesterday);
            }
            else
            {
                mi.setReleaseDate(null);
            }

            controller.saveMediaItem(mi);
        }

        List releasedToday = controller.getMediaItemsReleasedToday(createdFakeCategory);

        for(MediaItem s: shouldBe)
        {
            assertTrue(s+" not in released today", releasedToday.contains(s));
        }
    }

    /**
     * Tests validateMediaItem()
     */
    @Test
    public void testValidateMediaItem()
    {
        controller.saveMediaItem(createMediaItem("AlreadyInDB", true));

        MediaItem mediaItem;

        assertValidate(false, null);

        mediaItem = new Movie();
        assertValidate(false, mediaItem);

        mediaItem = new Movie();
        mediaItem.setCategory(createdFakeCategory.getId());
        assertValidate(false, mediaItem);

        mediaItem = new Movie();
        mediaItem.setTitle("Valid");
        assertValidate(false, mediaItem);

        mediaItem = new Movie();
        mediaItem.setTitle("AlreadyInDB");
        mediaItem.setCategory(createdFakeCategory.getId());
        assertValidate(false, mediaItem);

        mediaItem = new Movie();
        mediaItem.setTitle("Valid");
        mediaItem.setCategory(createdFakeCategory.getId());
        assertValidate(true, mediaItem);
        assertNotNull("The importance level is not initialized", mediaItem.getImportanceLevel());
    }











    /***************************** HELPERS ******************************/

    private void assertValidate(boolean valid, MediaItem mediaItem)
    {
        assertEquals("Wrong validation for "+mediaItem, valid, controller.validateMediaItem(InstrumentationRegistry.getTargetContext(), mediaItem)==null);
    }

    private void assertSearch(String search, Boolean completed, List<MediaItem> shouldBe)
    {
        List<MediaItem> found = controller.searchMediaItemsInCategory(0, createdFakeCategory, search, completed);

        assertEquals("Wrong number of search results", shouldBe.size(), found.size());

        for(MediaItem s: shouldBe)
        {
            assertTrue(s+" not found in results", found.contains(s));
        }
    }

    private List<MediaItem> initTrackedAndCompleted(int trackedNum, int completedNum)
    {
        List<MediaItem> added = new ArrayList<>();

        for(int t=0, c=0; t+c<trackedNum+completedNum; )
        {
            if( c>=completedNum || (t<trackedNum && Utils.randInt(0, 1)==0) )
            {
                added.add(createMediaItem(null, false));
                t++;
            }
            else
            {
                added.add(createMediaItem(null, true));
                c++;
            }
        }

        for(MediaItem mi: added)
        {
            controller.saveMediaItem(mi);
        }

        return added;
    }

    private MediaItem createMediaItem(String name, boolean completed)
    {
        MediaItem mi = new Movie();

        mi.setTitle(name==null ? InstrumentationTestUtils.getRandomName() : name);
        mi.setImportanceLevel(ImportanceLevel.HIGH);
        mi.setCategory(createdFakeCategory.getId());
        if(completed) mi.setCompletionDate(lastYear);

        return mi;
    }
}
