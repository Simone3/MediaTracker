package it.polimi.dima.mediatracker.external_services.model_json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A book search representation in JSON, used to retrieve data from the external service responses
 */
public class BookSearchJSON
{
    @SerializedName("items")
    @Expose
    private List<BookSearchResultJSON> items;

    private class BookSearchResultJSON
    {
        @SerializedName("id")
        @Expose
        private String apiId;

        @SerializedName("volumeInfo")
        @Expose
        private VolumeInfoJSON volInfo;

        private class VolumeInfoJSON
        {
            @SerializedName("publishedDate")
            @Expose
            private String releaseDate;

            @SerializedName("title")
            @Expose
            private String title;

            @SerializedName("authors")
            @Expose
            private String[] authors;
        }

        /**
         * Converts this search result JSON representation to an actual search result model
         * @return the search result with all properties of this JSON representation
         */
        private MediaItemSearchResult convertToItemSearchResult()
        {
            int year = 0;
            if (volInfo.releaseDate != null && !volInfo.releaseDate.equals(""))
            {
                int length = volInfo.releaseDate.length();
                String format;
                if (length == 4) format = "yyyy";
                else if (length == 7) format = "yyyy-MM";
                else format = "yyyy-MM-dd";
                year = Utils.parseYearFromString(volInfo.releaseDate, format);
            }

            return new MediaItemSearchResult(apiId, volInfo.title, Utils.joinIfNotEmpty(", ", volInfo.authors), year<=0 ? null : String.valueOf(year));
        }
    }

    /**
     * Returns the list of search results associated with this JSON search representation
     * @return the list of search results
     */
    public List<MediaItemSearchResult> getSearchList()
    {
        List<MediaItemSearchResult> result = new ArrayList<>();
        if(items!=null) for(BookSearchResultJSON s: items) result.add(s.convertToItemSearchResult());
        return result;
    }
}
