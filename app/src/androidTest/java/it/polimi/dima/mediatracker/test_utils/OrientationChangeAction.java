package it.polimi.dima.mediatracker.test_utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.Collection;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * An Espresso ViewAction that changes the orientation of the screen
 *
 * TODO: "android.view.ContextThemeWrapper cannot be cast to android.app.Activity" when a dialog is open --> problem in the caller onView(!isRoot()!).perform(orientationLandscape())?
 */
public class OrientationChangeAction implements ViewAction
{
    private final int orientation;

    private OrientationChangeAction(int orientation)
    {
        this.orientation = orientation;
    }

    @Override
    public Matcher<View> getConstraints()
    {
        return isRoot();
    }

    @Override
    public String getDescription()
    {
        return "change orientation to " + orientation;
    }

    @Override
    public void perform(UiController uiController, View view)
    {
        uiController.loopMainThreadUntilIdle();
        final Activity activity = (Activity) view.getContext();
        activity.setRequestedOrientation(orientation);

        Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        if(resumedActivities.isEmpty())
        {
            throw new RuntimeException("Could not change orientation");
        }
    }

    public static ViewAction orientationLandscape()
    {
        return new OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static ViewAction orientationPortrait()
    {
        return new OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}