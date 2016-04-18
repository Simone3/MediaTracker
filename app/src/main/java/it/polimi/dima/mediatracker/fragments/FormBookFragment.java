package it.polimi.dima.mediatracker.fragments;


import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.inputs.AbstractInput;
import it.polimi.dima.mediatracker.inputs.EditTextInput;
import it.polimi.dima.mediatracker.layout.EditTextWithMeasureUnit;
import it.polimi.dima.mediatracker.model.Book;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Form to add/edit a book
 */
public class FormBookFragment extends FormMediaItemAbstractFragment
{
    /**
     * Instance creation
     * @param categoryId category ID parameter
     * @param mediaItemId media item ID parameter (null if adding a new media item, the media item to edit otherwise)
     * @param isEmpty true if the form should be empty (= no fields are displayed, only the action bar)
     * @return the fragment instance
     */
    public static FormBookFragment newInstance(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        FormBookFragment fragment = new FormBookFragment();
        fragment.setArguments(getMediaItemFragmentParametersBundle(categoryId, mediaItemId, isEmpty));
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getContentLayout()
    {
        return R.layout.content_form_books;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<AbstractInput<MediaItem>> getSpecificInputs(View view, MediaItem initialMediaItem)
    {
        List<AbstractInput<MediaItem>> inputs = new ArrayList<>();

        // Number of pages
        inputs.add(new EditTextInput<>(true, view, R.id.form_duration_input, new EditTextInput.CallbackExtended<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((Book) mediaItem).setPagesNumber(Utils.parseInt(text));
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return String.valueOf(((Book) mediaItem).getPagesNumber());
            }

            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                EditTextWithMeasureUnit duration = (EditTextWithMeasureUnit) editText;

                duration.setMeasureUnit(Book.getDurationMeasureUnitName(getActivity()));

                duration.setHint(R.string.form_title_pages_number);
            }

            @Override
            public void onValueChange(String text)
            {
                // Do nothing here
            }
        }));

        // Author
        inputs.add(new EditTextInput<>(true, view, R.id.form_creator_input, new EditTextInput.CallbackExtended<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                ((Book) mediaItem).setAuthor(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return ((Book) mediaItem).getAuthor();
            }

            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                editText.setHint(R.string.form_title_author);
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
