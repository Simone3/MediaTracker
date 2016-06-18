package it.polimi.dima.mediatracker.model;

/**
 * Interface implemented by the items passed to {@link it.polimi.dima.mediatracker.adapters.media_items_list.SectionedRecyclerViewAdapter}
 */
public interface Sectioned
{
    /**
     * Returns the section of the item
     * @return the item section
     */
    Section getSection();
}
