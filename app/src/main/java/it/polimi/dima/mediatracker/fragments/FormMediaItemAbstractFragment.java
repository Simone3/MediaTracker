package it.polimi.dima.mediatracker.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.adapters.AutoCompleteMediaItemsAdapter;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.external_services.services.MediaItemService;
import it.polimi.dima.mediatracker.inputs.AbstractInput;
import it.polimi.dima.mediatracker.inputs.SwitchInput;
import it.polimi.dima.mediatracker.inputs.DatePickerInput;
import it.polimi.dima.mediatracker.inputs.EditTextInput;
import it.polimi.dima.mediatracker.inputs.SelectDialogInput;
import it.polimi.dima.mediatracker.layout.AutoCompleteTextViewWithDelay;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Form to add/edit an abstract media item, contains the management of
 * the inputs common to all media items (like title)
 */
public abstract class FormMediaItemAbstractFragment extends FormAbstractFragment
{
    private final static String CATEGORY_ID_PARAMETER = "CATEGORY_ID_PARAMETER";
    private final static String MEDIA_ITEM_ID_PARAMETER = "MEDIA_ITEM_ID_PARAMETER";

    private final static String IMAGE_BITMAP_SAVED_INSTANCE = "IMAGE_BITMAP_SAVED_INSTANCE";
    private final static String IMAGE_URL_SAVED_INSTANCE = "IMAGE_URL_SAVED_INSTANCE";
    private final static String API_ID_SAVED_INSTANCE = "API_ID_SAVED_INSTANCE";

    private final static int AUTOCOMPLETE_THRESHOLD = 3;

    private Category category;
    private MediaItem mediaItem;

    private MediaItemsAbstractController controller;

    private Bitmap imageBitmap;
    private ImageView image;
    private ImageAsyncLoader imageAsyncLoader;
    private LinearLayout imageContainer;

    private EditTextInput<MediaItem> titleInput;
    private List<AbstractInput<MediaItem>> inputs;

    /**
     * Helper to build the parameter bundle for the subclasses
     * @param categoryId the category ID
     * @param mediaItemId the media item ID
     * @param isEmpty true if the form should be empty
     * @return the bundle with the fragment parameters
     */
    static Bundle getMediaItemFragmentParametersBundle(Long categoryId, Long mediaItemId, boolean isEmpty)
    {
        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_PARAMETER, categoryId);
        if(mediaItemId!=null) args.putLong(MEDIA_ITEM_ID_PARAMETER, mediaItemId);
        args.putBoolean(IS_EMPTY_PARAMETER, isEmpty);
        return args;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Bundle args = getArguments();

        // Get category and media item controller
        Long categoryId = args.getLong(CATEGORY_ID_PARAMETER, -1);
        category = CategoriesController.getInstance().getCategoryById(categoryId);
        controller = category.getMediaType().getController();

        // Get current media item (available only if we are editing one)
        Long itemId = args.getLong(MEDIA_ITEM_ID_PARAMETER, -1);
        mediaItem = controller.getMediaItemById(category, itemId);

        // Check if empty (= show only the toolbar but no fields)
        boolean isEmpty = args.getBoolean(IS_EMPTY_PARAMETER);
        if(isEmpty)
        {
            return view;
        }

        // If mediaItem is not specified, we are adding a new one
        setIsEditingForm(mediaItem!=null);

        // If we are adding a new media item, initialize it
        if(!isEditingForm())
        {
            mediaItem = controller.initializeEmptyMediaItem();
            mediaItem.setCategory(category.getId());
        }

        // Set title
        if(isEditingForm())
        {
            getToolbar().setTitle(mediaItem.getTitle());
        }
        else
        {
            getToolbar().setTitle(getString(R.string.title_activity_add_form, category.getMediaType().getNameSingular(getActivity())));
        }

        // Initialize input array
        inputs = new ArrayList<>();

        // Setup inputs common to all media items
        setupCommonInputs(view);

        // Setup specific inputs
        inputs.addAll(getSpecificInputs(view, mediaItem));

        // If we are editing, set all values in the inputs
        if(isEditingForm())
        {
            for(AbstractInput<MediaItem> input: inputs)
            {
                input.setAreValueChangeListenersDisabled(true);
                input.setInputFromModelObject(mediaItem);
                input.setAreValueChangeListenersDisabled(false);
            }
        }

        // Image
        setupImageAndExtraOptions(view);

        return view;
    }

    /**
     * Restores saved data after a configuration change
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null)
        {
            // Restore inputs
            if(inputs!=null) for(AbstractInput input: inputs)
            {
                input.setAreValueChangeListenersDisabled(true);
                input.restoreInstance(savedInstanceState);
                input.setAreValueChangeListenersDisabled(false);
            }

            // Restore image
            if(savedInstanceState.containsKey(IMAGE_BITMAP_SAVED_INSTANCE))
            {
                imageBitmap = savedInstanceState.getParcelable(IMAGE_BITMAP_SAVED_INSTANCE);
            }

            // Restore non-input media item data
            if(savedInstanceState.containsKey(IMAGE_URL_SAVED_INSTANCE))
            {
                mediaItem.setImageUrl((URL) savedInstanceState.getSerializable(IMAGE_URL_SAVED_INSTANCE));
            }
            if(savedInstanceState.containsKey(API_ID_SAVED_INSTANCE))
            {
                mediaItem.setExternalServiceId(savedInstanceState.getString(API_ID_SAVED_INSTANCE));
            }

            // If we are adding a new media item but the user already selected a media item from the autocomplete, show the image
            if(!isEditingForm() && !Utils.isEmpty(mediaItem.getExternalServiceId()))
            {
                setImageFromCurrentUrl(true);
                showImageAndExtraOptions(true);
            }
        }
    }

    /**
     * Saves data before a configuration change
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Save inputs
        if(inputs!=null) for(AbstractInput input: inputs)
        {
            input.saveInstance(outState);
        }

        // Save image
        if(imageBitmap!=null)
        {
            outState.putParcelable(IMAGE_BITMAP_SAVED_INSTANCE, imageBitmap);
        }

        // Save non-input media item data
        if(mediaItem!=null && mediaItem.getImageUrl()!=null)
        {
            outState.putSerializable(IMAGE_URL_SAVED_INSTANCE, mediaItem.getImageUrl());
        }
        if(mediaItem!=null && !Utils.isEmpty(mediaItem.getExternalServiceId()))
        {
            outState.putString(API_ID_SAVED_INSTANCE, mediaItem.getExternalServiceId());
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
        if(inputs!=null) for(AbstractInput input: inputs)
        {
            input.dismiss();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canExitForm()
    {
        Log.v("((((((((((((", "canExit");
        boolean canExit = true;
        if(inputs!=null) for(AbstractInput input: inputs)
        {
            Log.v("((((((((((((", "wcbu@"+input.getClass()+"="+input.wasChangedByUser());
            if(input.wasChangedByUser())
            {
                canExit = false;
                break;
            }
        }
        Log.v("((((((((((((", "res="+canExit);
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



    /************************************************ SAVE MEDIA ITEM ************************************************/


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setObjectValuesFromInputs()
    {
        // If we are editing, backup current importance level
        ImportanceLevel previousImportanceLevel = null;
        if(isEditingForm()) previousImportanceLevel = mediaItem.getImportanceLevel();

        // Update the media item with the current input values
        for(AbstractInput<MediaItem> input: inputs)
        {
            input.setModelObjectFromInput(mediaItem);
        }

        // If we are editing and importance level changed, update order value
        if(isEditingForm() && !mediaItem.getImportanceLevel().equals(previousImportanceLevel))
        {
            controller.setMediaItemOrderInImportanceLevelBeforeUpdatingImportanceLevel(category, mediaItem);
        }

        // If we are adding, set first order value
        if(!isEditingForm())
        {
            controller.setMediaItemOrderInImportanceLevelBeforeInserting(category, mediaItem);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String validateObject()
    {
        return controller.validateMediaItem(getActivity(), mediaItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveObject()
    {
        // Save media item in the database
        controller.saveMediaItem(mediaItem);
    }



    /************************************************ SETUP INPUTS COMMON TO ALL MEDIA TYPES ************************************************/

    /**
     * Adds to the input list the inputs that are shared by all media types
     * @param view the fragment inflated view
     */
    private void setupCommonInputs(View view)
    {
        // Title
        titleInput = new EditTextInput<>(false, view, R.id.form_title_input, new EditTextInput.CallbackExtended<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                mediaItem.setTitle(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return mediaItem.getTitle();
            }

            @Override
            public void setExtraAttributes(View view, EditText editText)
            {
                AutoCompleteTextViewWithDelay title = (AutoCompleteTextViewWithDelay) editText;

                // Hint
                title.setHint(getString(R.string.form_title_media_item_title, category.getMediaType().getNameSingular(getActivity())));

                // Setup autocomplete settings
                title.setThreshold(AUTOCOMPLETE_THRESHOLD);
                ProgressBar loadingIndicator = (ProgressBar) view.findViewById(R.id.auto_complete_loading_spinner);
                title.setAdapter(new AutoCompleteMediaItemsAdapter(getActivity(), controller.getMediaItemService(getActivity()), loadingIndicator));

                // Add on click listener for the autocomplete options
                title.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
                    {
                        // Get selected item from the adapter
                        MediaItemSearchResult searchResult = (MediaItemSearchResult) adapterView.getItemAtPosition(position);

                        // Load and set data
                        loadAndSetMediaItemInfoFromExternalService(searchResult.getApiId());
                    }
                });
            }

            @Override
            public void onValueChange(String text)
            {
                // Do nothing
            }
        });
        inputs.add(titleInput);

        // Genres
        inputs.add(new EditTextInput<>(true, view, R.id.form_genres_input, new EditTextInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                mediaItem.setGenres(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return mediaItem.getGenres();
            }
        }));

        // Description
        inputs.add(new EditTextInput<>(true, view, R.id.form_description_input, new EditTextInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                mediaItem.setDescription(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return mediaItem.getDescription();
            }
        }));

        // User comment
        inputs.add(new EditTextInput<>(false, view, R.id.form_user_comment_input, new EditTextInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, String text)
            {
                mediaItem.setUserComment(text);
            }

            @Override
            public String getModelObjectValue(MediaItem mediaItem)
            {
                return mediaItem.getUserComment();
            }
        }));

        // Importance level
        ImportanceLevel[] allImportanceLevels = ImportanceLevel.values();
        String[] allImportanceLevelNames = new String[allImportanceLevels.length];
        for(int i = 0; i < allImportanceLevelNames.length; i++) allImportanceLevelNames[i] = allImportanceLevels[i].getName(getActivity());
        inputs.add(new SelectDialogInput<>(false, getFragmentManager(), view, R.id.form_importance_level_button, R.string.form_importance_level_select_title, allImportanceLevels, allImportanceLevelNames, null, new SelectDialogInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, Object selectedOption)
            {
                mediaItem.setImportanceLevel((ImportanceLevel) selectedOption);
            }

            @Override
            public Object getModelObjectValue(MediaItem mediaItem)
            {
                return mediaItem.getImportanceLevel();
            }
        }));

        // Release date
        inputs.add(new DatePickerInput<>(true, getActivity(), view, R.id.form_release_date_button, new DatePickerInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, Date date)
            {
                mediaItem.setReleaseDate(date);
            }

            @Override
            public Date getModelObjectValue(MediaItem mediaItem)
            {
                return mediaItem.getReleaseDate();
            }
        }));

        // Owned
        inputs.add(new SwitchInput<>(false, view, R.id.form_owned_input, new SwitchInput.Callback<MediaItem>()
        {
            @Override
            public void setModelObjectValue(MediaItem mediaItem, boolean checked)
            {
                mediaItem.setOwned(checked);
            }

            @Override
            public boolean getModelObjectValue(MediaItem mediaItem)
            {
                return mediaItem.isOwned();
            }
        }));
    }

    /**
     * Manages the media item image container (image + search links + reload button)
     * @param view the fragment inflated view
     */
    private void setupImageAndExtraOptions(View view)
    {
        // Get components
        imageContainer = (LinearLayout) view.findViewById(R.id.form_image_container);
        image = (ImageView) view.findViewById(R.id.form_image);
        ImageView reloadButton = (ImageView) view.findViewById(R.id.form_reload_button);
        ImageView googleLink = (ImageView) view.findViewById(R.id.google_link);
        ImageView wikipediaLink = (ImageView) view.findViewById(R.id.wikipedia_link);

        // At startup, show the image container only if we are editing
        showImageAndExtraOptions(isEditingForm());
        if(isEditingForm()) setImageFromCurrentUrl(true);

        // Add Google link
        if(googleLink!=null) googleLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openUrl(getString(R.string.google_search_url, titleInput.getText()));
            }
        });

        // Add Wikipedia link
        if(wikipediaLink!=null) wikipediaLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openUrl(getString(R.string.wikipedia_search_url, titleInput.getText()));
            }
        });

        // Setup reload button
        if(isEditingForm() && mediaItem.getExternalServiceId()!=null)
        {
            // Set icon and color
            reloadButton.setImageResource(R.drawable.ic_download);
            reloadButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.form_reload_button_enabled_color));

            // Add click listener
            reloadButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Ask confirmation
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.form_reload_alert_content)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    // Reload data
                                    loadAndSetMediaItemInfoFromExternalService(mediaItem.getExternalServiceId());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            });
        }
        else
        {
            // Set icon and color
            reloadButton.setImageResource(R.drawable.ic_download_not_available);
            reloadButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.form_reload_button_disabled_color));
        }
    }



    /************************************************ HELPERS TO SET DATA IN VIEW ************************************************/


    /**
     * Calls the external service controller to fetch the media item data, then sets it in the form inputs
     * @param externalServiceId the external service ID of the media item
     */
    private void loadAndSetMediaItemInfoFromExternalService(String externalServiceId)
    {
        // Show a loading dialog
        final ProgressDialog progressDialog =
                ProgressDialog.show(getActivity(),
                        getResources().getString(R.string.fetch_media_item_data_wait_title),
                        getResources().getString(R.string.fetch_media_item_data_wait_message), true, false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // Call the media item service to retrieve the information
        MediaItemService mediaItemService = controller.getMediaItemService(getActivity());
        mediaItemService.getMediaItemInfo(externalServiceId, new MediaItemService.MediaItemInfoCallback()
        {
            @Override
            public void onLoad(MediaItem loadedMediaItem)
            {
                if(loadedMediaItem==null)
                {
                    showErrorToast();
                }
                else
                {
                    // Save service id for any future operation
                    mediaItem.setExternalServiceId(loadedMediaItem.getExternalServiceId());

                    // Save image url
                    if (loadedMediaItem.getImageUrl() != null)
                    {
                        mediaItem.setImageUrl(loadedMediaItem.getImageUrl());
                        setImageFromCurrentUrl(false);
                    }

                    // Show image
                    showImageAndExtraOptions(true);

                    // Set input values based on the retrieved data
                    setInputValuesFromAPIItem(loadedMediaItem);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure()
            {
                showErrorToast();
                progressDialog.dismiss();
            }

            private void showErrorToast()
            {
                Toast.makeText(getActivity(), R.string.form_error_external_service, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper to re(load) the image from the current image URL parameter
     * @param firstRun true if we are calling this method from onCreateView
     */
    private void setImageFromCurrentUrl(boolean firstRun)
    {
        if(firstRun && imageBitmap!=null)
        {
            image.setImageBitmap(imageBitmap);
        }
        else if(mediaItem.getImageUrl()!=null)
        {
            if(imageAsyncLoader!=null) imageAsyncLoader.cancel(true);
            imageAsyncLoader = new ImageAsyncLoader();
            imageAsyncLoader.execute(mediaItem.getImageUrl());
        }
    }

    /**
     * Helper to open the given URL in the user browser
     * @param url the url to open
     */
    private void openUrl(final String url)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url.replaceAll(" ", "%20")));
        startActivity(intent);
    }


    /**
     * Shows/hides the image container (image + search links + reload button)
     * @param show true if container should be visible
     */
    private void showImageAndExtraOptions(boolean show)
    {
        if(show)
        {
            imageContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            imageContainer.setVisibility(View.GONE);
        }
    }



    /************************************************ EXTERNAL APIs ************************************************/


    /**
     * Sets the values of the shared inputs given the media item retrieved from the external service,
     * then calls setSpecificInputValuesFromAPIItem() to set the specific values for the subclass
     * @param tempMediaItem a temporary media item that contains the values to set
     */
    private void setInputValuesFromAPIItem(MediaItem tempMediaItem)
    {
        // Update input values given the media item fields
        for(AbstractInput<MediaItem> input: inputs)
        {
            if(input.isUpdatedByExternalService())
            {
                input.setAreValueChangeListenersDisabled(true);
                input.setInputFromModelObject(tempMediaItem);
                input.setWasChangedByUser(true);
                input.setAreValueChangeListenersDisabled(false);
            }
        }
    }



    /************************************************ SUBCLASS METHODS ************************************************/

    /**
     * Allows to create and manage the subclass specific inputs
     * @param view the fragment inflated view
     * @param initialMediaItem the media item initial values (meaningful only if we are editing)
     * @return the list of the inputs of the subclass
     */
    protected abstract List<AbstractInput<MediaItem>> getSpecificInputs(View view, MediaItem initialMediaItem);



    /************************************************ CLASSES ************************************************/

    /**
     * AsyncTask to load an image from URL asynchronously
     */
    private class ImageAsyncLoader extends AsyncTask<URL, String, Bitmap>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected Bitmap doInBackground(URL... args)
        {
            Bitmap bitmap;
            try
            {
                bitmap = BitmapFactory.decodeStream((InputStream) args[0].getContent());
            }
            catch(Exception e)
            {
                e.printStackTrace();
                bitmap = null;
            }

            return bitmap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageBitmap = bitmap;

            if(bitmap!=null)
            {
                image.setImageBitmap(bitmap);
            }
        }
    }
}
