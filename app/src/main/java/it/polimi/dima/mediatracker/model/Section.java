package it.polimi.dima.mediatracker.model;

import android.content.Context;
import android.support.annotation.StringRes;

import it.polimi.dima.mediatracker.adapters.media_items_list.SectionedRecyclerViewAdapter;

/**
 * Represents a section for the {@link SectionedRecyclerViewAdapter}
 */
public class Section
{
    private String sectionId;
    private @StringRes int sectionNameResource;
    private String sectionName;

    /**
     * Constructor
     * @param sectionId the (unique) ID of this section
     * @param sectionName the name of this section
     */
    public Section(String sectionId, String sectionName)
    {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
    }

    /**
     * Constructor
     * @param sectionId the (unique) ID of this section
     * @param sectionNameResource the resource id of the section name
     */
    public Section(String sectionId, @StringRes int sectionNameResource)
    {
        this.sectionId = sectionId;
        this.sectionNameResource = sectionNameResource;
    }

    /**
     * Getter
     * @return the (unique) ID of this section
     */
    public String getSectionId()
    {
        return sectionId;
    }

    /**
     * Getter
     * @return the name of this section
     */
    public String getSectionName(Context context)
    {
        if(sectionName==null && sectionNameResource!=0) sectionName = context.getString(sectionNameResource);
        return sectionName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object)
    {
        if(object==null || !(object instanceof Section)) return false;

        Section other = (Section) object;

        return sectionId!=null && other.sectionId!=null && other.sectionId.equals(sectionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this.sectionId;
    }
}
