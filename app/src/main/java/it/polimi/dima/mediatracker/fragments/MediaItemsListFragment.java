package it.polimi.dima.mediatracker.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.adapters.media_items_list.AdapterSection;
import it.polimi.dima.mediatracker.adapters.media_items_list.CompletedMediaItemsAdapterWrapper;
import it.polimi.dima.mediatracker.adapters.media_items_list.SectionedAbstractAdapterWrapper;
import it.polimi.dima.mediatracker.adapters.media_items_list.TrackedMediaItemsListAdapterWrapper;
import it.polimi.dima.mediatracker.controllers.CategoriesController;
import it.polimi.dima.mediatracker.controllers.MediaItemsAbstractController;
import it.polimi.dima.mediatracker.layout.RecyclerViewDividerItemDecoration;
import it.polimi.dima.mediatracker.listeners.EndlessRecyclerViewScrollListener;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.utils.GlobalConstants;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Manages the media item lists (both "tracking" and "completed")
 */
public class MediaItemsListFragment extends ContentAbstractFragment
{
    private final static String CATEGORY_ID_PARAMETER = "CATEGORY_ID_PARAMETER";
    private final static String IS_COMPLETED_LIST_PARAMETER = "IS_COMPLETED_LIST_PARAMETER";

    private String currentSearchQuery = null;

    private SectionedAbstractAdapterWrapper adapterWrapper;

    private SearchView searchView;

    private boolean isCompletedItemsPage;

    private Category category;

    private MediaItemsAbstractController controller;

    /**
     * Instance creation
     * @param categoryId category ID parameter
     * @param isCompletedList true if it's the completed list, false otherwise
     * @return the fragment instance
     */
    public static MediaItemsListFragment newInstance(Long categoryId, boolean isCompletedList)
    {
        MediaItemsListFragment fragment = new MediaItemsListFragment();

        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_PARAMETER, categoryId);
        args.putBoolean(IS_COMPLETED_LIST_PARAMETER, isCompletedList);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_media_items_list, container, false);

        // Get parameters
        Bundle args = getArguments();
        Long categoryId = args.getLong(CATEGORY_ID_PARAMETER, -1);
        category = CategoriesController.getInstance().getCategoryById(categoryId);
        isCompletedItemsPage = args.getBoolean(IS_COMPLETED_LIST_PARAMETER);
        controller = category.getMediaType().getController();

        // Build toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.action_bar_item_list);
        toolbar.setOnMenuItemClickListener(this);

        // Set title
        String title;
        if(isCompletedItemsPage)
        {
            title = getString(R.string.title_activity_completed_media_items_list, category.getMediaType().getNamePlural(getActivity()));
        }
        else
        {
            title = getString(R.string.title_activity_tracked_media_items_list, category.getMediaType().getNamePlural(getActivity()));
        }
        toolbar.setTitle(title);

        // Call the drawer listener, if any
        if(onSetupDrawerListener!=null)
        {
            onSetupDrawerListener.onSetupDrawer(toolbar);
        }

        // Setup search
        final MenuItem item = toolbar.getMenu().findItem(R.id.action_search);
        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                currentSearchQuery = query;

                if(Utils.isEmpty(query))
                {
                    adapterWrapper.setSearchMode(false);
                    refreshMediaItemsListFromDatabase();
                }
                else
                {
                    adapterWrapper.setSearchMode(true);
                    searchMediaItems(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                currentSearchQuery = null;
                adapterWrapper.setSearchMode(false);
                refreshMediaItemsListFromDatabase();
                return true;
            }
        });

        // Manage floating action button (remove it in the completed media items list, redirect to item form in the tracked media items list)
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.items_fab);
        if(fab!=null)
        {
            if(isCompletedItemsPage)
            {
                ((ViewGroup) fab.getParent()).removeView(fab);
            }
            else
            {
                fab.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(mediaItemListener!=null) mediaItemListener.onMediaItemSelected(null);
                    }
                });
            }
        }

        // Setup the list
        loadRecyclerView(view);

        return view;
    }

    /**
     * Creates the recycler view that manages the media items list
     */
    private void loadRecyclerView(View view)
    {
        // Get all media items in this category and type (at page 0)
        List<MediaItem> mediaItems = getMediaItems(0);

        // Get recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.media_items_list);
        if(recyclerView==null) return;

        // Build adapter based on the type of list
        if(isCompletedItemsPage)
        {
            // In the completed list media items are grouped by year
            adapterWrapper = new CompletedMediaItemsAdapterWrapper(mediaItems, controller.getRedoOptionName(), new SectionedAbstractAdapterWrapper.SectionNameCallback()
            {
                @Override
                public AdapterSection onSectionNameRequest(MediaItem mediaItem)
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mediaItem.getCompletionDate());
                    String year = String.valueOf(calendar.get(Calendar.YEAR));
                    return new AdapterSection(year, year);
                }
            });
        }
        else
        {
            // In the tracked list media items are grouped by doing/importance/upcoming
            adapterWrapper = new TrackedMediaItemsListAdapterWrapper(mediaItems, controller.getCompleteOptionName(), controller.getDoingOptionName(), new SectionedAbstractAdapterWrapper.SectionNameCallback()
            {
                @Override
                public AdapterSection onSectionNameRequest(MediaItem mediaItem)
                {
                    if(mediaItem.isUpcoming())
                    {
                        return new AdapterSection(GlobalConstants.SECTION_UPCOMING, getString(R.string.upcoming));
                    }
                    else
                    {
                        if(mediaItem.isDoingNow())
                        {
                            return new AdapterSection(GlobalConstants.SECTION_DOING_NOW, getString(controller.getDoingNowName()));
                        }
                        else
                        {
                            return new AdapterSection(mediaItem.getImportanceLevel().name(), mediaItem.getImportanceLevel().getName(getActivity()));
                        }
                    }
                }
            });
        }

        // Manage layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Set adapter
        adapterWrapper.setAdapterToRecyclerView(recyclerView);

        // "Load More" listener
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager)
        {
            @Override
            public void onLoadMore(int page, int totalItemsCount)
            {
                // Get next media items
                List<MediaItem> mediaItems;
                if(Utils.isEmpty(currentSearchQuery))
                {
                    mediaItems = getMediaItems(page);
                }
                else
                {
                    mediaItems = controller.searchMediaItemsInCategory(page, category, currentSearchQuery, isCompletedItemsPage);
                }

                // Add them to the adapter
                if(mediaItems.size()>0)
                {
                    adapterWrapper.addMediaItemsAtTheEndAndNotify(mediaItems);
                }
            }
        });

        // Add a listener for menu options for each list element
        adapterWrapper.setOnItemOptionSelectListener(new SectionedAbstractAdapterWrapper.ListElementListener()
        {
            @Override
            public void onMediaItemOptionSelected(MenuItem menuItem, final int selectedPosition)
            {
                switch(menuItem.getItemId())
                {
                    // [BOTH LIST TYPES] To edit a media item we go to the corresponding form
                    case R.id.media_item_options_edit:
                        if(mediaItemListener!=null)
                            mediaItemListener.onMediaItemSelected(adapterWrapper.get(selectedPosition));
                        return;

                    // [BOTH LIST TYPES] To delete a media item
                    case R.id.media_item_options_delete:

                        // We first show a confirmation message
                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.delete_media_item_confirm_message)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        MediaItem deletedMediaItem = adapterWrapper.get(selectedPosition);

                                        // Callback
                                        if(mediaItemListener!=null)
                                        {
                                            mediaItemListener.onMediaItemRemoved(deletedMediaItem);
                                        }

                                        // Delete media item
                                        controller.deleteMediaItem(deletedMediaItem);

                                        // Remove it from the adapter
                                        adapterWrapper.removeItemAndNotify(selectedPosition);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        dialog.cancel();
                                    }
                                }).show();
                        return;

                    // [TRACKED LIST ONLY] To set a media item as complete we call the given method
                    case R.id.media_item_options_complete:
                        setItemAsCompleted(selectedPosition);
                        return;

                    // [TRACKED LIST ONLY] To set a media item as doing now we do so and then we reload the list
                    case R.id.media_item_options_doing:
                        controller.setMediaItemAsDoingNow(adapterWrapper.get(selectedPosition), true);
                        refreshMediaItemsListFromDatabase();
                        return;

                    // [TRACKED LIST ONLY] To set a media item as owned we do so and then we notify the adapter of the change
                    case R.id.media_item_options_own:
                        controller.setMediaItemAsOwned(adapterWrapper.get(selectedPosition), true);
                        adapterWrapper.notifyItemChanged(selectedPosition);
                        return;

                    // [COMPLETED LIST ONLY] To change the media item completion date we show a dialog and we reload the list
                    case R.id.media_item_options_change_completion_date:
                        final MediaItem selectedMediaItem = adapterWrapper.get(selectedPosition);
                        final Calendar completionDateCalendar = Calendar.getInstance();
                        completionDateCalendar.setTime(selectedMediaItem.getCompletionDate());
                        DatePickerDialog completionDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                            {
                                completionDateCalendar.set(year, monthOfYear, dayOfMonth);
                                controller.setMediaItemAsCompleted(selectedMediaItem, completionDateCalendar.getTime());
                                refreshMediaItemsListFromDatabase();
                            }
                        }, completionDateCalendar.get(Calendar.YEAR), completionDateCalendar.get(Calendar.MONTH), completionDateCalendar.get(Calendar.DAY_OF_MONTH));
                        completionDatePicker.show();
                        return;

                    // [COMPLETED LIST ONLY] To set a media item as redoing we do so and then we remove it from the list
                    case R.id.media_item_options_redo:
                        MediaItem redoingMediaItem = adapterWrapper.get(selectedPosition);
                        if(mediaItemListener!=null)
                        {
                            mediaItemListener.onMediaItemRemoved(redoingMediaItem);
                        }
                        controller.setMediaItemAsToRedo(redoingMediaItem);
                        adapterWrapper.removeItemAndNotify(selectedPosition);
                        //return;
                }
            }

            @Override
            public void onMediaItemClicked(int position)
            {
                MediaItem item = adapterWrapper.get(position);
                if(mediaItemListener!=null)
                {
                    mediaItemListener.onMediaItemSelected(item);
                }
            }
        });

        // If we are in the tracked media items list...
        if(!isCompletedItemsPage)
        {
            // Add listener for swiping and dragging
            SectionedAbstractAdapterWrapper.SectionedAdapterTouchHelperCallback simpleItemTouchCallback = adapterWrapper.new SectionedAdapterTouchHelperCallback()
            {
                @Override
                public int getMediaItemMovementFlags(int mediaItemPosition, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
                {
                    // Allow dragging only if not in the upcoming section
                    int dragFlags;
                    if(adapterWrapper.get(mediaItemPosition).isUpcoming()) dragFlags = 0;
                    else dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

                    // All swiping only if owned and not upcoming
                    int swipeFlags;
                    if(adapterWrapper.get(mediaItemPosition).isUpcoming() || !adapterWrapper.get(mediaItemPosition).isOwned()) swipeFlags = 0;
                    else swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                    // Make flags
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

                @Override
                public boolean onMediaItemMove(int mediaItemPosition, int targetMediaItemPosition, AdapterSection mediaItemSection, AdapterSection targetMediaItemSection, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
                {
                    // Disallow moving an item INTO the upcoming section
                    if(GlobalConstants.SECTION_UPCOMING.equals(targetMediaItemSection.getSectionId()))
                    {
                        return false;
                    }

                    // Move media items in the adapter
                    adapterWrapper.moveItemAndNotify(mediaItemPosition, targetMediaItemPosition, mediaItemSection, targetMediaItemSection);

                    // Movement successful
                    return true;
                }

                @Override
                public void onMediaItemSwiped(int mediaItemPosition, RecyclerView.ViewHolder viewHolder, int direction)
                {
                    // Set item as completed
                    final MediaItem mediaItemToSetAsCompleted = setItemAsCompleted(mediaItemPosition);

                    // Show "undo" option
                    Snackbar snackbar = Snackbar
                            .make(viewHolder.itemView, R.string.set_as_completed_notice, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    controller.undoSetAsCompleted(mediaItemToSetAsCompleted);
                                    refreshMediaItemsListFromDatabase();
                                }
                            });
                    snackbar.show();
                }

                @Override
                public void onMediaItemDropped(int mediaItemPosition, int targetMediaItemPosition, AdapterSection mediaItemSection, AdapterSection targetMediaItemSection)
                {
                    // If the section changed during the drag&drop...
                    if(!mediaItemSection.equals(targetMediaItemSection))
                    {
                        boolean checkImportanceSection = true;

                        // If the item is now in "Doing Now" set it as such
                        if(GlobalConstants.SECTION_DOING_NOW.equals(targetMediaItemSection.getSectionId()))
                        {
                            controller.setMediaItemAsDoingNow(adapterWrapper.get(targetMediaItemPosition), true);
                            checkImportanceSection = false;
                        }

                        // If the item was in "Doing Now" set it as not doing now anymore
                        else if(GlobalConstants.SECTION_DOING_NOW.equals(mediaItemSection.getSectionId()))
                        {
                            controller.setMediaItemAsDoingNow(adapterWrapper.get(targetMediaItemPosition), false);
                        }

                        // If the importance level changed, update it
                        if(checkImportanceSection)
                        {
                            for(ImportanceLevel level: ImportanceLevel.values())
                            {
                                if(level.name().equals(targetMediaItemSection.getSectionId()))
                                {
                                    adapterWrapper.get(targetMediaItemPosition).setImportanceLevel(level);
                                    controller.saveMediaItem(adapterWrapper.get(targetMediaItemPosition));
                                }
                            }
                        }
                    }

                    // If we aren't in the "doing now" section...
                    if(!GlobalConstants.SECTION_DOING_NOW.equals(targetMediaItemSection.getSectionId()))
                    {
                        // Get moved media item
                        MediaItem movedMediaItem = adapterWrapper.get(targetMediaItemPosition);

                        // Get previous media item in the moved media item importance level section (null if it's the first)
                        MediaItem previousMediaItemInSection = null;
                        if(targetMediaItemPosition>0)
                        {
                            MediaItem temp = adapterWrapper.get(targetMediaItemPosition-1);
                            if(!temp.isDoingNow() && movedMediaItem.getImportanceLevel().equals(temp.getImportanceLevel()))
                            {
                                previousMediaItemInSection = temp;
                            }
                        }

                        // Get next media item in the moved media item importance level section (null if it's the last)
                        MediaItem nextMediaItemInSection = null;
                        if(targetMediaItemPosition<adapterWrapper.getItemCount()-1)
                        {
                            MediaItem temp = adapterWrapper.get(targetMediaItemPosition+1);
                            if(!temp.isUpcoming() && movedMediaItem.getImportanceLevel().equals(temp.getImportanceLevel()))
                            {
                                nextMediaItemInSection = temp;
                            }
                        }

                        // Update order value
                        controller.setMediaItemOrderInImportanceLevelAfterMove(category, movedMediaItem, previousMediaItemInSection, nextMediaItemInSection);

                        // Save media item
                        controller.saveMediaItem(movedMediaItem);
                    }

                    // Notify that all mediaItems between old and new position have changed (actual edits done above or just the option menu index)
                    adapterWrapper.notifyItemRangeChanged(mediaItemPosition < targetMediaItemPosition ? mediaItemPosition : targetMediaItemPosition, Math.abs(mediaItemPosition - targetMediaItemPosition)+1);
                }
            };

            // Setup the touch helper in the recycler view
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);

            // Also manage the "drag handler" icon
            adapterWrapper.setDragHandlerListener(new SectionedAbstractAdapterWrapper.DragHandlerListener()
            {
                @Override
                public void onStartDrag(RecyclerView.ViewHolder viewHolder)
                {
                    itemTouchHelper.startDrag(viewHolder);
                }
            });
        }

        // Remove the "flashing" when updating an element (e.g. notifyItemChanged)
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if(animator instanceof SimpleItemAnimator)
        {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        // Add decorator for list elements borders
        recyclerView.addItemDecoration(new RecyclerViewDividerItemDecoration(ContextCompat.getDrawable(getActivity(), R.drawable.abc_list_divider_mtrl_alpha)));

        // Set empty view
        adapterWrapper.setEmptyView(view.findViewById(R.id.list_empty_view));
    }

    /**
     * Helper to load media items from the database
     * @param page the current page
     * @return the media items on that page
     */
    private List<MediaItem> getMediaItems(int page)
    {
        List<MediaItem> mediaItems;

        if(isCompletedItemsPage)
        {
            mediaItems = controller.getCompletedMediaItemsInCategory(page, category);
        }
        else
        {
            mediaItems = controller.getTrackedMediaItemsInCategory(page, category);
        }

        if(mediaItems==null) mediaItems = new ArrayList<>();
        return mediaItems;
    }

    /**
     * Reloads the media items list querying the data from the database
     */
    public void refreshMediaItemsListFromDatabase()
    {
        // Get data
        List<MediaItem> tempMediaItems = getMediaItems(0);

        // Set it in the adapter
        adapterWrapper.setItemsAndNotifyDataSetChanged(tempMediaItems);

        // Reset search query
        searchView.setQuery("", false);
        adapterWrapper.setSearchMode(false);
    }

    /**
     * Helper to submit a search to the database
     * @param query the search query
     */
    private void searchMediaItems(String query)
    {
        // Get data
        List<MediaItem> searchResults = controller.searchMediaItemsInCategory(0, category, query, isCompletedItemsPage);

        // Set it in the adapter
        adapterWrapper.setItemsAndNotifyDataSetChanged(searchResults);
    }

    /**
     * Helper to set a media item as completed
     * @param position the position in the items list
     * @return the media item that has been set as completed
     */
    private MediaItem setItemAsCompleted(int position)
    {
        final MediaItem mediaItemToSetAsCompleted = adapterWrapper.get(position);
        if(mediaItemListener!=null)
        {
            mediaItemListener.onMediaItemRemoved(mediaItemToSetAsCompleted);
        }
        controller.setMediaItemAsCompleted(mediaItemToSetAsCompleted, new Date());
        adapterWrapper.removeItemAndNotify(position);
        return mediaItemToSetAsCompleted;
    }
}
