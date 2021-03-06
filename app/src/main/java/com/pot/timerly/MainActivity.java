package com.pot.timerly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
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

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mSettingsEditor;

    public static final String RECORDING_START = "recording_start";
    public static final String RECORDING_DURATION = "recording_duration";

    public static final String SETTINGS_URL = "settings";

    private GoogleApiClient mGoogleApiClient;

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
        Log.d("Timerly", "Activity Creation");

        setContentView(R.layout.activity_main);

        // Loading dataset (in the adapter)

        // Set up the timer
        mRecordingHandler = new Handler();

        // Configure the recycler view

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true); // For improving performance (static size)
        // Linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Specify the adapter
        //mAdapter = new MyAdapter(getApplicationContext());
        mAdapter = new MyAdapter(this);
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

                if (mStartDate == null) { // No recording yet (paused or stoped)
                    // Invert the fab icon
                    AnimatedVectorDrawable iconAnimation = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_play_to_pause);

                    mFab.setImageDrawable(iconAnimation);
                    iconAnimation.start();

                    // Start the recording
                    setmStartDate(new Date());
                    mRecordingRunable.run();
                } else {
                    // Pause the recording
                    pauseRecording();
                }
            }

        });

        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (getmDuration() > 0 || mStartDate != null) { // No action if we didn't start the recording
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
                    mAdapter.addItem(new RecordingItem(getmDuration(), new Date()));
                    setmDuration(0);
                }

                return true; // No other listener called
            }
        });

        // Restore states
        mSettings = getSharedPreferences(SETTINGS_URL, 0);
        mSettingsEditor = mSettings.edit();

        long dateTime = mSettings.getLong(RECORDING_START, -1);
        Log.d("Timerly", "Read settings" + String.format(" %d, %d", mDuration, dateTime));
        if (dateTime < 0) {
            Log.d("Timerly", "Reset recording");
            setmDuration(0); // The last state was a pause state => Reset
        }

        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (connectionResult.hasResolution()) {
                            try {
                                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException e) {
                                // Unable to resolve, message user appropriately
                            }
                        } else {
                            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getParent(), 0).show();
                        }
                    }
                })
                .build();
        mGoogleApiClient.connect();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Timerly", "Activity paused");

        // Pause the GUI update (without stoping nor reinitializing the recording)
        mRecordingHandler.removeCallbacks(mRecordingRunable);

        // Launch the notification (if recording)
        if(mStartDate != null) {
            Intent intent = new Intent(getApplicationContext(), RecordingBackground.class);
            intent.putExtra(RECORDING_START, mStartDate.getTime());
            intent.putExtra(RECORDING_DURATION, mDuration);
            getApplicationContext().startService(intent);
            /*AsyncTask recordingTask = new AsyncTask<Integer, Integer, Integer>() {
                @Override
                protected Integer doInBackground (Integer... params) {

                    return null;
                }
            };
            recordingTask.execute();*/
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Restart chrono if it was launched
        mDuration = mSettings.getLong(RECORDING_DURATION, 0);
        long dateTime = mSettings.getLong(RECORDING_START, -1);
        Log.d("Timerly", "Read settings" + String.format(" %d, %d", mDuration, dateTime));
        if (dateTime < 0) {
            Log.d("Timerly", "Restore pause state");
            mStartDate = null;
        } else {
            Log.d("Timerly", "Restore recording");
            mStartDate = new Date(dateTime);
            mFab.setImageResource(R.drawable.ic_pause);
        }

        if(mStartDate != null) { // Currently a recording: we refresh the GUI
            mRecordingRunable.run();
        }

        // Remove the eventual notification
        stopService(new Intent(getApplicationContext(), RecordingBackground.class));
    }

    private void pauseRecording() {
        // Invert the fab icon
        AnimatedVectorDrawable iconAnimation = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_pause_to_play);;
        mFab.setImageDrawable(iconAnimation);
        iconAnimation.start();

        // Pause the recording
        mRecordingHandler.removeCallbacks(mRecordingRunable);
        setmDuration(mDuration + mEndDate.getTime() - mStartDate.getTime()); // Buffer time
        setmStartDate(null);
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

    public long getmDuration() {
        return mDuration;
    }

    public void setmDuration(long mDuration) {
        this.mDuration = mDuration;
        mSettingsEditor.putLong(RECORDING_DURATION, mDuration);
        mSettingsEditor.commit();
    }

    public Date getmStartDate() {
        return mStartDate;
    }

    public void setmStartDate(Date mStartDate) {
        this.mStartDate = mStartDate;
        if(mStartDate == null) {
            mSettingsEditor.putLong(RECORDING_START, -1);
        } else {
            mSettingsEditor.putLong(RECORDING_START, mStartDate.getTime());
        }
        mSettingsEditor.commit();
    }
}
