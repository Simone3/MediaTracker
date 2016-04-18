package it.polimi.dima.mediatracker.external_services.model_json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A TV Show season representation in JSON, used to retrieve data from the external service responses
 */
public class TVShowSeasonJSON
{
    @SerializedName("episodes")
    @Expose
    private List<EpisodeJSON>  episodes;

    private class EpisodeJSON
    {
        @SerializedName("air_date")
        @Expose
        private String airDate;

        private Date getAirDate()
        {
            return Utils.parseDateFromString(airDate, "yyyy-MM-dd");
        }
    }

    public Date getNextEpisodeAirDate()
    {
        Date nextEpisodeDate = null;

        // If we have episodes
        if(episodes!=null)
        {
            // Get current date
            Date now = Calendar.getInstance().getTime();

            // Get next episode (if any)
            Date currentAirDate;
            for(int i=episodes.size()-1; i>=0; i--)
            {
                currentAirDate = episodes.get(i).getAirDate();
                if(now.before(currentAirDate))
                {
                    nextEpisodeDate = currentAirDate;
                }
                else
                {
                    break;
                }
            }
        }

        return nextEpisodeDate;
    }
}
