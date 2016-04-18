package it.polimi.dima.mediatracker.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import it.polimi.dima.mediatracker.listeners.MediaItemListener;
import it.polimi.dima.mediatracker.listeners.OnSetupDrawerListener;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Abstract fragment that represents a "content": it's a fragment that manages the navigation drawer and that,
 * on tablets, is displayed on the left side of the multi-pane layout
 */
public class ContentAbstractFragment extends Fragment implements Toolbar.OnMenuItemClickListener
{
    MediaItemListener mediaItemListener;
    OnSetupDrawerListener onSetupDrawerListener;

    /**
     * Setter
     * @param mediaItemListener the listener to get the media item selection events
     */
    public void setMediaItemListener(MediaItemListener mediaItemListener)
    {
        this.mediaItemListener = mediaItemListener;
    }

    /**
     * Use the onAttach method to add the drawer listener (the fragment needs it during onCreateView)
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        // If the host activity is a listener, set it in the fragment
        if(context instanceof OnSetupDrawerListener)
        {
            onSetupDrawerListener = (OnSetupDrawerListener) context;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        // Handle action bar item clicks here
        int id = item.getItemId();
        return Utils.manageToolbarMenuSelection(getActivity(), id);
    }
}
