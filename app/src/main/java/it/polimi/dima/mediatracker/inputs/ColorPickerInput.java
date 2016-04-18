package it.polimi.dima.mediatracker.inputs;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import it.polimi.dima.mediatracker.dialogs.ColorPickerDialog;

/**
 * Form input for a {@link ColorPickerDialog} created on a {@link ImageButton} click
 * @param <T> the model object
 */
public class ColorPickerInput<T> extends AbstractInput<T>
{
    private ImageButton button;
    private ColorPickerDialog colorPickerDialog;

    private Callback<T> callback;
    private Activity activity;

    /**
     * Constructor
     * @param isUpdatedByExternalService true if this input is updated by an external service
     * @param activity the activity
     * @param fragmentManager the fragment manager
     * @param view the view containing the button that opens the ColorPickerDialog
     * @param inputButtonId the ID of the button that opens the SelectDialog
     * @param dialogTitleResource the resource ID of the ColorPickerDialog title
     * @param columnsNumber the number of columns in the ColorPickerDialog
     * @param colors the options
     * @param callback the callback for this input
     */
    public ColorPickerInput(boolean isUpdatedByExternalService, Activity activity, final FragmentManager fragmentManager, View view, final int inputButtonId, int dialogTitleResource, int columnsNumber, int[] colors, Callback<T> callback)
    {
        super(isUpdatedByExternalService, inputButtonId);

        this.callback = callback;
        this.activity = activity;

        // Create dialog
        colorPickerDialog = ColorPickerDialog.newInstance(dialogTitleResource, colors, colors[0], columnsNumber);
        colorPickerDialog.setColorPickerListener(new ColorPickerDialog.ColorPickerListener()
        {
            @Override
            public void onColorSelected(int color)
            {
                setButtonColor(color);

                updateDependentInputsVisibility(true);

                if(!areValueChangeListenersDisabled())
                {
                    setWasChangedByUser(true);
                }

            }
        });

        // Open dialog on button click
        button = (ImageButton) view.findViewById(inputButtonId);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if(fragmentManager.findFragmentByTag("ColorPicker"+inputButtonId)==null)
                {
                    colorPickerDialog.show(fragmentManager, "ColorPicker"+inputButtonId);
                }
            }
        });

        // Default selected value
        setValue(colors[0]);
    }

    /**
     * Helper to set the button color equal to currently selected color
     * @param color the selected color resource ID
     */
    private void setButtonColor(int color)
    {
        GradientDrawable buttonBackground = (GradientDrawable) button.getBackground();
        buttonBackground.setColor(ContextCompat.getColor(activity, color));
    }

    /**
     * Helper to update the selected color
     * @param color the selected color
     */
    private void setValue(int color)
    {
        if(color!=0)
        {
            colorPickerDialog.setSelectedColor(color);
            setButtonColor(color);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputFromModelObject(T modelObject)
    {
        int color = callback.getModelObjectValue(modelObject);
        setValue(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModelObjectFromInput(T modelObject)
    {
        callback.setModelObjectValue(modelObject, colorPickerDialog.getSelectedColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSpecificInstance(Bundle outState)
    {
        outState.putInt(SPECIFIC_INSTANCE_NAME, colorPickerDialog.getSelectedColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreSpecificInstance(Bundle inState)
    {
        if(inState.containsKey(SPECIFIC_INSTANCE_NAME))
        {
            int color = inState.getInt(SPECIFIC_INSTANCE_NAME, 0);
            setValue(color);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismiss()
    {
        if(colorPickerDialog!=null && colorPickerDialog.isShowing())
        {
            colorPickerDialog.dismiss();
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
     * The callback for this input, used to get/set values from/to the corresponding model object field
     * @param <T> the model object
     */
    public interface Callback<T>
    {
        /**
         * Allows to set the given color to the given model object
         * @param modelObject the model object to be updated
         * @param color the color currently selected
         */
        void setModelObjectValue(T modelObject, int color);

        /**
         * Allows to return the color saved in the given model object
         * @param modelObject the model object to be queried
         * @return the color saved in the model object
         */
        int getModelObjectValue(T modelObject);
    }
}
