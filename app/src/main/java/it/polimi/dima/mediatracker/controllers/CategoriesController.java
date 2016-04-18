package it.polimi.dima.mediatracker.controllers;


import android.content.Context;
import android.content.res.TypedArray;

import java.util.HashMap;
import java.util.Iterator;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaType;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Controller for categories that allows to query the database to retrieve data
 */
public class CategoriesController
{
    private static CategoriesController instance;

    /**
     * Singleton pattern
     */
    public static synchronized CategoriesController getInstance()
    {
        if(instance==null) instance = new CategoriesController();
        return instance;
    }



    /************************************************ DB SELECT QUERIES ************************************************/


    /**
     * Getter
     * @return an iterator containing all categories
     */
    public Iterator<Category> getAllCategories()
    {
        return Category.findAll(Category.class);
    }

    /**
     * Getter
     * @param categoryId the ID of the category
     * @return the category with the given ID or null if not found
     */
    public Category getCategoryById(Long categoryId)
    {
        return Category.findById(Category.class, categoryId);
    }

    /**
     * Checks if a category name is already in use
     * @param currentCategoryId if not null, the retrieved category must have an id DIFFERENT from this one (otherwise we would match the category itself if already in the database!)
     * @param name the name to search
     * @return true if a category with the same name already exists
     */
    private boolean doesCategoryWithSameNameAlreadyExist(Long currentCategoryId, String name)
    {
        return Category.count(Category.class, "name = ? AND id != ?", new String[]{name, String.valueOf(currentCategoryId)}) > 0;
    }



    /************************************************ DB INSERT QUERIES ************************************************/


    /**
     * If the categories table is empty, adds the default categories (i.e. those corresponding to all media types)
     * @param context the context
     */
    public void addDefaultCategoriesIfEmpty(Context context)
    {
        // Get all categories
        Iterator<Category> iterator = getAllCategories();

        // If the iterator is empty...
        if(!iterator.hasNext())
        {
            // Add a category for each media type
            Category category;
            for(MediaType mediaType: MediaType.values())
            {
                category = new Category(context, mediaType.getNamePlural(context), mediaType.getColor(), mediaType);
                category.save();
            }
        }
    }

    /**
     * Inserts or updates a category in the database
     * @param category the category to update/insert
     */
    public void saveCategory(Category category)
    {
        category.save();
    }


    /************************************************ DB DELETE QUERIES ************************************************/

    /**
     * Removes a category from the database (together with all media items associated to it)
     * @param category the category to remove
     */
    public void deleteCategory(Category category)
    {
        category.getMediaType().getController().deleteAllMediaItemsInCategory(category);
        category.delete();
    }


    /************************************************ MISC HELPERS ************************************************/

    /**
     * Validates a category, possibly fixing what is wrong or, if impossible, returning an error string
     * @param context the context
     * @param category the category to validate
     * @return null if no error (or errors were fixed by this method), a string containing the error otherwise
     */
    public String validateCategory(Context context, Category category)
    {
        // Null category
        if(category==null)
        {
            return context.getString(R.string.validation_category_null);
        }

        // Check empty name
        if(Utils.isEmpty(category.getName()))
        {
            return context.getString(R.string.validation_category_empty_name);
        }

        // Check category with same name
        if(doesCategoryWithSameNameAlreadyExist(category.getId(), category.getName()))
        {
            return context.getString(R.string.validation_category_same_name);
        }

        // Check media type
        try
        {
            // Check if empty or no enum value with the saved name (exception)
            if(category.getMediaType()==null)
            {
                throw new Exception();
            }
        }
        catch(Exception e)
        {
            return context.getString(R.string.validation_category_no_media_type);
        }

        // Check color
        try
        {
            // Check if empty or invalid resource name (exception)
            if(category.getColor(context)==0)
            {
                throw new Exception();
            }
        }
        catch(Exception e)
        {
            // Set default
            category.setColor(context, R.color.blue);
        }

        // No error
        return null;
    }

    /**
     * Like {@link CategoriesController#validateCategory(Context, Category)} but with actual DB values
     * @param context the context
     * @param values hash column name => value
     * @return null if no error (or errors were fixed by this method), a string containing the error otherwise
     */
    public String validateCategoryDbRow(Context context, HashMap<String, Object> values)
    {
        // Check empty name
        Object name = values.get(Category.COLUMN_NAME);
        if(name==null || !(name instanceof String) || Utils.isEmpty((String) name))
        {
            return context.getString(R.string.validation_category_empty_name);
        }

        // Media type
        Object mediaType = values.get(Category.COLUMN_MEDIA_TYPE_NAME);
        try
        {
            // Check if empty or no enum value with the saved name (exception)
            if(mediaType==null || !(mediaType instanceof String) || Utils.isEmpty((String) mediaType) || MediaType.valueOf((String) mediaType)==null)
            {
                throw new Exception();
            }
        }
        catch(Exception e)
        {
            return context.getString(R.string.validation_category_no_media_type);
        }

        // Color
        Object color = values.get(Category.COLUMN_COLOR_RESOURCE_NAME);
        try
        {
            // Not null
            if(color == null || !(color instanceof String) || Utils.isEmpty((String) color)) throw new Exception();
            int colorId = context.getResources().getIdentifier((String) color, "color", context.getPackageName());

            // In the available colors
            boolean colorOk = false;
            TypedArray colorsTypedArray = context.getResources().obtainTypedArray(R.array.color_picker_options);
            for(int i = 0; i < colorsTypedArray.length(); i++)
            {
                if(colorId==colorsTypedArray.getResourceId(i, 0))
                {
                    colorOk = true;
                    break;
                }
            }
            colorsTypedArray.recycle();
            if(!colorOk) throw new Exception();
        }
        catch(Exception e)
        {
            // Set default
            values.put(Category.COLUMN_COLOR_RESOURCE_NAME, context.getResources().getResourceEntryName(R.color.blue));
        }

        // No error
        return null;
    }
}
