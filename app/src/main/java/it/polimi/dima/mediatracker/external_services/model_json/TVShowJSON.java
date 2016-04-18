package it.polimi.dima.mediatracker.external_services.model_json;

import android.text.Html;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;

import it.polimi.dima.mediatracker.model.TVShow;
import it.polimi.dima.mediatracker.utils.Utils;

import static it.polimi.dima.mediatracker.external_services.model_json.MovieJSON.IMAGE_BASE_URL;

/**
 * A TV show representation in JSON, used to retrieve data from the external service responses
 */
public class TVShowJSON extends MediaItemJSON
{
    @SerializedName("id")
    @Expose
    private String apiId;

    @SerializedName("first_air_date")
    @Expose
    private String firstAirDate;

    @SerializedName("in_production")
    @Expose
    private boolean inProduction;

    @SerializedName("genres")
    @Expose
    private GenreJSON[] genres;

    @SerializedName("name")
    @Expose
    private String title;

    @SerializedName("overview")
    @Expose
    private String description;

    @SerializedName("episode_run_time")
    @Expose
    private int[] episodeRuntimeMin;

    @SerializedName("number_of_episodes")
    @Expose
    private int episodesNumber;

    @SerializedName("number_of_seasons")
    @Expose
    private int seasonsNumber;

    @SerializedName("created_by")
    @Expose
    private CreatorJSON[] creators;

    @SerializedName("backdrop_path")
    @Expose
    private String backdropPath;

    private class CreatorJSON
    {
        @SerializedName("name")
        @Expose
        private String name;

        @Override
        public String toString()
        {
            return name;
        }
    }

    private class GenreJSON
    {
        @SerializedName("name")
        @Expose
        private String genreName;

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
    public TVShow convertToMediaItem()
    {
        TVShow tvShow = new TVShow();

        tvShow.setExternalServiceId(apiId);
        tvShow.setReleaseDate(Utils.parseDateFromString(firstAirDate, "yyyy-MM-dd"));
        tvShow.setGenres(Utils.joinIfNotEmpty(", ", genres));
        tvShow.setTitle(title);
        if(description!=null) tvShow.setDescription(Html.fromHtml(description).toString());
        tvShow.setEpisodeRuntimeMin(Utils.arrayAverage(episodeRuntimeMin));
        tvShow.setCreatedBy(Utils.joinIfNotEmpty(", ", creators));
        tvShow.setEpisodesNumber(episodesNumber);
        tvShow.setSeasonsNumber(seasonsNumber);
        tvShow.setInProduction(inProduction);

        if(!Utils.isEmpty(backdropPath))
        {
            try
            {
                tvShow.setImageUrl(new URL(IMAGE_BASE_URL+backdropPath));
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }

        return tvShow;
    }
}
