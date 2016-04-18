package it.polimi.dima.mediatracker.fragments;

import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.inputs.AbstractInput;
import it.polimi.dima.mediatracker.inputs.DatePickerInput;
import it.polimi.dima.mediatracker.inputs.SwitchInput;
import it.polimi.dima.mediatracker.inputs.EditTextInput;
import it.polimi.dima.mediatracker.layout.EditTextWithMeasureUnit;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.TVShow;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Form to add/edit a TV show
 */
public class FormTVShowFragment extends FormMediaItemAbstractFragment
{
    /**
     * Instance creation
     * @param categoryId category ID parameter
     * @param mediaItemId media item ID parameter (null if adding a new media item, the media item to edit otherwise)
     * @param isEmpty true if the form should be empty (= no fields are displayed, only the action bar)
     * @return the fragment instance
     */
    public static FormTVShowFragment newInstance(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        FormTVShowFragment fragment = new FormTVShowFragment();
        fragment.setArguments(getMediaItemFragmentParametersBundle(categoryId, mediaItemId, isEmpty));
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getContentLayout()
    {
        return R.layout.content_form_tv_shows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<AbstractInput<MediaItem>> getSpecificInputs(View view, MediaItem initialMediaItem)
    {
        List<AbstractInput<MediaItem>> inputs = new ArrayList<>();

        // Episode duration
        inputs.add(new EditTextInput<>(true, view, R.id.form_duration_input, new EditTextInput.CallbackExtended<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((TVShow) mediaItem).setEpisodeRuntimeMin(Utils.parseInt(text));
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return String.valueOf(((TVShow) mediaItem).getEpisodeRuntimeMin());
            }

            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                EditTextWithMeasureUnit duration = (EditTextWithMeasureUnit) editText;

                duration.setMeasureUnit(TVShow.getDurationMeasureUnitName(getActivity()));

                duration.setHint(R.string.form_title_episode_duration);
            }

            @Override
            public void onValueChange(String text)
            {
                // Do nothing here
            }
        }));

        // Created by
        inputs.add(new EditTextInput<>(true, view, R.id.form_creator_input, new EditTextInput.CallbackExtended<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((TVShow) mediaItem).setCreatedBy(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return ((TVShow) mediaItem).getCreatedBy();
            }

            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                editText.setHint(R.string.form_title_created_by);
            }

            @Override
            public void onValueChange(String text)
            {
                // Do nothing here
            }
        }));

        // Number of episodes
        inputs.add(new EditTextInput<>(true, view, R.id.form_episodes_number_input, new EditTextInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((TVShow) mediaItem).setEpisodesNumber(Utils.parseInt(text));
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return String.valueOf(((TVShow) mediaItem).getEpisodesNumber());
            }
        }));

        // Number of seasons
        inputs.add(new EditTextInput<>(true, view, R.id.form_seasons_number_input, new EditTextInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((TVShow) mediaItem).setSeasonsNumber(Utils.parseInt(text));
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return String.valueOf(((TVShow) mediaItem).getSeasonsNumber());
            }
        }));

        // Next episode
        DatePickerInput<MediaItem> nextEpisodeInput = new DatePickerInput<>(true, getActivity(), view, R.id.form_next_episode_button, new DatePickerInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, Date date)
            {
                ((TVShow) mediaItem).setNextEpisodeAirDate(date);
            }

            @Override
            public Date getModelObjectValue(MediaItem mediaItem)
            {
                return ((TVShow) mediaItem).getNextEpisodeAirDate();
            }
        });
        inputs.add(nextEpisodeInput);

        // In production
        SwitchInput<MediaItem> inProductionInput = new SwitchInput<>(true, view, R.id.form_in_production_input, new SwitchInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, boolean checked)
            {
                ((TVShow) mediaItem).setInProduction(checked);
            }

            @Override
            public boolean getModelObjectValue(MediaItem mediaItem)
            {
                return ((TVShow) mediaItem).isInProduction();
            }
        });
        inProductionInput.addDependentInput(nextEpisodeInput);
        inputs.add(inProductionInput);

        return inputs;
    }
}
