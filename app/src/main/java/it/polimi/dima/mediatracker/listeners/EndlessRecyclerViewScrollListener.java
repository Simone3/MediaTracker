package it.polimi.dima.mediatracker.listeners;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Scroll listener that allows to load more data in a list when the user scrolls down
 */
public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener
{
    private int visibleThreshold = 5;
    private int currentPage = 0;
    private int previousTotalItemCount = 0;
    private boolean loading = true;

    private RecyclerView.LayoutManager layoutManager;

    /**
     * Constructor
     * @param layoutManager the layout manager
     */
    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager)
    {
        this.layoutManager = layoutManager;
    }

    /**
     * Constructor
     * @param layoutManager the layout manager
     */
    public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager)
    {
        this.layoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    /**
     * Constructor
     * @param layoutManager the layout manager
     */
    public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager)
    {
        this.layoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    /**
     * Used for StaggeredGridView
     * @param lastVisibleItemPositions the visible positions
     * @return the last visible position
     */
    private int getLastVisibleItem(int[] lastVisibleItemPositions)
    {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++)
        {
            if (i == 0)
            {
                maxSize = lastVisibleItemPositions[i];
            }
            else if (lastVisibleItemPositions[i] > maxSize)
            {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy)
    {
        int lastVisibleItemPosition = 0;
        int totalItemCount = layoutManager.getItemCount();

        if(layoutManager instanceof StaggeredGridLayoutManager)
        {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
        }
        else if (layoutManager instanceof LinearLayoutManager)
        {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            if(lastVisibleItemPosition==RecyclerView.NO_POSITION) return;
        }

        // If we have LESS items than before, assume the list is invalidated and reset back to initial state
        if (totalItemCount < previousTotalItemCount)
        {
            this.currentPage = 0;
            this.previousTotalItemCount = totalItemCount;
            this.loading = false;
        }

        // If it’s still loading, we check to see if the dataset count has changed, if so we conclude it has finished loading and update the current page number and total item count
        if(loading && (totalItemCount > previousTotalItemCount))
        {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached the visibleThreshold and need to reload more data.
        if(!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount)
        {
            currentPage++;
            onLoadMore(currentPage, totalItemCount);
            loading = true;
        }
    }

    /**
     * What to do when a new page needs to be loaded
     * @param page the page to load
     * @param totalItemsCount the current items number
     */
    public abstract void onLoadMore(int page, int totalItemsCount);
}
