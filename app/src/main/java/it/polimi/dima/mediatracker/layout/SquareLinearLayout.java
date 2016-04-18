package it.polimi.dima.mediatracker.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * LinearLayout that always has height = width
 */
public class SquareLinearLayout extends LinearLayout
{
    /**
     * {@inheritDoc}
     */
    public SquareLinearLayout(Context context)
    {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    public SquareLinearLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    public SquareLinearLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}