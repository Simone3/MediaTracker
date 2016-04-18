package it.polimi.dima.mediatracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;

/**
 * Dialog that allows to pick a color among several options
 */
public class ColorPickerDialog extends DialogFragment
{
    private final static String TITLE_PARAMETER = "TITLE_PARAMETER";
    private final static String OPTIONS_PARAMETER = "OPTIONS_PARAMETER";
    private final static String SELECTED_OPTION_PARAMETER = "SELECTED_OPTION_PARAMETER";
    private final static String COLUMN_NUMBER_PARAMETER = "COLUMN_NUMBER_PARAMETER";

    private ColorPickerGridAdapter colorGridAdapter;
    private ColorPickerListener listener;
    private int[] colorChoices;
    private int columnNumber;
    private int selectedColor = 0;
    private int title;

    /**
     * Allows to create a new instance of the dialog
     * @param title the dialog title
     * @param colors the dialog options
     * @param selectedColor the first selected option
     * @param columnNumber the number of columns in which the options are placed
     * @return the dialog instance
     */
    public static ColorPickerDialog newInstance(int title, int[] colors, int selectedColor, int columnNumber)
    {
        ColorPickerDialog colorPicker = new ColorPickerDialog();

        Bundle args = new Bundle();
        args.putInt(TITLE_PARAMETER, title);
        args.putIntArray(OPTIONS_PARAMETER, colors);
        args.putInt(SELECTED_OPTION_PARAMETER, selectedColor);
        args.putInt(COLUMN_NUMBER_PARAMETER, columnNumber);
        colorPicker.setArguments(args);

        return colorPicker;
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
        colorChoices = args.getIntArray(OPTIONS_PARAMETER);
        if(selectedColor==0) selectedColor = args.getInt(SELECTED_OPTION_PARAMETER, 0);
        columnNumber = args.getInt(COLUMN_NUMBER_PARAMETER, 0);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Set layout (pass null as the parent view because its going in the dialog layout)
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dialogView = layoutInflater.inflate(R.layout.dialog_color_picker_grid, null);

        // Setup grid
        GridView colorGrid = (GridView) dialogView.findViewById(R.id.color_picker_grid);
        colorGrid.setNumColumns(columnNumber);

        // Add click listener on the grid elements
        colorGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long itemId)
            {
                colorGridAdapter.setSelectedColor(colorGridAdapter.getItem(position));
                if (listener != null) listener.onColorSelected(colorGridAdapter.getItem(position));
                dismiss();
            }
        });

        // Setup adapter
        if(isAdded() && colorGridAdapter==null)
        {
            colorGridAdapter = new ColorPickerGridAdapter();
        }
        if(colorGridAdapter!=null)
        {
            colorGridAdapter.setSelectedColor(selectedColor);
            colorGrid.setAdapter(colorGridAdapter);
        }

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setView(dialogView)
                .setTitle(title)
                .create();
    }

    /**
     * Setter
     * @param listener the listener for the color picker
     */
    public void setColorPickerListener(ColorPickerListener listener)
    {
        this.listener = listener;
    }

    /**
     * Setter
     * @param selectedColor the new selected color resource ID
     */
    public void setSelectedColor(int selectedColor)
    {
        this.selectedColor = selectedColor;
    }

    /**
     * Getter
     * @return the selected color resource ID
     */
    public int getSelectedColor()
    {
        Log.v("XXXXX", "----------gsc="+selectedColor);
        return selectedColor;
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
     * Listener for color selected in color picker
     */
    public interface ColorPickerListener
    {
        /**
         * Called when the user selects a color in the color picker
         * @param color the selected color
         */
        void onColorSelected(int color);
    }

    /**
     * The adapter of the color picker
     */
    private class ColorPickerGridAdapter extends BaseAdapter
    {
        private List<Integer> colors = new ArrayList<>();

        /**
         * Constructor
         */
        private ColorPickerGridAdapter()
        {
            for(int color: colorChoices)
            {
                colors.add(color);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getCount()
        {
            return colors.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer getItem(int position)
        {
            return colors.get(position);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getItemId(int position)
        {
            return getItem(position);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(int position, View convertView, ViewGroup container)
        {
            // Create the view if necessary
            if(convertView==null)
            {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_color_picker_item, container, false);
            }

            // Get color at this position
            int color = getItem(position);

            // Get UI elements
            ImageView colorItem = (ImageView) convertView.findViewById(R.id.color_picker_item);
            ImageView colorSelectedIcon = (ImageView) convertView.findViewById(R.id.color_picker_selected_icon);

            // Set option background
            GradientDrawable colorItemBackground = (GradientDrawable) colorItem.getBackground();
            colorItemBackground.setColor(ContextCompat.getColor(getActivity(), color));

            // Set the selected icon if necessary
            if(color==selectedColor) colorSelectedIcon.setImageResource(R.drawable.ic_completed);
            else colorSelectedIcon.setImageDrawable(null);

            return convertView;
        }

        /**
         * Setter
         * @param selectedColor the currently selected color
         */
        public void setSelectedColor(int selectedColor)
        {
            if(ColorPickerDialog.this.selectedColor!=selectedColor)
            {
                ColorPickerDialog.this.selectedColor = selectedColor;
                notifyDataSetChanged();
            }
        }
    }
}
