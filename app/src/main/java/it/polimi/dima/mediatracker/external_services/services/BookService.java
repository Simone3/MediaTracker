package it.polimi.dima.mediatracker.external_services.services;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.external_services.rest_interfaces.BookRestInterface;
import it.polimi.dima.mediatracker.external_services.model_json.BookJSON;
import it.polimi.dima.mediatracker.external_services.model_json.BookSearchJSON;
import it.polimi.dima.mediatracker.external_services.model_json.MediaItemJSON;
import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Service for books that allows to connect to an external API to retrieve data
 * Uses Retrofit external library to manage the connection
 */
public class BookService extends MediaItemService
{
    private Context context;

    private static BookService instance;

    private BookRestInterface restInterface;

    /**
     * Private constructor
     * @param context the context
     */
    private BookService(Context context)
    {
        super();
        this.context = context;
    }

    /**
     * Singleton pattern
     */
    public static synchronized BookService getInstance(Context context)
    {
        if(instance==null) instance = new BookService(context);
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getBaseUrl()
    {
        return "https://www.googleapis.com/books/v1/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeRestInterface(Retrofit retrofit)
    {
        restInterface = retrofit.create(BookRestInterface.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchMediaItems(String query, final MediaItemSearchCallback mediaItemSearchCallback)
    {
        restInterface.searchBooks(query, MediaItemService.MAX_SEARCH_RESULTS).enqueue(new Callback<BookSearchJSON>()
        {
            @Override
            public void onResponse(Call<BookSearchJSON> call, Response<BookSearchJSON> response)
            {
                if(response.body()!=null)
                {
                    List<MediaItemSearchResult> books = response.body().getSearchList();
                    mediaItemSearchCallback.onLoad(books);
                }
                else
                {
                    mediaItemSearchCallback.onLoad(new ArrayList<MediaItemSearchResult>());
                }
            }

            @Override
            public void onFailure(Call<BookSearchJSON> call, Throwable t)
            {
                t.printStackTrace();
                mediaItemSearchCallback.onFailure();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getMediaItemInfo(String externalServiceId, final MediaItemInfoCallback mediaItemInfoCallback)
    {
        restInterface.getBookInfo(externalServiceId).enqueue(new Callback<BookJSON>()
        {
            @Override
            public void onResponse(Call<BookJSON> call, Response<BookJSON> response)
            {
                if(response.body()!=null)
                {
                    MediaItemJSON mediaItemJSON = response.body();
                    mediaItemInfoCallback.onLoad(mediaItemJSON.convertToMediaItem());
                }
                else
                {
                    mediaItemInfoCallback.onLoad(null);
                }
            }

            @Override
            public void onFailure(Call<BookJSON> call, Throwable t)
            {
                mediaItemInfoCallback.onFailure();
            }
        });
    }
}
