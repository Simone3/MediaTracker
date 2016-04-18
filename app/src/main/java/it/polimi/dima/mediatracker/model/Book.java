package it.polimi.dima.mediatracker.model;

import android.content.Context;

import it.polimi.dima.mediatracker.R;

/**
 * A book
 */
public class Book extends MediaItem
{
    public final static String COLUMN_PAGES_NUMBER = "PAGES_NUMBER";

    private int pagesNumber;
    private String author;


    /************************************************ GETTERS ************************************************/


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCreator()
    {
        return getAuthor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getDuration(Context context)
    {
        return new Duration(getPagesNumber(), Book.getDurationMeasureUnitName(context), context.getString(R.string.duration_pages_short));
    }

    /**
     * Getter
     * @param context the context
     * @return the name of the measure unit for this media type
     */
    public static String getDurationMeasureUnitName(Context context)
    {
        return context.getString(R.string.duration_pages);
    }

    /**
     * Getter
     * @return the book author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Getter
     * @return the number of pages in the book
     */
    public int getPagesNumber()
    {
        return pagesNumber;
    }



    /************************************************ SETTERS ************************************************/


    /**
     * Setter
     * @param author the book author
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * Setter
     * @param pagesNumber the number of pages in the book
     */
    public void setPagesNumber(int pagesNumber)
    {
        this.pagesNumber = pagesNumber;
    }
}
