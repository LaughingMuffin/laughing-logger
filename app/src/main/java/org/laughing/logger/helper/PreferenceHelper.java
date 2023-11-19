package org.laughing.logger.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import org.laughing.logger.R;
import org.laughing.logger.data.ColorScheme;
import org.laughing.logger.util.StringUtil;
import org.laughing.logger.util.UtilLogger;
import org.laughing.logger.widget.MultipleChoicePreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreferenceHelper {

    private static final String WIDGET_EXISTS_PREFIX = "widget_";
    private static float textSize = -1;
    private static Character defaultLogLevel = null;
    private static Boolean showTimestampAndPid = null;
    private static ColorScheme colorScheme = null;
    private static int displayLimit = -1;
    private static String filterPattern = null;
    private static UtilLogger log = new UtilLogger(PreferenceHelper.class);

    public static void clearCache() {
        defaultLogLevel = null;
        filterPattern = null;
        textSize = -1;
        showTimestampAndPid = null;
        colorScheme = null;
        displayLimit = -1;
    }

    /**
     * Record that we managed to get root in JellyBean.
     *
     * @param context
     * @return
     */
    public static void setJellybeanRootRan(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();
        editor.putBoolean(context.getString(R.string.pref_ran_jellybean_su_update), true);
        editor.commit();
    }

    /**
     * Return true if we have root in jelly bean.
     *
     * @param context
     * @return
     */
    public static boolean getJellybeanRootRan(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(context.getString(R.string.pref_ran_jellybean_su_update), false);
    }

    public static boolean getWidgetExistsPreference(Context context, int appWidgetId) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String widgetExists = WIDGET_EXISTS_PREFIX.concat(Integer.toString(appWidgetId));

        return sharedPrefs.getBoolean(widgetExists, false);
    }

    public static void setWidgetExistsPreference(Context context, int[] appWidgetIds) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sharedPrefs.edit();

        for (int appWidgetId : appWidgetIds) {
            String widgetExists = WIDGET_EXISTS_PREFIX.concat(Integer.toString(appWidgetId));
            editor.putBoolean(widgetExists, true);
        }

        editor.apply();

    }

    public static int getDisplayLimitPreference(Context context) {

        if (displayLimit == -1) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String defaultValue = context.getText(R.string.pref_display_limit_default).toString();

            String intAsString = sharedPrefs.getString(context.getText(R.string.pref_display_limit).toString(), defaultValue);

            try {
                displayLimit = Integer.parseInt(intAsString);
            } catch (NumberFormatException e) {
                displayLimit = Integer.parseInt(defaultValue);
            }
        }

        return displayLimit;
    }

    public static String getFilterPatternPreference(Context context) {

        if (filterPattern == null) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String defaultValue = context.getText(R.string.pref_filter_pattern_default).toString();

            filterPattern = sharedPrefs.getString(context.getText(R.string.pref_filter_pattern).toString(), defaultValue);

        }

        return filterPattern;
    }

    public static void setFilterPatternPreference(Context context, String value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putString(context.getText(R.string.pref_filter_pattern).toString(), value);

        editor.apply();
    }

    public static int getLogLinePeriodPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String defaultValue = context.getText(R.string.pref_log_line_period_default).toString();

        String intAsString = sharedPrefs.getString(context.getText(R.string.pref_log_line_period).toString(), defaultValue);

        try {
            return Integer.parseInt(intAsString);
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultValue);
        }
    }

    public static void setDisplayLimitPreference(Context context, int value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putString(context.getText(R.string.pref_display_limit).toString(), Integer.toString(value));

        editor.apply();
    }

    public static void setLogLinePeriodPreference(Context context, int value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putString(context.getText(R.string.pref_log_line_period).toString(), Integer.toString(value));

        editor.apply();
    }

    public static char getDefaultLogLevelPreference(Context context) {

        if (defaultLogLevel == null) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String logLevelPref = sharedPrefs.getString(
                    context.getText(R.string.pref_default_log_level).toString(),
                    context.getText(R.string.log_level_value_verbose).toString());

            defaultLogLevel = logLevelPref.charAt(0);
        }

        return defaultLogLevel;


    }

    public static float getTextSizePreference(Context context) {

        if (textSize == -1) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String textSizePref = sharedPrefs.getString(
                    context.getText(R.string.pref_text_size).toString(),
                    context.getText(R.string.text_size_medium_value).toString());

            if (textSizePref.contentEquals(context.getText(R.string.text_size_xsmall_value))) {
                cacheTextsize(context, R.dimen.text_size_xsmall);
            } else if (textSizePref.contentEquals(context.getText(R.string.text_size_small_value))) {
                cacheTextsize(context, R.dimen.text_size_small);
            } else if (textSizePref.contentEquals(context.getText(R.string.text_size_medium_value))) {
                cacheTextsize(context, R.dimen.text_size_medium);
            } else if (textSizePref.contentEquals(context.getText(R.string.text_size_large_value))) {
                cacheTextsize(context, R.dimen.text_size_large);
            } else { // xlarge
                cacheTextsize(context, R.dimen.text_size_xlarge);
            }
        }

        return textSize;

    }

    private static void cacheTextsize(Context context, int dimenId) {

        float unscaledSize = context.getResources().getDimension(dimenId);

        log.d("unscaledSize is %g", unscaledSize);

        textSize = unscaledSize;
    }

    public static boolean getShowTimestampAndPidPreference(Context context) {

        if (showTimestampAndPid == null) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            showTimestampAndPid = sharedPrefs.getBoolean(context.getText(R.string.pref_show_timestamp).toString(), true);
        }

        return showTimestampAndPid;

    }

    public static boolean getHidePartialSelectHelpPreference(Context context) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(
                context.getText(R.string.pref_hide_partial_select_help).toString(), false);
    }

    public static void setHidePartialSelectHelpPreference(Context context, boolean bool) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putBoolean(context.getString(R.string.pref_hide_partial_select_help), bool);

        editor.apply();

    }

    public static boolean getExpandedByDefaultPreference(Context context) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(
                context.getText(R.string.pref_expanded_by_default).toString(), false);
    }

    public static ColorScheme getColorScheme(Context context) {

        if (colorScheme == null) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String colorSchemeName = sharedPrefs.getString(
                    context.getText(R.string.pref_theme).toString(), context.getText(ColorScheme.Light.getNameResource()).toString());

            colorScheme = ColorScheme.findByPreferenceName(colorSchemeName, context);
        }

        return colorScheme;

    }

    public static void setColorScheme(Context context, ColorScheme colorScheme) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putString(context.getString(R.string.pref_theme), context.getText(colorScheme.getNameResource()).toString());

        editor.apply();

    }

    public static List<String> getBuffers(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String defaultValue = context.getString(R.string.pref_buffer_choice_main_value);
        String key = context.getString(R.string.pref_buffer);

        String value = sharedPrefs.getString(key, defaultValue);

        return Arrays.asList(StringUtil.split(value, MultipleChoicePreference.DELIMITER));
    }

    public static List<String> getBufferNames(Context context) {
        List<String> buffers = getBuffers(context);

        List<String> bufferNames = new ArrayList<>();

        // TODO: this is inefficient - O(n^2)
        for (String buffer : buffers) {
            int idx = Arrays.asList(context.getResources().getStringArray(
                    R.array.pref_buffer_choice_values)).indexOf(buffer);
            bufferNames.add(context.getResources().getStringArray(R.array.pref_buffer_choices)[idx]);
        }
        return bufferNames;
    }

    public static void setBuffer(Context context, int stringResId) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String key = context.getString(R.string.pref_buffer);
        String value = context.getString(stringResId);

        Editor editor = sharedPrefs.edit();

        editor.putString(key, value);

        editor.apply();
    }

    public static boolean getIncludeDeviceInfoPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(context.getString(R.string.pref_include_device_info), true);
    }

    public static void setIncludeDeviceInfoPreference(Context context, boolean value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sharedPrefs.edit();
        editor.putBoolean(context.getString(R.string.pref_include_device_info), value);
        editor.apply();
    }

    public static boolean isScrubberEnabled(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("scrubber", false);
    }

    public static boolean getIncludeDmesgPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(context.getString(R.string.pref_include_dmesg), true);
    }

    public static void setIncludeDmesgPreference(Context context, boolean value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sharedPrefs.edit();
        editor.putBoolean(context.getString(R.string.pref_include_dmesg), value);
        editor.apply();
    }
}
