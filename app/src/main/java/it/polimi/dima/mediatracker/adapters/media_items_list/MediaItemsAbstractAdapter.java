package it.polimi.dima.mediatracker.adapters.media_items_list;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.MediaItem;

/**
 * Abstract implementation of the sectioned adapter for media items
 */
public abstract class MediaItemsAbstractAdapter extends SectionedRecyclerViewAdapter<MediaItem>
{
    private RowListener rowListener;
    private DragHandlerListener dragHandlerListener;

    private boolean searchMode = false;

    /**
     * Allows to add an options listener (each media item in the list has a menu button to display some options)
     * @param rowListener the listener to add to the adapter
     */
    public void setOnItemOptionSelectListener(final RowListener rowListener)
    {
        this.rowListener = rowListener;
    }

    /**
     * Allows to add a "drag handler" listener (each media item in the list has a handler that, when pressed, should start a drag&drop operation)
     * @param dragHandlerListener the listener to add to the adapter
     */
    public void setDragHandlerListener(final DragHandlerListener dragHandlerListener)
    {
        this.dragHandlerListener = dragHandlerListener;
    }

    /**
     * Setter. Search mode displays media items in a different way, e.g. no sections, no drag&drop handler, etc.
     * @param searchMode true if search mode is active, false otherwise
     */
    public void setSearchMode(boolean searchMode)
    {
        this.searchMode = searchMode;
        enableSectioning(!searchMode);
        enableMovement(!searchMode);
    }

    /**
     * {@inheritDoc}
     */
    abstract class MediaItemsInternalAdapter extends SectionedInternalAdapter
    {
        /**
         * {@inheritDoc}
         */
        MediaItemsInternalAdapter(List<MediaItem> mediaItems)
        {
            super(mediaItems);
        }

        /**
         * Helper to add the onClick behavior to the options menu for each item in the list
         * @param context the context
         * @param button the button to be clicked
         * @param actualItemPosition the index in the ITEMS LIST of the item
         * @param optionNameDone the resource ID of the string for "I've done this" option
         * @param optionNameDoing the resource ID of the string for "I'm doing this" option
         * @param optionNameRedo the resource ID of the string for "I could redo this" option
         */
        void setupOptionsButton(Context context, final ImageButton button, final int actualItemPosition, int optionNameDone, int optionNameDoing, int optionNameRedo)
        {
            // Get menu
            final PopupMenu popup = new PopupMenu(context, button);
            Menu menu = popup.getMenu();

            // Build menu
            int optionsContextMenu = R.menu.context_menu_media_items;
            MediaItem mediaItem = getItemsList().get(actualItemPosition);
            Resources res = context.getResources();
            if(mediaItem.isCompleted())
            {
                menu.add(Menu.NONE, R.id.media_item_options_redo, res.getInteger(R.integer.media_item_options_order_redo), optionNameRedo);
                menu.add(Menu.NONE, R.id.media_item_options_change_completion_date, res.getInteger(R.integer.media_item_options_order_change_completion_date), R.string.change_completion_date);
            }
            else if(!mediaItem.isUpcoming())
            {
                if(mediaItem.isDoingNow())
                {
                    menu.add(Menu.NONE, R.id.media_item_options_complete, res.getInteger(R.integer.media_item_options_order_complete), optionNameDone);
                }
                else if(mediaItem.isOwned())
                {
                    menu.add(Menu.NONE, R.id.media_item_options_doing, res.getInteger(R.integer.media_item_options_order_doing), optionNameDoing);
                    menu.add(Menu.NONE, R.id.media_item_options_complete, res.getInteger(R.integer.media_item_options_order_complete), optionNameDone);
                }
                else
                {
                    menu.add(Menu.NONE, R.id.media_item_options_own, res.getInteger(R.integer.media_item_options_order_own), R.string.set_as_owned);
                }
            }

            // Inflate menu
            popup.getMenuInflater().inflate(optionsContextMenu, menu);

            // Add onClick
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        @Override
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            // Call the media item listener
                            rowListener.onMediaItemOptionSelected(item, actualItemPosition);
                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }

        /**
         * Adds the touch listener to the "drag handler" icon of the given holder
         * @param itemHolder the holder
         * @param canMediaItemBeMoved true if the media item can be moved
         */
        void setupDragHandler(final ElementAbstractViewHolder itemHolder, boolean canMediaItemBeMoved)
        {
            // Remove it if we are searching or we don't have a listener
            if(!canMediaItemBeMoved || searchMode || dragHandlerListener==null)
            {
                itemHolder.handleView.setVisibility(View.GONE);
            }
            else
            {
                // Add touch listener on the icon
                itemHolder.handleView.setVisibility(View.VISIBLE);
                itemHolder.handleView.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        // Call drag listener on long tap
                        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                        {
                            dragHandlerListener.onStartDrag(itemHolder);
                        }
                        return false;
                    }
                });
            }
        }


        /**
         * Helper to setup the media item click listener
         * @param clickablePart the view that can be clicked by the user
         * @param actualItemPosition the index in the MEDIA ITEMS LIST of the media item
         */
        void setupClickablePart(View clickablePart, final int actualItemPosition)
        {
            // Add onClick
            clickablePart.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Call the media item listener
                    rowListener.onMediaItemClicked(actualItemPosition);
                }
            });
        }
    }

    /**
     * Abstract ViewHolder for the private adapter that describes an item
     */
    public abstract static class ElementAbstractViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout clickablePart;
        ImageButton optionsButton;
        TextView title;
        TextView subtitle;
        ImageView handleView;

        public ElementAbstractViewHolder(View itemView)
        {
            super(itemView);

            // Get view components
            clickablePart = (LinearLayout) itemView.findViewById(R.id.item_clickable_part);
            optionsButton = (ImageButton) itemView.findViewById(R.id.item_options_button);
            title = (TextView) itemView.findViewById(R.id.item_title);
            subtitle = (TextView) itemView.findViewById(R.id.item_subtitle);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
        }
    }

    /**
     * Callback used by the adapter to inform of clicks or options select on the media items in the list
     */
    public interface RowListener
    {
        /**
         * Called when a media item option (each media item in the list has a menu button to display some options) is selected
         * @param menuItem the selected option
         * @param position the selected media item position
         */
        void onMediaItemOptionSelected(MenuItem menuItem, int position);

        /**
         * Called when a media item is clicked
         * @param position the position of the media item
         */
        void onMediaItemClicked(int position);
    }

    /**
     * Interface for the "drag handler" icon for each list item
     */
    public interface DragHandlerListener
    {
        /**
         * Called when a view is requesting a start of a drag
         * @param viewHolder the holder of the view to drag
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
