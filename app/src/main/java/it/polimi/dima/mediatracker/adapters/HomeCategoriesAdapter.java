package it.polimi.dima.mediatracker.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.Category;

/**
 * Adapter for the home page categories grid
 */
public class HomeCategoriesAdapter extends ArrayAdapter<Category>
{
    private Context context;
    private List<Category> categories;
    private int layoutResourceId;

    /**
     * Constructor
     * @param context the context
     * @param layoutResourceId the resource id of the layout
     * @param categories all categories
     */
    public HomeCategoriesAdapter(Context context, int layoutResourceId, List<Category> categories)
    {
        super(context, layoutResourceId, categories);

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.categories = categories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;

        // If it's not recycled, set some attributes
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(layoutResourceId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.category_item_icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.category_item_name);
            viewHolder.container = (LinearLayout) convertView.findViewById(R.id.category_item);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set values
        Category category = categories.get(position);
        viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(context, category.getMediaType().getIcon()));
        viewHolder.name.setText(category.getName());
        viewHolder.container.setBackgroundColor(ContextCompat.getColor(context, category.getColor(context)));

        return convertView;
    }

    /**
     * ViewHolder pattern
     */
    private static class ViewHolder
    {
        LinearLayout container;
        ImageView icon;
        TextView name;
    }
}
