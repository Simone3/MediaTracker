package it.polimi.dima.mediatracker.listeners;

import android.support.v7.widget.Toolbar;

/**
 * Listener used by fragments to pass their toolbar to the host activity in order to setup the navigation drawer
 */
public interface OnSetupDrawerListener
{
    /**
     * Called just after the fragment has initialized its toolbar
     * @param toolbar the fragment toolbar
     */
    void onSetupDrawer(Toolbar toolbar);
}
