package com.orm;

import android.app.Application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycleApplication;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

import it.polimi.dima.mediatracker.BuildConfig;

/**
 * Class needed to make Robolectric and Sugar work together
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TestSugarApp extends Application implements TestLifecycleApplication
{
    @Test
    public void startEverTestSugarAppAsFirst()
    {

    }

    @Override
    public void beforeTest(Method method)
    {

    }

    @Override
    public void prepareTest(Object test)
    {

    }

    @Override
    public void afterTest(Method method)
    {

    }
}
