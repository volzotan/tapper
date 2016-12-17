package de.volzo.tapper;

import android.app.Application;
import android.content.Context;

/**
 * Created by volzotan on 17.12.16.
 */

public class App extends Application {
    public static Context context;

    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}