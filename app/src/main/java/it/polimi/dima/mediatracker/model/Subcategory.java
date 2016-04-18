package it.polimi.dima.mediatracker.model;

import android.content.Context;

import java.io.Serializable;

import it.polimi.dima.mediatracker.R;

/**
 * A subcategory for a given category (used for example in the navigation drawer to display sub-elements)
 */
public enum Subcategory implements Serializable
{
    COMPLETED(R.string.subcategory_completed, R.drawable.ic_completed),
    SUGGESTION(R.string.subcategory_suggestions, R.drawable.ic_suggestions);

    private final int name;
    private final int icon;

    /**
     * Constructor
     * @param name the subcategory name resource ID
     * @param icon the subcategory icon resource ID
     */
    Subcategory(int name, int icon)
    {
        this.name = name;
        this.icon = icon;
    }

    /**
     * Getter
     * @return the subcategory icon resource ID
     */
    public int getIcon()
    {
        return icon;
    }

    /**
     * Getter
     * @param context the context
     * @return the subcategory name resource ID
     */
    public String getName(Context context)
    {
        return context.getString(this.name);
    }
}
