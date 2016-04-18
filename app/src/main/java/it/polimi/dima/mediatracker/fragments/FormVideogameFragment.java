package it.polimi.dima.mediatracker.fragments;

import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.inputs.AbstractInput;
import it.polimi.dima.mediatracker.inputs.EditTextInput;
import it.polimi.dima.mediatracker.layout.EditTextWithMeasureUnit;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.Videogame;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Form to add/edit a videogame
 */
public class FormVideogameFragment extends FormMediaItemAbstractFragment
{
    /**
     * Instance creation
     * @param categoryId category ID parameter
     * @param mediaItemId media item ID parameter (null if adding a new media item, the media item to edit otherwise)
     * @param isEmpty true if the form should be empty (= no fields are displayed, only the action bar)
     * @return the fragment instance
     */
    public static FormVideogameFragment newInstance(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        FormVideogameFragment fragment = new FormVideogameFragment();
        fragment.setArguments(getMediaItemFragmentParametersBundle(categoryId, mediaItemId, isEmpty));
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getContentLayout()
    {
        return R.layout.content_form_videogames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<AbstractInput<MediaItem>> getSpecificInputs(View view, MediaItem initialMediaItem)
    {
        List<AbstractInput<MediaItem>> inputs = new ArrayList<>();

        // Publisher
        inputs.add(new EditTextInput<>(true, view, R.id.form_publisher_input, new EditTextInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((Videogame) mediaItem).setPublisher(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return ((Videogame) mediaItem).getPublisher();
            }
        }));

        // Developer
        inputs.add(new EditTextInput<>(true, view, R.id.form_creator_input, new EditTextInput.CallbackExtended<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((Videogame) mediaItem).setDeveloper(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return ((Videogame) mediaItem).getDeveloper();
            }

            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                editText.setHint(R.string.form_title_developer);
            }

            @Override
            public void onValueChange(String text)
            {
                // Do nothing here
            }
        }));

        // Platforms
        inputs.add(new EditTextInput<>(true, view, R.id.form_platforms_input, new EditTextInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((Videogame) mediaItem).setPlatforms(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return ((Videogame) mediaItem).getPlatforms();
            }
        }));

        // Average length
        inputs.add(new EditTextInput<>(false, view, R.id.form_duration_input, new EditTextInput.CallbackExtended<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((Videogame) mediaItem).setAverageLengthHours(Utils.parseInt(text));
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return String.valueOf(((Videogame) mediaItem).getAverageLengthHours());
            }

            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                EditTextWithMeasureUnit duration = (EditTextWithMeasureUnit) editText;

                duration.setMeasureUnit(Videogame.getDurationMeasureUnitName(getActivity()));

                duration.setHint(R.string.form_title_average_length);
            }

            @Override
            public void onValueChange(String text)
            {
                // Do nothing here
            }
        }));

        return inputs;
    }
}
