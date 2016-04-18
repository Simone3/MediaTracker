package it.polimi.dima.mediatracker.adapters.media_items_list;

/**
 * Represents a section for the {@link SectionedAbstractAdapterWrapper}
 */
public class AdapterSection
{
    private String sectionId;
    private String sectionName;

    /**
     * Constructor
     * @param sectionId the (unique) ID of this section
     * @param sectionName the name of this section
     */
    public AdapterSection(String sectionId, String sectionName)
    {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
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
    public String getSectionName()
    {
        return sectionName;
    }

    @Override
    public boolean equals(Object object)
    {
        if(object==null || !(object instanceof AdapterSection)) return false;

        AdapterSection other = (AdapterSection) object;

        return sectionId!=null && other.sectionId!=null && other.sectionId.equals(sectionId);
    }

    @Override
    public String toString()
    {
        return this.sectionId;
    }
}
