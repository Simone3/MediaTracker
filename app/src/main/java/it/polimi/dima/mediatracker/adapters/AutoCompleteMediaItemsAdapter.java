package it.polimi.dima.mediatracker.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.external_services.services.MediaItemService;
import it.polimi.dima.mediatracker.model.MediaItemSearchResult;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Adapter for the autocomplete title input used in {@link it.polimi.dima.mediatracker.fragments.FormMediaItemAbstractFragment}
 */
public class AutoCompleteMediaItemsAdapter extends BaseAdapter implements Filterable
{
    private Context context;
    private List<MediaItemSearchResult> resultList = new ArrayList<>();
    private MediaItemService service;

    private ProgressBar loadingIndicator;

    /**
     * Constructor
     * @param context the context
     * @param service the external service associated with this adapter
     */
    public AutoCompleteMediaItemsAdapter(Context context, MediaItemService service, ProgressBar loadingIndicator)
    {
        this.context = context;
        this.service = service;
        this.loadingIndicator = loadingIndicator;
    }

    /**
     * Getter
     * @return the number of results (max {@link it.polimi.dima.mediatracker.external_services.services.MediaItemService#MAX_SEARCH_RESULTS})
     */
    @Override
    public int getCount()
    {
        return resultList.size() >= MediaItemService.MAX_SEARCH_RESULTS ? MediaItemService.MAX_SEARCH_RESULTS : resultList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaItemSearchResult getItem(int index)
    {
        return resultList.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_autocomplete, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.search_result_title)).setText(getSearchResultTitle(context, getItem(position)));
        TextView subtitle = (TextView) convertView.findViewById(R.id.search_result_sub_title);
        if(getItem(position).getAuthor()!=null)
        {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(getItem(position).getAuthor());
        }
        else
        {
            subtitle.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * Shows or hides the loading indicator
     * @param display true if loading indicator should be visible
     */
    private void displayLoadingIndicator(final boolean display)
    {
        // Since this method is called by the filter we need to run it in the UI thread via handler
        new Handler(context.getMainLooper()).post(new Runnable()
        {
            public void run()
            {
                loadingIndicator.setVisibility(display ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint)
            {
                // If we have data...
                FilterResults filterResults = new FilterResults();
                if(constraint!=null)
                {
                    // Show loading indicator
                    displayLoadingIndicator(true);

                    // Get title given by the user
                    String mediaItemTitle = constraint.toString();

                    // Call service to search for media items
                    service.searchMediaItems(mediaItemTitle, new MediaItemService.MediaItemSearchCallback()
                    {
                        @Override
                        public void onLoad(List<MediaItemSearchResult> results)
                        {
                            // Notify changes
                            if(results!=null && results.size()>0)
                            {
                                resultList = results;
                                notifyDataSetChanged();
                            }
                            else
                            {
                                notifyDataSetInvalidated();
                            }

                            // Hide loading indicator
                            displayLoadingIndicator(false);
                        }

                        @Override
                        public void onFailure()
                        {
                            // Show error
                            Toast.makeText(context, R.string.form_error_external_service, Toast.LENGTH_SHORT).show();
                            notifyDataSetInvalidated();

                            // Hide loading indicator
                            displayLoadingIndicator(false);
                        }
                    });
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                // Do nothing here
            }
        };
    }

    /**
     * Helper to get the displayed title of a media item search result
     * @param context the context
     * @param mediaItemSearchResult the search result
     * @return the title to display
     */
    private String getSearchResultTitle(Context context, MediaItemSearchResult mediaItemSearchResult)
    {
        // Add year after the media item name if it's not empty
        return (Utils.isEmpty(mediaItemSearchResult.getYear()) ? mediaItemSearchResult.getTitle() : context.getString(R.string.search_result_media_item_name_with_year, mediaItemSearchResult.getTitle(), mediaItemSearchResult.getYear()));
    }
}

