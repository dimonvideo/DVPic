package ua.cv.westward.dvpic;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    // true if application on foreground
    public static boolean isForeground = false;

    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sAppContext = this;
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
