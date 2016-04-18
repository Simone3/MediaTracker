package it.polimi.dima.mediatracker.model;

import android.content.Context;

import it.polimi.dima.mediatracker.R;

/**
 * A videogame
 */
public class Videogame extends MediaItem
{
    public final static String COLUMN_AVERAGE_LENGTH = "AVERAGE_LENGTH_HOURS";

    private String developer;
    private String publisher;
    private String platforms;
    private int averageLengthHours;


    /************************************************ GETTERS ************************************************/


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCreator()
    {
        return getDeveloper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getDuration(Context context)
    {
        return new Duration(getAverageLengthHours(), Videogame.getDurationMeasureUnitName(context), context.getString(R.string.duration_hours_short));
    }

    /**
     * Getter
     * @param context the context
     * @return the name of the measure unit for this media type
     */
    public static String getDurationMeasureUnitName(Context context)
    {
        return context.getString(R.string.duration_hours);
    }

    /**
     * Getter
     * @return the videogame developer
     */
    public String getDeveloper()
    {
        return developer;
    }

    /**
     * Getter
     * @return the videogame publisher
     */
    public String getPublisher()
    {
        return publisher;
    }

    /**
     * Getter
     * @return the videogame platforms
     */
    public String getPlatforms()
    {
        return platforms;
    }

    /**
     * Getter
     * @return the videogame average length in hours
     */
    public int getAverageLengthHours()
    {
        return averageLengthHours;
    }



    /************************************************ SETTERS ************************************************/


    /**
     * Setter
     * @param averageLengthHours the videogame average length in hours
     */
    public void setAverageLengthHours(int averageLengthHours)
    {
        this.averageLengthHours = averageLengthHours;
    }

    /**
     * Setter
     * @param developer the videogame developer
     */
    public void setDeveloper(String developer)
    {
        this.developer = developer;
    }

    /**
     * Setter
     * @param publisher the videogame publisher
     */
    public void setPublisher(String publisher)
    {
        this.publisher = publisher;
    }

    /**
     * Setter
     * @param platforms the videogame platforms
     */
    public void setPlatforms(String platforms)
    {
        this.platforms = platforms;
    }
}
