package it.polimi.dima.mediatracker.adapters.drawer;

import android.content.Context;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.mediatracker.BuildConfig;
import it.polimi.dima.mediatracker.R;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests the navigation drawer adapter
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DrawerAdapterUnitTest
{
    private static DrawerAdapter adapter;

    private static List<DrawerElement> drawerElements;

    private static Context context;

    @BeforeClass
    public static void beforeClass()
    {
        context = RuntimeEnvironment.application;

        drawerElements = new ArrayList<>();

        List<DrawerSubElement> subElements = new ArrayList<>();
        subElements.add(new DrawerSubElement("SUB1", R.drawable.ic_book, 0));
        subElements.add(new DrawerSubElement("SUB2", R.drawable.ic_book, 0));
        drawerElements.add(new DrawerElement("NAME1", R.drawable.ic_book, 0, subElements));

        drawerElements.add(new DrawerElement("NAME2", R.drawable.ic_book, 0, null));

        subElements = new ArrayList<>();
        subElements.add(new DrawerSubElement("SUB3", R.drawable.ic_book, 0));
        drawerElements.add(new DrawerElement("NAME3", R.drawable.ic_book, 0, subElements));
    }

    /**
     * Number of elements in the drawer (selected element has sub-elements)
     */
    @Test
    public void testGetCountWithSubElements()
    {
        adapter = new DrawerAdapter(context, drawerElements, 0, -1, null);

        int size = drawerElements.size() + 2;
        assertEquals("Elements amount incorrect", size, adapter.getItemCount());
    }

    /**
     * Number of elements in the drawer (selected element has no sub-elements)
     */
    @Test
    public void testGetCountWithoutSubElements()
    {
        adapter = new DrawerAdapter(context, drawerElements, 1, -1, null);

        int size = drawerElements.size();
        assertEquals("Elements amount incorrect", size, adapter.getItemCount());
    }

    /**
     * Test elements vs. sub-elements (selected element has sub-elements)
     */
    @Test
    public void testViewTypeWithSubElements()
    {
        adapter = new DrawerAdapter(context, drawerElements, 2, -1, null);

        assertEquals("Wrong view type", DrawerAdapter.ELEMENT_TYPE, adapter.getItemViewType(0));
        assertEquals("Wrong view type", DrawerAdapter.ELEMENT_TYPE, adapter.getItemViewType(1));
        assertEquals("Wrong view type", DrawerAdapter.ELEMENT_TYPE, adapter.getItemViewType(2));
        assertEquals("Wrong view type", DrawerAdapter.SUB_ELEMENT_TYPE, adapter.getItemViewType(3));
    }

    /**
     * Test elements vs. sub-elements (selected element has no sub-elements)
     */
    @Test
    public void testViewTypeWithoutSubElements()
    {
        adapter = new DrawerAdapter(context, drawerElements, 1, -1, null);

        assertEquals("Wrong view type", DrawerAdapter.ELEMENT_TYPE, adapter.getItemViewType(0));
        assertEquals("Wrong view type", DrawerAdapter.ELEMENT_TYPE, adapter.getItemViewType(1));
        assertEquals("Wrong view type", DrawerAdapter.ELEMENT_TYPE, adapter.getItemViewType(2));
    }
}
