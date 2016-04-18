package it.polimi.dima.mediatracker.model;


import android.content.Context;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.Serializable;

/**
 * A category
 */
public class Category extends SugarRecord implements Serializable
{
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_MEDIA_TYPE_NAME = "MEDIA_TYPE_NAME";
    public static final String COLUMN_COLOR_RESOURCE_NAME = "COLOR_RESOURCE_NAME";

    private String name;
    @Ignore
    private int color;
    private String colorResourceName;
    @Ignore
    private MediaType mediaType;
    private String mediaTypeName;

    /**
     * Constructor
     */
    public Category()
    {
        // Empty
    }

    /**
     * Constructor
     * @param context the context
     * @param name the category name
     * @param color the category color RESOURCE ID
     * @param mediaType the category media type
     */
    public Category(Context context, String name, int color, MediaType mediaType)
    {
        this.name = name;
        this.color = color;
        this.colorResourceName = context.getResources().getResourceEntryName(color);
        this.mediaType = mediaType;
        this.mediaTypeName = mediaType.name();
    }

    /**
     * Constructor
     * @param colorResourceName the name of the color resource
     * @param name the category name
     * @param color the category color RESOURCE ID
     * @param mediaType the category media type
     */
    public Category(String colorResourceName, String name, int color, MediaType mediaType)
    {
        this.name = name;
        this.color = color;
        this.colorResourceName = colorResourceName;
        this.mediaType = mediaType;
        this.mediaTypeName = mediaType.name();
    }

    /************************************************ GETTERS ************************************************/


    /**
     * Getter
     * @return the category name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Getter
     * @param context the context
     * @return the category color RESOURCE ID
     */
    public int getColor(Context context)
    {
        if(color==0 && colorResourceName!=null) color = context.getResources().getIdentifier(colorResourceName, "color", context.getPackageName());
        return color;
    }

    /**
     * Getter
     * @return the category media type
     */
    public MediaType getMediaType()
    {
        if(mediaType==null && mediaTypeName!=null) mediaType = MediaType.valueOf(mediaTypeName);
        return mediaType;
    }



    /************************************************ SETTERS ************************************************/


    /**
     * Setter
     * @param name the category name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Setter
     * @param context the context
     * @param color the category color RESOURCE ID
     */
    public void setColor(Context context, int color)
    {
        this.color = color;
        this.colorResourceName = context.getResources().getResourceEntryName(color);
    }

    /**
     * Setter
     * @param mediaType the category media type
     */
    public void setMediaType(MediaType mediaType)
    {
        this.mediaType = mediaType;
        this.mediaTypeName = mediaType.name();
    }



    /************************************************ MISC ************************************************/


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object)
    {
        return !(object == null || !(object instanceof Category)) && getId().equals(((Category) object).getId());
    }
}
