package it.polimi.dima.mediatracker.model;

import android.content.Context;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.utils.GlobalConstants;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A generic media item, contains all properties common to all its implementations (e.g. title)
 * It's a SugarRecord implementation: the external library Sugar ORM takes care of the database table automatically
 */
public abstract class MediaItem extends SugarRecord implements Serializable, Sectioned
{
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "NAME";
    public final static String COLUMN_CATEGORY = "CATEGORY";
    public final static String COLUMN_COMPLETION_DATE = "COMPLETION_DATE";
    public final static String COLUMN_TIMES_COMPLETED = "TIMES_COMPLETED";
    public final static String COLUMN_DOING_NOW = "DOING_NOW";
    public final static String COLUMN_RELEASE_DATE = "RELEASE_DATE";
    public final static String COLUMN_IMPORTANCE_LEVEL = "IMPORTANCE_LEVEL";
    public final static String COLUMN_ORDER_IN_SECTION = "ORDER_IN_SECTION";
    public final static String COLUMN_OWNED = "OWNED";
    public final static String COLUMN_GENRES = "GENRES";

    private String name;
    private String genres;
    private String description;
    private String userComment;
    private Date completionDate;
    private int timesCompleted;
    private int importanceLevel;
    @Ignore
    private ImportanceLevel importanceLevelEnum;
    private Long category;
    private boolean owned;
    private Date releaseDate;
    private boolean doingNow;
    private String externalServiceId;
    @Ignore
    private boolean upcoming;
    @Ignore
    private boolean isUpcomingSet = false;
    @Ignore
    private URL imageUrl;
    private String image;
    private int orderInSection;


    /************************************************ GETTERS ************************************************/

    /**
     * Getter
     * @return the media item title
     */
    public String getTitle()
    {
        return name;
    }

    /**
     * Getter
     * @return the last media item completion date (null if the user hasn't completed it yet)
     */
    public Date getCompletionDate()
    {
        return completionDate;
    }

    /**
     * Getter
     * @return true if the user has completed this media item (i.e. completion date is not null)
     */
    public boolean isCompleted()
    {
        return !isUpcoming() && getCompletionDate()!=null;
    }

    /**
     * Getter
     * @return the media item category ID
     */
    public Long getCategory()
    {
        return category;
    }

    /**
     * Getter
     * @return the media item release date
     */
    public Date getReleaseDate()
    {
        return releaseDate;
    }

    /**
     * Getter
     * @return the media item genres
     */
    public String getGenres()
    {
        return genres;
    }

    /**
     * Getter
     * @return the media item description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Getter
     * @return the media item user comment
     */
    public String getUserComment()
    {
        return userComment;
    }

    /**
     * Getter
     * @return the media item importance level
     */
    public ImportanceLevel getImportanceLevel()
    {
        if(importanceLevelEnum==null && importanceLevel>0)
        {
            for(ImportanceLevel il: ImportanceLevel.values())
            {
                if(importanceLevel==il.getDbValue())
                {
                    importanceLevelEnum = il;
                    break;
                }
            }
        }
        return importanceLevelEnum;
    }

    /**
     * Getter
     * @return true if the user owns this media item
     */
    public boolean isOwned()
    {
        return !isUpcoming() && owned;
    }

    /**
     * Getter
     * @return true if the media item is upcoming (release date > now)
     */
    public boolean isUpcoming()
    {
        if(isUpcomingSet)
        {
            return upcoming;
        }
        else
        {
            upcoming = releaseDate!=null && Calendar.getInstance().getTime().before(releaseDate);
            isUpcomingSet = true;
            return upcoming;
        }
    }

    /**
     * Getter
     * @return the media item creator (e.g. author, director, etc.)
     */
    public abstract String getCreator();

    /**
     * Getter
     * @return true if the user is "doing" (e.g. watching, reading, etc.) the media item right now
     */
    public boolean isDoingNow()
    {
        return !isUpcoming() && doingNow;
    }

    /**
     * Getter
     * @return the media item external API ID, if it was created from a search result
     */
    public String getExternalServiceId()
    {
        return externalServiceId;
    }

    /**
     * Getter
     * @return the URL of the image
     */
    public URL getImageUrl()
    {
        if(imageUrl==null && !Utils.isEmpty(image))
        {
            try
            {
                imageUrl = new URL(image);
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }
        return imageUrl;
    }

    /**
     * Getter
     * @return the media item duration (e.g. runtime, number of pages, etc.)
     */
    public abstract Duration getDuration(Context context);

    /**
     * Getter
     * @return the order value inside the media item's importance level (i.e. items with the same importance level are ordered by this value)
     */
    public int getOrderInSection()
    {
        return orderInSection;
    }

    /**
     * Getter
     * @return the number of times this media item was completed in the past
     */
    public int getTimesCompleted()
    {
        return isUpcoming() ? 0 : timesCompleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Section getSection()
    {
        // If it's completed...
        if(isCompleted())
        {
            // The section is the completion year
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getCompletionDate());
            String year = String.valueOf(calendar.get(Calendar.YEAR));
            return new Section(year, year);
        }

        // Otherwise...
        else
        {
            // "Upcoming" section
            if(isUpcoming())
            {
                return new Section(GlobalConstants.SECTION_UPCOMING, R.string.upcoming);
            }
            else
            {
                // "Doing Now" section
                if(isDoingNow())
                {
                    return new Section(GlobalConstants.SECTION_DOING_NOW, getDoingNowName());
                }

                // Importance Level section
                else
                {
                    return new Section(getImportanceLevel().name(), getImportanceLevel().getNameResource());
                }
            }
        }
    }

    /**
     * String for the "doing now" section name
     * @return the resource id of the string
     */
    abstract int getDoingNowName();



    /************************************************ SETTERS ************************************************/


    /**
     * Setter
     * @param releaseDate the media item release date
     */
    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
        this.isUpcomingSet = false;
    }

    /**
     * Setter
     * @param title the media item title
     */
    public void setTitle(String title)
    {
        this.name = title;
    }

    /**
     * Setter
     * @param category the media item category ID
     */
    public void setCategory(Long category)
    {
        this.category = category;
    }

    /**
     * Setter
     * @param importanceLevel the media item importance level
     */
    public void setImportanceLevel(ImportanceLevel importanceLevel)
    {
        this.importanceLevelEnum = importanceLevel;
        this.importanceLevel = importanceLevel.getDbValue();
    }

    /**
     * Setter
     * @param owned true if the user owns this media item
     */
    public void setOwned(boolean owned)
    {
        this.owned = owned;
    }

    /**
     * Setter
     * @param genres the media item genres
     */
    public void setGenres(String genres)
    {
        this.genres = genres;
    }

    /**
     * Setter
     * @param description the media item description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Setter
     * @param userComment the media item user comment
     */
    public void setUserComment(String userComment)
    {
        this.userComment = userComment;
    }

    /**
     * Setter
     * @param externalServiceId the media item external API ID
     */
    public void setExternalServiceId(String externalServiceId)
    {
        this.externalServiceId = externalServiceId;
    }

    /**
     * Setter
     * @param completionDate the last media item completion date (null if the user hasn't completed it yet)
     */
    public void setCompletionDate(Date completionDate)
    {
        this.completionDate = completionDate;
    }

    /**
     * Setter
     * @param doingNow true if the user is "doing" (e.g. watching, reading, etc.) the media item right now
     */
    public void setDoingNow(boolean doingNow)
    {
        this.doingNow = doingNow;
    }

    /**
     * Setter
     * @param imageUrl the URL of the image
     */
    public void setImageUrl(URL imageUrl)
    {
        this.imageUrl = imageUrl;
        this.image = imageUrl==null ? "" : imageUrl.toString();
    }

    /**
     * Setter
     * @param orderInSection the order value inside the media item's importance level (i.e. items with the same importance level are ordered by this value)
     */
    public void setOrderInSection(int orderInSection)
    {
        this.orderInSection = orderInSection;
    }

    /**
     * Setter
     * @param timesCompleted the number of times this media item was completed in the past
     */
    public void setTimesCompleted(int timesCompleted)
    {
        this.timesCompleted = timesCompleted;
    }

    /************************************************ MISC ************************************************/


    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o)
    {
        if(o==null || !(o instanceof MediaItem)) return false;

        MediaItem other = (MediaItem) o;

        return getId().equals(other.getId());
    }



    /************************************************ NESTED CLASSES ************************************************/

    /**
     * Represents a generic duration (value + measure unit)
     */
    public static class Duration
    {
        private int value;
        private String measureUnit;
        private String measureUnitShort;

        /**
         * Constructor
         * @param value the absolute value of the duration
         * @param measureUnit the name of the unit of measurement (e.g. minutes)
         * @param measureUnitShort the abbreviation of the name of the unit of measurement (e.g. ')
         */
        public Duration(int value, String measureUnit, String measureUnitShort)
        {
            this.value = value;
            this.measureUnit = measureUnit;
            this.measureUnitShort = measureUnitShort;
        }

        /**
         * Getter
         * @return the absolute value of the duration
         */
        public int getValue()
        {
            return value;
        }

        /**
         * Getter
         * @return the name of the unit of measurement (e.g. minutes)
         */
        public String getMeasureUnit()
        {
            return measureUnit;
        }

        /**
         * Getter
         * @return the abbreviation of the name of the unit of measurement (e.g. ')
         */
        public String getMeasureUnitShort()
        {
            return measureUnitShort;
        }
    }
}
