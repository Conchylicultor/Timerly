package com.pot.timerly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Created by pot on 04/02/16.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private static boolean colorInitialized = false;
    private static int backgroundColor; // Default color
    private static int selectedColor; // When item selected

    private List<RecordingItem> mDataset;

    private Context mContext;

    private final String FILE_SAVE_RECORDING = "recording.csv";

    // Class which correspond to each element of our RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        // Each data item is just a string in this case

        // Should contain:
        // - Date of recording
        // - Duration

        public TextView mDurationText;
        public TextView mDateText;
        public Button mDeleteButton;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);

            mDurationText = (TextView) v.findViewById(R.id.duration_text);
            mDateText = (TextView) v.findViewById(R.id.date_text);
            mDeleteButton = (Button) v.findViewById(R.id.delete_button);
            mDeleteButton.setOnClickListener(this);

            // TODO: mDeleteButton.setOnClickListener(); << PB with the click of the view holder ?!

            if(!colorInitialized) {
                // TODO: Use theme color instead of a cst color
                backgroundColor = ContextCompat.getColor(v.getContext(), R.color.background_material_light);
                selectedColor = ContextCompat.getColor(v.getContext(), R.color.item_selected);
                colorInitialized = true;
            }
        }

        @Override
        public void onClick(final View view) {
            if (view.getId() == mDeleteButton.getId()) { // Button clicked

                // Allow the restoration of the object
                final RecordingItem savedItem = new RecordingItem(mDataset.get(getAdapterPosition())); // Copy the item before removing it
                final int savedPosition = getAdapterPosition();

                Snackbar.make(view, "Recording deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addItem(savedPosition, savedItem);
                            }
                        })
                        .show();

                // Delete the object from the database
                removeItem(getAdapterPosition());
            } else { // Item clicked
                // Open the dialog
                final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle("Item details");
                alert.setMessage("Edit duration");
                alert.setView(R.layout.duration_item_details);

                alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() // TODO: Not clean (button created here and initialised later)
                {
                    public void onClick(DialogInterface dialog,int id)
                    {
                    }
                });

                alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog,int id)
                    {
                    }
                });

                AlertDialog alertDialog = alert.create();
                alertDialog.show(); // Necessary here to instantiate the widget (otherwise findViewById return null)

                final SeekBar seekBar = (SeekBar) alertDialog.findViewById(R.id.duration_seekbar);
                final TextView textView = (TextView) alertDialog.findViewById(R.id.text_duration_help);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        textView.setText(String.valueOf(progress/2.0));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mDataset.get(getAdapterPosition()).setDuration(seekBar.getProgress()*60/2 * 1000);

                        // Update the database
                        notifyItemChanged(getAdapterPosition());
                        saveDatabase();
                    }
                });
            }
        }
    }

    public void addItem(int position, RecordingItem newItem) {
        mDataset.add(position, newItem);
        notifyItemInserted(position);
        saveDatabase();
    }

    public void addItem(RecordingItem newItem) {
        mDataset.add(newItem);
        notifyItemInserted(mDataset.size());
        saveDatabase();
    }

    public void removeItem(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
        saveDatabase();
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context) {
        this.mContext = context;

        final List<RecordingItem> myDataset = new ArrayList<>();

        try {
            FileInputStream file = mContext.openFileInput(FILE_SAVE_RECORDING);

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLong()) {
                long duration = scanner.nextLong();
                Date date = scanner.hasNextLong() ? new Date(scanner.nextLong()) : new Date();

                myDataset.add(new RecordingItem(duration, date));
            }

            String theString = scanner.hasNext() ? scanner.next() : "";



            file.close();
        } catch (FileNotFoundException e) {
            Log.d("Timerly", "File don't: first launch");

        } catch (IOException e) {
            e.printStackTrace();
            // FILE NOT CLOSED !!!
        }

        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.duration_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //...
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        long duration = mDataset.get(position).getDuration();
        long minutes = duration/(60*1000);
        duration -= minutes*(60*1000);
        long seconds = duration/(1000);
        duration -= seconds*1000;
        long milisecond = duration/100; // Only keep the decimal
        holder.mDurationText.setText(String.format("Duration: %dmin %02d.%dsec", minutes, seconds, milisecond));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm"); // TODO: Replace that by LocalDateTime on java 8
        holder.mDateText.setText(dateFormat.format(mDataset.get(position).getDate()));

        // TODO: Restore also the state (selected or not) >> change background accordingly

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void saveDatabase() {

        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(FILE_SAVE_RECORDING, Context.MODE_PRIVATE);

            for (RecordingItem item : mDataset) {
                outputStream.write(item.toString().getBytes());
                outputStream.write('\n');
            }

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            //Snackbar() // TODO
        }
    }
}
