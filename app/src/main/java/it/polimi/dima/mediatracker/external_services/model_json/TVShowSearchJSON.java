package it.polimi.dima.mediatracker.external_services.model_json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A TV show search representation in JSON, used to retrieve data from the external service responses
 */
public class TVShowSearchJSON
{
    @SerializedName("results")
    @Expose
    private List<TVShowSearchResultJSON> items;

    private class TVShowSearchResultJSON
    {
        @SerializedName("id")
        @Expose
        private String apiId;

        @SerializedName("name")
        @Expose
        private String title;

        @SerializedName("first_air_date")
        @Expose
        private String firstAirDate;

        /**
         * Converts this search result JSON representation to an actual search result model
         * @return the search result with all properties of this JSON representation
         */
        private MediaItemSearchResult convertToItemSearchResult()
        {
            int year = firstAirDate==null ? 0 : Utils.parseYearFromString(firstAirDate, "yyyy-MM-dd");
            return new MediaItemSearchResult(apiId, title, null, String.valueOf(year));
        }
    }

    /**
     * Returns the list of search results associated with this JSON search representation
     * @return the list of search results
     */
    public List<MediaItemSearchResult> getSearchList()
    {
        List<MediaItemSearchResult> result = new ArrayList<>();
        if(items!=null) for(TVShowSearchResultJSON s: items) result.add(s.convertToItemSearchResult());
        return result;
    }
}
