package it.polimi.dima.mediatracker.external_services.services;

import java.util.List;

import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Abstract service for a generic media item that allows to connect to an external API to retrieve data
 * Uses Retrofit external library to manage the connection
 */
public abstract class MediaItemService
{
    public static final int MAX_SEARCH_RESULTS = 10;

    /**
     * Constructor
     */
    MediaItemService()
    {
        // For logging
        /*HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();*/

        // Build retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                //.client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Allow the subclass to set its own rest interface to the retrofit object
        initializeRestInterface(retrofit);
    }

    /**
     * Get the external API base URL
     * @return the base url
     */
    protected abstract String getBaseUrl();

    /**
     * Allows the subclasses to initialize the REST interface with the given retrofit instance
     * @param retrofit the retrofit instance
     */
    protected abstract void initializeRestInterface(Retrofit retrofit);

    /**
     * Allows to search for media items
     * @param query the title to search
     * @param mediaItemSearchCallback the callback that will receive the results
     */
    public abstract void searchMediaItems(String query, final MediaItemSearchCallback mediaItemSearchCallback);

    /**
     * Allows to get information about a specific media item
     * @param externalServiceId the external service ID linked with the media item
     * @param mediaItemInfoCallback the callback that will receive the results
     */
    public abstract void getMediaItemInfo(String externalServiceId, final MediaItemInfoCallback mediaItemInfoCallback);

    /**
     * Callback called when the external service search results are available
     */
    public interface MediaItemSearchCallback
    {
        /**
         * Called when the search results are correctly retrieved
         * @param searchResults the list of search results (may be empty)
         */
        void onLoad(List<MediaItemSearchResult> searchResults);

        /**
         * Called in case of failure (e.g. no internet)
         */
        void onFailure();
    }

    /**
     * Callback called when the external service media item details are available
     */
    public interface MediaItemInfoCallback
    {
        /**
         * Called when the media item details are correctly retrieved
         * @param mediaItem the retrieved media item (may be null)
         */
        void onLoad(MediaItem mediaItem);

        /**
         * Called in case of failure (e.g. no internet)
         */
        void onFailure();
    }
}
