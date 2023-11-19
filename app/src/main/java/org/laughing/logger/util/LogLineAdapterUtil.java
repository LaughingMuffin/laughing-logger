package org.laughing.logger.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.laughing.logger.R;
import org.laughing.logger.data.ColorScheme;
import org.laughing.logger.helper.PreferenceHelper;

import java.util.HashMap;

public class LogLineAdapterUtil {

    public static final int LOG_WTF = 100; // arbitrary int to signify 'wtf' log level

    private static final int NUM_COLORS = 17;

    @SuppressLint("UseSparseArrays")
    private static final HashMap<Integer, Integer> BACKGROUND_COLORS = new HashMap<>(6);
    @SuppressLint("UseSparseArrays")
    private static final HashMap<Integer, Integer> FOREGROUND_COLORS = new HashMap<>(6);

    static {
        BACKGROUND_COLORS.put(Log.DEBUG, R.color.background_debug);
        BACKGROUND_COLORS.put(Log.ERROR, R.color.background_error);
        BACKGROUND_COLORS.put(Log.INFO, R.color.background_info);
        BACKGROUND_COLORS.put(Log.VERBOSE, R.color.background_verbose);
        BACKGROUND_COLORS.put(Log.WARN, R.color.background_warn);
        BACKGROUND_COLORS.put(LOG_WTF, R.color.background_wtf);
        FOREGROUND_COLORS.put(Log.DEBUG, R.color.foreground_debug);
        FOREGROUND_COLORS.put(Log.ERROR, R.color.foreground_error);
        FOREGROUND_COLORS.put(Log.INFO, R.color.foreground_info);
        FOREGROUND_COLORS.put(Log.VERBOSE, R.color.foreground_verbose);
        FOREGROUND_COLORS.put(Log.WARN, R.color.foreground_warn);
        FOREGROUND_COLORS.put(LOG_WTF, R.color.foreground_wtf);
    }

    public static int getBackgroundColorForLogLevel(Context context, int logLevel) {
        Integer result = BACKGROUND_COLORS.get(logLevel);
        if (result == null) result = android.R.color.black;
        return ContextCompat.getColor(context, result);
    }

    public static int getForegroundColorForLogLevel(Context context, int logLevel) {
        Integer result = FOREGROUND_COLORS.get(logLevel);
        if (result == null) result = android.R.color.primary_text_dark;
        return ContextCompat.getColor(context, result);
    }

    public static synchronized int getOrCreateTagColor(Context context, String tag) {

        int hashCode = (tag == null) ? 0 : tag.hashCode();

        int smear = Math.abs(hashCode) % NUM_COLORS;

        return getColorAt(smear, context);

    }

    private static int getColorAt(int i, Context context) {

        ColorScheme colorScheme = PreferenceHelper.getColorScheme(context);

        int[] colorArray = colorScheme.getTagColors(context);

        return colorArray[i];

    }

    public static boolean logLevelIsAcceptableGivenLogLevelLimit(int logLevel, int logLevelLimit) {

        int minVal;
        switch (logLevel) {

            case Log.VERBOSE:
                minVal = 0;
                break;
            case Log.DEBUG:
                minVal = 1;
                break;
            case Log.INFO:
                minVal = 2;
                break;
            case Log.WARN:
                minVal = 3;
                break;
            case Log.ERROR:
                minVal = 4;
                break;
            case LOG_WTF:
                minVal = 5;
                break;
            default: // e.g. the starting line that says "output of log such-and-such"
                return true;
        }

        return minVal >= logLevelLimit;

    }
}
