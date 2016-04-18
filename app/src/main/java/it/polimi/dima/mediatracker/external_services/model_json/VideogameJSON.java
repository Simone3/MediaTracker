package it.polimi.dima.mediatracker.external_services.model_json;

import android.text.Html;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import it.polimi.dima.mediatracker.model.Videogame;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A videogame representation in JSON, used to retrieve data from the external service responses
 */
public class VideogameJSON extends MediaItemJSON
{
    @SerializedName("results")
    @Expose
    private ResultJSON result;

    private class ResultJSON
    {
        @SerializedName("id")
        @Expose
        private String apiId;

        @SerializedName("original_release_date")
        @Expose
        private String originalReleaseDate;

        @SerializedName("expected_release_day")
        @Expose
        private int expectedReleaseDay;

        @SerializedName("expected_release_month")
        @Expose
        private int expectedReleaseMonth;

        @SerializedName("expected_release_year")
        @Expose
        private int expectedReleaseYear;

        @SerializedName("genres")
        @Expose
        private GenreJSON[] genres;

        @SerializedName("name")
        @Expose
        private String title;

        @SerializedName("deck")
        @Expose
        private String description;

        @SerializedName("developers")
        @Expose
        private DeveloperJSON[] developers;

        @SerializedName("publishers")
        @Expose
        private PublisherJSON[] publishers;

        @SerializedName("platforms")
        @Expose
        private PlatformJSON[] platforms;

        @SerializedName("image")
        @Expose
        private ImageJSON image;

        private class ImageJSON
        {
            @SerializedName("screen_url")
            @Expose
            private String image;

            @SerializedName("medium_url")
            @Expose
            private String imageAlternative;
        }

        private class GenreJSON
        {
            @SerializedName("name")
            @Expose
            String genreName;

            @Override
            public String toString()
            {
                return genreName;
            }
        }

        private class PublisherJSON
        {
            @SerializedName("name")
            @Expose
            String name;

            @Override
            public String toString()
            {
                return name;
            }
        }

        private class DeveloperJSON
        {
            @SerializedName("name")
            @Expose
            String name;

            @Override
            public String toString()
            {
                return name;
            }
        }

        private class PlatformJSON
        {
            @SerializedName("name")
            @Expose
            String fullName;

            @SerializedName("abbreviation")
            @Expose
            String abbreviation;

            @Override
            public String toString()
            {
                return (abbreviation == null || "".equals(abbreviation) ? fullName : abbreviation);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Videogame convertToMediaItem()
    {
        Videogame videogame = new Videogame();

        if(result==null) return videogame;

        videogame.setExternalServiceId(result.apiId);

        Date date = result.originalReleaseDate != null ? Utils.parseDateFromString(result.originalReleaseDate, "yyyy-MM-dd HH:mm:ss") : Utils.parseDateFromYearMonthDay(result.expectedReleaseYear, result.expectedReleaseMonth, result.expectedReleaseDay);
        videogame.setReleaseDate(date);

        videogame.setGenres(Utils.joinIfNotEmpty(", ", result.genres));
        videogame.setTitle(result.title);
        if(result.description!=null) videogame.setDescription(Html.fromHtml(result.description).toString());

        videogame.setDeveloper(Utils.joinIfNotEmpty(", ", result.developers));
        videogame.setPublisher(Utils.joinIfNotEmpty(", ", result.publishers));
        videogame.setPlatforms(Utils.joinIfNotEmpty(", ", result.platforms));

        String image = result.image.image!=null ? result.image.image : result.image.imageAlternative;
        if(!Utils.isEmpty(image))
        {
            try
            {
                videogame.setImageUrl(new URL(image));
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }

        return videogame;
    }
}
