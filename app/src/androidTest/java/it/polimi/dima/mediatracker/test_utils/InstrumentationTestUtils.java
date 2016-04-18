package it.polimi.dima.mediatracker.test_utils;

import android.support.annotation.NonNull;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Calendar;

import it.polimi.dima.mediatracker.model.Category;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withResourceName;
import static android.support.test.internal.util.Checks.checkNotNull;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Some utilities for testing, mainly matchers for Espresso
 */
public class InstrumentationTestUtils
{
    /**
     * Matches the item in a list with the adapter value equal to the given one
     */
    public static Matcher<Object> withValue(final Object value)
    {
        return new BoundedMatcher<Object, Object>(Object.class)
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText("has value " + value);
            }

            @Override
            public boolean matchesSafely(Object item)
            {
                return item.equals(value);
            }
        };
    }

    /**
     * Matcher for categories list, matches the one with the given name
     */
    public static Matcher<Object> withCategoryName(final String value)
    {
        return new BoundedMatcher<Object, Category>(Category.class)
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText("has category name " + value);
            }

            @Override
            public boolean matchesSafely(Category item)
            {
                return item.getName().equals(value);
            }
        };
    }

    /**
     * Matches a list with the given number of elements
     */
    public static Matcher<View> withListSize(final int size)
    {
        return new TypeSafeMatcher<View>()
        {
            @Override
            public boolean matchesSafely (final View view)
            {
                return ((ListView) view).getChildCount() == size;
            }

            @Override public void describeTo (final Description description)
            {
                description.appendText("ListView should have " + size + " items");
            }
        };
    }

    /**
     * Matches a grid with the given number of elements
     */
    public static Matcher<View> withGridSize(final int size)
    {
        return new TypeSafeMatcher<View>()
        {
            @Override
            public boolean matchesSafely (final View view)
            {
                return ((GridView) view).getChildCount() == size;
            }

            @Override public void describeTo (final Description description)
            {
                description.appendText("GridView should have " + size + " items");
            }
        };
    }

    /**
     * Util to get a random name
     */
    public static String getRandomName()
    {
        // Take the current time in milliseconds, should be unique...!
        return String.valueOf(Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Matches the item of a RecyclerView at the given position matching the given itemMatcher
     */
    public static Matcher<View> recyclerViewAtPosition(final int position, @NonNull final Matcher<View> itemMatcher)
    {
        checkNotNull(itemMatcher);

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view)
            {
                if(position>=view.getChildCount()) return itemMatcher.matches(false);
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForLayoutPosition(position);//findViewHolderForAdapterPosition(position);
                return viewHolder!=null && itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    /**
     * Action to click on a child of the current view with the given ID
     */
    public static ViewAction clickChildViewWithId(final int id)
    {
        return new ViewAction()
        {
            @Override
            public Matcher<View> getConstraints()
            {
                return null;
            }

            @Override
            public String getDescription()
            {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                View v = view.findViewById(id);

                if (v != null)
                {
                    v.performClick();
                }
            }
        };
    }

    /**
     * Matches the n-th child of the given parent
     */
    public static Matcher<View> nthChildOf(final int n, final Matcher<View> parentMatcher)
    {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with "+n+"th child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {

                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }
                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent()) && group.getChildAt(n).equals(view);
            }
        };
    }

    /**
     * Helper to click on the pos-th item in a SelectDialog
     */
    public static void clickOnDialogOptionAtPosition(int pos)
    {
        onView(nthChildOf(pos, withResourceName(equalTo("select_dialog_listview")))).perform(click());
    }
}
