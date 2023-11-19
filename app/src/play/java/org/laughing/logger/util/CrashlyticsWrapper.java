package org.laughing.logger.util;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * Wrapper for play build flavor to initialize Crashlytics.
 */
public class CrashlyticsWrapper {
    public static void initCrashlytics(Context context) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
    }
}
