package it.polimi.dima.mediatracker.controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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

import static junit.framework.Assert.assertTrue;

/**
 * Tests the media items controller (moved this list test in a separate class for readability,
 * since it needs several helpers and fields)
 */
public class MediaItemsControllerListOrderTest
{
    private final static int OPERATIONS = 3000;

    private final static int ADD_PROBABILITY = 20;
    private final static int DELETE_PROBABILITY = 20;
    private final static int UPDATE_IL_PROBABILITY = 10;

    private final static int INITIAL_ELEMENTS = 20;

    private MediaItemsAbstractController controller;
    private CategoriesController categoriesController;

    private Category createdFakeCategory;
    private List<MediaItem> mediaItems;

    private Date nextYear;
    private Date lastYear;
    ImportanceLevel[] importanceLevels;

    @Before
    public void setUp()
    {
        // Create category
        categoriesController = CategoriesController.getInstance();
        createdFakeCategory = new Category("colorPrimary", InstrumentationTestUtils.getRandomName(), R.color.colorPrimary, MediaType.MOVIES);
        categoriesController.saveCategory(createdFakeCategory);

        // Get media items controller
        controller = createdFakeCategory.getMediaType().getController();

        // Dates
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        nextYear = calendar.getTime();
        calendar.add(Calendar.YEAR, -2);
        lastYear = calendar.getTime();

        // Importance levels
        importanceLevels = ImportanceLevel.values();
    }

    @After
    public void tearDown()
    {
        // Remove category and all media items in it
        categoriesController.deleteCategory(createdFakeCategory);
    }

    /**
     * Tests the stored order in the media items list
     */
    @Test
    public void testMediaItemsListOrder()
    {
        // Load first list
        reloadListFromDb();

        // Loop for all operations on the list
        boolean initialized = false;
        for(int i=0; i<OPERATIONS+INITIAL_ELEMENTS; i++)
        {
            // Get random to decide an action to do
            int action = Utils.randInt(0, 100);

            // Set initialized flag if we created the initial elements
            if(!initialized && mediaItems.size()>=INITIAL_ELEMENTS) initialized = true;

            // Create a new element if we have no items OR if we initializing OR if we fall in its probability
            if(mediaItems.size()==0 || !initialized  || action>(100-ADD_PROBABILITY) )
            {
                System.out.println("########### adding ###########");

                MediaItem newMediaItem = createRandomMediaItem();

                controller.setMediaItemOrderInSectionBeforeInserting(createdFakeCategory, newMediaItem);
                controller.saveMediaItem(newMediaItem);
                reloadListFromDb();

                System.out.println("########### [DUMP] " + dumpList());
            }

            // Delete media item
            else if(action<DELETE_PROBABILITY)
            {
                int pos = Utils.randInt(0, mediaItems.size()-1);
                System.out.println("########### removing "+pos+" ###########");

                controller.deleteMediaItem(mediaItems.get(pos));
                mediaItems.remove(pos);

                System.out.println("########### [DUMP] " + dumpList());
            }

            // Update media item importance level
            else if(action>=DELETE_PROBABILITY && action<=DELETE_PROBABILITY+UPDATE_IL_PROBABILITY)
            {
                int pos = Utils.randInt(0, mediaItems.size()-1);
                MediaItem mi = mediaItems.get(pos);

                int currentIL = Arrays.asList(importanceLevels).indexOf(mi.getImportanceLevel());
                int newIL = currentIL;
                while(currentIL==newIL)
                {
                    newIL = Utils.randInt(0, importanceLevels.length-1);
                }
                mi.setImportanceLevel(importanceLevels[newIL]);

                System.out.println("########### updating "+pos+" ("+importanceLevels[currentIL]+"->"+importanceLevels[newIL]+") ###########");

                controller.setMediaItemOrderInSectionBeforeUpdatingImportanceLevel(createdFakeCategory, mediaItems.get(pos));
                controller.saveMediaItem(mediaItems.get(pos));
                reloadListFromDb();

                System.out.println("########### [DUMP] " + dumpList());
            }

            // Move media item in the list
            else
            {
                if(mediaItems.get(0).isUpcoming()) continue;

                int upcomingEnd = mediaItems.size()-1;
                for(int j=upcomingEnd; j>=0; j--)
                {
                    upcomingEnd = j;
                    if(!mediaItems.get(upcomingEnd).isUpcoming()) break;
                }

                int from = Utils.randInt(0, upcomingEnd);
                int to = Utils.randInt(0, upcomingEnd);
                System.out.println("########### moving " + from + "->" + to + "###########");

                moveMediaItem(from, to);

                if(to!=mediaItems.size()-1 && !mediaItems.get(to+1).isUpcoming() && !mediaItems.get(to+1).getImportanceLevel().equals(mediaItems.get(to).getImportanceLevel()))
                {
                    System.out.println("0000000000000000 set next il !"+mediaItems.get(to+1).getImportanceLevel()+"["+mediaItems.get(to+1).getId()+"].equals("+mediaItems.get(to).getImportanceLevel()+"["+mediaItems.get(to).getId()+"]");
                    mediaItems.get(to).setImportanceLevel(mediaItems.get(to+1).getImportanceLevel());
                }
                else if(to!=0 && !mediaItems.get(to-1).isDoingNow() && !mediaItems.get(to-1).getImportanceLevel().equals(mediaItems.get(to).getImportanceLevel()))
                {
                    System.out.println("0000000000000000 set prev il !"+mediaItems.get(to-1).getImportanceLevel()+"["+mediaItems.get(to-1).getId()+"].equals("+mediaItems.get(to).getImportanceLevel()+"["+mediaItems.get(to).getId()+"]");
                    mediaItems.get(to).setImportanceLevel(mediaItems.get(to-1).getImportanceLevel());
                }

                MediaItem previousMediaItemInSection = null;
                if(to>0)
                {
                    MediaItem temp = mediaItems.get(to-1);
                    if(!temp.isDoingNow() && mediaItems.get(to).getImportanceLevel().equals(temp.getImportanceLevel()))
                    {
                        previousMediaItemInSection = temp;
                    }
                }

                MediaItem nextMediaItemInSection = null;
                if(to<mediaItems.size()-1)
                {
                    MediaItem temp = mediaItems.get(to+1);
                    if(!temp.isUpcoming() && mediaItems.get(to).getImportanceLevel().equals(temp.getImportanceLevel()))
                    {
                        nextMediaItemInSection = temp;
                    }
                }

                controller.setOrderInSectionAfterMove(createdFakeCategory, mediaItems.get(to), previousMediaItemInSection, nextMediaItemInSection);

                System.out.println("########### [DUMP] " + dumpList());
            }

            // Check if the order is correct (after each iteration)
            assertOrder();
        }
    }







    /*********************************************** HELPERS ***********************************************/

    /**
     * Checks if the media items order is correct
     */
    private void assertOrder()
    {
        if(mediaItems.size()>1)
        {
            for(int i=1; i<mediaItems.size(); i++)
            {
                if(!mediaItems.get(i).isUpcoming())
                {
                    if(mediaItems.get(i-1).getImportanceLevel().equals(mediaItems.get(i).getImportanceLevel()) && mediaItems.get(i-1).getOrderInSection()>=mediaItems.get(i).getOrderInSection())
                    {
                        assertTrue("Wrong order at "+i, false);
                    }
                }
            }
        }
    }

    /**
     * Reloads the complete media items list from the database
     */
    private void reloadListFromDb()
    {
        mediaItems = new ArrayList<>();
        List<MediaItem> temp;

        int page = 0;
        while(true)
        {
            temp = controller.getTrackedMediaItemsInCategory(page, createdFakeCategory);
            page++;
            if(temp!=null && temp.size()>0) mediaItems.addAll(temp);
            else break;
        }
    }

    /**
     * Creates a random movie
     */
    private MediaItem createRandomMediaItem()
    {
        MediaItem mi = new Movie();

        // Title
        mi.setTitle(InstrumentationTestUtils.getRandomName());

        // Importance level
        mi.setImportanceLevel(importanceLevels[Utils.randInt(0, importanceLevels.length-1)]);

        // Release date
        int u = Utils.randInt(0, 4);
        if(u==0) mi.setReleaseDate(nextYear);
        else mi.setReleaseDate(lastYear);

        // Category
        mi.setCategory(createdFakeCategory.getId());

        // Doing now
        mi.setDoingNow(false);

        return mi;
    }

    /**
     * Debug method to get the current list as a string
     */
    private String dumpList()
    {
        String temp = "";
        String prevSection = null;
        String section;
        for(MediaItem mi: mediaItems)
        {
            section = mi.isUpcoming() ? "UPCOMING" : (mi.isDoingNow() ? "DOING_NOW" : mi.getImportanceLevel().name());
            if(!section.equals(prevSection))
            {
                temp += "|"+section+"|";
                prevSection = section;
            }

            temp += "["+mi.getId()+","+mi.getOrderInSection()+"]";
        }
        return temp;
    }

    /**
     * Helper to move a media item of the current list
     */
    private void moveMediaItem(int from, int to)
    {
        if(from==to) return;

        if(from < to)
        {
            for(int i=from; i<to; i++)
            {
                Collections.swap(mediaItems, i, i+1);
            }
        }
        else
        {
            for(int i = from; i > to; i--)
            {
                Collections.swap(mediaItems, i, i - 1);
            }
        }
    }
}
