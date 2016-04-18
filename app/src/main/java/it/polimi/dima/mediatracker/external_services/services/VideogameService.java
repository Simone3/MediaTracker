package it.polimi.dima.mediatracker.external_services.services;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.model_json.MediaItemJSON;
import it.polimi.dima.mediatracker.external_services.model_json.VideogameJSON;
import it.polimi.dima.mediatracker.external_services.model_json.VideogameSearchJSON;
import it.polimi.dima.mediatracker.external_services.rest_interfaces.VideogameRestInterface;
import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Service for videogames that allows to connect to an external API to retrieve data
 * Uses Retrofit external library to manage the connection
 */
public class VideogameService extends MediaItemService
{
    private Context context;

    private static VideogameService instance;

    private VideogameRestInterface restInterface;

    /**
     * Private constructor
     * @param context the context
     */
    private VideogameService(Context context)
    {
        super();
        this.context = context;
    }

    /**
     * Singleton pattern
     */
    public static synchronized VideogameService getInstance(Context context)
    {
        if(instance==null) instance = new VideogameService(context);
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getBaseUrl()
    {
        return "http://www.giantbomb.com/api/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeRestInterface(Retrofit retrofit)
    {
        restInterface = retrofit.create(VideogameRestInterface.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchMediaItems(String query, final MediaItemSearchCallback mediaItemSearchCallback)
    {
        restInterface.searchVideogames(query, MAX_SEARCH_RESULTS, context.getString(R.string.giantbomb_api_key)).enqueue(new Callback<VideogameSearchJSON>()
        {
            @Override
            public void onResponse(Call<VideogameSearchJSON> call, Response<VideogameSearchJSON> response)
            {
                if(response.body()!=null)
                {
                    List<MediaItemSearchResult> videogames = response.body().getSearchList();
                    mediaItemSearchCallback.onLoad(videogames);
                }
                else
                {
                    mediaItemSearchCallback.onLoad(new ArrayList<MediaItemSearchResult>());
                }
            }

            @Override
            public void onFailure(Call<VideogameSearchJSON> call, Throwable t)
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
        restInterface.getVideogameInfo(externalServiceId, context.getString(R.string.giantbomb_api_key)).enqueue(new Callback<VideogameJSON>()
        {
            @Override
            public void onResponse(Call<VideogameJSON> call, Response<VideogameJSON> response)
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
            public void onFailure(Call<VideogameJSON> call, Throwable t)
            {
                mediaItemInfoCallback.onFailure();
            }
        });
    }
}
