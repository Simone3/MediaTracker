package it.polimi.dima.mediatracker.model;

import android.content.Context;
import android.support.annotation.StringRes;

import it.polimi.dima.mediatracker.R;

/**
 * Importance levels for media items, set by the user
 */
public enum ImportanceLevel
{
    NONE(100, R.string.importance_level_none),
    LOW(200, R.string.importance_level_low),
    MEDIUM(300, R.string.importance_level_medium),
    HIGH(400, R.string.importance_level_high);

    private int dbValue;
    private int name;

    /**
     * Constructor
     * @param dbValue the importance level "weight" (will be saved in the database and used for ordering)
     * @param name the importance level name resource ID
     */
    ImportanceLevel(int dbValue, @StringRes int name)
    {
        this.dbValue = dbValue;
        this.name = name;
    }

    /**
     * Getter
     * @return the importance level "weight" (will be saved in the database and used for ordering)
     */
    public int getDbValue()
    {
        return dbValue;
    }

    /**
     * Getter
     * @param context the context
     * @return the importance level name
     */
    public String getName(Context context)
    {
        return context.getString(name);
    }

    /**
     * Getter
     * @return the importance level name resource ID
     */
    public @StringRes int getNameResource()
    {
        return name;
    }
}
