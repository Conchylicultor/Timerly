package com.pot.timerly;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by pot on 14/02/16.
 */
public class RecordingBackground extends Service {
    private final int ID_NOTIFICATION = 101; // Define cst in the resource file ??

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private BackgroundThread mBackgroundThread;

    private long mDuration;
    private Date mStartDate;

    public RecordingBackground() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Timerly", "Service creating...");

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public class BackgroundThread extends Thread {
        private boolean isRunning = true;

        public void exit() {
            isRunning = false;
        }

        public void run() {
            while(isRunning)
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(isRunning) { // Is running can change it's state while in the loop
                    updateNotification();
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
        Log.d("Timerly", "Service starting...");

        // Get starting date and currentDuration from the intent
        Bundle bundle = intent.getExtras();
        mStartDate = new Date(bundle.getLong(MainActivity.RECORDING_START));
        mDuration = bundle.getLong(MainActivity.RECORDING_DURATION);

        // Create the notification
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_play);
        mBuilder.setContentTitle("Recording");
        mBuilder.setContentText(getCurrentRecordingText());
        mBuilder.setUsesChronometer(true);

        // TODO: Define actions

        Intent resultIntent = new Intent(this, MainActivity.class);
        // Warning: Create a new activity!!! Does not restore the old one
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(ID_NOTIFICATION, mBuilder.build());

        mBackgroundThread = new BackgroundThread();
        mBackgroundThread.start();

        return START_REDELIVER_INTENT;
    }

    private void updateNotification() {
        Log.d("Timerly", "Still running...");
        mBuilder.setContentText(getCurrentRecordingText());
        mNotificationManager.notify(ID_NOTIFICATION, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Timerly", "Service biding...");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Timerly", "Service closing...");

        mBackgroundThread.exit();
        mNotificationManager.cancel(ID_NOTIFICATION); // Do nothing if no notification currently
    }


    // TODO: This is a copy from getCurrentRecordingText (merge the wo functions)
    public String getCurrentRecordingText() {
        Date mEndDate   = new Date();// Set end date

        long duration  = mDuration + mEndDate.getTime() - mStartDate.getTime();

        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        duration -= diffInHours*60*60*1000;
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= diffInMinutes*60*1000;
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        duration -= diffInSeconds*1000;
        long diffInMillisecond = TimeUnit.MILLISECONDS.toMillis(duration)/100;

        // TODO: Improve format (Duration with java 8)
        String timeStr = diffInHours + "h " + diffInMinutes + "min " + diffInSeconds + "." + diffInMillisecond +"sec";

        return timeStr;
    }
}
