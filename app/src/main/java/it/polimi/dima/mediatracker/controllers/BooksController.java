package it.polimi.dima.mediatracker.controllers;

import android.content.Context;

import java.util.HashMap;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.services.BookService;
import it.polimi.dima.mediatracker.external_services.services.MediaItemService;
import it.polimi.dima.mediatracker.fragments.FormBookFragment;
import it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsAbstractFragment;
import it.polimi.dima.mediatracker.fragments.SuggestionsBookFragment;
import it.polimi.dima.mediatracker.model.Book;
import it.polimi.dima.mediatracker.model.MediaItem;

/**
 * {@inheritDoc}
 *
 * Controller for books
 */
public class BooksController extends MediaItemsAbstractController
{
    private static BooksController instance;

    /**
     * Singleton pattern
     */
    public static synchronized BooksController getInstance()
    {
        if(instance==null) instance = new BooksController();
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getModelClass()
    {
        return Book.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItem initializeEmptyMediaItem()
    {
        return new Book();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItemService getMediaItemService(Context context)
    {
        return BookService.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRedoOptionName()
    {
        return R.string.redo_book;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCompleteOptionName()
    {
        return R.string.set_as_done_book;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDoingOptionName()
    {
        return R.string.set_as_doing_book;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDurationDatabaseFieldName()
    {
        return Book.COLUMN_PAGES_NUMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormMediaItemAbstractFragment getMediaItemFormFragment(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        return FormBookFragment.newInstance(categoryId, mediaItemId, isEmpty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuggestionsAbstractFragment getMediaItemSuggestionsFragment(Long categoryId)
    {
        return SuggestionsBookFragment.newInstance(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String validateSpecificMediaItem(Context context, MediaItem mediaItem)
    {
        Book book = (Book) mediaItem;

        // Negative pages
        if(book.getPagesNumber()<0)
        {
            book.setPagesNumber(0);
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
        // Negative pages
        Object pages = values.get(Book.COLUMN_PAGES_NUMBER);
        if(pages!=null)
        {
            try
            {
                int v = Integer.valueOf((String) pages);
                if(v<0)
                {
                    throw new Exception();
                }
            }
            catch(Exception e)
            {
                values.put(Book.COLUMN_PAGES_NUMBER, 0);
            }
        }

        // No error
        return null;
    }
}
