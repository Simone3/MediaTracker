package it.polimi.dima.mediatracker.external_services.model_json;

import it.polimi.dima.mediatracker.model.MediaItem;

/**
 * An abstract representation of a media item in JSON, used to retrieve data from the external service responses
 */
public abstract class MediaItemJSON
{
    /**
     * Converts the current media item from JSON to the actual media item model
     * @return the media item with all the properties contained in the JSON representation
     */
    public abstract MediaItem convertToMediaItem();
}
