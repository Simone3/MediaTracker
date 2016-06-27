package it.polimi.dima.mediatracker.external_services.services;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.model_json.MediaItemJSON;
import it.polimi.dima.mediatracker.external_services.model_json.MovieJSON;
import it.polimi.dima.mediatracker.external_services.model_json.MovieSearchJSON;
import it.polimi.dima.mediatracker.external_services.rest_interfaces.MovieRestInterface;
import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Service for movies that allows to connect to an external API to retrieve data
 * Uses Retrofit external library to manage the connection
 */
public class MovieService extends MediaItemService
{
    private Context appContext;

    private static MovieService instance;

    private MovieRestInterface restInterface;

    /**
     * Private constructor
     * @param context the context
     */
    private MovieService(Context context)
    {
        super();
        this.appContext = context.getApplicationContext();
    }

    /**
     * Singleton pattern
     */
    public static synchronized MovieService getInstance(Context context)
    {
        if(instance==null) instance = new MovieService(context);
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getBaseUrl()
    {
        return "http://api.themoviedb.org/3/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeRestInterface(Retrofit retrofit)
    {
        restInterface = retrofit.create(MovieRestInterface.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchMediaItems(String query, final MediaItemSearchCallback mediaItemSearchCallback)
    {
        restInterface.searchMovies(query, appContext.getString(R.string.themoviedb_api_key)).enqueue(new Callback<MovieSearchJSON>()
        {
            @Override
            public void onResponse(Call<MovieSearchJSON> call, Response<MovieSearchJSON> response)
            {
                if(response.body()!=null)
                {
                    List<MediaItemSearchResult> movies = response.body().getSearchList();
                    mediaItemSearchCallback.onLoad(movies);
                }
                else
                {
                    mediaItemSearchCallback.onLoad(new ArrayList<MediaItemSearchResult>());
                }
            }

            @Override
            public void onFailure(Call<MovieSearchJSON> call, Throwable t)
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
        restInterface.getMovieInfo(externalServiceId, appContext.getString(R.string.themoviedb_api_key)).enqueue(new Callback<MovieJSON>()
        {
            @Override
            public void onResponse(Call<MovieJSON> call, Response<MovieJSON> response)
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
            public void onFailure(Call<MovieJSON> call, Throwable t)
            {
                mediaItemInfoCallback.onFailure();
            }
        });
    }
}
