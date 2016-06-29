package it.polimi.dima.mediatracker.external_services.model_json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A movie search representation in JSON, used to retrieve data from the external service responses
 */
public class MovieSearchJSON
{
    @SerializedName("results")
    @Expose
    private List<MovieSearchResultJSON> items;

    private class MovieSearchResultJSON
    {
        @SerializedName("id")
        @Expose
        private String apiId;

        @SerializedName("release_date")
        @Expose
        private String releaseDate;

        @SerializedName("title")
        @Expose
        private String title;

        /**
         * Converts this search result JSON representation to an actual search result model
         * @return the search result with all properties of this JSON representation
         */
        private MediaItemSearchResult convertToItemSearchResult()
        {
            int year = Utils.parseYearFromString(releaseDate, "yyyy-MM-dd");
            return new MediaItemSearchResult(apiId, title, null, year<=0 ? null : String.valueOf(year));
        }
    }

    /**
     * Returns the list of search results associated with this JSON search representation
     * @return the list of search results
     */
    public List<MediaItemSearchResult> getSearchList()
    {
        List<MediaItemSearchResult> result = new ArrayList<>();
        if(items!=null) for(MovieSearchResultJSON s: items) result.add(s.convertToItemSearchResult());
        return result;
    }
}
