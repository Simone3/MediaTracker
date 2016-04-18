package it.polimi.dima.mediatracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * A simple dialog that allows to select one of the provided options
 */
public class SelectDialog extends DialogFragment
{
    private final static String TITLE_PARAMETER = "TITLE_PARAMETER";
    private final static String OPTIONS_PARAMETER = "OPTIONS_PARAMETER";
    private final static String SELECTED_OPTION_PARAMETER = "SELECTED_OPTION_PARAMETER";

    private int title;
    private String[] options;
    private int selectedOptionIndex = -1;

    private SelectDialogListener listener;
    
    /**
     * Allows to create a new instance of the dialog
     * @param title the dialog title
     * @param options the dialog options
     * @param selectedOptionIndex the first selected option
     * @return the dialog instance
     */
    public static SelectDialog newInstance(int title, String[] options, int selectedOptionIndex)
    {
        SelectDialog selectDialog = new SelectDialog();

        Bundle args = new Bundle();
        args.putInt(TITLE_PARAMETER, title);
        args.putStringArray(OPTIONS_PARAMETER, options);
        args.putInt(SELECTED_OPTION_PARAMETER, selectedOptionIndex);
        selectDialog.setArguments(args);

        return selectDialog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Parameters
        Bundle args = getArguments();
        title = args.getInt(TITLE_PARAMETER, 0);
        options = args.getStringArray(OPTIONS_PARAMETER);
        if(selectedOptionIndex<0) selectedOptionIndex = args.getInt(SELECTED_OPTION_PARAMETER, 0);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(title))
                .setSingleChoiceItems(options, selectedOptionIndex, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int item)
                    {
                        selectedOptionIndex = item;
                        if (listener != null) listener.onOptionSelected(item);
                        dismiss();
                    }
                });

        return builder.create();
    }

    /**
     * Getter
     * @return the current selected position
     */
    public int getSelectedOptionIndex()
    {
        return selectedOptionIndex;
    }

    /**
     * Setter
     * @param listener the listener for the select dialog
     */
    public void setSelectDialogListener(SelectDialogListener listener)
    {
        this.listener = listener;
    }

    /**
     * Setter
     * @param position the new selected position
     */
    public void setSelectedOptionIndex(int position)
    {
        this.selectedOptionIndex = position;
    }

    /**
     * Getter
     * @return true if the dialog is currently shown
     */
    public boolean isShowing()
    {
        return this.getDialog()!=null;
    }

    /**
     * Listener for option selected in select picker
     */
    public interface SelectDialogListener
    {
        /**
         * Called when the user selects a color in the color picker
         * @param position the selected option position
         */
        void onOptionSelected(int position);
    }
}
