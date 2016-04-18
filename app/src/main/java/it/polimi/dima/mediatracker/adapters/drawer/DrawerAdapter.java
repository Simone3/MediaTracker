package it.polimi.dima.mediatracker.adapters.drawer;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.polimi.dima.mediatracker.R;

/**
 * Adapter for the navigation drawer listener, allows to have "elements" (main drawer options) and "sub-elements" (options that are shown under the corresponding element if the user selects it)
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder>
{
    public final static int ELEMENT_TYPE = 1;
    public final static int SUB_ELEMENT_TYPE = 2;

    private List<DrawerElement> drawerElements;
    private Context context;
    private DrawerListener drawerListener;

    private int selectedElement = 0;
    private int selectedSubElement = -1;

    /**
     * Constructor
     * @param context the context
     * @param drawerElements all the drawer elements, possibly with sub-elements
     * @param firstSelectedElement the index of the first selected element
     * @param firstSelectedSubElement the index of the first selected sub-element (-1 if none)
     * @param drawerListener the drawer listener implementation to receive the callbacks
     */
    public DrawerAdapter(Context context, List<DrawerElement> drawerElements, int firstSelectedElement, int firstSelectedSubElement, DrawerListener drawerListener)
    {
        this.drawerElements = drawerElements;
        this.context = context;
        this.drawerListener = drawerListener;

        this.selectedElement = firstSelectedElement;
        this.selectedSubElement = firstSelectedSubElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Create ViewHolder getting the corresponding layout (element or sub-element)
        if(viewType==ELEMENT_TYPE)
        {
            View itemView = LayoutInflater.from(context).inflate(R.layout.drawer_element, parent, false);
            return new ViewHolder(itemView, viewType, this);
        }
        else if(viewType==SUB_ELEMENT_TYPE)
        {
            View itemView = LayoutInflater.from(context).inflate(R.layout.drawer_sub_element, parent, false);
            return new ViewHolder(itemView, viewType, this);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder viewHolder, int position)
    {
        // Get data based on the type (element or sub-element)
        int icon = 0;
        String name = "";
        int color = 0;
        boolean selected = false;
        if(viewHolder.getItemViewType()==ELEMENT_TYPE)
        {
            DrawerElement element = drawerElements.get(getElementIndexAtListPosition(position));

            icon = element.getIcon();
            name = element.getName();
            color = element.getColor();
            selected = selectedSubElement == -1 && position == selectedElement;
        }
        else if(viewHolder.getItemViewType()==SUB_ELEMENT_TYPE)
        {
            DrawerSubElement subElement = drawerElements.get(selectedElement).getSubElements().get(getSubElementIndexAtListPosition(position));

            icon = subElement.getIcon();
            name = subElement.getName();
            color = subElement.getColor();
            selected = getSubElementIndexAtListPosition(position) == selectedSubElement;
        }

        // Set data
        viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(context, icon));
        viewHolder.icon.setColorFilter(ContextCompat.getColor(context, color));
        viewHolder.name.setText(name);
        viewHolder.container.setSelected(selected);
    }

    /**
     * Getter
     * @return size is all elements + all sub-elements of the current element (if any)
     */
    @Override
    public int getItemCount()
    {
        List<DrawerSubElement> subElements = drawerElements.get(selectedElement).getSubElements();
        return drawerElements.size() + (subElements==null ? 0 : subElements.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position)
    {
        List<DrawerSubElement> selectedElementSubElements = drawerElements.get(selectedElement).getSubElements();
        if(selectedElementSubElements==null)
        {
            return ELEMENT_TYPE;
        }
        else
        {
            if(position>selectedElement && position<=selectedElement+selectedElementSubElements.size())
            {
                return SUB_ELEMENT_TYPE;
            }
            else
            {
                return ELEMENT_TYPE;
            }
        }
    }

    /**
     * Called by the ViewHolder when a drawer element is clicked
     * @param listPosition the clicked position in the DRAWER LIST
     * @param elementType the clicked element type
     */
    private void onElementClicked(int listPosition, int elementType)
    {
        // Get element and sub-element indices
        if(elementType==SUB_ELEMENT_TYPE)
        {
            selectedSubElement = getSubElementIndexAtListPosition(listPosition);
        }
        else if(elementType==ELEMENT_TYPE)
        {
            selectedSubElement = -1;
            selectedElement = getElementIndexAtListPosition(listPosition);
        }

        // Perform the callback
        drawerListener.onDrawerElementSelected(selectedElement, selectedSubElement);
    }

    /**
     * Translation from the DRAWER LIST position (actual position clicked by the user in the drawer) to the ELEMENTS LIST index (the parameter of this class)
     * @param listPosition the position in the drawer list
     * @return the index of the elements list
     */
    private int getElementIndexAtListPosition(int listPosition)
    {
        if(listPosition<=selectedElement || drawerElements.get(selectedElement).getSubElements()==null)
        {
            return listPosition;
        }
        else
        {
            return listPosition - drawerElements.get(selectedElement).getSubElements().size();
        }
    }

    /**
     * Translation from the DRAWER LIST position (actual position clicked by the user in the drawer) to the SUB-ELEMENT index in the ELEMENTS LIST index (the parameter of this class)
     * @param listPosition the position in the drawer list
     * @return the index of the sub-element in the subElements array of the current selected element
     */
    private int getSubElementIndexAtListPosition(int listPosition)
    {
        return listPosition - selectedElement - 1;
    }

    /**
     * ViewHolder pattern
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        ImageView icon;
        LinearLayout container;

        public ViewHolder(final View drawerElement, final int itemType, final DrawerAdapter drawerAdapter)
        {
            super(drawerElement);

            container = (LinearLayout) itemView.findViewById(R.id.drawer_element_container);
            name = (TextView) drawerElement.findViewById(R.id.drawer_element_name);
            icon = (ImageView) drawerElement.findViewById(R.id.drawer_element_icon);

            // Redirect clicks to the adapter
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Call adapter only if the clicked element is not already selected
                    if(!drawerElement.isSelected())
                    {
                        drawerAdapter.onElementClicked(getAdapterPosition(), itemType);
                    }
                }
            });
        }
    }

    /**
     * Listener for drawer elements clicks
     */
    public interface DrawerListener
    {
        /**
         * Called when a navigation drawer element is selected
         * @param selectedElementIndex the element selected
         * @param selectedSubElementIndex the sub-element selected (possibly -1 if the user clicked a main element)
         */
        void onDrawerElementSelected(int selectedElementIndex, int selectedSubElementIndex);
    }
}