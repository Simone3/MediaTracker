package it.polimi.dima.mediatracker.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import it.polimi.dima.mediatracker.R;

/**
 * Abstract fragment that manages a form to add/edit "something" in the database
 */
public abstract class FormAbstractFragment extends Fragment implements Toolbar.OnMenuItemClickListener
{
    final static String IS_EMPTY_PARAMETER = "IS_EMPTY_PARAMETER";

    private OnObjectSaveListener onObjectSaveListener;

    private boolean isEditingForm;

    private Toolbar toolbar;

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_form, container, false);
        Bundle args = getArguments();

        // Check if empty (= show only the toolbar but no fields)
        if(args.getBoolean(IS_EMPTY_PARAMETER))
        {
            return view;
        }

        // Setup toolbar
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.action_bar_form);
        toolbar.setOnMenuItemClickListener(this);

        // Back button, if necessary
        if(!getActivity().getResources().getBoolean(R.bool.is_multi_pane_layout) || this instanceof FormCategoryFragment)
        {
            toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_arrow_back_white));
            toolbar.setNavigationOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    getActivity().onBackPressed();
                }
            });
        }

        // Set the form content getting it from the implementation's methods
        LinearLayout contentContainer = (LinearLayout) view.findViewById(R.id.form_container);
        LinearLayout formContent = (LinearLayout) inflater.inflate(getContentLayout(), contentContainer, false);
        contentContainer.addView(formContent);

        return view;
    }

    /**
     * Getter for subclasses
     * @return the fragment toolbar
     */
    Toolbar getToolbar()
    {
        return toolbar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        // Handle action bar item clicks here
        int id = item.getItemId();

        // If it's the save action...
        if(id==R.id.action_save)
        {
            // Set fields from inputs
            setObjectValuesFromInputs();

            // Validate object and show error if something went wrong
            String errorMessage = validateObject();
            if(errorMessage!=null && !errorMessage.isEmpty())
            {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                return false;
            }

            // If the object is ok, save it
            saveObject();

            // Reset flags on inputs
            resetInputUpdatedByUserFlags();

            // Callback
            if(onObjectSaveListener!=null)
            {
                onObjectSaveListener.onObjectSaved();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called whenever the user tries to exit from a form (e.g. back pressed). Checks if unsaved changed were made
     * @param formExitListener the listener receiving the exit result
     */
    public void manageExitFromForm(final FormExitListener formExitListener)
    {
        // If there are no unsaved changes, do nothing
        if(canExitForm())
        {
            formExitListener.onExit(true);
        }

        // Otherwise, show dialog
        else
        {
            new AlertDialog.Builder(getActivity())
                //.setTitle("Closing Activity")
                .setMessage(R.string.form_exit_error)
                .setPositiveButton(R.string.form_exit_error_keep, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Don't allow exit
                        formExitListener.onExit(false);
                    }
                })
                .setNegativeButton(R.string.form_exit_error_discard, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Allow exit (discard changes)
                        formExitListener.onExit(true);
                    }

                })

                .show();
        }
    }

    /**
     * Checks if the user can exit the form (no unsaved changes)
     * @return true if the user can exit the form
     */
    protected abstract boolean canExitForm();

    /**
     * Resets the "wasChangedByUser" flags in all the form inputs
     */
    protected abstract void resetInputUpdatedByUserFlags();

    /**
     * Returns the form content layout
     * @return content layout resource id
     */
    protected abstract int getContentLayout();

    /**
     * Retrieves all values from the inputs and sets them in the model object
     */
    protected abstract void setObjectValuesFromInputs();

    /**
     * Checks if the form object is correct. Called after {@link FormAbstractFragment#setObjectValuesFromInputs()} so that the model object already has all the fields set
     * @return null if the object is ok, a string containing the error message otherwise
     */
    protected abstract String validateObject();

    /**
     * Saves the model object somewhere. Called after {@link FormAbstractFragment#setObjectValuesFromInputs()} (the model object already has all the fields set) and only if {@link FormAbstractFragment#validateObject()} returns null
     */
    protected abstract void saveObject();

    /**
     * Getter
     * @return true if we are editing an object, false if we are adding a new one
     */
    boolean isEditingForm()
    {
        return isEditingForm;
    }

    /**
     * Setter
     * @param isEditingForm true if we are editing an object, false if we are adding a new one
     */
    void setIsEditingForm(boolean isEditingForm)
    {
        this.isEditingForm = isEditingForm;
    }

    /**
     * Setter
     * @param onObjectSaveListener the listener that receives the save events
     */
    public void setOnObjectSaveListener(OnObjectSaveListener onObjectSaveListener)
    {
        this.onObjectSaveListener = onObjectSaveListener;
    }

    /**
     * Listener that receives an event each time a media item is saved in the form
     */
    public interface OnObjectSaveListener
    {
        /**
         * Called each time a media item is saved in the form
         */
        void onObjectSaved();
    }

    /**
     * Listener for exiting from the form
     */
    public interface FormExitListener
    {
        /**
         * Called on exit
         * @param canExit true if the user can exit (no unsaved changes)
         */
        void onExit(boolean canExit);
    }
}
