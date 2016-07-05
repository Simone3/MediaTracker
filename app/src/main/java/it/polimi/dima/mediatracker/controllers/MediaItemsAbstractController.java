package it.polimi.dima.mediatracker.controllers;


import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.services.MediaItemService;
import it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsAbstractFragment;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.Section;
import it.polimi.dima.mediatracker.model.Subcategory;
import it.polimi.dima.mediatracker.utils.GlobalConstants;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Controller for a generic media item. Allows to get several parameters of the media item (e.g. the model class)
 * and to query the database to retrieve media items
 */
public abstract class MediaItemsAbstractController
{
    private final static int ORDER_DEFAULT_STEP = 1000;
    private final static int PAGE_LENGTH = 50;


    /************************************************ GENERAL (classes, services, etc.) ************************************************/


    /**
     * Getter
     * @return the class that represents the model of this media type
     */
    public abstract Class getModelClass();

    /**
     * Initializes and returns a new (empty) object of this media type
     * @return the new media item
     */
    public abstract MediaItem initializeEmptyMediaItem();

    /**
     * Getter
     * @return the external service that manages this media type
     */
    public abstract MediaItemService getMediaItemService(Context context);

    /**
     * String for the "I could redo this" media item option
     * @return the resource id of the string
     */
    public abstract int getRedoOptionName();

    /**
     * String for the "I've done this" media item option
     * @return the resource id of the string
     */
    public abstract int getCompleteOptionName();

    /**
     * String for the "I'm doing this now" media item option
     * @return the resource id of the string
     */
    public abstract int getDoingOptionName();

    /**
     * "Internal" getter (used in {@link MediaItemsAbstractController#getRandomMediaItem(Category, Long, List, List, String, int, int)})
     * @return the database column name for the duration (pages number, runtime, etc.) for this media type
     */
    protected abstract String getDurationDatabaseFieldName();

    /**
     * The subcategories of this media type (= subcategories of each category of this media type)
     * @return the array of subcategories
     */
    public Subcategory[] getMediaTypeSubcategories()
    {
        return Subcategory.values();
    }

    /**
     * Returns the fragment for the form of this media type
     * @param categoryId the ID of the media item category
     * @param mediaItemId null if adding new media item, the ID of the media item to edit otherwise
     * @param isEmpty true if the form should be empty (= no fields are displayed, only the action bar)
     * @return the form fragment
     */
    public abstract FormMediaItemAbstractFragment getMediaItemFormFragment(Long categoryId, Long mediaItemId, boolean isEmpty);

    /**
     * Returns the fragment for the suggestions page of this media type
     * @param categoryId the ID of the media item category
     * @return the suggestions fragment
     */
    public abstract SuggestionsAbstractFragment getMediaItemSuggestionsFragment(Long categoryId);



    /************************************************ DB SELECT QUERIES ************************************************/

    /**
     * Helper to build the LIMIT value considering the current page
     * @param page the current page
     * @return the LIMIT value
     */
    private String buildSelectLimitWithPage(int page)
    {
        // Skip "page*PAGE_LENGTH" elements and retrieve max "PAGE_LENGTH" elements
        return page*PAGE_LENGTH+", "+PAGE_LENGTH;
    }

    /**
     * Gets all tracked media items in the given category
     * @param page the current page for the media items (e.g. 0 retrieves the first "PAGE_LENGTH" elements, 1 the elements from PAGE_LENGTH to 2*PAGE_LENGTH-1, etc.)
     * @param category the media items category
     * @return all tracked media items
     */
    @SuppressWarnings("unchecked")
    public List<MediaItem> getTrackedMediaItemsInCategory(int page, Category category)
    {
        List<MediaItem> mediaItems = null;
        if(category.getId()!=null)
        {
            String where = MediaItem.COLUMN_CATEGORY+" = ? AND "+ MediaItem.COLUMN_COMPLETION_DATE+" IS NULL";
            String[] whereArgs = new String[]{category.getId().toString()};

            Integer minIl = null, maxIl = null;
            for(ImportanceLevel il: ImportanceLevel.values())
            {
                if(minIl==null || il.getDbValue()<minIl)
                {
                    minIl = il.getDbValue();
                }
                if(maxIl==null || il.getDbValue()>maxIl)
                {
                    maxIl = il.getDbValue();
                }
            }
            if(minIl==null) throw new IllegalStateException("Something went really wrong with importance levels");
            long now = Calendar.getInstance().getTimeInMillis();
            final String IS_DOING_NOW = MediaItem.COLUMN_DOING_NOW+" = 1";
            final String IS_UPCOMING = "("+MediaItem.COLUMN_RELEASE_DATE+" IS NOT NULL AND "+MediaItem.COLUMN_RELEASE_DATE+" > "+now+")";
            String orderBy =
                    "(CASE WHEN "+IS_DOING_NOW+" "+
                        "THEN "+(maxIl+1)+" "+
                        "ELSE "+
                            "(CASE WHEN "+IS_UPCOMING+" "+
                                "THEN "+(minIl-1)+" "+
                                "ELSE "+MediaItem.COLUMN_IMPORTANCE_LEVEL+" END) END) DESC, "+
                    "(CASE WHEN "+IS_UPCOMING+" "+
                        "THEN "+MediaItem.COLUMN_RELEASE_DATE+" "+
                        "ELSE "+MediaItem.COLUMN_ORDER_IN_SECTION+" END) ASC";

            String limit = buildSelectLimitWithPage(page);
            mediaItems = MediaItem.find(getModelClass(), where, whereArgs, "", orderBy, limit);
        }
        return mediaItems;
    }

    /**
     * Gets all completed media items in the given category
     * @param page the current page for the media items (e.g. 0 retrieves the first "PAGE_LENGTH" elements, 1 the elements from PAGE_LENGTH to 2*PAGE_LENGTH-1, etc.)
     * @param category the media items category
     * @return all completed media items
     */
    @SuppressWarnings("unchecked")
    public List<MediaItem> getCompletedMediaItemsInCategory(int page, Category category)
    {
        List<MediaItem> mediaItems = null;
        if(category.getId()!=null)
        {
            String where = MediaItem.COLUMN_CATEGORY+" = ? AND "+ MediaItem.COLUMN_COMPLETION_DATE+" IS NOT NULL";
            String[] whereArgs = new String[]{category.getId().toString()};
            String orderBy = MediaItem.COLUMN_COMPLETION_DATE+" DESC";
            String limit = buildSelectLimitWithPage(page);
            mediaItems = MediaItem.find(getModelClass(), where, whereArgs, "", orderBy, limit);
        }
        return mediaItems;
    }

    /**
     * Searches for media items matching the given query
     * @param page the current page for the media items (e.g. 0 retrieves the first "PAGE_LENGTH" elements, 1 the elements from PAGE_LENGTH to 2*PAGE_LENGTH-1, etc.)
     * @param category the media items category
     * @param query the string to search for
     * @param completed true if only completed media items, false if only tracked (= not completed), null if any
     * @return the media items that match the query
     */
    @SuppressWarnings("unchecked")
    public List<MediaItem> searchMediaItemsInCategory(int page, Category category, String query, Boolean completed)
    {
        List<MediaItem> mediaItems = null;
        if(category.getId()!=null)
        {
            String where = MediaItem.COLUMN_CATEGORY+" = ? AND "+MediaItem.COLUMN_NAME+" LIKE ?";
            if(completed!=null)
            {
                if(completed) where += " AND "+ MediaItem.COLUMN_COMPLETION_DATE+" IS NOT NULL";
                else where += " AND "+ MediaItem.COLUMN_COMPLETION_DATE+" IS NULL";
            }
            String[] whereArgs = new String[]{category.getId().toString(), "%"+query+"%"};
            String orderBy = MediaItem.COLUMN_NAME+" ASC";
            String limit = buildSelectLimitWithPage(page);
            mediaItems = MediaItem.find(getModelClass(), where, whereArgs, "", orderBy, limit);
        }
        return mediaItems;
    }

    /**
     * Gets all media items whose release date is today
     * @param category the media items category
     * @return all media items released today
     */
    @SuppressWarnings("unchecked")
    public List<MediaItem> getMediaItemsReleasedToday(Category category)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String todayStart = String.valueOf(calendar.getTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        String todayEnd = String.valueOf(calendar.getTimeInMillis());

        // Get media items
        List<MediaItem> mediaItems = null;
        if(category.getId()!=null)
        {
            String where = MediaItem.COLUMN_CATEGORY+" = ? AND "+MediaItem.COLUMN_RELEASE_DATE+" BETWEEN ? AND ?";
            String[] whereArgs = new String[]{category.getId().toString(), todayStart, todayEnd};
            mediaItems = MediaItem.find(getModelClass(), where, whereArgs);
        }

        return mediaItems;
    }

    /**
     * Gets the count of all media items (tracked or completed) in this category
     * @param category the media items category
     * @return the number of media items
     */
    public long getAllMediaItemsNumberInCategory(Category category)
    {
        return MediaItem.count(getModelClass(), MediaItem.COLUMN_CATEGORY + " = ?", new String[]{category.getId().toString()});
    }

    /**
     * Gets a media item by its ID in the given category
     * @param category the media item category
     * @param itemId the media item ID
     * @return the media item if found, null otherwise
     */
    public MediaItem getMediaItemById(Category category, Long itemId)
    {
        MediaItem mediaItem = null;
        if(category.getId()!=null)
        {
            mediaItem = (MediaItem) MediaItem.findById(getModelClass(), itemId);
        }
        return mediaItem;
    }

    /**
     * Retrieves a random tracked media item in the given category that satisfies the given constraints
     * @param category the media item category
     * @param idDifferentFromIfPossible if possible, the retrieved media item has a different ID from this one ("if possible" means that if there's only one item that matches in the whole database, it is returned anyway). May be null
     * @param genresContains genres contains this value. May be null
     * @param owned true if the media item is owned by the user, false if it's not owned, null if it doesn't matter (owned or not)
     * @param minDuration min duration (e.g. pages number, runtime,...). May be <= 0 if not interested in this constraint
     * @param maxDuration max duration (e.g. pages number, runtime,...). May be <= 0 if not interested in this constraint
     * @return a random media item or null if no match
     */
    public MediaItem getRandomTrackedMediaItem(Category category, Long idDifferentFromIfPossible, String genresContains, Boolean owned, int minDuration, int maxDuration)
    {
        List<String> whereAndTerms = new ArrayList<>();
        List<String> whereArgs = new ArrayList<>();

        // Add constraint for tracked media items
        whereAndTerms.add(MediaItem.COLUMN_COMPLETION_DATE+" IS NULL");

        // Add constraint for owned, if any
        if(owned!=null)
        {
            whereAndTerms.add(MediaItem.COLUMN_OWNED+" = ?");
            whereArgs.add(owned ? "1" : "0");
        }

        // Don't show media items already set as "doing now"
        whereAndTerms.add(MediaItem.COLUMN_DOING_NOW + " = ?");
        whereArgs.add("0");

        // Add release date constraint (no upcoming media items in suggestions)
        whereAndTerms.add(MediaItem.COLUMN_RELEASE_DATE + " <= ?");
        whereArgs.add(String.valueOf(Calendar.getInstance().getTimeInMillis()));

        // Call helper method
        return getRandomMediaItem(category, idDifferentFromIfPossible, whereAndTerms, whereArgs, genresContains, minDuration, maxDuration);
    }

    /**
     * Retrieves a random completed media item in the given category that satisfies the given constraints
     * @param category the media item category
     * @param idDifferentFromIfPossible if possible, the retrieved media item has a different ID from this one ("if possible" means that if there's only one item that matches in the whole database, it is returned anyway). May be null
     * @param genresContains genres contains this value. May be null
     * @param completedYearsAgo completion date is at least the given amount of years ago. May be 0
     * @param minDuration min duration (e.g. pages number, runtime,...). May be 0
     * @param maxDuration max duration (e.g. pages number, runtime,...). May be 0
     * @return a random media item or null if no match
     */
    public MediaItem getRandomCompletedMediaItem(Category category, Long idDifferentFromIfPossible, String genresContains, int completedYearsAgo, int minDuration, int maxDuration)
    {
        List<String> whereAndTerms = new ArrayList<>();
        List<String> whereArgs = new ArrayList<>();

        // Add constraint for completed media items
        whereAndTerms.add(MediaItem.COLUMN_COMPLETION_DATE + " IS NOT NULL");

        // Add constraint for completion date, if any
        if(completedYearsAgo>0)
        {
            whereAndTerms.add(MediaItem.COLUMN_COMPLETION_DATE+" < ?");
            long now = Calendar.getInstance().getTimeInMillis();
            long yearsAgoMs = (long) completedYearsAgo*GlobalConstants.MILLISECONDS_IN_YEAR;
            whereArgs.add(String.valueOf(now-yearsAgoMs));
        }

        // Call helper method
        return getRandomMediaItem(category, idDifferentFromIfPossible, whereAndTerms, whereArgs, genresContains, minDuration, maxDuration);
    }

    /**
     * Internal helper to manage the common parts of {@link MediaItemsAbstractController#getRandomCompletedMediaItem(Category, Long, String, int, int, int)}
     * and {@link MediaItemsAbstractController#getRandomTrackedMediaItem(Category, Long, String, Boolean, int, int)}
     */
    @SuppressWarnings("unchecked")
    private MediaItem getRandomMediaItem(Category category, Long idDifferentFromIfPossible, List<String> whereAndTerms, List<String> whereArgs, String genresContains, int minDuration, int maxDuration)
    {
        // Check category
        if(category.getId()!=null)
        {
            // Add category constraint
            whereAndTerms.add(MediaItem.COLUMN_CATEGORY+" = ?");
            whereArgs.add(category.getId().toString());

            // Add constraint for genres, if any
            if(!Utils.isEmpty(genresContains))
            {
                whereAndTerms.add(MediaItem.COLUMN_GENRES+" LIKE ?");
                whereArgs.add("%"+genresContains+"%");
            }

            // Add constraint for duration, if any
            String durationField = getDurationDatabaseFieldName();
            if(minDuration>0 || maxDuration>0)
            {
                if (minDuration > 0 && maxDuration > 0)
                {
                    whereAndTerms.add(durationField + " BETWEEN ? AND ?");
                    whereArgs.add(String.valueOf(minDuration));
                    whereArgs.add(String.valueOf(maxDuration));
                }
                else if (minDuration > 0)
                {
                    whereAndTerms.add(durationField + " > ?");
                    whereArgs.add(String.valueOf(minDuration));
                }
                else
                {
                    whereAndTerms.add(durationField + " < ?");
                    whereArgs.add(String.valueOf(maxDuration));
                }

                whereAndTerms.add(durationField + " > ?");
                whereArgs.add("0");
            }

            // Build query parameters
            String where = "("+ TextUtils.join(") AND (", whereAndTerms)+")";
            String orderBy = "RANDOM()";
            String limit = "2";
            List<MediaItem> mediaItems = MediaItem.find(getModelClass(), where, whereArgs.toArray(new String[whereArgs.size()]), "", orderBy, limit);

            // If we have some match...
            if(mediaItems!=null && mediaItems.size()>0)
            {
                // If we have a "idDifferentFromIfPossible" parameter and we have at least two results, make sure to return a result different from that one
                if(mediaItems.size()>1 && idDifferentFromIfPossible!=null && idDifferentFromIfPossible.equals(mediaItems.get(0).getId()))
                {
                    return mediaItems.get(1);
                }

                // Otherwise (no "idDifferentFromIfPossible" or just one result) return the first one
                else
                {
                    return mediaItems.get(0);
                }
            }
            else return null;
        }

        return null;
    }

    /**
     * Retrieves a media item (tracked or completed) that has the same name as the given one
     * @param category the media item category ID
     * @param currentMediaItemId if not null, the retrieved media item must have an id DIFFERENT from this one (otherwise we would match the media item itself if already in the database!)
     * @param name the name of the media item to search
     * @return null if no match, the media item that has that name otherwise
     */
    @SuppressWarnings("unchecked")
    private MediaItem getMediaItemWithSameName(Long category, Long currentMediaItemId, String name)
    {
        List<MediaItem> mediaItems = null;
        if(category!=null)
        {
            String where = MediaItem.COLUMN_CATEGORY+" = ? AND "+MediaItem.COLUMN_ID+" != ? AND "+MediaItem.COLUMN_NAME+" = ?";
            String[] whereArgs = new String[]{category.toString(), String.valueOf(currentMediaItemId), name};
            String limit = "1";
            mediaItems = MediaItem.find(getModelClass(), where, whereArgs, "", "", limit);
        }
        return mediaItems!=null && mediaItems.size()>0 ? mediaItems.get(0) : null;
    }

    /**
     * Queries the database for the last media item in the order inside the given importance level
     * @param category the media item category
     * @param section the media item section
     * @return the media item with highest order value
     */
    @SuppressWarnings("unchecked")
    private MediaItem getLastMediaItemInSection(Category category, Section section)
    {
        List<MediaItem> mediaItems = getAllMediaItemsInSection(category, section);
        return mediaItems!=null && mediaItems.size()>0 ? mediaItems.get(mediaItems.size()-1) : null;
    }

    /**
     * Returns the ordered list of all media items in the given importance level (without considering upcoming or "doing now" media items)
     * @param category the media item category
     * @param section the media item section
     * @return the list of media items with the given importance level
     */
    @SuppressWarnings("unchecked")
    private List<MediaItem> getAllMediaItemsInSection(Category category, Section section)
    {
        List<MediaItem> mediaItems = null;
        if(category.getId()!=null)
        {
            String where = MediaItem.COLUMN_CATEGORY+" = ? ";
            List<String> whereArgs = new ArrayList<>();
            whereArgs.add(category.getId().toString());

            // TODO this switch is bad, find a better way! Move all section management in controllers?
            sectionSwitch: switch(section.getSectionId())
            {
                case GlobalConstants.SECTION_DOING_NOW:
                    where += " AND "+MediaItem.COLUMN_DOING_NOW+" = ?";
                    whereArgs.add("1");
                    break;

                case GlobalConstants.SECTION_UPCOMING:
                    where += " AND "+MediaItem.COLUMN_RELEASE_DATE+" > ?";
                    whereArgs.add(String.valueOf(Calendar.getInstance().getTimeInMillis()));
                    break;

                default:
                    for(ImportanceLevel il: ImportanceLevel.values())
                    {
                        if(section.getSectionId().equals(il.name()))
                        {
                            where += " AND "+MediaItem.COLUMN_IMPORTANCE_LEVEL+" = ? AND ("+MediaItem.COLUMN_RELEASE_DATE+" IS NULL OR "+MediaItem.COLUMN_RELEASE_DATE+" <= ?) AND "+MediaItem.COLUMN_DOING_NOW+" = ?";
                            whereArgs.add(String.valueOf(il.getDbValue()));
                            whereArgs.add(String.valueOf(Calendar.getInstance().getTimeInMillis()));
                            whereArgs.add("0");
                            break sectionSwitch;
                        }
                    }
            }

            String orderBy = MediaItem.COLUMN_ORDER_IN_SECTION+" ASC";
            mediaItems = MediaItem.find(getModelClass(), where, whereArgs.toArray(new String[whereArgs.size()]), "", orderBy, null);
        }
        return mediaItems;
    }



    /************************************************ DB UPDATE QUERIES ************************************************/

    /**
     * Sets a media item as completed with the given date
     * @param mediaItem the media item
     * @param completionDate the completion date
     */
    public void setMediaItemAsCompleted(MediaItem mediaItem, Date completionDate)
    {
        mediaItem.setCompletionDate(completionDate);
        mediaItem.setTimesCompleted(mediaItem.getTimesCompleted()+1);
        saveMediaItem(mediaItem);
    }

    /**
     * Updates a media item completion date
     * @param mediaItem the media item
     * @param completionDate the completion date
     */
    public void updateMediaItemCompletionDate(MediaItem mediaItem, Date completionDate)
    {
        mediaItem.setCompletionDate(completionDate);
        saveMediaItem(mediaItem);
    }

    /**
     * Sets a media item as "doing" (watching, reading, etc.) now
     * @param mediaItem the media item
     * @param doingNow true if user is "doing" the media item now
     */
    public void setMediaItemAsDoingNow(MediaItem mediaItem, boolean doingNow)
    {
        mediaItem.setOwned(true);
        mediaItem.setDoingNow(doingNow);
        saveMediaItem(mediaItem);
    }

    /**
     * Sets a media item as owned by the user
     * @param mediaItem the media item
     * @param owned true if the user owns the media item
     */
    public void setMediaItemAsOwned(MediaItem mediaItem, boolean owned)
    {
        mediaItem.setOwned(owned);
        saveMediaItem(mediaItem);
    }

    /**
     * Undoes a previous call to {@link MediaItemsAbstractController#setMediaItemAsCompleted(MediaItem, Date)}
     * @param mediaItem the media item
     */
    public void undoSetAsCompleted(MediaItem mediaItem)
    {
        mediaItem.setCompletionDate(null);
        mediaItem.setTimesCompleted(mediaItem.getTimesCompleted()-1);
        saveMediaItem(mediaItem);
    }

    /**
     * Sets a media item as "possibly to redo" (i.e. moves it from the completed list back to the tracked list)
     * @param mediaItem the media item
     */
    public void setMediaItemAsToRedo(MediaItem mediaItem)
    {
        mediaItem.setDoingNow(false);
        mediaItem.setCompletionDate(null);
        saveMediaItem(mediaItem);
    }

    /**
     * Inserts or updates a media item in the database
     * @param mediaItem the media item to update/insert
     */
    public void saveMediaItem(MediaItem mediaItem)
    {
        mediaItem.save();
    }



    /************************************************ DB DELETE QUERIES ************************************************/


    /**
     * Removes a media item from the database
     * @param mediaItem the media item to remove
     */
    public void deleteMediaItem(MediaItem mediaItem)
    {
        mediaItem.delete();
    }

    /**
     * Removes all media items in the given category
     * @param category the category
     */
    public void deleteAllMediaItemsInCategory(Category category)
    {
        if(category.getId()!=null)
        {
            MediaItem.deleteAll(getModelClass(), MediaItem.COLUMN_CATEGORY+" = ?", category.getId().toString());
        }
    }


    /************************************************ MANAGE LIST ORDER ************************************************/

    /**
     * Called to update the order field (for the database) AFTER "movedMediaItem" has been moved in the list
     * @param category the media item category
     * @param movedMediaItem the media items that has just moved
     * @param previousMediaItemInSection the media item before the moved one in the section
     * @param nextMediaItemInSection the media item after the moved one in the section
     */
    public void setOrderInSectionAfterMove(Category category, MediaItem movedMediaItem, MediaItem previousMediaItemInSection, MediaItem nextMediaItemInSection)
    {
        int newOrder;

        // If it's the only media item in the list...
        if(previousMediaItemInSection==null && nextMediaItemInSection==null)
        {
            // Order is 0
            newOrder = 0;
        }

        // If the moved media item is the first in its section...
        else if(previousMediaItemInSection==null)
        {
            // The order is the next media item order - the default step
            newOrder = nextMediaItemInSection.getOrderInSection() - ORDER_DEFAULT_STEP;
        }

        // If the moved media item is the last in its section...
        else if(nextMediaItemInSection==null)
        {
            // The order is the previous media item order + the default step
            newOrder = previousMediaItemInSection.getOrderInSection() + ORDER_DEFAULT_STEP;
        }

        // If it's in between (not first nor last)
        else
        {
            // Get previous and next order values
            int prev = previousMediaItemInSection.getOrderInSection();
            int next = nextMediaItemInSection.getOrderInSection();

            // If there's space for the new media item...
            if(Math.abs(next-prev)>1)
            {
                // The order is the value between next and prev
                newOrder = prev + Math.round((next-prev)/2);
            }

            // Otherwise...
            else
            {
                // Need to rebuild the order of the list
                rebuildMediaItemsListOrderInSection(category, movedMediaItem, previousMediaItemInSection);
                return;
            }
        }

        // Update order value
        movedMediaItem.setOrderInSection(newOrder);
    }

    /**
     * Helper to completely rebuild the order of the "movedMediaItem" section after a move operation that can't manage the current order values
     * @param category the media item category
     * @param movedMediaItem the media items that has just moved
     * @param previousMediaItemInSection the media item before the moved one in the section
     */
    private void rebuildMediaItemsListOrderInSection(Category category, MediaItem movedMediaItem, MediaItem previousMediaItemInSection)
    {
        // Get all media items in the section
        List<MediaItem> mediaItems = getAllMediaItemsInSection(category, movedMediaItem.getSection());

        // Loop all media items in the interval and set an increasing order value
        int count = 0;
        for(MediaItem mediaItem: mediaItems)
        {
            // Skip moved item (can be anywhere in the list!)
            if(mediaItem.equals(movedMediaItem)) continue;

            // Set this media item value
            mediaItem.setOrderInSection(count);
            saveMediaItem(mediaItem);

            // If it's the previous media item...
            if(mediaItem.equals(previousMediaItemInSection))
            {
                // Set moved media item value
                movedMediaItem.setOrderInSection(count + ORDER_DEFAULT_STEP);

                // Increase twice the count
                count += ORDER_DEFAULT_STEP;
            }

            // Increase count
            count += ORDER_DEFAULT_STEP;
        }
    }

    /**
     * Called BEFORE inserting a new media item to compute its order value in its section
     * @param category the media item category
     * @param newMediaItem the media item to be inserted
     */
    public void setMediaItemOrderInSectionBeforeInserting(Category category, MediaItem newMediaItem)
    {
        // Get last item in the section
        MediaItem last = getLastMediaItemInSection(category, newMediaItem.getSection());

        // The new order is that value + the default step (we add the media item at the end of the list)
        int newOrder = last==null ? 0 : last.getOrderInSection() + ORDER_DEFAULT_STEP;

        // Set the order value
        newMediaItem.setOrderInSection(newOrder);
    }

    /**
     * Called BEFORE changing a media item importance level
     * NOTE: called e.g. when we submit a form, not when the media item is moved in the list: in that case call {@link MediaItemsAbstractController#setOrderInSectionAfterMove(Category, MediaItem, MediaItem, MediaItem)}
     * @param category the media item category
     * @param updatedMediaItem the media item to be updated with a different importance level
     */
    public void setMediaItemOrderInSectionBeforeUpdatingImportanceLevel(Category category, MediaItem updatedMediaItem)
    {
        // Do the same as the above method
        setMediaItemOrderInSectionBeforeInserting(category, updatedMediaItem);
    }



    /************************************************ MISC HELPERS ************************************************/

    /**
     * Validates a media item, possibly fixing what is wrong or, if impossible, returning an error string
     * @param context the context
     * @param mediaItem the media item to validate
     * @return null if no error (or errors were fixed by this method), a string containing the error otherwise
     */
    public String validateMediaItem(Context context, MediaItem mediaItem)
    {
        // Null media item
        if(mediaItem==null)
        {
            return context.getString(R.string.validation_media_item_null);
        }

        // Check category
        if(mediaItem.getCategory()==null || mediaItem.getCategory()<=0)
        {
            return context.getString(R.string.validation_media_item_no_category);
        }

        // Check empty title
        if(Utils.isEmpty(mediaItem.getTitle()))
        {
            return context.getString(R.string.validation_media_item_empty_title);
        }

        // Check media item with same title
        MediaItem mediaItemWithSameName = getMediaItemWithSameName(mediaItem.getCategory(), mediaItem.getId(), mediaItem.getTitle());
        if(mediaItemWithSameName!=null)
        {
            return context.getString(mediaItemWithSameName.isCompleted() ? R.string.validation_media_item_same_title_completed : R.string.validation_media_item_same_title_tracking);
        }

        // Check importance level
        if(mediaItem.getImportanceLevel()==null)
        {
            // Set default
            mediaItem.setImportanceLevel(ImportanceLevel.values()[0]);
        }

        // Check times completed
        if(mediaItem.getTimesCompleted()<0)
        {
            mediaItem.setTimesCompleted(0);
        }
        if(mediaItem.isCompleted() && mediaItem.getTimesCompleted()==0)
        {
            mediaItem.setTimesCompleted(1);
        }

        // Subclass validation
        return validateSpecificMediaItem(context, mediaItem);
    }

    /**
     * Validation for media item implementations
     *
     * @see MediaItemsAbstractController#validateMediaItem(Context, MediaItem)
     */
    protected abstract String validateSpecificMediaItem(Context context, MediaItem mediaItem);

    /**
     * Like {@link MediaItemsAbstractController#validateMediaItem(Context, MediaItem)} but with actual DB values
     * @param context the context
     * @param values hash column name => value
     * @return null if no error (or errors were fixed by this method), a string containing the error otherwise
     */
    public String validateMediaItemDbRow(Context context, HashMap<String, Object> values)
    {
        // Check empty title
        Object title = values.get(MediaItem.COLUMN_NAME);
        if(title==null || !(title instanceof String) || Utils.isEmpty((String) title))
        {
            return context.getString(R.string.validation_media_item_empty_title);
        }

        // Check importance level
        Object importanceLevel = values.get(MediaItem.COLUMN_IMPORTANCE_LEVEL);
        try
        {
            // Not empty
            if(importanceLevel==null) throw new Exception();

            // Value among those available
            int ilValue = Integer.parseInt((String) importanceLevel);
            boolean importanceLevelOk = false;
            for(ImportanceLevel il: ImportanceLevel.values())
            {
                if(il.getDbValue()==ilValue)
                {
                    importanceLevelOk = true;
                    break;
                }
            }
            if(!importanceLevelOk) throw new Exception();
        }
        catch(Exception e)
        {
            values.put(MediaItem.COLUMN_IMPORTANCE_LEVEL, ImportanceLevel.values()[0].getDbValue());
        }

        // Check times completed
        long completionDate;
        try
        {
            completionDate = Long.parseLong((String) values.get(MediaItem.COLUMN_COMPLETION_DATE));
        }
        catch(Exception e)
        {
            completionDate = 0;
        }
        int timesCompleted;
        try
        {
            timesCompleted = Integer.parseInt((String) values.get(MediaItem.COLUMN_TIMES_COMPLETED));
        }
        catch(Exception e)
        {
            timesCompleted = 0;
        }
        if(timesCompleted<0)
        {
            timesCompleted = 0;
            values.put(MediaItem.COLUMN_TIMES_COMPLETED, 0);
        }
        if(completionDate>0 && timesCompleted==0)
        {
            values.put(MediaItem.COLUMN_TIMES_COMPLETED, 1);
        }

        // Subclass validation
        return validateSpecificMediaItemDbRow(context, values);
    }

    /**
     * Validation for media item implementations
     *
     * @see MediaItemsAbstractController#validateMediaItemDbRow(Context, HashMap)
     */
    protected abstract String validateSpecificMediaItemDbRow(Context context, HashMap<String, Object> values);
}
