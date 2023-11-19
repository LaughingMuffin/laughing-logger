package org.laughing.logger;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.laughing.logger.R;

import org.laughing.logger.data.LogLine;
import org.laughing.logger.data.SearchCriteria;
import org.laughing.logger.helper.PreferenceHelper;
import org.laughing.logger.helper.SaveLogHelper;
import org.laughing.logger.helper.ServiceHelper;
import org.laughing.logger.helper.WidgetHelper;
import org.laughing.logger.reader.LogcatReader;
import org.laughing.logger.reader.LogcatReaderLoader;
import org.laughing.logger.ui.LogcatActivity;
import org.laughing.logger.util.ArrayUtil;
import org.laughing.logger.util.LogLineAdapterUtil;
import org.laughing.logger.util.UtilLogger;

import java.io.IOException;
import java.util.Random;

/**
 * Reads logs.
 *
 * @author nolan
 */
public class LogcatRecordingService extends IntentService {

    public static final String URI_SCHEME = "catlog_recording_service";
    public static final String EXTRA_FILENAME = "filename";
    public static final String EXTRA_LOADER = "loader";
    public static final String EXTRA_QUERY_FILTER = "filter";
    public static final String EXTRA_LEVEL = "level";
    private static final String ACTION_STOP_RECORDING = "com.pluscubed.catlog.action.STOP_RECORDING";
    private static UtilLogger log = new UtilLogger(LogcatRecordingService.class);
    private final Object lock = new Object();
    private LogcatReader mReader;
    private boolean mKilled;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log.d("onReceive()");

            // received broadcast to kill service
            killProcess();
            ServiceHelper.stopBackgroundServiceIfRunning(context);
        }
    };

    private Handler handler;


    public LogcatRecordingService() {
        super("AppTrackerService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        log.d("onCreate()");

        IntentFilter intentFilter = new IntentFilter(ACTION_STOP_RECORDING);
        intentFilter.addDataScheme(URI_SCHEME);

        try {
            registerReceiver(receiver, intentFilter);
        } catch (SecurityException securityException) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED);
        }

        handler = new Handler(Looper.getMainLooper());
    }


    private void initializeReader(Intent intent) {
        try {
            // use the "time" log so we can see what time the logs were logged at
            LogcatReaderLoader loader = intent.getParcelableExtra(EXTRA_LOADER);
            mReader = loader.loadReader();

            while (mReader != null && !mReader.readyToRecord() && !mKilled) {
                mReader.readLine();
                // keep skipping lines until we find one that is past the last log line, i.e.
                // it's ready to record
            }
            if (!mKilled) {
                makeToast(R.string.log_recording_started, Toast.LENGTH_SHORT);
            }
        } catch (IOException e) {
            log.d(e, "");
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        log.d("onDestroy()");
        killProcess();

        unregisterReceiver(receiver);

        stopForeground(true);

        WidgetHelper.updateWidgets(getApplicationContext(), false);
    }

    // This is the old onStart method that will be called on the pre-2.0
    // platform.
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        log.d("onStart()");
        handleCommand();
    }

    private void handleCommand() {

        // notify the widgets that we're running
        WidgetHelper.updateWidgets(getApplicationContext());

        CharSequence tickerText = getText(R.string.notification_ticker);

        Intent stopRecordingIntent = new Intent();
        stopRecordingIntent.setAction(ACTION_STOP_RECORDING);
        // have to make this unique for God knows what reason
        stopRecordingIntent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://stop/"),
                Long.toHexString(new Random().nextLong())));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0 /* no requestCode */, stopRecordingIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        final String CHANNEL_ID = "laughing_logger_logging_channel";
        // Set the icon, scrolling text and timestamp
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        notification.setSmallIcon(R.drawable.notif_icon);
        notification.setTicker(tickerText);
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle(getString(R.string.notification_title));
        notification.setContentText(getString(R.string.notification_subtext));
        notification.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // Fix Oreo notifications showing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final CharSequence name = getString(R.string.app_name);
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            manager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(R.string.notification_title, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(R.string.notification_title, notification.build());
        }
    }

    protected void onHandleIntent(Intent intent) {
        log.d("onHandleIntent()");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        log.d("Starting up %s now with intent: %s", LogcatRecordingService.class.getSimpleName(), intent);

        String filename = intent.getStringExtra(EXTRA_FILENAME);
        String queryText = intent.getStringExtra(EXTRA_QUERY_FILTER);
        String logLevel = intent.getStringExtra(EXTRA_LEVEL);

        SearchCriteria searchCriteria = new SearchCriteria(queryText);

        CharSequence[] logLevels = getResources().getStringArray(R.array.log_levels_values);
        int logLevelLimit = ArrayUtil.indexOf(logLevels, logLevel);

        boolean searchCriteriaWillAlwaysMatch = searchCriteria.isEmpty();
        boolean logLevelAcceptsEverything = logLevelLimit == 0;

        SaveLogHelper.deleteLogIfExists(filename);

        initializeReader(intent);

        StringBuilder stringBuilder = new StringBuilder();

        try {

            String line;
            int lineCount = 0;
            int logLinePeriod = PreferenceHelper.getLogLinePeriodPreference(this);
            String filterPattern = PreferenceHelper.getFilterPatternPreference(this);
            while (mReader != null && (line = mReader.readLine()) != null && !mKilled) {

                // filter
                if (!searchCriteriaWillAlwaysMatch || !logLevelAcceptsEverything) {
                    if (!checkLogLine(line, searchCriteria, logLevelLimit, filterPattern)) {
                        continue;
                    }
                }

                stringBuilder.append(line).append("\n");

                if (++lineCount % logLinePeriod == 0) {
                    // avoid OutOfMemoryErrors; flush now
                    SaveLogHelper.saveLog(stringBuilder, filename);
                    stringBuilder.delete(0, stringBuilder.length()); // clear
                }
            }
        } catch (IOException e) {
            log.e(e, "unexpected exception");
        } finally {
            killProcess();
            log.d("CatlogService ended");

            boolean logSaved = SaveLogHelper.saveLog(stringBuilder, filename);

            if (logSaved) {
                makeToast(R.string.log_saved, Toast.LENGTH_SHORT);
                startLogcatActivityToViewSavedFile(filename);
            } else {
                makeToast(R.string.unable_to_save_log, Toast.LENGTH_LONG);
            }
        }
    }

    private boolean checkLogLine(String line, SearchCriteria searchCriteria, int logLevelLimit, String filterPattern) {
        LogLine logLine = LogLine.newLogLine(line, false, filterPattern);
        return searchCriteria.matches(logLine)
                && LogLineAdapterUtil.logLevelIsAcceptableGivenLogLevelLimit(logLine.getLogLevel(), logLevelLimit);
    }


    private void startLogcatActivityToViewSavedFile(String filename) {

        // start up the logcat activity if necessary and show the saved file

        Intent targetIntent = new Intent(getApplicationContext(), LogcatActivity.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        targetIntent.setAction(Intent.ACTION_MAIN);
        targetIntent.putExtra("filename", filename);

        startActivity(targetIntent);

    }


    private void makeToast(final int stringResId, final int toastLength) {
        handler.post(Toast.makeText(LogcatRecordingService.this, stringResId, toastLength)::show);

    }

    private void killProcess() {
        if (!mKilled) {
            synchronized (lock) {
                if (!mKilled && mReader != null) {
                    // kill the logcat process
                    mReader.killQuietly();
                    mKilled = true;
                }
            }
        }
    }

}
