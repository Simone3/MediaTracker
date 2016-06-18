package it.polimi.dima.mediatracker.model;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import it.polimi.dima.mediatracker.R;

/**
 * A TV show
 */
public class TVShow extends MediaItem
{
    public final static String COLUMN_EPISODE_RUNTIME = "EPISODE_RUNTIME_MIN";
    public final static String COLUMN_EPISODES_NUMBER = "EPISODES_NUMBER";
    public final static String COLUMN_SEASONS_NUMBER = "SEASONS_NUMBER";

    private int episodeRuntimeMin;
    private String createdBy;
    private int episodesNumber;
    private int seasonsNumber;
    private boolean inProduction;
    private Date nextEpisodeAirDate;


    /************************************************ GETTERS ************************************************/


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCreator()
    {
        return getCreatedBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getDuration(Context context)
    {
        return new Duration(getEpisodeRuntimeMin(), TVShow.getDurationMeasureUnitName(context), context.getString(R.string.duration_minutes_per_episode_short));
    }

    /**
     * Getter
     * @param context the context
     * @return the name of the measure unit for this media type
     */
    public static String getDurationMeasureUnitName(Context context)
    {
        return context.getString(R.string.duration_minutes_per_episode);
    }

    /**
     * Getter
     * @return the TV show runtime in minutes for each episode
     */
    public int getEpisodeRuntimeMin()
    {
        return episodeRuntimeMin;
    }

    /**
     * Getter
     * @return the TV show creators
     */
    public String getCreatedBy()
    {
        return createdBy;
    }

    /**
     * Getter
     * @return the TV show total number of episodes
     */
    public int getEpisodesNumber()
    {
        return episodesNumber;
    }

    /**
     * Getter
     * @return the TV show number of seasons
     */
    public int getSeasonsNumber()
    {
        return seasonsNumber;
    }

    /**
     * Getter
     * @return true if the TV show is currently in production
     */
    public boolean isInProduction()
    {
        return inProduction;
    }

    /**
     * Getter
     * @return the TV show next episode (meaningful only if the TV show is in production)
     */
    public Date getNextEpisodeAirDate()
    {
        if(!isInProduction() || nextEpisodeAirDate==null) return null;
        return Calendar.getInstance().getTime().after(nextEpisodeAirDate) ? null : nextEpisodeAirDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int getDoingNowName()
    {
        return R.string.doing_now_tv_show;
    }



    /************************************************ SETTERS ************************************************/


    /**
     * Setter
     * @param episodeRuntimeMin the TV show runtime in minutes for each episode
     */
    public void setEpisodeRuntimeMin(int episodeRuntimeMin)
    {
        this.episodeRuntimeMin = episodeRuntimeMin;
    }

    /**
     * Setter
     * @param episodesNumber the TV show total number of episodes
     */
    public void setEpisodesNumber(int episodesNumber)
    {
        this.episodesNumber = episodesNumber;
    }

    /**
     * Setter
     * @param seasonsNumber the TV show number of seasons
     */
    public void setSeasonsNumber(int seasonsNumber)
    {
        this.seasonsNumber = seasonsNumber;
    }

    /**
     * Setter
     * @param inProduction true if the TV show is currently in production
     */
    public void setInProduction(boolean inProduction)
    {
        this.inProduction = inProduction;
    }

    /**
     * Setter
     * @param createdBy the TV show creators
     */
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    /**
     * Setter
     * @param nextEpisodeAirDate the TV show next episode (meaningful only if the TV show is in production)
     */
    public void setNextEpisodeAirDate(Date nextEpisodeAirDate)
    {
        this.nextEpisodeAirDate = nextEpisodeAirDate;
    }
}
