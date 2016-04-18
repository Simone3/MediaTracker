package it.polimi.dima.mediatracker.layout;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Image that completely fits the given height (= no cropping) and crops on X if the given width is too small, keeping the aspect ratio
 */
public class FitYCropXImageView extends ImageView
{
    private final RectF drawableRect = new RectF(0, 0, 0,0);
    private final RectF viewRect = new RectF(0, 0, 0,0);
    private final Matrix matrix = new Matrix();

    /**
     * {@inheritDoc}
     */
    public FitYCropXImageView(Context context)
    {
        super(context);
        setScaleType(ScaleType.MATRIX);
    }

    /**
     * {@inheritDoc}
     */
    public FitYCropXImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
    }

    /**
     * {@inheritDoc}
     */
    public FitYCropXImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.MATRIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get current drawable
        final Drawable drawable = getDrawable();

        // Do nothing if no drawable
        if(drawable==null)
        {
            return;
        }

        // Get dimensions
        int viewHeight = getMeasuredHeight();
        int viewWidth = getMeasuredWidth();
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        // Represents the original image
        drawableRect.set(0, 0, drawableWidth, drawableHeight);

        // Compute the left and right bounds for the scaled image
        float viewHalfWidth = viewWidth / 2;
        float scale = (float) viewHeight / (float) drawableHeight;
        float scaledWidth = drawableWidth * scale;
        float scaledHalfWidth = scaledWidth / 2;
        viewRect.set(viewHalfWidth - scaledHalfWidth, 0, viewHalfWidth + scaledHalfWidth, viewHeight);

        // Set matrix
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER /* This constant doesn't matter? */);
        setImageMatrix(matrix);

        // Reload
        requestLayout();
    }
}
