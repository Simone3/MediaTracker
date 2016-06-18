package it.polimi.dima.mediatracker.model;

import android.content.Context;

import it.polimi.dima.mediatracker.R;

/**
 * A movie
 */
public class Movie extends MediaItem
{
    public final static String COLUMN_DURATION = "DURATION_MIN";

    private int durationMin;
    private String director;


    /************************************************ GETTERS ************************************************/


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCreator()
    {
        return getDirector();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getDuration(Context context)
    {
        return new Duration(getDurationMin(), Movie.getDurationMeasureUnitName(context), context.getString(R.string.duration_minutes_short));
    }

    /**
     * Getter
     * @param context the context
     * @return the name of the measure unit for this media type
     */
    public static String getDurationMeasureUnitName(Context context)
    {
        return context.getString(R.string.duration_minutes);
    }

    /**
     * Getter
     * @return the movie director
     */
    public String getDirector()
    {
        return director;
    }

    /**
     * Getter
     * @return the movie duration in minutes
     */
    public int getDurationMin()
    {
        return durationMin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int getDoingNowName()
    {
        return R.string.doing_now_movie;
    }

    /************************************************ SETTERS ************************************************/


    /**
     * Setter
     * @param director the movie director
     */
    public void setDirector(String director)
    {
        this.director = director;
    }

    /**
     * Setter
     * @param durationMin the movie duration in minutes
     */
    public void setDurationMin(int durationMin)
    {
        this.durationMin = durationMin;
    }
}
