package it.polimi.dima.mediatracker.external_services.rest_interfaces;

import it.polimi.dima.mediatracker.external_services.model_json.TVShowSeasonJSON;
import it.polimi.dima.mediatracker.external_services.model_json.TVShowJSON;
import it.polimi.dima.mediatracker.external_services.model_json.TVShowSearchJSON;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * REST interface to communicate with the TV shows external service API
 */
public interface TVShowRestInterface
{
    /**
     * Searches TV shows
     * @param query the title to search
     * @param apiKey the service API key
     * @return a search JSON representation
     */
    @GET("search/tv/?")
    Call<TVShowSearchJSON> searchTVShows(@Query("query") String query, @Query("api_key") String apiKey);

    /**
     * Get data for a specific TV show
     * @param tvShowId the ID of the TV show
     * @param apiKey the service API key
     * @return the TV show JSON representation
     */
    @GET("tv/{tvShowId}?")
    Call<TVShowJSON> getTVShowInfo(@Path("tvShowId") String tvShowId, @Query("api_key") String apiKey);

    /**
     * Retrieves the next episode date for a given TV show
     * @param tvShowId the ID of the TV show
     * @param lastSeasonNumber the number of the last season
     * @param apiKey the service API key
     * @return a TV show season JSON representation
     */
    @GET("tv/{tvShowId}/season/{lastSeasonNumber}?")
    Call<TVShowSeasonJSON> getTVShowNextEpisodeDate(@Path("tvShowId") String tvShowId, @Path("lastSeasonNumber") int lastSeasonNumber, @Query("api_key") String apiKey);
}
