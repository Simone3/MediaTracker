package it.polimi.dima.mediatracker.utils;

import com.orm.SugarApp;
//import com.squareup.leakcanary.LeakCanary;

public class MediaTracker extends SugarApp
{
    @Override public void onCreate()
    {
        super.onCreate();
        //LeakCanary.install(this);
    }
}