package it.polimi.dima.mediatracker.external_services.rest_interfaces;

import it.polimi.dima.mediatracker.external_services.model_json.VideogameJSON;
import it.polimi.dima.mediatracker.external_services.model_json.VideogameSearchJSON;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * REST interface to communicate with the videogames external service API
 */
public interface VideogameRestInterface
{
    /**
     * Searches videogames
     * @param query the title to search
     * @param maxResults max number of results
     * @param apiKey the service API key
     * @return a search JSON representation
     */
    @GET("search?format=json&resources=game")
    Call<VideogameSearchJSON> searchVideogames(@Query("query") String query, @Query("limit") int maxResults, @Query("api_key") String apiKey);

    /**
     * Get data for a specific videogame
     * @param gameId the ID of the videogame
     * @param apiKey the service API key
     * @return the videogame JSON representation
     */
    @GET("game/{gameId}/?format=json")
    Call<VideogameJSON> getVideogameInfo(@Path("gameId") String gameId, @Query("api_key") String apiKey);
}
