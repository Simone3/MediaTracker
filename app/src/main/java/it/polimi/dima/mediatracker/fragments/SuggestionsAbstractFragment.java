package it.polimi.dima.mediatracker.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.controllers.ScreenController;
import it.polimi.dima.mediatracker.inputs.AbstractInput;
import it.polimi.dima.mediatracker.inputs.EditTextInput;
import it.polimi.dima.mediatracker.inputs.SelectDialogInput;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Manages the suggestions page for a generic media item
 */
public abstract class SuggestionsAbstractFragment extends ContentAbstractFragment
{
    private final static String CATEGORY_ID_PARAMETER = "CATEGORY_ID_PARAMETER";

    private Category category;
    private MediaItemsAbstractController controller;

    private List<AbstractInput<Void>> inputs;
    private SelectDialogInput<Void> status;
    private EditTextInput<Void> genres;
    private SelectDialogInput<Void> duration;
    private SelectDialogInput<Void> owned;
    private SelectDialogInput<Void> completion;

    private MediaItem suggestedMediaItem;
    private LinearLayout resultArea;
    private LinearLayout noResultArea;
    private TextView resultTitle;
    private TextView resultSubtitle;
    private TextView resultDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_suggestions, container, false);
        Bundle args = getArguments();

        // Get category and media item controller
        Long categoryId = args.getLong(CATEGORY_ID_PARAMETER, -1);
        category = CategoriesController.getInstance().getCategoryById(categoryId);
        controller = category.getMediaType().getController();

        // Setup toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.action_bar_main);
        toolbar.setOnMenuItemClickListener(this);

        // Set activity title
        toolbar.setTitle(getString(R.string.title_activity_suggestions, category.getMediaType().getNameSingular(getActivity())));

        // Setup drawer
        if(onSetupDrawerListener!=null)
        {
            onSetupDrawerListener.onSetupDrawer(toolbar);
        }

        // Setup the page content (retrieving it from the subclass method)
        LinearLayout contentContainer = (LinearLayout) view.findViewById(R.id.suggestions_container);
        LinearLayout suggestionsContent = (LinearLayout) inflater.inflate(getContentLayout(), contentContainer, false);
        contentContainer.addView(suggestionsContent);

        // Initialize inputs
        inputs = new ArrayList<>();

        // Setup inputs
        setupInputs(view);

        // Set default values
        for(AbstractInput input: inputs)
        {
            input.setAreValueChangeListenersDisabled(true);
            input.setInputFromModelObject(null);
            input.setAreValueChangeListenersDisabled(false);
        }

        // Setup extra stuff
        setupSuggestionsButtonAndResultArea(view);
        updateOptionsVisibility();

        return view;
    }

    /**
     * Restores saved input states after a configuration change
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null)
        {
            // Restore inputs
            for(AbstractInput input: inputs)
            {
                input.setAreValueChangeListenersDisabled(true);
                input.restoreInstance(savedInstanceState);
                input.setAreValueChangeListenersDisabled(false);
            }

            // Show the right options based on the selected status
            updateOptionsVisibility();

            // Submit suggestion
            getAndDisplayRandomSuggestion();
        }
    }

    /**
     * Saves input states before a configuration change
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Save inputs
        for(AbstractInput input: inputs)
        {
            input.saveInstance(outState);
        }
    }

    /**
     * Closes all open inputs before destroying the fragment
     * {@inheritDoc}
     */
    @Override
    public void onPause()
    {
        super.onPause();

        // Close inputs (if necessary)
        for(AbstractInput input: inputs)
        {
            input.dismiss();
        }
    }

    /**
     * Helper to build the parameter bundle for the subclasses
     * @param categoryId the category ID
     * @return the bundle with the fragment parameters
     */
    static Bundle getSuggestionsFragmentParametersBundle(Long categoryId)
    {
        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_PARAMETER, categoryId);
        return args;
    }

    /**
     * Manages the suggestion constraints inputs
     * @param view the fragment inflated view
     */
    private void setupInputs(View view)
    {
        // Status
        final StatusOption[] statusOptions = StatusOption.values();
        String[] statusOptionNames = new String[statusOptions.length];
        for (int i = 0; i < statusOptions.length; i++) statusOptionNames[i] = statusOptions[i].getName(getActivity(), this);
        status = new SelectDialogInput<>(false, getFragmentManager(), view, R.id.suggestions_item_status, R.string.suggestions_status_dialog_title, statusOptions, statusOptionNames, null, new SelectDialogInput.CallbackExtended<Void>()
        {
            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                // Set media type icon
                editText.setCompoundDrawablesRelativeWithIntrinsicBounds(category.getMediaType().getFormIcon(), 0, 0, 0);
            }

            @Override
            public void onValueChange(Object selectedOption)
            {
                // Show the right options based on the selected status
                updateOptionsVisibility();

                // Submit suggestion
                getAndDisplayRandomSuggestion();
            }

            @Override
            public void setModelObjectValue(Void modelObject, Object selectedOption)
            {
                // Do nothing
            }

            @Override
            public Object getModelObjectValue(Void modelObject)
            {
                // Default value
                return statusOptions[0];
            }
        });
        inputs.add(status);

        // Genres
        genres = new EditTextInput<>(false, view, R.id.suggestions_genres, new EditTextInput.CallbackExtended<Void>()
        {
            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                // Do nothing
            }

            @Override
            public void onValueChange(String text)
            {
                // Submit suggestion
                getAndDisplayRandomSuggestion();
            }

            @Override
            public void setModelObjectValue(Void modelObject, String text)
            {
                // Do nothing
            }

            @Override
            public String getModelObjectValue(Void modelObject)
            {
                return "";
            }
        });
        inputs.add(genres);

        // Owned
        final OwnedOption[] ownedOptions = OwnedOption.values();
        String[] ownedOptionNames = new String[ownedOptions.length];
        for(int i = 0; i < ownedOptions.length; i++) ownedOptionNames[i] = ownedOptions[i].getName(getActivity());
        owned = new SelectDialogInput<>(false, getFragmentManager(), view, R.id.suggestions_owned, R.string.suggestions_owned_dialog_title, ownedOptions, ownedOptionNames, null, new SelectDialogInput.CallbackExtended<Void>()
        {
            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                // Do nothing
            }

            @Override
            public void onValueChange(Object selectedOption)
            {
                // Submit suggestion
                getAndDisplayRandomSuggestion();
            }

            @Override
            public void setModelObjectValue(Void modelObject, Object selectedOption)
            {
                // Do nothing
            }

            @Override
            public Object getModelObjectValue(Void modelObject)
            {
                // Default value
                return ownedOptions[0];
            }
        });
        inputs.add(owned);

        // Duration
        final DurationOptionInterface[] durationOptions = getAllDurationOptions();
        String[] durationOptionNames = new String[durationOptions.length];
        for(int i = 0; i < durationOptions.length; i++) durationOptionNames[i] = durationOptions[i].getName(getActivity());
        duration = new SelectDialogInput<>(false, getFragmentManager(), view, R.id.suggestions_duration, getDurationDialogTitle(), durationOptions, durationOptionNames, null, new SelectDialogInput.CallbackExtended<Void>()
        {
            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                // Do nothing
            }

            @Override
            public void onValueChange(Object selectedOption)
            {
                // Submit suggestion
                getAndDisplayRandomSuggestion();
            }

            @Override
            public void setModelObjectValue(Void modelObject, Object selectedOption)
            {
                // Do nothing
            }

            @Override
            public Object getModelObjectValue(Void modelObject)
            {
                // Default value
                return durationOptions[0];
            }
        });
        inputs.add(duration);

        // Completion
        final CompletionOption[] completionOptions = CompletionOption.values();
        String[] completionOptionNames = new String[completionOptions.length];
        for(int i = 0; i < completionOptions.length; i++) completionOptionNames[i] = completionOptions[i].getName(getActivity());
        completion = new SelectDialogInput<>(false, getFragmentManager(), view, R.id.suggestions_completion, R.string.suggestions_completed_dialog_title, completionOptions, completionOptionNames, null, new SelectDialogInput.CallbackExtended<Void>()
        {
            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                // Set label (getting it from the subclass implementation)
                editText.setHint(getCompletionLabel());
            }

            @Override
            public void onValueChange(Object selectedOption)
            {
                // Submit suggestion
                getAndDisplayRandomSuggestion();
            }

            @Override
            public void setModelObjectValue(Void modelObject, Object selectedOption)
            {
                // Do nothing
            }

            @Override
            public Object getModelObjectValue(Void modelObject)
            {
                // Default value
                return completionOptions[0];
            }
        });
        inputs.add(completion);
    }

    /**
     * Manages the result area (where the actual suggestion will be placed)
     * @param view the fragment inflated view
     */
    private void setupSuggestionsButtonAndResultArea(View view)
    {
        // Get all components
        LinearLayout resultInfo = (LinearLayout) view.findViewById(R.id.suggestions_result_info);
        resultArea = (LinearLayout) view.findViewById(R.id.suggestions_result_area);
        noResultArea = (LinearLayout) view.findViewById(R.id.suggestions_no_result_area);
        resultTitle = (TextView) view.findViewById(R.id.suggestions_result_title);
        resultSubtitle = (TextView) view.findViewById(R.id.suggestions_result_subtitle);
        resultDescription = (TextView) view.findViewById(R.id.suggestions_result_description);
        ImageButton confirmButton = (ImageButton) view.findViewById(R.id.suggestions_result_confirm);
        ImageButton reloadButton = (ImageButton) view.findViewById(R.id.suggestions_result_reload);

        // Click listener for reload button
        reloadButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Reload result
                getAndDisplayRandomSuggestion();
            }
        });

        // Click listener for confirm button
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // If we have a result...
                if(suggestedMediaItem!=null)
                {
                    // If it's completed, move it back to the tracked list
                    if(suggestedMediaItem.isCompleted())
                    {
                        controller.setMediaItemAsToRedo(suggestedMediaItem);
                    }

                    // Set it as "doing" now
                    controller.setMediaItemAsDoingNow(suggestedMediaItem, true);

                    // Redirect to the media items list page
                    Intent intent = ScreenController.getCategoryPageIntent(getActivity(), category, null);
                    startActivity(intent);
                }
            }
        });

        // Redirect to media item form on result area click
        resultInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mediaItemListener.onMediaItemSelected(suggestedMediaItem);
            }
        });

        // Submit first result
        getAndDisplayRandomSuggestion();
    }

    /**
     * Helper that sets the "owned" input visible only if status=NEW, the "completion" input only if status=COMPLETED
     */
    private void updateOptionsVisibility()
    {
        boolean isOwnedOptionVisible = StatusOption.NEW.equals(status.getSelectedOptionValue());
        owned.setVisibility(isOwnedOptionVisible ? View.VISIBLE : View.GONE);

        boolean isCompletionOptionVisible = StatusOption.COMPLETED.equals(status.getSelectedOptionValue());
        completion.setVisibility(isCompletionOptionVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Called to actually retrieve and print the suggested media item
     */
    private void getAndDisplayRandomSuggestion()
    {
        // Get input values
        StatusOption statusValue = (StatusOption) status.getSelectedOptionValue();
        String genreValue = genres.getText();
        OwnedOption ownedValue = (OwnedOption) owned.getSelectedOptionValue();
        DurationOptionInterface durationValue = (DurationOptionInterface) duration.getSelectedOptionValue();
        CompletionOption completionValue = (CompletionOption) completion.getSelectedOptionValue();

        // Get a suggestion from the controller
        if(statusValue==StatusOption.NEW)
        {
            Boolean ownedBooleanValue = OwnedOption.ANY.equals(ownedValue) ? null : OwnedOption.OWNED.equals(ownedValue);
            suggestedMediaItem = controller.getRandomTrackedMediaItem(category, (suggestedMediaItem == null ? null : suggestedMediaItem.getId()), genreValue, ownedBooleanValue, durationValue.getMinDuration(), durationValue.getMaxDuration());
        }
        else if(statusValue==StatusOption.COMPLETED)
        {
            suggestedMediaItem = controller.getRandomCompletedMediaItem(category, (suggestedMediaItem==null ? null : suggestedMediaItem.getId()), genreValue, completionValue.yearsAgo, durationValue.getMinDuration(), durationValue.getMaxDuration());
        }

        // If no result, show a message
        if(suggestedMediaItem == null)
        {
            resultArea.setVisibility(View.GONE);
            noResultArea.setVisibility(View.VISIBLE);
        }

        // Otherwise set the media item data in the result area
        else
        {
            resultArea.setVisibility(View.VISIBLE);
            noResultArea.setVisibility(View.GONE);

            // Set title
            resultTitle.setText(suggestedMediaItem.getTitle());

            // Set subtitle (creator and/or duration)
            String creator = suggestedMediaItem.getCreator();
            MediaItem.Duration mediaItemDuration = suggestedMediaItem.getDuration(getActivity());
            boolean isCreatorSet = !Utils.isEmpty(creator);
            boolean isDurationSet = mediaItemDuration!=null && mediaItemDuration.getValue()>0;
            if(isCreatorSet || isDurationSet)
            {
                if(isCreatorSet && isDurationSet)
                {
                    resultSubtitle.setText(getString(R.string.list_media_item_by_and_duration, suggestedMediaItem.getCreator(), mediaItemDuration.getValue() + mediaItemDuration.getMeasureUnitShort()));
                }
                else if(isCreatorSet)
                {
                    resultSubtitle.setText(suggestedMediaItem.getCreator());
                }
                else
                {
                    resultSubtitle.setText(getString(R.string.duration_value_and_measure_unit_short, mediaItemDuration.getValue(), mediaItemDuration.getMeasureUnitShort()));
                }
                resultSubtitle.setVisibility(View.VISIBLE);
            }
            else
            {
                resultSubtitle.setVisibility(View.GONE);
            }

            // Set genres
            String mediaItemGenres = suggestedMediaItem.getGenres();
            if(!Utils.isEmpty(mediaItemGenres))
            {
                resultDescription.setText(mediaItemGenres);
                resultDescription.setVisibility(View.VISIBLE);
            }
            else
            {
                resultSubtitle.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Getter
     * @return the content layout resource id
     */
    private int getContentLayout()
    {
        return R.layout.content_suggestions;
    }

    /**
     * Getter
     * @return the duration (pages number, runtime, etc.) picker dialog title
     */
    protected abstract int getDurationDialogTitle();

    /**
     * Getter
     * @return the resource representing status=NEW (e.g. "A book I'm tracking")
     */
    protected abstract int getStatusNewName();

    /**
     * Getter
     * @return the resource representing status=COMPLETED (e.g. "A book I've already read")
     */
    protected abstract int getStatusCompletedName();

    /**
     * Getter
     * @return the resource representing the completion input label (e.g. "I've read")
     */
    protected abstract int getCompletionLabel();

    /**
     * Getter
     * @return all available duration options (e.g. Long, Medium, etc.)
     */
    protected abstract DurationOptionInterface[] getAllDurationOptions();

    /**
     * Available options for the media item status, i.e. new (= tracked) or completed
     */
    private enum StatusOption
    {
        NEW, COMPLETED;

        /**
         * Getter
         * @param context the activity
         * @return the name of the option to show to the user, calling {@link SuggestionsAbstractFragment#getStatusNewName()}
         * and {@link SuggestionsAbstractFragment#getStatusCompletedName()}
         */
        String getName(Context context, SuggestionsAbstractFragment suggestionsAbstractFragment)
        {
            switch(this)
            {
                case NEW:
                    return context.getString(suggestionsAbstractFragment.getStatusNewName());

                case COMPLETED:
                    return context.getString(suggestionsAbstractFragment.getStatusCompletedName());

                default:
                    return "";
            }
        }
    }

    /**
     * Available options for the media item owned status
     */
    private enum OwnedOption
    {
        OWNED, NOT_OWNED, ANY;

        /**
         * Getter
         * @param context the activity
         * @return the name of the option to show to the user
         */
        String getName(Context context)
        {
            switch(this)
            {
                case OWNED:
                    return context.getString(R.string.suggestions_owned_option_owned);

                case NOT_OWNED:
                    return context.getString(R.string.suggestions_owned_option_not_owned);

                case ANY:
                    return context.getString(R.string.suggestions_owned_option_any);

                default:
                    return "";
            }
        }
    }

    /**
     * Available options for the media item completion status
     */
    private enum CompletionOption
    {
        ONE_YEAR(1), TWO_YEARS(2), THREE_YEARS(3), ANY(0);

        int yearsAgo;

        CompletionOption(int yearsAgo)
        {
            this.yearsAgo = yearsAgo;
        }

        /**
         * Getter
         * @param context the activity
         * @return the name of the option to show to the user
         */
        String getName(Context context)
        {
            switch(this)
            {
                case ONE_YEAR:
                case TWO_YEARS:
                case THREE_YEARS:
                    return context.getResources().getQuantityString(R.plurals.suggestions_completion_option_x_year, yearsAgo, yearsAgo);

                case ANY:
                    return context.getString(R.string.suggestions_completion_option_any);

                default:
                    return "";
            }
        }
    }

    /**
     * Interface describing the media duration item options (needs to be implemented by subclasses
     * with an enum and then return all the values using {@link SuggestionsAbstractFragment#getAllDurationOptions()}
     */
    protected interface DurationOptionInterface
    {
        /**
         * Getter
         * @param context the activity
         * @return the name of the duration option
         */
        String getName(Context context);

        /**
         * Getter
         * @return minimum value of the duration interval
         */
        int getMinDuration();

        /**
         * Getter
         * @return maximum value of the duration interval
         */
        int getMaxDuration();
    }
}
