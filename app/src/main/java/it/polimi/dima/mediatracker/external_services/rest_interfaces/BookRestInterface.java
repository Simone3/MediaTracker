package it.polimi.dima.mediatracker.external_services.rest_interfaces;

import it.polimi.dima.mediatracker.external_services.model_json.BookJSON;
import it.polimi.dima.mediatracker.external_services.model_json.BookSearchJSON;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * REST interface to communicate with the books external service API
 */
public interface BookRestInterface
{
    /**
     * Searches books
     * @param query the title to search
     * @param maxResults max number of results
     * @return a search JSON representation
     */
    @GET("volumes?langRestrict=en&country=US&orderBy=relevance&projection=lite")
    Call<BookSearchJSON> searchBooks(@Query("q") String query, @Query("maxResults") int maxResults);

    /**
     * Get data for a specific book
     * @param bookId the ID of the book
     * @return the book JSON representation
     */
    @GET("volumes/{bookId}")
    Call<BookJSON> getBookInfo(@Path("bookId") String bookId);
}
