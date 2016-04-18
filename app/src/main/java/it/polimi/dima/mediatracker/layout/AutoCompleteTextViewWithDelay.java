package it.polimi.dima.mediatracker.layout;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

/**
 * An AutoCompleteTextView that performs the autocomplete steps only if the user stops typing for a given amount of milliseconds
 */
public class AutoCompleteTextViewWithDelay extends AutoCompleteTextView
{
    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int AUTOCOMPLETE_DELAY = 750;

    private ProgressBar loadingIndicator;

    private final Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            AutoCompleteTextViewWithDelay.super.performFiltering((CharSequence) msg.obj, msg.arg1);
            return true;
        }
    });

    /**
     * {@inheritDoc}
     */
    public AutoCompleteTextViewWithDelay(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * Setter
     * @param progressBar the progress bar to show when the user is typing (= inform that the autocomplete is waiting for the user to stop typing to show the suggestions)
     */
    public void setLoadingIndicator(ProgressBar progressBar)
    {
        loadingIndicator = progressBar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performFiltering(CharSequence text, int keyCode)
    {
        // Show the progress bar, if any
        if(loadingIndicator!=null)
        {
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        // Remove old message and send a new one delayed by the amount of milliseconds
        handler.removeMessages(MESSAGE_TEXT_CHANGED);
        handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_TEXT_CHANGED, text), AUTOCOMPLETE_DELAY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFilterComplete(int count)
    {
        // Hide the progress bar, if any
        if(loadingIndicator!=null)
        {
            loadingIndicator.setVisibility(View.GONE);
        }
        super.onFilterComplete(count);
    }
}

