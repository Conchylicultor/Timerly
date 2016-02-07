package com.pot.timerly;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Handler mRecordingHandler;
    private int mCurrentTime = 0; // TODO: Use clock system time > more precise (instead of interval increment)
    private final int UPDATE_INTERVAL = 100; // 100ms
    private boolean mIsRecording = false;
    private TextView mRecoringText; // Correspond to the timer text

    // Runable (executable code run every x ms) which update the timer
    Runnable mRecordingRunable = new Runnable() {
        @Override
        public void run() {
            // Update the GUI:
            // TODO: Format as date
            mCurrentTime += UPDATE_INTERVAL;
            mRecoringText.setText(Integer.toString(mCurrentTime));

            mRecordingHandler.postDelayed(mRecordingRunable, UPDATE_INTERVAL);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loading dataset (Here ???)

        // TODO: Load and use real dataset!
        String[] myDataset = {
                "String_1",
                "Blezffdgdfg",
                "Test 43"
        };

        // Set up the timer
        mRecordingHandler = new Handler();

        // Configure the recycler view

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // For improving performance (static size)
        mRecyclerView.setHasFixedSize(true);
        // Linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Specify the adapter
        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Configure other GUI elements

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecoringText = (TextView) findViewById(R.id.recording_text);
        mRecoringText.setText("Inactive");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        // TODO: Add animations (elevation change and inc when pressed), + haptic response
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mIsRecording) {
                    // Invert the fab icon
                    // TODO: Add transition icon animation
                    fab.setImageResource(android.R.drawable.ic_media_pause);

                    // Start the recording
                    mRecordingRunable.run();
                } else {
                    // Invert the fab icon
                    fab.setImageResource(android.R.drawable.ic_media_play);

                    // Stop the recording
                    mRecordingHandler.removeCallbacks(mRecordingRunable);
                }
                mIsRecording = !mIsRecording;
            }

        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if(mCurrentTime > 0) // No action if we didn't start the recording
                {
                    // Resore state
                    fab.setImageResource(android.R.drawable.ic_media_play);
                    mIsRecording = false;
                    mCurrentTime = 0;

                    // Change GUI
                    // TODO: Hide gui elem
                    mRecoringText.setText("Inactive");

                    // Stop the recording
                    mRecordingHandler.removeCallbacks(mRecordingRunable);

                    // Save the recording
                    Snackbar.make(view, "Saving data...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                }

                return true; // No other listener called
            }
        });

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
}
