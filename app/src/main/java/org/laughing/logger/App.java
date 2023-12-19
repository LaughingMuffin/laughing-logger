package org.laughing.logger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.laughing.logger.util.CrashlyticsWrapper;

public class App extends Application {
    public static final String MUFFIN_ADS = "MUFFIN-ADS";
    private static App instance;
    private SharedPreferences preferences;

    public App() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CrashlyticsWrapper.initCrashlytics(this);

        // Log the Mobile Ads SDK version.
        Log.d(MUFFIN_ADS, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.i(MUFFIN_ADS, "onInitializationComplete: Init Done!");
            }
        });
    }

    public static App get() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return preferences;
    }

    @ColorInt
    public static int getColorFromAttr(Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        if (context != null && context.getTheme().resolveAttribute(attr, typedValue, true))
            return typedValue.data;
        else
            return Color.RED;
    }
}
