package it.polimi.dima.mediatracker.controllers;

import android.content.Context;

import java.util.HashMap;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.services.MediaItemService;
import it.polimi.dima.mediatracker.external_services.services.TVShowService;
import it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment;
import it.polimi.dima.mediatracker.fragments.FormTVShowFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsAbstractFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsTVShowFragment;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.TVShow;

/**
 * {@inheritDoc}
 *
 * Controller for TV shows
 */
public class TVShowsController extends MediaItemsAbstractController
{
    private static TVShowsController instance;

    /**
     * Singleton pattern
     */
    public static synchronized TVShowsController getInstance()
    {
        if(instance==null) instance = new TVShowsController();
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getModelClass()
    {
        return TVShow.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItem initializeEmptyMediaItem()
    {
        return new TVShow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItemService getMediaItemService(Context context)
    {
        return TVShowService.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDoingNowName()
    {
        return R.string.doing_now_tv_show;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRedoOptionName()
    {
        return R.string.redo_tv_show;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCompleteOptionName()
    {
        return R.string.set_as_done_tv_show;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDoingOptionName()
    {
        return R.string.set_as_doing_tv_show;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDurationDatabaseFieldName()
    {
        return TVShow.COLUMN_EPISODE_RUNTIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormMediaItemAbstractFragment getMediaItemFormFragment(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        return FormTVShowFragment.newInstance(categoryId, mediaItemId, isEmpty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuggestionsAbstractFragment getMediaItemSuggestionsFragment(Long categoryId)
    {
        return SuggestionsTVShowFragment.newInstance(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String validateSpecificMediaItem(Context context, MediaItem mediaItem)
    {
        TVShow tvShow = (TVShow) mediaItem;

        // Negative duration
        if(tvShow.getEpisodeRuntimeMin()<0)
        {
            tvShow.setEpisodeRuntimeMin(0);
        }

        // Negative episodes number
        if(tvShow.getEpisodesNumber()<0)
        {
            tvShow.setEpisodesNumber(0);
        }

        // Negative seasons number
        if(tvShow.getSeasonsNumber()<0)
        {
            tvShow.setSeasonsNumber(0);
        }

        // No errors
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String validateSpecificMediaItemDbRow(Context context, HashMap<String, Object> values)
    {
        // Negative duration
        Object episodeRuntime = values.get(TVShow.COLUMN_EPISODE_RUNTIME);
        if(episodeRuntime!=null)
        {
            try
            {
                int v = Integer.valueOf((String) episodeRuntime);
                if(v<0)
                {
                    throw new Exception();
                }
            }
            catch(Exception e)
            {
                values.put(TVShow.COLUMN_EPISODE_RUNTIME, 0);
            }
        }

        // Negative episodes number
        Object episodesNumber = values.get(TVShow.COLUMN_EPISODES_NUMBER);
        if(episodesNumber!=null)
        {
            try
            {
                int v = Integer.valueOf((String) episodesNumber);
                if(v<0)
                {
                    throw new Exception();
                }
            }
            catch(Exception e)
            {
                values.put(TVShow.COLUMN_EPISODES_NUMBER, 0);
            }
        }

        // Negative seasons number
        Object seasonsNumber = values.get(TVShow.COLUMN_SEASONS_NUMBER);
        if(seasonsNumber!=null)
        {
            try
            {
                int v = Integer.valueOf((String) seasonsNumber);
                if(v<0)
                {
                    throw new Exception();
                }
            }
            catch(Exception e)
            {
                values.put(TVShow.COLUMN_SEASONS_NUMBER, 0);
            }
        }

        // No error
        return null;
    }
}
