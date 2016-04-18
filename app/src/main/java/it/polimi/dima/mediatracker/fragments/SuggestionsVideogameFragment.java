package it.polimi.dima.mediatracker.fragments;


import android.content.Context;

import it.polimi.dima.mediatracker.R;

/**
 * Manages the suggestions page for movies
 */
public class SuggestionsVideogameFragment extends SuggestionsAbstractFragment
{
    /**
     * Instance creation
     * @param categoryId category ID parameter
     * @return the fragment instance
     */
    public static SuggestionsVideogameFragment newInstance(Long categoryId)
    {
        SuggestionsVideogameFragment fragment = new SuggestionsVideogameFragment();
        fragment.setArguments(getSuggestionsFragmentParametersBundle(categoryId));

        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getDurationDialogTitle()
    {
        return R.string.suggestions_videogame_duration_dialog_title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getStatusNewName()
    {
        return R.string.suggestions_videogame_status_option_new;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getStatusCompletedName()
    {
        return R.string.suggestions_videogame_status_option_completed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getCompletionLabel()
    {
        return R.string.suggestions_videogame_completion_label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DurationOptionInterface[] getAllDurationOptions()
    {
        return VideogameDurationOption.values();
    }

    /**
     * {@inheritDoc}
     */
    private enum VideogameDurationOption implements DurationOptionInterface
    {
        ANY(-1, -1), SHORT(-1, 5), MEDIUM(5, 30), LONG(30, 100), VERY_LONG(100, -1);

        private int minDuration;
        private int maxDuration;

        VideogameDurationOption(int minDuration, int maxDuration)
        {
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName(Context context)
        {
            switch(this)
            {
                case VERY_LONG:
                    return context.getString(R.string.suggestions_duration_option_very_long, getDurationDetails(context));

                case LONG:
                    return context.getString(R.string.suggestions_duration_option_long, getDurationDetails(context));

                case MEDIUM:
                    return context.getString(R.string.suggestions_duration_option_medium, getDurationDetails(context));

                case SHORT:
                    return context.getString(R.string.suggestions_duration_option_short, getDurationDetails(context));

                case ANY:
                    return context.getString(R.string.suggestions_duration_option_any);

                default:
                    return "";
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getMinDuration()
        {
            return minDuration;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getMaxDuration()
        {
            return maxDuration;
        }

        /**
         * Getter
         * @param context the activity
         * @return the string representing the current duration interval
         */
        private String getDurationDetails(Context context)
        {
            if(minDuration>0 && maxDuration>0) return context.getString(R.string.duration_hours_between, minDuration, maxDuration);
            else if(minDuration>0) return context.getString(R.string.duration_hours_more_then, minDuration);
            else if(maxDuration>0) return context.getString(R.string.duration_hours_less_then, maxDuration);
            else return "";
        }
    }
}
