package it.polimi.dima.mediatracker.inputs;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

/**
 * Form input for a {@link EditText}
 * @param <T> the model object
 */
public class EditTextInput<T> extends AbstractInput<T>
{
    private Callback<T> callback;

    private EditText editText;

    /**
     * Constructor
     * @param isUpdatedByExternalService true if this input is updated by an external service
     * @param view the view containing the EditText
     * @param inputId the EditText ID
     * @param callback the callback for this input
     */
    public EditTextInput(boolean isUpdatedByExternalService, View view, int inputId, final Callback<T> callback)
    {
        super(isUpdatedByExternalService, inputId);

        this.callback = callback;

        // Setup EditText
        editText = (EditText) view.findViewById(inputId);
        if(callback instanceof CallbackExtended) ((CallbackExtended) callback).setExtraAttributes(view, editText);

        // Add listener
        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                updateDependentInputsVisibility(s.length()>0);

                if(!areValueChangeListenersDisabled())
                {
                    setWasChangedByUser(true);

                    if(callback instanceof CallbackExtended)
                    {
                        ((CallbackExtended) callback).onValueChange(s.toString());
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputFromModelObject(T modelObject)
    {
        if(AutoCompleteTextView.class.isAssignableFrom(editText.getClass()))
        {
            ((AutoCompleteTextView) editText).setText(callback.getModelObjectValue(modelObject), false);
        }
        else
        {
            editText.setText(callback.getModelObjectValue(modelObject));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModelObjectFromInput(T modelObject)
    {
        callback.setModelObjectValue(modelObject, getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSpecificInstance(Bundle outState)
    {
        // NOTE: by default Android automatically saves and restores EditText state,
        // but here it was disabled (android:saveEnabled=false in the view) to avoid
        // problems with the change listener defined above

        outState.putString(SPECIFIC_INSTANCE_NAME, editText.getText().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreSpecificInstance(Bundle inState)
    {
        if(inState.containsKey(SPECIFIC_INSTANCE_NAME))
        {
            editText.setText(inState.getString(SPECIFIC_INSTANCE_NAME));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismiss()
    {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisibility(int visibility)
    {
        editText.setVisibility(visibility);
    }

    /**
     * Getter
     * @return the current input text
     */
    public String getText()
    {
        return editText.getText().toString();
    }

    /**
     * The callback for this input, used to get/set values from/to the corresponding model object field
     * @param <T> the model object
     */
    public interface Callback<T>
    {
        /**
         * Allows to set the given string to the given model object
         * @param modelObject the model object to be updated
         * @param text the text currently written
         */
        void setModelObjectValue(T modelObject, String text);

        /**
         * Allows to return the text saved in the given model object
         * @param modelObject the model object to be queried
         * @return the text saved in the model object
         */
        String getModelObjectValue(T modelObject);
    }

    /**
     * Extended callback that has additional methods to manage the input
     * @param <T> the model object
     */
    public interface CallbackExtended<T> extends Callback<T>
    {
        /**
         * Allows to edit the EditText linked with this input, just after its setup
         * @param view the view that contains the EditText
         * @param editText the EditText linked with this input
         */
        void setExtraAttributes(View view, EditText editText);

        /**
         * Called when the user types a new text
         * NOTE: called only if {@link AbstractInput#areValueChangeListenersDisabled} is false
         * @param text the new text
         */
        void onValueChange(String text);
    }
}
