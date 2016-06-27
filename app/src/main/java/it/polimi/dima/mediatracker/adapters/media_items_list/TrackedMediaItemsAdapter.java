package it.polimi.dima.mediatracker.adapters.media_items_list;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.inputs.DatePickerInput;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Sectioned adapter for the tracked media items list
 */
public class TrackedMediaItemsAdapter extends MediaItemsAbstractAdapter
{
    private Context context;

    /**
     * Constructor
     * @param mediaItemList the media items in the list
     * @param optionNameDone the resource ID of the string for "I've done this" option
     * @param optionNameDoing the resource ID of the string for "I'm doing this" option
     */
    public TrackedMediaItemsAdapter(List<MediaItem> mediaItemList, int optionNameDone, int optionNameDoing)
    {
        TrackedInternalAdapter adapter = new TrackedInternalAdapter(mediaItemList, optionNameDone, optionNameDoing);
        super.setAdapterFromSubclass(adapter);
    }

    /**
     * {@inheritDoc}
     */
    private class TrackedInternalAdapter extends MediaItemsInternalAdapter
    {
        private int optionNameDone;
        private int optionNameDoing;

        /**
         * {@inheritDoc}
         */
        private TrackedInternalAdapter(List<MediaItem> mediaItemList, int optionNameDone, int optionNameDoing)
        {
            super(mediaItemList);

            this.optionNameDone = optionNameDone;
            this.optionNameDoing = optionNameDoing;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType)
        {
            context = parent.getContext();

            // Inflate view
            View itemView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

            // Create ViewHolder
            return new TrackedElementViewHolder(itemView);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBindItemViewHolder(RecyclerView.ViewHolder itemHolder, int actualItemPosition)
        {
            // Get data
            TrackedElementViewHolder holder = (TrackedElementViewHolder) itemHolder;
            MediaItem mediaItem = getItemsList().get(actualItemPosition);

            // Get values depending on media item status
            int titleColor = R.color.owned_media_item_text_color;
            if(mediaItem.isUpcoming())
            {
                titleColor = R.color.upcoming_media_item_text_color;
            }
            else if(mediaItem.isDoingNow())
            {
                titleColor = R.color.doing_now_media_item_text_color;
            }
            else if(!mediaItem.isOwned())
            {
                titleColor = R.color.non_owned_media_item_text_color;
            }

            // Options button
            setupOptionsButton(context, holder.optionsButton, actualItemPosition, optionNameDone, optionNameDoing, 0);

            // Clickable part
            setupClickablePart(holder.clickablePart, actualItemPosition);

            // "Drag handler" icon
            setupDragHandler(holder, !mediaItem.isUpcoming());

            // Set title
            holder.title.setText(mediaItem.getTitle());
            holder.title.setTextColor(ContextCompat.getColor(context, titleColor));

            // Set subtitle
            String creator = mediaItem.getCreator();
            MediaItem.Duration duration = mediaItem.getDuration(context);
            boolean isCreatorSet = !Utils.isEmpty(creator);
            boolean isDurationSet = duration!=null && duration.getValue()>0;
            if(isCreatorSet || isDurationSet)
            {
                if(isCreatorSet && isDurationSet)
                {
                    holder.subtitle.setText(context.getString(R.string.list_media_item_by_and_duration, mediaItem.getCreator(), duration.getValue()+duration.getMeasureUnitShort()));
                }
                else if(isCreatorSet)
                {
                    holder.subtitle.setText(mediaItem.getCreator());
                }
                else
                {
                    holder.subtitle.setText(context.getString(R.string.duration_value_and_measure_unit_short, duration.getValue(), duration.getMeasureUnitShort()));
                }
                holder.subtitle.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.subtitle.setVisibility(View.GONE);
            }

            // Set description (genres or release date)
            if(mediaItem.isUpcoming())
            {
                if(mediaItem.getReleaseDate()!=null)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(mediaItem.getReleaseDate());
                    if(cal.get(Calendar.YEAR)<DatePickerInput.FUTURE_YEAR)
                    {
                        holder.description.setText(context.getString(R.string.will_be_available_on, DateFormat.getDateFormat(context).format(mediaItem.getReleaseDate())));
                    }
                    else
                    {
                        holder.description.setText(context.getString(R.string.will_be_available_future));
                    }
                    holder.description.setVisibility(View.VISIBLE);
                }
                else
                {
                    holder.description.setText(context.getString(R.string.will_be_available_on_unknown));
                }
            }
            else
            {
                if(!Utils.isEmpty(mediaItem.getGenres()))
                {
                    holder.description.setText(mediaItem.getGenres());
                    holder.description.setVisibility(View.VISIBLE);
                }
                else
                {
                    holder.description.setVisibility(View.GONE);
                }
            }

            // Set background
            if(mediaItem.getTimesCompleted()>0)
            {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.list_redoing_background_color));
            }
            else
            {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.list_normal_background_color));
            }
        }
    }

    /**
     * ViewHolder for this list media items
     */
    public static class TrackedElementViewHolder extends ElementAbstractViewHolder
    {
        TextView description;

        public TrackedElementViewHolder(View itemView)
        {
            super(itemView);

            // Get view components
            description = (TextView) itemView.findViewById(R.id.item_description);
        }
    }
}
