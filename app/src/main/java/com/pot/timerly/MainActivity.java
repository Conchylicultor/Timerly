package com.pot.timerly;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.support.DividerItemDecoration;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Handler mRecordingHandler;
    private final int UPDATE_INTERVAL = 100; // 100ms
    private long mDuration = 0;// Combine the durations
    private Date mStartDate = null;
    private Date mEndDate = null;

    private TextView mRecoringText; // Correspond to the timer text

    private FloatingActionButton mFab;

    private NotificationManager mNotificationManager;
    private AsyncTask<Integer, Integer, Integer> mRecordingTask;

    final int ID_NOTIFICATION = 101; // TODO: Define cst in the ressource files ??

    // Runable (executable code run every x ms) which update the timer
    Runnable mRecordingRunable = new Runnable() {
        @Override
        public void run() {
            // Update the GUI:
            mRecoringText.setText(getCurrentRecordingText());
            mRecordingHandler.postDelayed(mRecordingRunable, UPDATE_INTERVAL);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loading dataset (Here ???)



        // final List<RecordingItem> myDataset = RecordingItem.generateItemList(2);

        // Set up the timer
        mRecordingHandler = new Handler();

        // Configure the recycler view

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true); // For improving performance (static size)
        // Linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Specify the adapter
        mAdapter = new MyAdapter(getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        // Configure other GUI elements

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecoringText = (TextView) findViewById(R.id.recording_text);
        mRecoringText.setText("Inactive");

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        // TODO: Add animations (elevation change and inc when pressed), + haptic response
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mStartDate == null) { // No recording yet (paused or stoped)
                    // Invert the fab icon
                    AnimatedVectorDrawable iconAnimation = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_play_to_pause);;
                    mFab.setImageDrawable(iconAnimation);
                    iconAnimation.start();

                    // Start the recording
                    mStartDate = new Date();
                    mRecordingRunable.run();
                } else {
                    // Pause the recording
                    pauseRecording();
                }
            }

        });

        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (mDuration > 0 || mStartDate != null) { // No action if we didn't start the recording
                    // User feedback
                    Snackbar.make(view, "Saving data...", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null)
                            .show();

                    // Change GUI
                    // TODO: Hide gui elem
                    mRecoringText.setText("Inactive");

                    // Stop the recording
                    if (mStartDate != null) {
                        pauseRecording();
                    }

                    // Add the new recording to the database(and save it)
                    mAdapter.addItem(new RecordingItem(mDuration, new Date()));
                    mDuration = 0;
                }

                return true; // No other listener called
            }
        });

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pause the GUI update (without stoping nor reinitializing the recording)
        mRecordingHandler.removeCallbacks(mRecordingRunable);

        // Launch the notification (if recording)

        // used to update the progress notification

        if(mStartDate != null) {
            // Building the notification
            final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext());
            nBuilder.setSmallIcon(R.drawable.ic_play);
            nBuilder.setContentTitle("Recording");
            nBuilder.setContentText(getCurrentRecordingText());
            nBuilder.setUsesChronometer(true);
            // TODO: Define actions

            /*Intent resultIntent = new Intent(this, MainActivity.class);
            // Warning: Create a new activity!!! Does not restore the old one
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            nBuilder.setContentIntent(resultPendingIntent);*/

            // TODO: Replace by Service !!
            mRecordingTask = new AsyncTask<Integer, Integer, Integer>() {

                boolean isRunning = true;

                void stopTask() {
                    isRunning = false;
                }

                @Override
                protected void onPreExecute () {
                    super.onPreExecute();
                    mNotificationManager.notify(ID_NOTIFICATION, nBuilder.build());
                }

                @Override
                protected Integer doInBackground (Integer... params) {
                    try {

                        // Update the recording
                        while(isRunning) {
                            //Log.d("Timerly", "Still running...");
                            nBuilder.setContentText(getCurrentRecordingText());
                            mNotificationManager.notify(ID_NOTIFICATION, nBuilder.build());
                            Thread.sleep(500);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };

            // Executes the progress task
            mRecordingTask.execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Restart chrono if it was launched

        if(mStartDate != null) { // Currently a recording: we refresh the GUI
            mRecordingRunable.run();
        }

        // Remove the notification
        if(mRecordingTask != null) {
            mRecordingTask.cancel(true);
            mRecordingTask = null;
        }
        mNotificationManager.cancel(ID_NOTIFICATION); // Do nothing if no notification currently // TODO: Not working
    }

    private void pauseRecording() {
        // Invert the fab icon
        AnimatedVectorDrawable iconAnimation = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_pause_to_play);;
        mFab.setImageDrawable(iconAnimation);
        iconAnimation.start();

        // Pause the recording
        mRecordingHandler.removeCallbacks(mRecordingRunable);
        mDuration += mEndDate.getTime() - mStartDate.getTime(); // Buffer time
        mStartDate = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getCurrentRecordingText() {
        mEndDate   = new Date();// Set end date

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
