package it.polimi.dima.mediatracker.controllers;

import android.content.Context;

import java.util.HashMap;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.services.MediaItemService;
import it.polimi.dima.mediatracker.external_services.services.MovieService;
import it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment;
import it.polimi.dima.mediatracker.fragments.FormMovieFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsAbstractFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsMovieFragment;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.Movie;

/**
 * {@inheritDoc}
 *
 * Controller for movies
 */
public class MoviesController extends MediaItemsAbstractController
{
    private static MoviesController instance;

    /**
     * Singleton pattern
     */
    public static synchronized MoviesController getInstance()
    {
        if(instance==null) instance = new MoviesController();
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getModelClass()
    {
        return Movie.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItem initializeEmptyMediaItem()
    {
        return new Movie();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItemService getMediaItemService(Context context)
    {
        return MovieService.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDoingNowName()
    {
        return R.string.doing_now_movie;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRedoOptionName()
    {
        return R.string.redo_movie;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCompleteOptionName()
    {
        return R.string.set_as_done_movie;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDoingOptionName()
    {
        return R.string.set_as_doing_movie;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDurationDatabaseFieldName()
    {
        return Movie.COLUMN_DURATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormMediaItemAbstractFragment getMediaItemFormFragment(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        return FormMovieFragment.newInstance(categoryId, mediaItemId, isEmpty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuggestionsAbstractFragment getMediaItemSuggestionsFragment(Long categoryId)
    {
        return SuggestionsMovieFragment.newInstance(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String validateSpecificMediaItem(Context context, MediaItem mediaItem)
    {
        Movie movie = (Movie) mediaItem;

        // Negative duration
        if(movie.getDurationMin()<0)
        {
            movie.setDurationMin(0);
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
        Object duration = values.get(Movie.COLUMN_DURATION);
        if(duration!=null)
        {
            try
            {
                int v = Integer.valueOf((String) duration);
                if(v<0)
                {
                    throw new Exception();
                }
            }
            catch(Exception e)
            {
                values.put(Movie.COLUMN_DURATION, 0);
            }
        }

        // No error
        return null;
    }
}
