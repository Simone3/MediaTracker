package it.polimi.dima.mediatracker.external_services.model_json;

import android.text.Html;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;

import it.polimi.dima.mediatracker.model.Book;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * A book representation in JSON, used to retrieve data from the external service responses
 */
public class BookJSON extends MediaItemJSON
{
    @SerializedName("id")
    @Expose
    private String apiId;

    @SerializedName("volumeInfo")
    @Expose
    private VolumeInfoJSON volInfo;

    private class VolumeInfoJSON
    {
        @SerializedName("publishedDate")
        @Expose
        private String releaseDate;

        @SerializedName("categories")
        @Expose
        private String[] genres;

        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("description")
        @Expose
        private String description;

        @SerializedName("authors")
        @Expose
        private String[] authors;

        @SerializedName("pageCount")
        @Expose
        private int pageCount;

        @SerializedName("imageLinks")
        @Expose
        private ImageLinksJSON imageLinks;

        private class ImageLinksJSON
        {
            @SerializedName("medium")
            @Expose
            private String image;

            @SerializedName("thumbnail")
            @Expose
            private String imageAlternative;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Book convertToMediaItem()
    {
        Book book = new Book();

        if(volInfo == null) return book;

        book.setExternalServiceId(apiId);

        if(volInfo.releaseDate!=null && !volInfo.releaseDate.equals(""))
        {
            int length = volInfo.releaseDate.length();
            String format;
            if(length==4) format = "yyyy";
            else if(length==7) format = "yyyy-MM";
            else format = "yyyy-MM-dd";
            book.setReleaseDate(Utils.parseDateFromString(volInfo.releaseDate, format));
        }

        book.setGenres(Utils.joinIfNotEmpty(", ", volInfo.genres));
        book.setTitle(volInfo.title);
        if(volInfo.description!=null) book.setDescription(Html.fromHtml(volInfo.description).toString());

        book.setAuthor(Utils.joinIfNotEmpty(", ", volInfo.authors));
        book.setPagesNumber(volInfo.pageCount);

        String image = volInfo.imageLinks.image!=null ? volInfo.imageLinks.image : volInfo.imageLinks.imageAlternative;
        if(!Utils.isEmpty(image))
        {
            try
            {
                book.setImageUrl(new URL(image));
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }

        return book;
    }
}
