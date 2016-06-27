package it.polimi.dima.mediatracker.adapters.media_items_list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.Section;
import it.polimi.dima.mediatracker.model.Sectioned;

/**
 * Abstract adapter that allows to display a list of items divided into sections.
 * Actually, this class is just a "wrapper" that allows to interface with the real (private)
 * adapter. Usage is very similar to a normal adapter, the only difference is the method
 * {@link SectionedRecyclerViewAdapter#setAdapterToRecyclerView(RecyclerView)}
 *
 * The wrapper is interfaced using the positions of the items list passed as parameter but
 * internally it uses a list that contains both items and sections
 */
public abstract class SectionedRecyclerViewAdapter<T extends Sectioned>
{
    public static final int VIEW_TYPE_SECTION = 1;
    public static final int VIEW_TYPE_ITEM = 2;

    private boolean enableSectioning = true;
    private boolean enableMovement = true;

    private SectionedInternalAdapter adapter;

    private Integer lastMoveTargetListPosition = null;

    private View emptyView;

    /**
     * Helper that needs to be called by the subclass to set the actual (private) adapter
     * @param adapter the {@link SectionedInternalAdapter} implementation of the subclass
     */
    void setAdapterFromSubclass(SectionedInternalAdapter adapter)
    {
        this.adapter = adapter;
    }

    /**
     * Helper for subclasses
     * @return the actual (private) adapter
     */
    SectionedInternalAdapter getAdapter()
    {
        return adapter;
    }

    /**
     * Allows to enable/disable the sections
     * @param enable true if sections are to be displayed
     */
    public void enableSectioning(boolean enable)
    {
        this.enableSectioning = enable;
    }

    /**
     * Allows to enable/disable movement
     * @param enable true if movement is allowed
     */
    public void enableMovement(boolean enable)
    {
        this.enableMovement = enable;
    }

    /**
     * Allows to remove the item at the given position and notify the underlying adapter of the removal
     * @param position the item position to remove
     */
    public void removeItemAndNotify(int position)
    {
        // Remove item from items list
        T removed = adapter.itemsList.remove(position);

        // Also remove it from allItems list
        int index = adapter.allItems.indexOf(removed);
        if(index>=0) adapter.allItems.remove(index);

        // Get elements list position
        int listPosition = adapter.getListPositionFromItemIndex(position);

        // Remove item from the elements list and notify to the adapter
        adapter.elements.remove(listPosition);
        adapter.notifyItemRemoved(listPosition);

        // Also remove its section if this was the only item in it
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
        int changedElements = adapter.elements.size()-listPosition;
        if(changedElements>0)
        {
            adapter.notifyItemRangeChanged(listPosition, changedElements);
        }

        // Show/hide the empty view
        showEmptyViewIfAdapterIsEmpty();
    }

    /**
     * Allows to move the item at the given "from position" (section "from section") to the "to position" (section "to section")
     * NOTE: if fromPosition=toPosition but fromSection!=toSection the item is not moved in the array but its section changes (happens when the
     * item is the last before a section and it gets moved to the first position of the next section)
     * @param fromPosition the starting position
     * @param toPosition the final position
     * @param fromSection the starting section
     * @param toSection the final section
     */
    public void moveItemAndNotify(int fromPosition, int toPosition, Section fromSection, Section toSection)
    {
        // If we actually have a position change (method might have been triggered only by a section change)
        if(fromPosition!=toPosition)
        {
            // Swap all items between fromPosition and toPosition in the items array
            swapHelper(adapter.itemsList, fromPosition, toPosition);

            // Do the same in allItems array
            int index1 = adapter.allItems.indexOf(adapter.itemsList.get(fromPosition));
            int index2 = adapter.allItems.indexOf(adapter.itemsList.get(toPosition));
            if(index1>=0 && index2>=0) swapHelper(adapter.allItems, index1, index2);
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
     * Reloads the whole list of items and notifies the underlying adapter of the data set change
     * @param items the new items
     */
    public void setItemsAndNotifyDataSetChanged(List<T> items)
    {
        // Save items
        adapter.allItems = new ArrayList<>(items);
        adapter.itemsList = new ArrayList<>(items);

        // Build the elements list
        adapter.setupListElements();

        // Reload list
        adapter.notifyDataSetChanged();

        // Show/hide the empty view
        showEmptyViewIfAdapterIsEmpty();
    }

    /**
     * Adds the given items at the end of the current list and notifies the underlying adapter of the data set change
     * @param newItems the items to be added
     */
    public void addItemsAtTheEndAndNotify(List<T> newItems)
    {
        int oldSize = adapter.itemsList.size();

        // Add items
        adapter.allItems.addAll(newItems);
        adapter.itemsList.addAll(newItems);

        // Update the elements list
        adapter.addToListElements(newItems);

        // Notify
        notifyItemRangeInserted(oldSize, newItems.size());

        // Show/hide the empty view
        showEmptyViewIfAdapterIsEmpty();
    }

    /**
     * Informs the underlying adapter that the item at the given position has changed
     * @param position the changed item position
     */
    public void notifyItemChanged(int position)
    {
        // Simply get the elements list position and notify the change to the adapter
        int listPosition = adapter.getListPositionFromItemIndex(position);
        adapter.notifyItemChanged(listPosition);
    }

    /**
     * Informs the underlying adapter that "itemCount" items have changed starting from "positionStart"
     * @param positionStart starting position
     * @param itemCount number of items that have changed
     */
    public void notifyItemRangeChanged(int positionStart, int itemCount)
    {
        // Get elements list position
        int listPositionStart = adapter.getListPositionFromItemIndex(positionStart);

        // Increase the item count adding the number of sections in the interval to update
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
     * Informs the underlying adapter that "itemCount" items have been added starting from "positionStart"
     * @param positionStart starting position
     * @param itemCount number of items that have been inserted
     */
    public void notifyItemRangeInserted(int positionStart, int itemCount)
    {
        // Get elements list position
        int listPositionStart = adapter.getListPositionFromItemIndex(positionStart);

        // Increase the item count adding the number of sections in the interval to insert
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
     * @param position the item position
     * @return the item at the given position
     */
    public T get(int position)
    {
        return adapter.itemsList.get(position);
    }

    /**
     * @see RecyclerView.Adapter#getItemCount()
     */
    public int getItemCount()
    {
        return adapter.itemsList.size();
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
     * The underlying private adapter used by the wrapper. It's a RecyclerView adapter that takes a list of ListElement objects (sections or items)
     */
    abstract class SectionedInternalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        private Context context;

        private List<ListElement> elements;
        private List<T> itemsList;
        private List<T> allItems;

        private Section currentLastSection = null;
        private int currentSectionCount = 0;

        /**
         * Constructor. Builds the list of ListElement objects from the given items list
         * @param itemsList the list of items
         */
        SectionedInternalAdapter(List<T> itemsList)
        {
            this.itemsList = new ArrayList<>(itemsList);
            this.allItems = new ArrayList<>(itemsList);

            setupListElements();
        }

        /**
         * Getter
         * @return the current list of items
         */
        List<T> getItemsList()
        {
            return itemsList;
        }

        /**
         * Builds the list of ListElement objects (sections and items) from the items list
         */
        public void setupListElements()
        {
            // Setting is like adding starting from an empty list
            elements = new ArrayList<>();
            currentLastSection = null;
            currentSectionCount = 0;
            addToListElements(getItemsList());
        }

        /**
         * Adds items to the list of ListElement objects (sections and items) from the given items list
         * @param newItems the items to add
         */
        public void addToListElements(List<T> newItems)
        {
            // Loop all items
            Section previousItemSection = currentLastSection;
            for(int i = 0; i < newItems.size(); i++)
            {
                // If we need to group them by sections...
                if(enableSectioning)
                {
                    // Get item section
                    currentLastSection = newItems.get(i).getSection();

                    // If needed, add the section to the elements list
                    if(previousItemSection==null || !currentLastSection.equals(previousItemSection))
                    {
                        previousItemSection = currentLastSection;
                        elements.add(new ListElement(currentLastSection));
                        currentSectionCount++;
                    }
                }

                // In any case add the item to the list
                elements.add(new ListElement<>(newItems.get(i), currentSectionCount));
            }
        }

        /**
         * {@inheritDoc}
         *
         * Size is all elements (items + sections)
         */
        @Override
        public int getItemCount()
        {
            return elements.size();
        }

        /**
         * {@inheritDoc}
         *
         * Two types: section and item
         */
        @Override
        public int getItemViewType(int position)
        {
            if (elements.get(position).isSection) return VIEW_TYPE_SECTION;
            else return VIEW_TYPE_ITEM;
        }

        /**
         * {@inheritDoc}
         *
         * Manages the ViewHolder for sections and calls subclass method
         * {@link SectionedInternalAdapter#onCreateItemViewHolder(ViewGroup, int)}
         * to get the view holder for the actual items
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // Get context
            context = parent.getContext();

            // If it's an item, delegate ViewHolder creation to the implementations
            if(viewType==VIEW_TYPE_ITEM)
            {
                return onCreateItemViewHolder(parent, viewType);
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
         * Implemented by subclasses to provide a ViewHolder for the actual items
         *
         * @see SectionedInternalAdapter#onCreateViewHolder(ViewGroup, int)
         */
        protected abstract RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

        /**
         * {@inheritDoc}
         *
         * Manages the ViewHolder for sections and calls subclass method
         * {@link SectionedInternalAdapter#onBindItemViewHolder(RecyclerView.ViewHolder, int)}
         * to bind the view holder for the actual items
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder itemHolder, int position)
        {
            // If it's an item, let the implementations take care of the binding
            if(itemHolder.getItemViewType() ==VIEW_TYPE_ITEM)
            {
                int actualItemPosition = getItemIndexFromListPosition(position);
                onBindItemViewHolder(itemHolder, actualItemPosition);
            }

            // If it's a section, set the text value
            else
            {
                // Get data
                SectionViewHolder holder = (SectionViewHolder) itemHolder;
                String sectionName = elements.get(position).section.getSectionName(context);

                // Set title
                holder.title.setText(sectionName);
            }
        }

        /**
         * Implemented by subclasses to bind the ViewHolder of the actual items
         *
         * @see SectionedInternalAdapter#onBindViewHolder(RecyclerView.ViewHolder, int)
         */
        protected abstract void onBindItemViewHolder(RecyclerView.ViewHolder itemHolder, int actualItemPosition);

        /**
         * Translation between an ELEMENTS LIST position to the corresponding ITEMS LIST index
         * @param listPosition elements list position
         * @return index in the original items list (passed as parameter to the wrapper)
         */
        private int getItemIndexFromListPosition(int listPosition)
        {
            // If the element in the given position is a section...
            if(adapter.elements.get(listPosition).isSection)
            {
                // Get first item upwards and return its index + 1
                for(int i=listPosition; i>=0; i--)
                {
                    if(!adapter.elements.get(i).isSection) return getItemIndexFromListPosition(i) + 1;
                }

                // If no item was found, it means that the index is 0
                return 0;
            }

            // Otherwise simply return the corresponding item
            else
            {
                return listPosition - elements.get(listPosition).numberOfSectionsBefore;
            }
        }

        /**
         * Translation between a ITEMS LIST index to the corresponding ELEMENTS LIST position
         * @param itemIndex index in the original items list (passed as parameter to the wrapper)
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
    }

    /**
     * The element in the list of the private adapter: can be a section or an item
     */
    private static class ListElement<U>
    {
        boolean isSection;

        U item;
        int numberOfSectionsBefore;

        Section section;

        /**
         * Constructor for items
         * @param item the linked item
         * @param numberOfSectionsBefore the number of sections created before this item
         */
        public ListElement(U item, int numberOfSectionsBefore)
        {
            this.isSection = false;
            this.item = item;
            this.numberOfSectionsBefore = numberOfSectionsBefore;
            this.section = null;
        }

        /**
         * Constructor for sections
         * @param section the linked section
         */
        public ListElement(Section section)
        {
            this.isSection = true;
            this.item = null;
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
            else return !other.isSection && !isSection && item.equals(other.item);
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
     * TouchHelper Callback for the adapter that allows to manage swiping and drag&drop on the list
     */
    public abstract class SectionedAdapterTouchHelperCallback extends ItemTouchHelper.Callback
    {
        private Integer dragAndDropStartItemPosition = null;
        private Integer dragAndDropEndItemPosition = null;

        private Section dragAndDropStartSection = null;
        private Section dragAndDropEndSection = null;

        /**
         * {@inheritDoc}
         *
         * Returns no allowed movement for section elements, calls the implementation
         * {@link SectionedRecyclerViewAdapter.SectionedAdapterTouchHelperCallback#getItemMovementFlags(int, RecyclerView, RecyclerView.ViewHolder)}
         * to the the allowed movement for the actual items
         */
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
        {
            // No swiping/moving for sections or if movement is disabled
            if(viewHolder.getItemViewType() == VIEW_TYPE_SECTION || !enableMovement)
            {
                return 0;
            }

            // Call implementation method to get the flags for the items
            else
            {
                int listPosition = viewHolder.getAdapterPosition();
                return getItemMovementFlags(adapter.getItemIndexFromListPosition(listPosition), recyclerView, viewHolder);
            }
        }

        /**
         * {@inheritDoc}
         *
         * Translates from the ELEMENTS LIST positions to the ITEMS LIST indices and then calls the implementation
         * {@link SectionedRecyclerViewAdapter.SectionedAdapterTouchHelperCallback#onItemMove(int, int, Section, Section, RecyclerView, RecyclerView.ViewHolder, RecyclerView.ViewHolder)}
         * to manage the movement of the actual items
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
        {
            int fromListPosition = viewHolder.getAdapterPosition();
            int toListPosition = target.getAdapterPosition();

            // Get item indices from elements list positions
            int fromIndex = adapter.getItemIndexFromListPosition(fromListPosition);
            int toIndex = adapter.getItemIndexFromListPosition(toListPosition);

            // Fix toIndex if we are over a section and we are going downwards
            if(adapter.elements.get(toListPosition).isSection && fromListPosition<toListPosition)
            {
                toIndex--;
            }

            // Get "from" section
            Section fromSection = null;
            for(int i=fromListPosition-1; i>=0; i--)
            {
                if(adapter.elements.get(i).isSection)
                {
                    fromSection = adapter.elements.get(i).section;
                    break;
                }
            }

            // Get "to" section (if we are going downwards we must also consider the element itself, otherwise we do not)
            Section toSection = null;
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
            return onItemMove(fromIndex, toIndex, fromSection, toSection, recyclerView, viewHolder, target);
        }

        /**
         * {@inheritDoc}
         *
         * Translates from the ELEMENTS LIST positions to the ITEMS LIST indices and then calls the implementation
         * {@link SectionedRecyclerViewAdapter.SectionedAdapterTouchHelperCallback#onItemSwiped(int, RecyclerView.ViewHolder, int)}
         * to manage the swiping of the actual items
         */
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
        {
            int listPosition = viewHolder.getAdapterPosition();

            // Call implementation method
            onItemSwiped(adapter.getItemIndexFromListPosition(listPosition), viewHolder, direction);
        }

        /**
         * {@inheritDoc}
         *
         * Allows to recognize a drop after a drag&drop operation, calling the implementation
         * {@link SectionedRecyclerViewAdapter.SectionedAdapterTouchHelperCallback#onItemDropped(int, int, Section, Section)}
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
                onItemDropped(dragAndDropStartItemPosition, dragAndDropEndItemPosition, dragAndDropStartSection, dragAndDropEndSection);
            }

            // Clear saved drag&drop fields
            dragAndDropStartItemPosition = null;
            dragAndDropEndItemPosition = null;
            dragAndDropStartSection = null;
            dragAndDropEndSection = null;
        }

        /**
         * Works just like {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#getMovementFlags(RecyclerView, RecyclerView.ViewHolder)}
         * but directly provides the item position (do NOT use getAdapterPosition() on the ViewHolder)
         */
        public abstract int getItemMovementFlags(int itemPosition, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

        /**
         * Works just like {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#onSwiped(RecyclerView.ViewHolder, int)}
         * but directly provides the item position (do NOT use getAdapterPosition() on the ViewHolder)
         */
        public abstract void onItemSwiped(int itemPosition, RecyclerView.ViewHolder viewHolder, int direction);

        /**
         * Works just like {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#onMove(RecyclerView, RecyclerView.ViewHolder, RecyclerView.ViewHolder)}
         * but directly provides the items positions (do NOT use getAdapterPosition() on the ViewHolders).
         * It also provides the starting and ending sections
         * NOTE: if itemPosition=targetItemPosition but itemSection!=targetItemSection the item has not been moved in the list
         * but its section changes (happens when the item is the last before a section and it gets moved to the first position of the next section)
         */
        public abstract boolean onItemMove(int itemPosition, int targetItemPosition, Section itemSection, Section targetItemSection, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);

        /**
         * Similar to {@link android.support.v7.widget.helper.ItemTouchHelper.Callback#onMove(RecyclerView, RecyclerView.ViewHolder, RecyclerView.ViewHolder)}
         * but fired only after the item is actually dropped after a drag&drop operation
         * NOTE: if itemPosition=targetItemPosition but itemSection!=targetItemSection the item has not been moved in the list
         * but its section changes (happens when the item is the last before a section and it gets moved to the first position of the next section)
         * @param itemPosition starting position
         * @param targetItemPosition ending position
         * @param itemSection starting section
         * @param targetItemSection ending section
         */
        public abstract void onItemDropped(int itemPosition, int targetItemPosition, Section itemSection, Section targetItemSection);
    }
}
