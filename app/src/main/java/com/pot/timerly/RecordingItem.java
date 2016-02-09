package com.pot.timerly;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by pot on 09/02/16.
 */
public class RecordingItem {
    private int mDuration; // Duration of the recording // TODO: Use Duration instead of int
    private Date mDate; // Date of the recording

    public RecordingItem(int mDuration, Date mDate) {
        this.mDuration = mDuration;
        this.mDate = mDate;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    // Temporary function which generate a list of random items
    public static List<RecordingItem> generateItemList(int numItem) {
        List<RecordingItem> recordingItems = new ArrayList<>();

        final int MAX_DURATION = 60*60*1000;
        Random rand = new Random(); // TODO: Where to initialize the instance of rand (seed and so on)

        for (int i = 0; i < numItem; ++i) {
            recordingItems.add(new RecordingItem(rand.nextInt(MAX_DURATION), new Date(rand.nextInt())));
        }

        return recordingItems;
    }
}
