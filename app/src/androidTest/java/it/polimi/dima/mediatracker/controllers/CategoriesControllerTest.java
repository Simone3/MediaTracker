package it.polimi.dima.mediatracker.controllers;

import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaType;
import it.polimi.dima.mediatracker.test_utils.InstrumentationTestUtils;
import it.polimi.dima.mediatracker.utils.Utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Tests the media items controller
 *
 * Note: the test for the list order is in a separate class
 */
public class CategoriesControllerTest
{
    private static CategoriesController controller;
    private static List<Category> testedCategories = new ArrayList<>();

    private int INITIAL_CATEGORIES;

    @Before
    public void before()
    {
        controller = CategoriesController.getInstance();
        INITIAL_CATEGORIES = Utils.iteratorToList(controller.getAllCategories()).size();
    }

    @After
    public void after()
    {
        if(testedCategories.size()>0)
        {
            for(Category c: testedCategories) controller.deleteCategory(c);
            testedCategories.clear();
        }
    }

    /**
     * Tests saveCategory() and GetCategoryById()
     */
    @Test
    public void testSaveCategoryAndGetCategoryById()
    {
        initCategory(null, MediaType.BOOKS);

        controller.saveCategory(testedCategories.get(0));

        Category saved = controller.getCategoryById(testedCategories.get(0).getId());

        assertNotNull("Saved is null", saved);

        assertEquals("Wrong category", testedCategories.get(0), saved);
    }

    /**
     * Tests deleteCategory()
     */
    @Test
    public void testDeleteCategory()
    {
        initCategory(null, MediaType.BOOKS);

        controller.saveCategory(testedCategories.get(0));

        Long savedId = testedCategories.get(0).getId();

        controller.deleteCategory(testedCategories.get(0));

        Category shouldBeNull = controller.getCategoryById(savedId);

        assertNull("Deleted is not null", shouldBeNull);
    }

    /**
     * Tests getAllCategories()
     */
    @Test
    public void testGetAllCategories()
    {
        initCategory(null, MediaType.BOOKS);
        initCategory(null, MediaType.VIDEOGAMES);
        initCategory(null, MediaType.TV_SHOWS);
        initCategory(null, MediaType.BOOKS);
        initCategory(null, MediaType.MOVIES);

        for(Category c: testedCategories) controller.saveCategory(c);

        Iterator<Category> allIt = controller.getAllCategories();
        List<Category> all = Utils.iteratorToList(allIt);

        assertNotNull("All are null", allIt);

        assertEquals("Wrong all categories count", INITIAL_CATEGORIES+5, all.size());

        for(Category c: testedCategories)
        {
            assertTrue(c+" not in results", all.contains(c));
        }
    }

    /**
     * Tests addDefaultCategoriesIfEmpty()
     */
    @Test
    public void testAddDefaultCategoriesIfEmpty()
    {
        // Make the controller believe that there are no categories
        CategoriesController mockedController = spy(controller);
        doReturn(Collections.emptyIterator()).when(mockedController).getAllCategories();

        mockedController.addDefaultCategoriesIfEmpty(InstrumentationRegistry.getTargetContext());

        List<Category> all = Utils.iteratorToList(controller.getAllCategories());
        testedCategories.addAll(all);

        assertNotNull("Default categories not created", all);

        List<MediaType> mediaTypes = Arrays.asList(MediaType.values());

        assertEquals("Wrong number of default categories", INITIAL_CATEGORIES+mediaTypes.size(), all.size());

        for(Category c: all)
        {
            assertTrue(c+" is wrong default category", mediaTypes.contains(c.getMediaType()));
        }


        controller.addDefaultCategoriesIfEmpty(InstrumentationRegistry.getTargetContext());
        all = Utils.iteratorToList(controller.getAllCategories());
        assertEquals("Default categories created twice", INITIAL_CATEGORIES+mediaTypes.size(), all.size());
    }

    /**
     * Tests validateCategory()
     */
    @Test
    public void testValidateCategory()
    {
        initCategory("AlreadyInDB", MediaType.MOVIES);
        controller.saveCategory(testedCategories.get(0));

        Category category;

        assertValidate(false, null);

        category = new Category();
        assertValidate(false, category);

        category = new Category();
        category.setMediaType(MediaType.BOOKS);
        assertValidate(false, category);

        category = new Category();
        category.setName("Valid");
        assertValidate(false, category);

        category = new Category();
        category.setName("AlreadyInDB");
        category.setMediaType(MediaType.BOOKS);
        assertValidate(false, category);

        category = new Category();
        category.setName("Valid");
        category.setMediaType(MediaType.BOOKS);
        assertValidate(true, category);
    }








    /********************* HELPERS *******************/


    private void initCategory(String name, MediaType type)
    {
        testedCategories.add(new Category(InstrumentationRegistry.getTargetContext(), name==null?InstrumentationTestUtils.getRandomName():name, R.color.colorPrimary, type));
    }

    private void assertValidate(boolean valid, Category category)
    {
        assertEquals("Wrong validation for "+category, valid, controller.validateCategory(InstrumentationRegistry.getTargetContext(), category)==null);
    }
}
