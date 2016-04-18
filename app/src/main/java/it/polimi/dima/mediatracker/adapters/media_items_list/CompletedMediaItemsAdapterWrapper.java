package it.polimi.dima.mediatracker.adapters.media_items_list;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.MediaItem;

/**
 * Sectioned adapter for the completed media items list
 */
public class CompletedMediaItemsAdapterWrapper extends SectionedAbstractAdapterWrapper
{
    private Context context;

    /**
     * Constructor
     * @param mediaItemList the media items in the list
     * @param optionNameRedo the resource ID of the string for "I could redo this" option
     * @param sectionNameCallback the callback to get the section for each media item
     */
    public CompletedMediaItemsAdapterWrapper(List<MediaItem> mediaItemList, int optionNameRedo, SectionNameCallback sectionNameCallback)
    {
        CompletedSectionedAdapter adapter = new CompletedSectionedAdapter(mediaItemList, optionNameRedo, sectionNameCallback);
        super.setAdapterFromSubclass(adapter);
    }

    /**
     * Private adapter
     */
    private class CompletedSectionedAdapter extends SectionedAbstractAdapter
    {
        private int optionNameRedo;

        /**
         * {@inheritDoc}
         */
        private CompletedSectionedAdapter(List<MediaItem> mediaItemList, int optionNameRedo, SectionNameCallback sectionNameCallback)
        {
            super(mediaItemList, sectionNameCallback);

            this.optionNameRedo = optionNameRedo;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected RecyclerView.ViewHolder onCreateElementViewHolder(ViewGroup parent, int viewType)
        {
            // Get context
            context = parent.getContext();

            // Inflate view for each mediaItem
            View itemView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

            return new CompletedElementViewHolder(itemView);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBindElementViewHolder(RecyclerView.ViewHolder itemHolder, int actualItemPosition)
        {
            // Get data
            CompletedElementViewHolder holder = (CompletedElementViewHolder) itemHolder;
            MediaItem mediaItem = getMediaItemList().get(actualItemPosition);

            // Options button
            setupOptionsButton(context, holder.optionsButton, actualItemPosition, 0, 0, optionNameRedo);

            // Clickable part
            setupClickablePart(holder.clickablePart, actualItemPosition);

            // Hide drag handler
            holder.handleView.setVisibility(View.GONE);

            // Set title
            holder.title.setText(mediaItem.getTitle());
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.completed_media_item_text_color));

            // Set subtitle (completion date)
            int times = mediaItem.getTimesCompleted();
            String last = DateFormat.getDateFormat(context).format(mediaItem.getCompletionDate());
            if(times<=1)
            {
                holder.subtitle.setText(context.getString(R.string.completed_on_single, last));
            }
            else
            {
                holder.subtitle.setText(context.getString(R.string.completed_on_multiple, times, last));
            }
        }
    }

    /**
     * ViewHolder for this list media items
     */
    public static class CompletedElementViewHolder extends ElementAbstractViewHolder
    {
        public CompletedElementViewHolder(View itemView)
        {
            super(itemView);
        }
    }
}
