package it.polimi.dima.mediatracker.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.inputs.AbstractInput;
import it.polimi.dima.mediatracker.inputs.ColorPickerInput;
import it.polimi.dima.mediatracker.inputs.EditTextInput;
import it.polimi.dima.mediatracker.inputs.SelectDialogInput;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.MediaType;

/**
 * Form to add/edit a category
 */
public class FormCategoryFragment extends FormAbstractFragment
{
    private final static int COLOR_PICKER_COLUMNS = 4;

    private final static String CATEGORY_ID_PARAMETER = "CATEGORY_ID_PARAMETER";

    private Category category;

    private MediaItemsAbstractController controller;

    private List<AbstractInput<Category>> inputs;

    /**
     * Instance creation
     * @param categoryId category ID parameter (null if adding a new category, the category to edit otherwise)
     * @return the fragment instance
     */
    public static FormCategoryFragment newInstance(Long categoryId)
    {
        FormCategoryFragment fragment = new FormCategoryFragment();

        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_PARAMETER, categoryId);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Get category parameter
        Bundle args = getArguments();
        Long categoryId = args.getLong(CATEGORY_ID_PARAMETER, -1);
        category = CategoriesController.getInstance().getCategoryById(categoryId);

        // If there's no category parameter, we are adding a new one
        setIsEditingForm(category!=null);

        // If we are adding a new category, initialize it
        if(!isEditingForm()) category = new Category();
        else controller = category.getMediaType().getController();

        // Set title
        if(isEditingForm()) getToolbar().setTitle(category.getName());
        else getToolbar().setTitle(getString(R.string.title_activity_add_form, getString(R.string.category)));

        // Initialize input array
        inputs = new ArrayList<>();

        // Setup inputs
        setupInputs(view);

        // If we are editing, set all values in the inputs
        if(isEditingForm())
        {
            for(AbstractInput<Category> input: inputs)
            {
                input.setAreValueChangeListenersDisabled(true);
                input.setInputFromModelObject(category);
                input.setAreValueChangeListenersDisabled(false);
            }
        }

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canExitForm()
    {
        boolean canExit = true;
        if(inputs!=null) for(AbstractInput input: inputs)
        {
            if(input.wasChangedByUser())
            {
                canExit = false;
                break;
            }
        }

        return canExit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void resetInputUpdatedByUserFlags()
    {
        for(AbstractInput input: inputs)
        {
            input.setWasChangedByUser(false);
        }
    }

    /**
     * Restores saved input states after a configuration change
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null)
        {
            // Restore inputs
            for(AbstractInput input: inputs)
            {
                input.setAreValueChangeListenersDisabled(true);
                input.restoreInstance(savedInstanceState);
                input.setAreValueChangeListenersDisabled(false);
            }
        }
    }

    /**
     * Saves input states before a configuration change
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Save inputs
        for(AbstractInput input: inputs)
        {
            input.saveInstance(outState);
        }
    }

    /**
     * Closes all open inputs before destroying the fragment
     * {@inheritDoc}
     */
    @Override
    public void onPause()
    {
        super.onPause();

        // Close inputs (if necessary)
        for(AbstractInput input: inputs)
        {
            input.dismiss();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getContentLayout()
    {
        return R.layout.content_form_categories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setObjectValuesFromInputs()
    {
        // Update the category with the current input values
        for(AbstractInput<Category> input: inputs)
        {
            input.setModelObjectFromInput(category);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String validateObject()
    {
        return CategoriesController.getInstance().validateCategory(getActivity(), category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveObject()
    {
        // Save category
        CategoriesController.getInstance().saveCategory(category);
    }

    private void setupInputs(View view)
    {
        // Title
        inputs.add(new EditTextInput<>(false, view, R.id.form_title_input, new EditTextInput.Callback<Category>()
        {
            @Override
            public void setModelObjectValue(Category category, String text)
            {
                category.setName(text);
            }

            @Override
            public String getModelObjectValue(Category category)
            {
                return category.getName();
            }
        }));

        // Color
        TypedArray colorsTypedArray = getResources().obtainTypedArray(R.array.color_picker_options);
        int[] colors = new int[colorsTypedArray.length()];
        for(int i=0; i<colors.length; i++) colors[i] = colorsTypedArray.getResourceId(i, 0);
        colorsTypedArray.recycle();
        inputs.add(new ColorPickerInput<>(false, getActivity(), getFragmentManager(), view, R.id.form_color_input, R.string.form_color_picker_title, COLOR_PICKER_COLUMNS, colors, new ColorPickerInput.Callback<Category>()
        {
            @Override
            public void setModelObjectValue(Category category, int color)
            {
                category.setColor(getActivity(), color);
            }

            @Override
            public int getModelObjectValue(Category category)
            {
                return category.getColor(getActivity());
            }
        }));

        // Media type
        MediaType[] allMediaTypes = MediaType.values();
        String[] allMediaTypeNames = new String[allMediaTypes.length];
        int[] allMediaTypeIcons = new int[allMediaTypes.length];
        for(int i = 0; i < allMediaTypeNames.length; i++)
        {
            allMediaTypeNames[i] = allMediaTypes[i].getNamePlural(getActivity());
            allMediaTypeIcons[i] = allMediaTypes[i].getIcon();
        }
        inputs.add(new SelectDialogInput<>(false, getFragmentManager(), view, R.id.form_media_type_button, R.string.form_media_type_select_title, allMediaTypes, allMediaTypeNames, allMediaTypeIcons, new SelectDialogInput.Callback<Category>()
        {
            @Override
            public void setModelObjectValue(Category category, Object selectedOption)
            {
                category.setMediaType((MediaType) selectedOption);
            }

            @Override
            public Object getModelObjectValue(Category category)
            {
                return category.getMediaType();
            }
        }));


        // Allow selection of media type only if the user is creating a new category
        final boolean canSelectMediaType = !(isEditingForm() && controller.getAllMediaItemsNumberInCategory(category)>0);
        EditText mediaTypeButton = (EditText) view.findViewById(R.id.form_media_type_button);
        if(!canSelectMediaType)
        {
            mediaTypeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    Toast.makeText(getActivity(), getString(R.string.form_error_cannot_edit_media_type), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
