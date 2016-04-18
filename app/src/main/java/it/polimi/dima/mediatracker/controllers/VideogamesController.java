package it.polimi.dima.mediatracker.controllers;

import android.content.Context;

import java.util.HashMap;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.services.MediaItemService;
import it.polimi.dima.mediatracker.external_services.services.VideogameService;
import it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment;
import it.polimi.dima.mediatracker.fragments.FormVideogameFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsAbstractFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsVideogameFragment;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.Videogame;

/**
 * {@inheritDoc}
 *
 * Controller for videogames
 */
public class VideogamesController extends MediaItemsAbstractController
{
    private static VideogamesController instance;

    /**
     * Singleton pattern
     */
    public static synchronized VideogamesController getInstance()
    {
        if(instance==null) instance = new VideogamesController();
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getModelClass()
    {
        return Videogame.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItem initializeEmptyMediaItem()
    {
        return new Videogame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItemService getMediaItemService(Context context)
    {
        return VideogameService.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDoingNowName()
    {
        return R.string.doing_now_videogame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRedoOptionName()
    {
        return R.string.redo_videogame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCompleteOptionName()
    {
        return R.string.set_as_done_videogame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDoingOptionName()
    {
        return R.string.set_as_doing_videogame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDurationDatabaseFieldName()
    {
        return Videogame.COLUMN_AVERAGE_LENGTH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormMediaItemAbstractFragment getMediaItemFormFragment(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        return FormVideogameFragment.newInstance(categoryId, mediaItemId, isEmpty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuggestionsAbstractFragment getMediaItemSuggestionsFragment(Long categoryId)
    {
        return SuggestionsVideogameFragment.newInstance(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String validateSpecificMediaItem(Context context, MediaItem mediaItem)
    {
        Videogame videogame = (Videogame) mediaItem;

        // Negative length
        if(videogame.getAverageLengthHours()<0)
        {
            videogame.setAverageLengthHours(0);
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
        Object duration = values.get(Videogame.COLUMN_AVERAGE_LENGTH);
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
                values.put(Videogame.COLUMN_AVERAGE_LENGTH, 0);
            }
        }

        // No error
        return null;
    }
}
