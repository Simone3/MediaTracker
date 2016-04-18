package it.polimi.dima.mediatracker.model;

import android.content.Context;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.BooksController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.controllers.MoviesController;
import it.polimi.dima.mediatracker.controllers.TVShowsController;
import it.polimi.dima.mediatracker.controllers.VideogamesController;

/**
 * Media types associated to each category. Each of them has a linked controller that will take care of
 * everything (database queries, associated activities, etc.)
 */
public enum MediaType
{
    BOOKS(BooksController.getInstance(), R.string.book, R.string.books, R.drawable.ic_book, R.drawable.ic_form_book, R.color.orange),
    MOVIES(MoviesController.getInstance(), R.string.movie, R.string.movies, R.drawable.ic_movie, R.drawable.ic_form_movie, R.color.blue),
    VIDEOGAMES(VideogamesController.getInstance(), R.string.videogame, R.string.videogames, R.drawable.ic_videogame, R.drawable.ic_form_videogame, R.color.green),
    TV_SHOWS(TVShowsController.getInstance(), R.string.tv_show, R.string.tv_shows, R.drawable.ic_tvshow, R.drawable.ic_form_tvshow, R.color.red);

    private MediaItemsAbstractController controller;
    private Integer nameSingular;
    private Integer namePlural;
    private Integer icon;
    private Integer color;
    private Integer formIcon;

    /**
     * Constructor
     * @param controller the controller associated with this media type
     * @param namePlural the media type name resource ID
     * @param icon the media type icon resource ID
     * @param formIcon the media type form icon resource ID
     * @param color the media type color
     */
    MediaType(MediaItemsAbstractController controller, Integer nameSingular, Integer namePlural, Integer icon, Integer formIcon, Integer color)
    {
        this.controller = controller;
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
        this.icon = icon;
        this.formIcon = formIcon;
        this.color = color;
    }

    /**
     * Getter
     * @return the controller associated with this media type
     */
    public MediaItemsAbstractController getController()
    {
        return controller;
    }

    /**
     * Getter
     * @param context the context
     * @return the media type singular name resource ID
     */
    public String getNameSingular(Context context)
    {
        return context.getString(this.nameSingular);
    }

    /**
     * Getter
     * @param context the context
     * @return the media type plural name resource ID
     */
    public String getNamePlural(Context context)
    {
        return context.getString(this.namePlural);
    }

    /**
     * Getter
     * @return the media type icon resource ID
     */
    public Integer getIcon()
    {
        return icon;
    }

    /**
     * Getter
     * @return the media type color
     */
    public Integer getColor()
    {
        return color;
    }

    public Integer getFormIcon()
    {
        return formIcon;
    }
}
