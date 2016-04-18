package it.polimi.dima.mediatracker.adapters.media_items_list;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.MediaItem;

/**
 * Abstract adapter that allows to display a list of media items divided into sections.
 * Actually, this class is just a "wrapper" that allows to interface with the real (private)
 * adapter. Usage is very similar to a normal adapter, the only difference is the method
 * {@link SectionedAbstractAdapterWrapper#setAdapterToRecyclerView(RecyclerView)}
 *
 * The wrapper is interfaced using the positions of the media items list passed as parameter but
 * internally it uses a list that contains both media items and sections
 */
public abstract class SectionedAbstractAdapterWrapper
{
    public static final int VIEW_TYPE_SECTION = 1;
    public static final int VIEW_TYPE_MEDIA_ITEM = 2;

    private boolean searchMode = false;

    private SectionedAbstractAdapter adapter;

    private Integer lastMoveTargetListPosition = null;

    private View emptyView;

    /**
     * Helper that needs to be called by the subclass to set the actual (private) adapter
     * @param adapter the SectionedAbstractAdapter implementation of the subclass
     */
    void setAdapterFromSubclass(SectionedAbstractAdapter adapter)
    {
        this.adapter = adapter;
    }

    /**
     * Allows to remove the media item at the given position and notify the underlying adapter of the removal
     * @param position the media item position to remove
     */
    public void removeItemAndNotify(int position)
    {
        // Remove mediaItem from items list
        MediaItem removed = adapter.mediaItemList.remove(position);

        // Also remove it from allMediaItems list
        int index = adapter.allMediaItems.indexOf(removed);
        if(index>=0) adapter.allMediaItems.remove(index);

        // Get elements list position
        int listPosition = adapter.getListPositionFromItemIndex(position);

        // Remove mediaItem from the elements list and notify to the adapter
        adapter.elements.remove(listPosition);
        adapter.notifyItemRemoved(listPosition);

        // Also remove its section if this was the only element in it
        boolean alsoRemovedSection = false;
        if(listPosition > 0)
        {
            if(adapter.elements.get(listPosition - 1).isSection)
            {
                if(listPosition == adapter.elements.size() || adapter.elements.get(listPosition).isSection)
                {
                    for (int i=listPosition; i<adapter.elements.size(); i++)
                    {
                        if(!adapter.elements.get(i).isSection) adapter.elements.get(i).numberOfSectionsBefore--;
                    }

                    adapter.elements.remove(listPosition - 1);
                    adapter.notifyItemRemoved(listPosition - 1);
                    alsoRemovedSection = true;
                }
            }
        }

        // Update all items after the removed ones (need to update on click positions)
        if(alsoRemovedSection) listPosition--;
        int elementsChanged = adapter.elements.size()-listPosition;
        if(elementsChanged>0)
        {
            adapter.notifyItemRangeChanged(listPosition, elementsChanged);
        }

        // Show/hide the empty view
        showEmptyViewIfAdapterIsEmpty();
    }

    /**
     * Allows to move the media item at the given "from position" (section "from section") to the "to position" (section "to section")
     * NOTE: if fromPosition=toPosition but fromSection!=toSection the media item is not moved in the array but its section changes (happens when the
     * media item is the last before a section and it gets moved to the first position of the next section)
     * @param fromPosition the starting position
     * @param toPosition the final position
     * @param fromSection the starting section
     * @param toSection the final section
     */
    public void moveItemAndNotify(int fromPosition, int toPosition, AdapterSection fromSection, AdapterSection toSection)
    {
        // If we actually have a position change (method might have been triggered only by a section change)
        if(fromPosition!=toPosition)
        {
            // Swap all items between fromPosition and toPosition in the items array
            swapHelper(adapter.mediaItemList, fromPosition, toPosition);

            // Do the same in allMediaItems array
            int index1 = adapter.allMediaItems.indexOf(adapter.mediaItemList.get(fromPosition));
            int index2 = adapter.allMediaItems.indexOf(adapter.mediaItemList.get(toPosition));
            if(index1>=0 && index2>=0) swapHelper(adapter.allMediaItems, index1, index2);
        }

        // Get elements list positions
        int fromListPosition = adapter.getListPositionFromItemIndex(fromPosition);
        int toListPosition = lastMoveTargetListPosition;

        // Swap all items between fromListPosition and toListPosition in the elements list
        swapHelper(adapter.elements, fromListPosition, toListPosition);

        // If the section changed, we need to update the "numberOfSectionsBefore" parameter of the moved item
        if(!fromSection.equals(toSection))
        {
            int sectionsBeforeToSet = 0;
            for(int i=toListPosition-1; i>=0; i--)
            {
                if(adapter.elements.get(i).isSection) sectionsBeforeToSet++;
            }
            adapter.elements.get(toListPosition).numberOfSectionsBefore = sectionsBeforeToSet;
        }

        // Notify movement in the adapter
        adapter.notifyItemMoved(fromListPosition, toListPosition);
    }

    /**
     * Helper that swaps all items in the given list between the two positions
     * @param list the list where the operation is performed
     * @param fromPosition the starting position
     * @param toPosition the ending position
     */
    private void swapHelper(List<?> list, int fromPosition, int toPosition)
    {
        // If we need to go downwards...
        if(fromPosition < toPosition)
        {
            for(int i=fromPosition; i<toPosition; i++)
            {
                Collections.swap(list, i, i + 1);
            }
        }

        // If we need to go upwards...
        else
        {
            for(int i = fromPosition; i > toPosition; i--)
            {
                Collections.swap(list, i, i - 1);
            }
        }
    }

    /**
     * Reloads the whole list of media items and notifies the underlying adapter of the data set change
     * @param mediaItems the new media items
     */
    public void setItemsAndNotifyDataSetChanged(List<MediaItem> mediaItems)
    {
        // Save mediaItems
        adapter.allMediaItems = new ArrayList<>(mediaItems);
        adapter.mediaItemList = new ArrayList<>(mediaItems);

        // Build the elements list
        adapter.setupListElements();

        // Reload list
        adapter.notifyDataSetChanged();

        // Show/hide the empty view
        showEmptyViewIfAdapterIsEmpty();
    }

    /**
     * Adds the given media items at the end of the current list and notifies the underlying adapter of the data set change
     * @param newMediaItems the media items to be added
     */
    public void addMediaItemsAtTheEndAndNotify(List<MediaItem> newMediaItems)
    {
        int oldSize = adapter.mediaItemList.size();

        // Add media items
        adapter.allMediaItems.addAll(newMediaItems);
        adapter.mediaItemList.addAll(newMediaItems);

        // Update the elements list
        adapter.addToListElements(newMediaItems);

        // Notify
        notifyItemRangeInserted(oldSize, newMediaItems.size());

        // Show/hide the empty view
        showEmptyViewIfAdapterIsEmpty();
    }

    /**
     * Informs the underlying adapter that the media item at the given position has changed
     * @param position the changed media item position
     */
    public void notifyItemChanged(int position)
    {
        // Simply get the elements list position and notify the change to the adapter
        int listPosition = adapter.getListPositionFromItemIndex(position);
        adapter.notifyItemChanged(listPosition);
    }

    /**
     * Informs the underlying adapter that "itemCount" media items have changed starting from "positionStart"
     * @param positionStart starting position
     * @param itemCount number of media items that have changed
     */
    public void notifyItemRangeChanged(int positionStart, int itemCount)
    {
        // Get elements list position
        int listPositionStart = adapter.getListPositionFromItemIndex(positionStart);

        // Increase the mediaItem count adding the number of sections in the interval to update
        for(int i=listPositionStart+1, totalItems = itemCount, counter = 1; i<adapter.elements.size(); i++)
        {
            if(adapter.elements.get(i).isSection) itemCount++;
            else counter++;

            if(totalItems==counter) break;
        }

        // Notify changes to the adapter
        adapter.notifyItemRangeChanged(listPositionStart, itemCount);
    }

    /**
     * Informs the underlying adapter that "itemCount" media items have been added starting from "positionStart"
     * @param positionStart starting position
     * @param itemCount number of media items that have been inserted
     */
    public void notifyItemRangeInserted(int positionStart, int itemCount)
    {
        // Get elements list position
        int listPositionStart = adapter.getListPositionFromItemIndex(positionStart);

        // Increase the mediaItem count adding the number of sections in the interval to insert
        for(int i=listPositionStart+1, totalItems = itemCount, counter = 1; i<adapter.elements.size(); i++)
        {
            if(adapter.elements.get(i).isSection) itemCount++;
            else counter++;

            if(totalItems==counter) break;
        }

        // Notify insertions to the adapter
        adapter.notifyItemRangeInserted(listPositionStart, itemCount);
    }

    /**
     * Getter
     * @param position the media item position
     * @return the media item at the given position
     */
    public MediaItem get(int position)
    {
        return adapter.mediaItemList.get(position);
    }

    /**
     * @see RecyclerView.Adapter#getItemCount()
     */
    public int getItemCount()
    {
        return adapter.mediaItemList.size();
    }

    /**
     * Allows to set the underlying adapter to the RecyclerView
     * @param recyclerView the RecyclerView that needs the adapter
     */
    public void setAdapterToRecyclerView(RecyclerView recyclerView)
    {
        recyclerView.setAdapter(adapter);
    }

    /**
     * Allows to add an options listener (each media item in the list has a menu button to display some options)
     * @param listElementListener the listener to add to the adapter
     */
    public void setOnItemOptionSelectListener(final ListElementListener listElementListener)
    {
        adapter.listElementListener = listElementListener;
    }

    /**
    * Allows to add a "drag handler" listener (each media item in the list has a handler that, when pressed, should start a drag&drop operation)
    * @param dragHandlerListener the listener to add to the adapter
    */
    public void setDragHandlerListener(final DragHandlerListener dragHandlerListener)
    {
        adapter.dragHandlerListener = dragHandlerListener;
    }

    /**
     * Setter
     * @param emptyView the view to be displayed if the adapter is empty
     */
    public void setEmptyView(View emptyView)
    {
        // Set field
        this.emptyView = emptyView;

        // Also call the method for the first time
        showEmptyViewIfAdapterIsEmpty();
    }

    /**
     * Helper to show/hide the empty view if the adapter is empty
     */
    private void showEmptyViewIfAdapterIsEmpty()
    {
        if(emptyView!=null)
        {
            if(adapter.getItemCount()==0) emptyView.setVisibility(View.VISIBLE);
            else emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Setter. Search mode displays media items in a different way, e.g. no sections, no drag&drop handler, etc.
     * @param searchMode true if search mode is active, false otherwise
     */
    public void setSearchMode(boolean searchMode)
    {
        this.searchMode = searchMode;
    }

    /**
     * The underlying private adapter used by the wrapper. It's a RecyclerView adapter that takes a list of ListElement objects (sections or media items)
     */
    abstract class SectionedAbstractAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        private Context context;

        private List<ListElement> elements;
        private List<MediaItem> mediaItemList;
        private List<MediaItem> allMediaItems;

        private SectionNameCallback sectionNameCallback;
        private ListElementListener listElementListener;
        private DragHandlerListener dragHandlerListener;

        private AdapterSection currentLastSection = null;
        private int currentSectionCount = 0;

        /**
         * Constructor. Builds the list of ListElement objects from the given media items list
         * @param mediaItemList the list of media items
         * @param sectionNameCallback the callback to get the section for each media item
         */
        SectionedAbstractAdapter(List<MediaItem> mediaItemList, SectionNameCallback sectionNameCallback)
        {
            this.mediaItemList = new ArrayList<>(mediaItemList);
            this.allMediaItems = new ArrayList<>(mediaItemList);

            this.sectionNameCallback = sectionNameCallback;

            setupListElements();
        }

        /**
         * Getter
         * @return the current list of media items
         */
        List<MediaItem> getMediaItemList()
        {
            return mediaItemList;
        }

        /**
         * Builds the list of ListElement objects (sections and media items) from the media items list
         */
        public void setupListElements()
        {
            // Setting is like adding starting from an empty list
            elements = new ArrayList<>();
            currentLastSection = null;
            currentSectionCount = 0;
            addToListElements(getMediaItemList());
        }

        /**
         * Adds elements to the list of ListElement objects (sections and media items) from the given media items list
         * @param newMediaItems the media items to add
         */
        public void addToListElements(List<MediaItem> newMediaItems)
        {
            // Loop all items
            AdapterSection previousElementSection = currentLastSection;
            for(int i = 0; i < newMediaItems.size(); i++)
            {
                // If we need to group them by sections...
                if(sectionNameCallback!=null && !searchMode)
                {
                    // Get media item section
                    currentLastSection = sectionNameCallback.onSectionNameRequest(newMediaItems.get(i));

                    // If needed, add the section to the elements list
                    if(i == 0 || !currentLastSection.equals(previousElementSection))
                    {
                        previousElementSection = currentLastSection;
                        elements.add(new ListElement(currentLastSection));
                        currentSectionCount++;
                    }
                }

                // In any case add the media item to the list
                elements.add(new ListElement(newMediaItems.get(i), currentSectionCount));
            }
        }


        /**
         * {@inheritDoc}
         *
         * Size is all elements (media items + sections)
         */
        @Override
        public int getItemCount()
        {
            return elements.size();
        }

        /**
         * {@inheritDoc}
         *
         * Two types: section and media item
         */
        @Override
        public int getItemViewType(int position)
        {
            if (elements.get(position).isSection) return VIEW_TYPE_SECTION;
            else return VIEW_TYPE_MEDIA_ITEM;
        }

        /**
         * {@inheritDoc}
         *
         * Manages the ViewHolder for sections and calls subclass method
         * {@link SectionedAbstractAdapterWrapper.SectionedAbstractAdapter#onCreateElementViewHolder(ViewGroup, int)}
         * to get the view holder for the actual media items
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // Get context
            context = parent.getContext();

            // If it's an element, delegate ViewHolder creation to the implementations
            if (viewType == VIEW_TYPE_MEDIA_ITEM)
            {
                return onCreateElementViewHolder(parent, viewType);
            }

            // If it's a section, create a SectionViewHolder
            else
            {
                // Inflate view
                View itemView = LayoutInflater.from(context).inflate(R.layout.list_section_title, parent, false);

                // Create ViewHolder
                return new SectionViewHolder(itemView);
            }
        }

        /**
         * Implemented by subclasses to provide a ViewHolder for the actual media items
         *
         * @see SectionedAbstractAdapterWrapper.SectionedAbstractAdapter#onCreateViewHolder(ViewGroup, int)
         */
        protected abstract RecyclerView.ViewHolder onCreateElementViewHolder(ViewGroup parent, int viewType);

        /**
         * {@inheritDoc}
         *
         * Manages the ViewHolder for sections and calls subclass method
         * {@link SectionedAbstractAdapterWrapper.SectionedAbstractAdapter#onBindElementViewHolder(RecyclerView.ViewHolder, int)}
         * to bind the view holder for the actual media items
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder itemHolder, int position)
        {
            // If it's an element, let the implementations take care of the binding
            if(itemHolder.getItemViewType() == VIEW_TYPE_MEDIA_ITEM)
            {
                int actualItemPosition = getMediaItemIndexFromListPosition(position);
                onBindElementViewHolder(itemHolder, actualItemPosition);
            }

            // If it's a section, set the text value
            else
            {
                // Get data
                SectionViewHolder holder = (SectionViewHolder) itemHolder;
                String sectionName = elements.get(position).section.getSectionName();

                // Set title
                holder.title.setText(sectionName);
            }
        }

        /**
         * Implemented by subclasses to bind the ViewHolder of the actual media items
         *
         * @see SectionedAbstractAdapterWrapper.SectionedAbstractAdapter#onBindViewHolder(RecyclerView.ViewHolder, int)
         */
        protected abstract void onBindElementViewHolder(RecyclerView.ViewHolder itemHolder, int actualItemPosition);

        /**
         * Translation between an ELEMENTS LIST position to the corresponding MEDIA ITEMS LIST index
         * @param listPosition elements list position
         * @return index in the original media items list (passed as parameter to the wrapper)
         */
        private int getMediaItemIndexFromListPosition(int listPosition)
        {
            // If the element in the given position is a section...
            if(adapter.elements.get(listPosition).isSection)
            {
                // Get first mediaItem upwards and return its index + 1
                for(int i=listPosition; i>=0; i--)
                {
                    if(!adapter.elements.get(i).isSection) return getMediaItemIndexFromListPosition(i) + 1;
                }

                // If no mediaItem was found, it means that the index is 0
                return 0;
            }

            // Otherwise simply return the corresponding mediaItem
            else
            {
                return listPosition - elements.get(listPosition).numberOfSectionsBefore;
            }
        }

        /**
         * Translation between a MEDIA ITEMS LIST index to the corresponding ELEMENTS LIST position
         * @param itemIndex index in the original media items list (passed as parameter to the wrapper)
         * @return elements list position
         */
        private int getListPositionFromItemIndex(int itemIndex)
        {
            for(int i=itemIndex; i<elements.size(); i++)
            {
                if(!elements.get(i).isSection && i - elements.get(i).numberOfSectionsBefore==itemIndex)
                {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Helper to add the onClick behavior to the options menu for each media item in the list
         * @param context the context
         * @param button the button to be clicked
         * @param actualItemPosition the index in the MEDIA ITEMS LIST of the media item
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
            MediaItem mediaItem = mediaItemList.get(actualItemPosition);
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
                            listElementListener.onMediaItemOptionSelected(item, actualItemPosition);
                            return true;
                        }
                    });
                    popup.show();
                }
            });
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
                    listElementListener.onMediaItemClicked(actualItemPosition);
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
         * The element in the list of the private adapter: can be a section or a media item
         */
        private class ListElement
        {
            boolean isSection;

            MediaItem mediaItem;
            int numberOfSectionsBefore;

            AdapterSection section;

            /**
             * Constructor for media items
             * @param mediaItem the linked media item
             * @param numberOfSectionsBefore the number of sections created before this media item
             */
            public ListElement(MediaItem mediaItem, int numberOfSectionsBefore)
            {
                this.isSection = false;
                this.mediaItem = mediaItem;
                this.numberOfSectionsBefore = numberOfSectionsBefore;
                this.section = null;
            }

            /**
             * Constructor for sections
             * @param section the linked section
             */
            public ListElement(AdapterSection section)
            {
                this.isSection = true;
                this.mediaItem = null;
                this.numberOfSectionsBefore = 0;
                this.section = section;
            }

            /**
             * {@inheritDoc}
             */
            public boolean equals(Object o)
            {
                if(o==null || !(o instanceof ListElement)) return false;

                ListElement other = (ListElement) o;

                if(other.isSection && isSection) return section.equals(other.section);
                else return !other.isSection && !isSection && mediaItem.equals(other.mediaItem);
            }
        }
    }

    /**
     * Abstract ViewHolder for the private adapter that describes a media item
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
     * ViewHolder for the private adapter that describes a section
     */
    public static class SectionViewHolder extends RecyclerView.ViewHolder
    {
        TextView title;

        public SectionViewHolder(View itemView)
        {
            super(itemView);

            // Get view components
            title = (TextView) itemView.findViewById(R.id.list_section_title);
        }
    }

    /**
     * Callback used by the adapter to get the section of a given media item
     */
    public interface SectionNameCallback
    {
        /**
         * Getter
         * @param mediaItem a media item
         * @return its section
         */
        AdapterSection onSectionNameRequest(MediaItem mediaItem);
    }

    /**
     * Callback used by the adapter to inform of clicks or options select on the media items in the list
     */
    public interface ListElementListener
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
     * TouchHelper Callback for the adapter that allows to manage swiping and drag&drop on the list
     */
    public abstract class SectionedAdapterTouchHelperCallback extends ItemTouchHelper.Callback
    {
        private Integer dragAndDropStartItemPosition = null;
        private Integer dragAndDropEndItemPosition = null;

        private AdapterSection dragAndDropStartSection = null;
        private AdapterSection dragAndDropEndSection = null;

        /**
         * {@inheritDoc}
         *
         * Returns no allowed movement for section elements, calls the implementation
         * {@link SectionedAbstractAdapterWrapper.SectionedAdapterTouchHelperCallback#getMediaItemMovementFlags(int, RecyclerView, RecyclerView.ViewHolder)}
         * to the the allowed movement for the actual media items
         */
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
        {
            // No swiping/moving for sections or if we are searching
            if(viewHolder.getItemViewType() == VIEW_TYPE_SECTION || searchMode)
            {
                return 0;
            }

            // Call implementation method to get the flags for the items
            else
            {
                int listPosition = viewHolder.getAdapterPosition();
                return getMediaItemMovementFlags(adapter.getMediaItemIndexFromListPosition(listPosition), recyclerView, viewHolder);
            }
        }

        /**
         * {@inheritDoc}
         *
         * Translates from the ELEMENTS LIST positions to the MEDIA ITEMS LIST indices and then calls the implementation
         * {@link SectionedAbstractAdapterWrapper.SectionedAdapterTouchHelperCallback#onMediaItemMove(int, int, AdapterSection, AdapterSection, RecyclerView, RecyclerView.ViewHolder, RecyclerView.ViewHolder)}
         * to manage the movement of the actual media items
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
        {
            int fromListPosition = viewHolder.getAdapterPosition();
            int toListPosition = target.getAdapterPosition();

            // Get media item indices from elements list positions
            int fromIndex = adapter.getMediaItemIndexFromListPosition(fromListPosition);
            int toIndex = adapter.getMediaItemIndexFromListPosition(toListPosition);

            // Fix toIndex if we are over a section and we are going downwards
            if(adapter.elements.get(toListPosition).isSection && fromListPosition<toListPosition)
            {
                toIndex--;
            }

            // Get "from" section
            AdapterSection fromSection = null;
            for(int i=fromListPosition-1; i>=0; i--)
            {
                if(adapter.elements.get(i).isSection)
                {
                    fromSection = adapter.elements.get(i).section;
                    break;
                }
            }

            // Get "to" section (if we are going downwards we must also consider the element itself, otherwise we do not)
            AdapterSection toSection = null;
            if(fromListPosition<toListPosition)
            {
                for(int i=toListPosition; i>=0; i--)
                {
                    if(adapter.elements.get(i).isSection)
                    {
                        toSection = adapter.elements.get(i).section;
                        break;
                    }
                }
            }
            else if(fromListPosition>toListPosition)
            {
                for(int i=toListPosition-1; i>=0; i--)
                {
                    if(adapter.elements.get(i).isSection)
                    {
                        toSection = adapter.elements.get(i).section;
                        break;
                    }
                }
            }

            // Do nothing if we lack data or if both indices and sections are the same
            if(fromSection==null || toSection==null || fromIndex==-1 || toIndex==-1 || (fromIndex==toIndex && fromSection.equals(toSection)))
            {
                return false;
            }

            // Save list position
            lastMoveTargetListPosition = toListPosition;

            // For the WHOLE Drag&Drop action, save FIRST from position/section and CURRENT to position/section
            if(dragAndDropStartItemPosition == null)
            {
                dragAndDropStartItemPosition = fromIndex;
                dragAndDropStartSection = fromSection;
            }
            dragAndDropEndItemPosition = toIndex;
            dragAndDropEndSection = toSection;

            // Call implementation method
            return onMediaItemMove(fromIndex, toIndex, fromSection, toSection, recyclerView, viewHolder, target);
        }

        /**
         * {@inheritDoc}
         *
         * Translates from the ELEMENTS LIST positions to the MEDIA ITEMS LIST indices and then calls the implementation
         * {@link SectionedAbstractAdapterWrapper.SectionedAdapterTouchHelperCallback#onMediaItemSwiped(int, RecyclerView.ViewHolder, int)}
         * to manage the swiping of the actual media items
         */
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
        {
            int listPosition = viewHolder.getAdapterPosition();

            // Call implementation method
            onMediaItemSwiped(adapter.getMediaItemIndexFromListPosition(listPosition), viewHolder, direction);
        }

        /**
         * {@inheritDoc}
         *
         * Allows to recognize a drop after a drag&drop operation, calling the implementation
         * {@link SectionedAbstractAdapterWrapper.SectionedAdapterTouchHelperCallback#onMediaItemDropped(int, int, AdapterSection, AdapterSection)}
         * to manage it
         */
        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
        {
            super.clearView(recyclerView, viewHolder);

            // If we previously set the drag&drop fields, this is a drop
            if(dragAndDropStartItemPosition!=null && dragAndDropEndItemPosition!=null)
            {
                // Call the implementation method
                onMediaItemDropped(dragAndDropStartItemPosition, dragAndDropEndItemPosition, dragAndDropStartSection, dragAndDropEndSection);
            }

            // Clear saved drag&drop fields
            dragAndDropStartItemPosition = null;
            dragAndDropEndItemPosition = null;
            dragAndDropStartSection = null;
            dragAndDropEndSection = null;
        }

        /**
         * Works just like {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#getMovementFlags(RecyclerView, RecyclerView.ViewHolder)}
         * but directly provides the media item position (do NOT use getAdapterPosition() on the ViewHolder)
         */
        public abstract int getMediaItemMovementFlags(int mediaItemPosition, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

        /**
         * Works just like {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#onSwiped(RecyclerView.ViewHolder, int)}
         * but directly provides the media item position (do NOT use getAdapterPosition() on the ViewHolder)
         */
        public abstract void onMediaItemSwiped(int mediaItemPosition, RecyclerView.ViewHolder viewHolder, int direction);

        /**
         * Works just like {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#onMove(RecyclerView, RecyclerView.ViewHolder, RecyclerView.ViewHolder)}
         * but directly provides the media items positions (do NOT use getAdapterPosition() on the ViewHolders).
         * It also provides the starting and ending sections
         * NOTE: if mediaItemPosition=targetMediaItemPosition but mediaItemSection!=targetMediaItemSection the media item has not been moved in the list
         * but its section changes (happens when the media item is the last before a section and it gets moved to the first position of the next section)
         */
        public abstract boolean onMediaItemMove(int mediaItemPosition, int targetMediaItemPosition, AdapterSection mediaItemSection, AdapterSection targetMediaItemSection, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);

        /**
         * Similar to {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#onMove(RecyclerView, RecyclerView.ViewHolder, RecyclerView.ViewHolder)}
         * but fired only after the media item is actually dropped after a drag&drop operation
         * NOTE: if mediaItemPosition=targetMediaItemPosition but mediaItemSection!=targetMediaItemSection the media item has not been moved in the list
         * but its section changes (happens when the media item is the last before a section and it gets moved to the first position of the next section)
         * @param mediaItemPosition starting position
         * @param targetMediaItemPosition ending position
         * @param mediaItemSection starting section
         * @param targetMediaItemSection ending section
         */
        public abstract void onMediaItemDropped(int mediaItemPosition, int targetMediaItemPosition, AdapterSection mediaItemSection, AdapterSection targetMediaItemSection);
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
