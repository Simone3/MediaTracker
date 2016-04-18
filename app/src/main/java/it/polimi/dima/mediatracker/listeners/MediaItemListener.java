package it.polimi.dima.mediatracker.listeners;


import it.polimi.dima.mediatracker.model.MediaItem;

/**
 * Listener used by fragments to inform the host activity that an event (select, delete) occurred on a media item
 */
public interface MediaItemListener
{
    /**
     * Called when a media item is selected by the user
     * @param mediaItem the selected media item
     */
    void onMediaItemSelected(MediaItem mediaItem);

    /**
     * Called when a media item is removed (deleted, set as completed, etc.) by the user
     * @param mediaItem the removed media item
     */
    void onMediaItemRemoved(MediaItem mediaItem);
}
