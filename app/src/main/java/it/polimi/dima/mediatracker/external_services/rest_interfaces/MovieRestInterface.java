package it.polimi.dima.mediatracker.external_services.rest_interfaces;

import it.polimi.dima.mediatracker.external_services.model_json.MovieJSON;
import it.polimi.dima.mediatracker.external_services.model_json.MovieSearchJSON;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * REST interface to communicate with the movies external service API
 */
public interface MovieRestInterface
{
    /**
     * Searches movies
     * @param query the title to search
     * @param apiKey the service API key
     * @return a search JSON representation
     */
    @GET("search/movie/?")
    Call<MovieSearchJSON> searchMovies(@Query("query") String query, @Query("api_key") String apiKey);

    /**
     * Get data for a specific movie
     * @param movieId the ID of the movie
     * @param apiKey the service API key
     * @return the movie JSON representation
     */
    @GET("movie/{movieId}?append_to_response=credits")
    Call<MovieJSON> getMovieInfo(@Path("movieId") String movieId, @Query("api_key") String apiKey);
}
