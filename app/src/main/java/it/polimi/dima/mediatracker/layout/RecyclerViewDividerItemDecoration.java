package it.polimi.dima.mediatracker.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Decoration to show line separators between list elements
 */
public class RecyclerViewDividerItemDecoration extends RecyclerView.ItemDecoration
{
    private Drawable divider;

    /**
     * Constructor
     * @param context the context
     * @param attrs the style attributes to define the divider
     */
    public RecyclerViewDividerItemDecoration(Context context, AttributeSet attrs)
    {
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.listDivider});
        divider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * Constructor
     * @param divider the divider drawable
     */
    public RecyclerViewDividerItemDecoration(Drawable divider)
    {
        this.divider = divider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);

        if(divider==null) return;
        if(parent.getChildAdapterPosition(view)<1) return;

        if(getOrientation(parent)==LinearLayoutManager.VERTICAL)
        {
            outRect.top = divider.getIntrinsicHeight();
        }
        else
        {
            outRect.left = divider.getIntrinsicWidth();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state)
    {
        if(divider==null)
        {
            super.onDrawOver(c, parent, state);
            return;
        }

        if(getOrientation(parent) == LinearLayoutManager.VERTICAL)
        {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int childCount = parent.getChildCount();

            for(int i=1; i < childCount; i++)
            {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int size = divider.getIntrinsicHeight();
                final int top = child.getTop() - params.topMargin;
                final int bottom = top + size;
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
        else
        {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();
            final int childCount = parent.getChildCount();

            for(int i=1; i < childCount; i++)
            {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int size = divider.getIntrinsicWidth();
                final int left = child.getLeft() - params.leftMargin;
                final int right = left + size;
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }

    /**
     * Helper to the the orientation (throws IllegalStateException if it's not a LinearLayout)
     * @param parent the recycler view
     * @return layout manager orientation
     */
    private int getOrientation(RecyclerView parent)
    {
        if(parent.getLayoutManager() instanceof LinearLayoutManager)
        {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        }
        else
        {
            throw new IllegalStateException("RecyclerViewDividerItemDecoration can only be used with a LinearLayoutManager.");
        }
    }
}
