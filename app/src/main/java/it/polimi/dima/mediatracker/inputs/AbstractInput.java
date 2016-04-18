package it.polimi.dima.mediatracker.inputs;


import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an abstract form input. Automatically manages creation, listeners, etc. on the input
 * and, thanks to the callback passed by the caller (see subclasses constructor), the translation
 * between the input value and the model object (e.g. media item, category) fields and vice versa
 * @param <T> the model object
 */
public abstract class AbstractInput<T>
{
    private final static String SAVED_INSTANCE_PREFIX = "SAVED_INSTANCE_PREFIX-";
    private final static String WAS_CHANGED_BY_USER_INSTANCE_PREFIX = "WAS_CHANGED_BY_USER_INSTANCE_PREFIX-";

    final String SPECIFIC_INSTANCE_NAME;
    private final String WAS_CHANGED_BY_USER_INSTANCE_NAME;

    private boolean isUpdatedByExternalService;
    private boolean areValueChangeListenersDisabled = false;
    private boolean wasChangedByUser = false;

    private List<AbstractInput> dependentInputs = new ArrayList<>();

    /**
     * Constructor
     * @param isUpdatedByExternalService true if this input is updated by an external service
     * @param resourceId the view ID, used to create a unique ID for the input
     */
    AbstractInput(boolean isUpdatedByExternalService, int resourceId)
    {
        SPECIFIC_INSTANCE_NAME = SAVED_INSTANCE_PREFIX+resourceId;
        WAS_CHANGED_BY_USER_INSTANCE_NAME = WAS_CHANGED_BY_USER_INSTANCE_PREFIX+resourceId;
        this.isUpdatedByExternalService = isUpdatedByExternalService;
    }

    /**
     * Sets the input value equal to the corresponding model object value, calling the callback passed to the input constructor
     * @param modelObject the model object that will be queried for the input value
     */
    public abstract void setInputFromModelObject(T modelObject);

    /**
     * Sets the model object value equal to the corresponding input value, calling the callback passed to the input constructor
     * @param modelObject the model object that will be updated with the input value
     */
    public abstract void setModelObjectFromInput(T modelObject);

    /**
     * Saves this input instance in the given bundle, in order to be able to restore it later
     * @param outState the bundle where the state will be saved
     */
    public void saveInstance(Bundle outState)
    {
        // Save changed flag
        outState.putBoolean(WAS_CHANGED_BY_USER_INSTANCE_NAME, wasChangedByUser);

        // Save subclass instance
        saveSpecificInstance(outState);
    }

    /**
     * Called by {@link AbstractInput#saveInstance(Bundle)} for saving the implementation instance
     * @param outState the bundle where the state will be saved
     */
    protected abstract void saveSpecificInstance(Bundle outState);

    /**
     * Restores this input previously saved instance from the given bundle
     * @param inState the bundle containing the saved instance of this input
     */
    public void restoreInstance(Bundle inState)
    {
        // Restore subclass instance
        restoreSpecificInstance(inState);

        // Restore changed flag
        if(inState.containsKey(WAS_CHANGED_BY_USER_INSTANCE_NAME))
        {
            wasChangedByUser = inState.getBoolean(WAS_CHANGED_BY_USER_INSTANCE_NAME);
        }
    }

    /**
     * Called by {@link AbstractInput#restoreInstance(Bundle)} for restoring the implementation instance
     * @param inState the bundle containing the saved instance of this input
     */
    protected abstract void restoreSpecificInstance(Bundle inState);

    /**
     * Getter
     * @return true if this input can be updated by an external service
     */
    public boolean isUpdatedByExternalService()
    {
        return isUpdatedByExternalService;
    }

    /**
     * Method used to "close" the input, useful e.g. on orientation change to avoid problems
     */
    public abstract void dismiss();

    /**
     * @see android.view.View#setVisibility(int)
     */
    public abstract void setVisibility(int visibility);

    /**
     * Getter
     * @return true if value change listeners should be disabled, i.e. no value change callbacks are to be called, "wasChangedByUser" is updated, etc.
     */
    public boolean areValueChangeListenersDisabled()
    {
        return areValueChangeListenersDisabled;
    }

    /**
     * Setter
     * @param areValueChangeCallbacksDisabled true if value change listeners should be disabled, i.e. no value change callbacks are to be called, "wasChangedByUser" is updated, etc.
     */
    public void setAreValueChangeListenersDisabled(boolean areValueChangeCallbacksDisabled)
    {
        this.areValueChangeListenersDisabled = areValueChangeCallbacksDisabled;
    }

    /**
     * Getter
     * @return true if the input was changed by the user in this session
     */
    public boolean wasChangedByUser()
    {
        return wasChangedByUser;
    }

    /**
     * Setter
     * @param wasChangedByUser true if the input was changed by the user in this session
     */
    public void setWasChangedByUser(boolean wasChangedByUser)
    {
        this.wasChangedByUser = wasChangedByUser;
    }

    /**
     * Adds an input to the dependent inputs list. These inputs will be visible only if this one is set (e.g. for a SwitchInput only if the switch is checked)
     * @param input the dependent input to add
     */
    public void addDependentInput(AbstractInput input)
    {
        dependentInputs.add(input);
    }

    /**
     * Called by subclasses to update the visibility of dependent inputs (if any)
     * @param isThisInputSet true if this input is set (e.g. for a SwitchInput if it's checked), i.e. true if the dependent inputs should be visible
     */
    void updateDependentInputsVisibility(boolean isThisInputSet)
    {
        for(AbstractInput input: dependentInputs)
        {
            input.setVisibility(isThisInputSet ? View.VISIBLE : View.GONE);
        }
    }
}
