package it.polimi.dima.mediatracker.inputs;


import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Form input for a {@link Switch}
 * @param <T> the model object
 */
public class SwitchInput<T> extends AbstractInput<T>
{
    private Callback<T> callback;

    private Switch switchView;

    /**
     * Constructor
     * @param isUpdatedByExternalService true if this input is updated by an external service
     * @param view the view containing the Switch
     * @param inputId the Switch ID
     * @param callback the callback for this input
     */
    public SwitchInput(boolean isUpdatedByExternalService, View view, int inputId, final Callback<T> callback)
    {
        super(isUpdatedByExternalService, inputId);

        this.callback = callback;

        // Setup switch
        switchView = (Switch) view.findViewById(inputId);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                updateDependentInputsVisibility(isChecked);

                if(!areValueChangeListenersDisabled())
                {
                    setWasChangedByUser(true);
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
        switchView.setChecked(callback.getModelObjectValue(modelObject));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModelObjectFromInput(T modelObject)
    {
        callback.setModelObjectValue(modelObject, switchView.isChecked());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSpecificInstance(Bundle outState)
    {
        // NOTE: by default Android automatically saves and restores Switch state,
        // but here it was disabled (android:saveEnabled=false in the view) to avoid
        // problems with the change listener defined above

        outState.putBoolean(SPECIFIC_INSTANCE_NAME, switchView.isChecked());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreSpecificInstance(Bundle inState)
    {
        if(inState.containsKey(SPECIFIC_INSTANCE_NAME))
        {
            switchView.setChecked(inState.getBoolean(SPECIFIC_INSTANCE_NAME));
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
        switchView.setVisibility(visibility);
    }

    /**
     * The callback for this input, used to get/set values from/to the corresponding model object field
     * @param <T> the model object
     */
    public interface Callback<T>
    {
        /**
         * Allows to set the given boolean to the given model object
         * @param modelObject the model object to be updated
         * @param checked the current checked status
         */
        void setModelObjectValue(T modelObject, boolean checked);

        /**
         * Allows to return the boolean saved in the given model object
         * @param modelObject the model object to be queried
         * @return the boolean saved in the model object
         */
        boolean getModelObjectValue(T modelObject);
    }
}
