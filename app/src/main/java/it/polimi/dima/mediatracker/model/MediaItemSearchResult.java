package it.polimi.dima.mediatracker.model;

/**
 * A search result for a generic media item, used for example in the form autocomplete title input
 */
public class MediaItemSearchResult
{
    private String apiId;
    private String title;
    private String author;
    private String year;

    /**
     * Constructor
     * @param apiId the external service API id
     * @param title the linked media item title
     * @param author the linked media item creator
     * @param year the linked media item year(s)
     */
    public MediaItemSearchResult(String apiId, String title, String author, String year)
    {
        this.apiId = apiId;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    /**
     * Getter
     * @return the external service API id
     */
    public String getApiId()
    {
        return apiId;
    }

    /**
     * Getter
     * @return the linked media item title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Getter
     * @return the linked media item year(s)
     */
    public String getYear()
    {
        return year;
    }

    /**
     * Getter
     * @return the linked media item creator
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return title;
    }
}
