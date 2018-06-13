package cn.jufuns.ws;

import android.app.Application;

public class GlobalApp extends Application {

    private static GlobalApp instance;

    public static GlobalApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (null == instance) {
            instance = this;
        }

    }

}
