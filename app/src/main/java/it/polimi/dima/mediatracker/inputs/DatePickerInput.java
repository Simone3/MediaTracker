package it.polimi.dima.mediatracker.inputs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Form input for a {@link DatePickerDialog} created on a {@link EditText} click
 * @param <T> the model object
 */
public class DatePickerInput<T> extends AbstractInput<T>
{
    private DatePickerDialog datePickerDialog;
    private EditText button;

    private boolean isUnknown = true;
    private Calendar calendar;
    private DateFormat format = SimpleDateFormat.getDateInstance();

    private Callback<T> callback;

    /**
     * Constructor
     * @param isUpdatedByExternalService true if this input is updated by an external service
     * @param activity the activity
     * @param view the view containing the button that opens the DatePickerDialog
     * @param inputButtonId the ID of the button that opens the DatePickerDialog
     * @param callback the callback for this input
     */
    public DatePickerInput(boolean isUpdatedByExternalService, Activity activity, View view, int inputButtonId, Callback<T> callback)
    {
        super(isUpdatedByExternalService, inputButtonId);

        this.callback = callback;
        calendar = Calendar.getInstance();

        // Create dialog
        datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                calendar.set(year, monthOfYear, dayOfMonth);
                isUnknown = false;
                setButtonText();

                updateDependentInputsVisibility(true);

                if(!areValueChangeListenersDisabled())
                {
                    setWasChangedByUser(true);
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Show dialog on button click
        button = (EditText) view.findViewById(inputButtonId);
        setButtonText();
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                datePickerDialog.show();
            }
        });
    }

    /**
     * Helper to set the button text equal to the selected date
     */
    private void setButtonText()
    {
        if(isUnknown) button.setText("");
        else button.setText(format.format(calendar.getTime()));
    }

    /**
     * Helper to update the selected date
     * @param date the new date
     */
    private void setValue(Date date)
    {
        if(date==null)
        {
            isUnknown = true;
            setButtonText();
        }
        else
        {
            isUnknown = false;
            calendar.setTime(date);
            datePickerDialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            setButtonText();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputFromModelObject(T modelObject)
    {
        Date date = callback.getModelObjectValue(modelObject);
        setValue(date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModelObjectFromInput(T modelObject)
    {
        callback.setModelObjectValue(modelObject, isUnknown ? null : calendar.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSpecificInstance(Bundle outState)
    {
        outState.putSerializable(SPECIFIC_INSTANCE_NAME, calendar.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreSpecificInstance(Bundle inState)
    {
        if(inState.containsKey(SPECIFIC_INSTANCE_NAME))
        {
            Date value = (Date) inState.getSerializable(SPECIFIC_INSTANCE_NAME);
            setValue(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismiss()
    {
        if(datePickerDialog!=null && datePickerDialog.isShowing())
        {
            datePickerDialog.dismiss();
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
         * Allows to set the given date to the given model object
         * @param modelObject the model object to be updated
         * @param date the date currently selected
         */
        void setModelObjectValue(T modelObject, Date date);

        /**
         * Allows to return the date saved in the given model object
         * @param modelObject the model object to be queried
         * @return the date saved in the model object
         */
        Date getModelObjectValue(T modelObject);
    }
}
