package it.polimi.dima.mediatracker.inputs;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;

import java.util.Arrays;

import it.polimi.dima.mediatracker.dialogs.SelectDialog;

/**
 * Form input for a {@link SelectDialog} created on a {@link EditText} click
 * @param <T> the model object
 */
public class SelectDialogInput<T> extends AbstractInput<T>
{
    private EditText button;
    private SelectDialog selectDialog;
    private Object[] optionValues;
    private String[] optionNames;
    private int[] optionIcons;

    private Callback<T> callback;

    /**
     * Constructor
     * @param isUpdatedByExternalService true if this input is updated by an external service
     * @param fragmentManager the fragment manager
     * @param view the view containing the button that opens the SelectDialog
     * @param inputButtonId the ID of the button that opens the SelectDialog
     * @param dialogTitleResource the resource ID of the SelectDialog title
     * @param optionValues the option values
     * @param optionNames the option names
     * @param optionIcons the option icons (can be null if no icons are to be set)
     * @param callback the callback for this input
     */
    public SelectDialogInput(boolean isUpdatedByExternalService, final FragmentManager fragmentManager, View view, final int inputButtonId, int dialogTitleResource, final Object[] optionValues, String[] optionNames, int[] optionIcons, final Callback<T> callback)
    {
        super(isUpdatedByExternalService, inputButtonId);

        this.optionValues = optionValues;
        this.optionNames = optionNames;
        this.optionIcons = optionIcons;
        this.callback = callback;

        // Create dialog
        selectDialog = SelectDialog.newInstance(dialogTitleResource, optionNames, 0);
        selectDialog.setSelectDialogListener(new SelectDialog.SelectDialogListener()
        {
            @Override
            public void onOptionSelected(int position)
            {
                setButtonText(position);

                updateDependentInputsVisibility(true);

                if(!areValueChangeListenersDisabled())
                {
                    setWasChangedByUser(true);

                    if(callback instanceof CallbackExtended)
                    {
                        ((CallbackExtended) callback).onValueChange(optionValues[position]);
                    }
                }
            }
        });

        // Open dialog on button click
        button = (EditText) view.findViewById(inputButtonId);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if(fragmentManager.findFragmentByTag("SelectDialog"+inputButtonId)==null)
                {
                    selectDialog.show(fragmentManager, "SelectDialog"+inputButtonId);
                }
            }
        });
        if(callback instanceof CallbackExtended) ((CallbackExtended) callback).setExtraAttributes(view, button);

        // Default selected value
        setValue(0);
    }

    /**
     * Helper to set the button text equal to the option name at the given position
     * @param position the selected position
     */
    private void setButtonText(int position)
    {
        button.setText(optionNames[position]);
        if(optionIcons!=null) button.setCompoundDrawablesRelativeWithIntrinsicBounds(optionIcons[position], 0, 0, 0);
    }

    /**
     * Helper to update the selected position
     * @param position the selected position
     */
    private void setValue(int position)
    {
        if(position>=0)
        {
            selectDialog.setSelectedOptionIndex(position);
            setButtonText(position);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputFromModelObject(T modelObject)
    {
        Object value = callback.getModelObjectValue(modelObject);
        int position = Arrays.asList(optionValues).indexOf(value);
        setValue(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModelObjectFromInput(T modelObject)
    {
        callback.setModelObjectValue(modelObject, getSelectedOptionValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSpecificInstance(Bundle outState)
    {
        outState.putInt(SPECIFIC_INSTANCE_NAME, selectDialog.getSelectedOptionIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreSpecificInstance(Bundle inState)
    {
        if(inState.containsKey(SPECIFIC_INSTANCE_NAME))
        {
            int position = inState.getInt(SPECIFIC_INSTANCE_NAME, 0);
            setValue(position);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismiss()
    {
        if(selectDialog!=null && selectDialog.isShowing())
        {
            selectDialog.dismiss();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisibility(int visibility)
    {
        button.setVisibility(visibility);
    }

    /**
     * Getter
     * @return the currently selected value
     */
    public Object getSelectedOptionValue()
    {
        return optionValues[selectDialog.getSelectedOptionIndex()];
    }

    /**
     * The callback for this input, used to get/set values from/to the corresponding model object field
     * @param <T> the model object
     */
    public interface Callback<T>
    {
        /**
         * Allows to set the given option to the given model object
         * @param modelObject the model object to be updated
         * @param selectedOption the option currently selected
         */
        void setModelObjectValue(T modelObject, Object selectedOption);

        /**
         * Allows to return the option saved in the given model object
         * @param modelObject the model object to be queried
         * @return the option saved in the model object
         */
        Object getModelObjectValue(T modelObject);
    }

    /**
     * Extended callback that has additional methods to manage the input
     * @param <T> the model object
     */
    public interface CallbackExtended<T> extends Callback<T>
    {
        /**
         * Allows to edit the EditText button linked with this input, just after its setup
         * @param view the view that contains the button
         * @param editText the button linked with this input
         */
        void setExtraAttributes(View view, EditText editText);

        /**
         * Called when the user selects a new option
         * NOTE: called only if {@link AbstractInput#areValueChangeListenersDisabled} is false
         * @param selectedOption the new option value
         */
        void onValueChange(Object selectedOption);
    }
}
