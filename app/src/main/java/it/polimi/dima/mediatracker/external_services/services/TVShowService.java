package it.polimi.dima.mediatracker.external_services.services;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.model_json.MediaItemJSON;
import it.polimi.dima.mediatracker.external_services.model_json.TVShowJSON;
import it.polimi.dima.mediatracker.external_services.model_json.TVShowSearchJSON;
import it.polimi.dima.mediatracker.external_services.model_json.TVShowSeasonJSON;
import it.polimi.dima.mediatracker.external_services.rest_interfaces.TVShowRestInterface;
import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import it.polimi.dima.mediatracker.model.TVShow;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Service for TV shows that allows to connect to an external API to retrieve data
 * Uses Retrofit external library to manage the connection
 */
public class TVShowService extends MediaItemService
{
    private Context context;

    private static TVShowService instance;

    private TVShowRestInterface restInterface;

    /**
     * Private constructor
     * @param context the context
     */
    private TVShowService(Context context)
    {
        super();
        this.context = context;
    }

    /**
     * Singleton pattern
     */
    public static synchronized TVShowService getInstance(Context context)
    {
        if(instance==null) instance = new TVShowService(context);
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
        restInterface = retrofit.create(TVShowRestInterface.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchMediaItems(String query, final MediaItemSearchCallback mediaItemSearchCallback)
    {
        restInterface.searchTVShows(query, context.getString(R.string.themoviedb_api_key)).enqueue(new Callback<TVShowSearchJSON>()
        {
            @Override
            public void onResponse(Call<TVShowSearchJSON> call, Response<TVShowSearchJSON> response)
            {
                if(response.body()!=null)
                {
                    List<MediaItemSearchResult> tvShows = response.body().getSearchList();
                    mediaItemSearchCallback.onLoad(tvShows);
                }
                else
                {
                    mediaItemSearchCallback.onLoad(new ArrayList<MediaItemSearchResult>());
                }
            }

            @Override
            public void onFailure(Call<TVShowSearchJSON> call, Throwable t)
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
    public void getMediaItemInfo(final String externalServiceId, final MediaItemInfoCallback mediaItemInfoCallback)
    {
        restInterface.getTVShowInfo(externalServiceId, context.getString(R.string.themoviedb_api_key)).enqueue(new Callback<TVShowJSON>()
        {
            @Override
            public void onResponse(Call<TVShowJSON> call, Response<TVShowJSON> response)
            {
                if(response.body()!=null)
                {
                    MediaItemJSON mediaItemJSON = response.body();
                    final TVShow tvShow = (TVShow) mediaItemJSON.convertToMediaItem();

                    // If it's in production, other query to get the next episode
                    if(tvShow.isInProduction() && tvShow.getSeasonsNumber()>0)
                    {
                        restInterface.getTVShowNextEpisodeDate(externalServiceId, tvShow.getSeasonsNumber(), context.getString(R.string.themoviedb_api_key)).enqueue(new Callback<TVShowSeasonJSON>()
                        {
                            @Override
                            public void onResponse(Call<TVShowSeasonJSON> call, Response<TVShowSeasonJSON> response)
                            {
                                if(response.body()!=null)
                                {
                                    TVShowSeasonJSON TVShowSeasonJSON = response.body();

                                    Date nextEpisodeDate = TVShowSeasonJSON.getNextEpisodeAirDate();
                                    tvShow.setNextEpisodeAirDate(nextEpisodeDate);

                                }

                                mediaItemInfoCallback.onLoad(tvShow);
                            }

                            @Override
                            public void onFailure(Call<TVShowSeasonJSON> call, Throwable t)
                            {
                                t.printStackTrace();
                                mediaItemInfoCallback.onLoad(tvShow);
                            }
                        });
                    }
                    else
                    {
                        mediaItemInfoCallback.onLoad(tvShow);
                    }
                }
                else
                {
                    mediaItemInfoCallback.onLoad(null);
                }
            }

            @Override
            public void onFailure(Call<TVShowJSON> call, Throwable t)
            {
                t.printStackTrace();
                mediaItemInfoCallback.onFailure();
            }
        });
    }
}
