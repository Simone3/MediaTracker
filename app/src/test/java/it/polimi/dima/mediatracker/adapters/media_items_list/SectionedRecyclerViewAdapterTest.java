package it.polimi.dima.mediatracker.adapters.media_items_list;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.polimi.dima.mediatracker.BuildConfig;
import it.polimi.dima.mediatracker.model.Section;
import it.polimi.dima.mediatracker.model.Sectioned;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests the adapter used in the media items list
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SectionedRecyclerViewAdapterTest
{
    private static MyAdapter sectionedAdapter;
    private static List<MyItem> adapterItems;

    private Section sectionA = new Section("A", "Section A");
    private Section sectionB = new Section("B", "Section B");

    /**
     * Tests the adapter first setup
     */
    @Test
    public void testInitialization()
    {
        initAdapter();

        externalAdapterElementsShouldBe(adapterItems);

        assertEquals("Wrong number of internal elements", adapterItems.size()+2, sectionedAdapter.getAdapter().getItemCount());

        shouldBeSections(0, 3);
    }

    /**
     * Tests the addItemsAtTheEndAndNotify method
     */
    @Test
    public void testAdd()
    {
        initAdapter();

        List<MyItem> newItems = new ArrayList<>();
        newItems.add(new MyItem("new1", sectionA));
        newItems.add(new MyItem("new2", sectionB));
        adapterItems.addAll(newItems);
        sectionedAdapter.addItemsAtTheEndAndNotify(newItems);

        externalAdapterElementsShouldBe(adapterItems);

        shouldBeSections(0, 3, 8, 10);
    }

    /**
     * Tests the removeItemAndNotify method
     */
    @Test
    public void testRemove()
    {
        initAdapter();

        sectionedAdapter.removeItemAndNotify(5);

        List<MyItem> shouldBe = new ArrayList<>();
        shouldBe.add(adapterItems.get(0));
        shouldBe.add(adapterItems.get(1));
        shouldBe.add(adapterItems.get(2));
        shouldBe.add(adapterItems.get(3));
        shouldBe.add(adapterItems.get(4));

        externalAdapterElementsShouldBe(shouldBe);

        shouldBeSections(0, 3);



        sectionedAdapter.removeItemAndNotify(3);
        sectionedAdapter.removeItemAndNotify(2);
        sectionedAdapter.removeItemAndNotify(2);

        shouldBe = new ArrayList<>();
        shouldBe.add(adapterItems.get(0));
        shouldBe.add(adapterItems.get(1));

        externalAdapterElementsShouldBe(shouldBe);

        shouldBeSections(0);
    }

    /**
     * Tests the setItemsAndNotifyDataSetChanged method
     */
    @Test
    public void testChange()
    {
        initAdapter();

        List<MyItem> newItems = new ArrayList<>();
        newItems.add(new MyItem("new1", sectionA));
        newItems.add(new MyItem("new2", sectionB));
        newItems.add(new MyItem("new3", sectionB));

        sectionedAdapter.setItemsAndNotifyDataSetChanged(newItems);

        externalAdapterElementsShouldBe(newItems);

        shouldBeSections(0, 2);
    }

    /**
     * Tests the enableSectioning method
     */
    @Test
    public void testEnableSectioning()
    {
        initAdapter();

        sectionedAdapter.enableSectioning(false);
        sectionedAdapter.setItemsAndNotifyDataSetChanged(adapterItems);

        externalAdapterElementsShouldBe(adapterItems);

        assertEquals("Wrong number of internal elements", adapterItems.size(), sectionedAdapter.getAdapter().getItemCount());

        shouldBeSections();
    }

    /*@Test
    public void testMove()
    {
        // TODO need to call it from touch helper (need RecyclerView), not directly moveItemAndNotify

        initAdapter();

        sectionedAdapter.moveItemAndNotify(1, 4, sectionA, sectionB);

        List<MyItem> shouldBe = new ArrayList<>();
        shouldBe.add(adapterItems.get(0));
        shouldBe.add(adapterItems.get(2));
        shouldBe.add(adapterItems.get(3));
        shouldBe.add(adapterItems.get(4));
        shouldBe.add(adapterItems.get(1));
        shouldBe.add(adapterItems.get(5));

        externalAdapterElementsShouldBe(shouldBe);
    }*/

    /*@Test
    public void testEmptyView()
    {
        // TODO View not mocked/working

        View view = new View(context);

        initAdapter();
        sectionedAdapter.setEmptyView(view);
        assertFalse("Empty view should be invisible", View.VISIBLE==view.getVisibility());

        sectionedAdapter = new MyAdapter(null);
        assertTrue("Empty view should be visible", View.VISIBLE==view.getVisibility());
    }*/









    /************************** HELPER CLASSES AND METHODS *************************/



    private static class MyItem implements Sectioned
    {
        private String name;
        private Section section;

        public MyItem(String name, Section section)
        {
            this.name = name;
            this.section = section;
        }

        @Override
        public Section getSection()
        {
            return section;
        }

        @Override
        public boolean equals(Object o)
        {
            return o instanceof MyItem && Objects.equals(name, ((MyItem) o).name) && Objects.equals(section, ((MyItem) o).section);
        }
    }

    private static class MyAdapter extends SectionedRecyclerViewAdapter<MyItem>
    {
        public MyAdapter(List<MyItem> items)
        {
            setAdapterFromSubclass(new MyInternal(items));
        }

        private class MyInternal extends SectionedInternalAdapter
        {
            MyInternal(List<MyItem> itemsList)
            {
                super(itemsList);
            }

            @Override
            protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType)
            {
                return null;
            }

            @Override
            protected void onBindItemViewHolder(RecyclerView.ViewHolder itemHolder, int actualItemPosition)
            {

            }
        }
    }

    private void initAdapter()
    {
        adapterItems = new ArrayList<>();
        adapterItems.add(new MyItem("Item1", sectionA));
        adapterItems.add(new MyItem("Item2", sectionA));
        adapterItems.add(new MyItem("Item3", sectionB));
        adapterItems.add(new MyItem("Item4", sectionB));
        adapterItems.add(new MyItem("Item5", sectionB));
        adapterItems.add(new MyItem("Item6", sectionB));

        sectionedAdapter = new MyAdapter(adapterItems);
    }

    private void externalAdapterElementsShouldBe(List<MyItem> shouldBe)
    {
        assertEquals("Wrong number of elements", sectionedAdapter.getItemCount(), shouldBe.size());

        MyItem item;
        for(int i=0; i<shouldBe.size(); i++)
        {
            item = sectionedAdapter.get(i);
            assertNotNull("Item is null", item);
            assertEquals("Wrong item", shouldBe.get(i), item);
        }
    }

    private void shouldBeSections(Integer... sections)
    {
        int type;
        for(int i=0; i<sectionedAdapter.getAdapter().getItemCount(); i++)
        {
            type = sectionedAdapter.getAdapter().getItemViewType(i);

            if(Arrays.asList(sections).contains(i)) assertEquals(i+" not a section", SectionedRecyclerViewAdapter.VIEW_TYPE_SECTION, type);
            else assertEquals(i+" not an item", SectionedRecyclerViewAdapter.VIEW_TYPE_ITEM, type);
        }
    }
}
