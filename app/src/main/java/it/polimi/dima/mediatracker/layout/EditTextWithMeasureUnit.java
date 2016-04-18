package it.polimi.dima.mediatracker.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import it.polimi.dima.mediatracker.R;

/**
 * EditText that appends a non-editable text (unit of measure) at the end of the input
 */
public class EditTextWithMeasureUnit extends EditText
{
    private String measureUnit;
    private int measureUnitColor;

    /**
     * {@inheritDoc}
     */
    public EditTextWithMeasureUnit(Context context)
    {
        this(context, null);
    }

    /**
     * {@inheritDoc}
     */
    public EditTextWithMeasureUnit(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     * {@inheritDoc}
     */
    public EditTextWithMeasureUnit(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        // Get custom parameters
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditTextWithMeasureUnit, 0, 0);
        try
        {
            measureUnitColor = a.getColor(R.styleable.EditTextWithMeasureUnit_measure_unit_text_color, Color.BLACK);
            measureUnit = a.getString(R.styleable.EditTextWithMeasureUnit_measure_unit);
        }
        finally
        {
            a.recycle();
        }

        // Add listener to show/hide measure unit
        this.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                // Show measure unit only if there is some text
                showMeasureUnit(s.length() > 0);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    /**
     * Setter
     * @param measureUnit the measure unit text
     */
    public void setMeasureUnit(String measureUnit)
    {
        this.measureUnit = measureUnit;
    }

    /**
     * Setter
     * @param color the measure unit text color
     */
    public void setMeasureUnitTextColor(int color)
    {
        this.measureUnitColor = color;
    }

    /**
     * Show/hides the measure unit text
     * @param show true if the measure unit should be displayed
     */
    private void showMeasureUnit(boolean show)
    {
        Drawable[] drawables = getCompoundDrawablesRelative();
        setCompoundDrawablesRelative(drawables[0], drawables[1], show ? new TextDrawable(measureUnit) : null, drawables[3]);
    }

    /**
     * Drawable for text
     */
    private class TextDrawable extends Drawable
    {
        private String text = "";

        /**
         * Constructor
         * @param text the text of the drawable
         */
        public TextDrawable(String text)
        {
            this.text = text;
            setBounds(0, 0, (int) getPaint().measureText(this.text) + 2, (int) getTextSize());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void draw(Canvas canvas)
        {
            Paint paint = getPaint();
            paint.setColor(measureUnitColor);
            int lineBaseline = getLineBounds(0, null);
            canvas.drawText(text, 0, canvas.getClipBounds().top + lineBaseline, paint);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setAlpha(int alpha) {/* Not supported */}

        /**
         * {@inheritDoc}
         */
        @Override
        public void setColorFilter(ColorFilter colorFilter) {/* Not supported */}

        /**
         * {@inheritDoc}
         */
        @Override
        public int getOpacity()
        {
            return 1;
        }
    }
}
