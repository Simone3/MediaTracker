package it.polimi.dima.mediatracker.external_services.model_json;

import android.text.Html;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;

import it.polimi.dima.mediatracker.model.Movie;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A movie representation in JSON, used to retrieve data from the external service responses
 */
public class MovieJSON extends MediaItemJSON
{
    final static String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w780";

    @SerializedName("id")
    @Expose
    private String apiId;

    @SerializedName("release_date")
    @Expose
    private String releaseDate;

    @SerializedName("genres")
    @Expose
    private GenreJSON[] genres;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("overview")
    @Expose
    private String description;

    @SerializedName("runtime")
    @Expose
    private int durationMin;

    @SerializedName("credits")
    @Expose
    private CreditsJSON credits;

    @SerializedName("backdrop_path")
    @Expose
    private String backdropPath;

    private class CreditsJSON
    {
        @SerializedName("crew")
        @Expose
        private CrewPersonJSON[] crewPeople;

        private class CrewPersonJSON
        {
            @SerializedName("name")
            @Expose
            private String name;

            @SerializedName("job")
            @Expose
            private String job;
        }
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Movie convertToMediaItem()
    {
        Movie movie = new Movie();

        movie.setExternalServiceId(apiId);
        movie.setReleaseDate(Utils.parseDateFromString(releaseDate, "yyyy-MM-dd"));
        movie.setGenres(Utils.joinIfNotEmpty(", ", genres));
        movie.setTitle(title);
        if(description!=null) movie.setDescription(Html.fromHtml(description).toString());
        movie.setDurationMin(durationMin);

        if(credits!=null)
        {
            CreditsJSON.CrewPersonJSON[] crew = credits.crewPeople;
            if(crew!=null && crew.length>0)
            {
                for(CreditsJSON.CrewPersonJSON person: crew)
                {
                    if("Director".equals(person.job))
                    {
                        movie.setDirector(person.name);
                        break;
                    }
                }
            }
        }

        if(!Utils.isEmpty(backdropPath))
        {
            try
            {
                movie.setImageUrl(new URL(IMAGE_BASE_URL+backdropPath));
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }

        return movie;
    }
}
