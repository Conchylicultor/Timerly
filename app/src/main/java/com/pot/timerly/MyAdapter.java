package com.pot.timerly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by pot on 04/02/16.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private static boolean colorInitialized = false;
    private static int backgroundColor; // Default color
    private static int selectedColor; // When item selected

    private List<RecordingItem> mDataset;

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
                // TODO: Dirty !! (should use a member which store the state)
                boolean isSelected = ((ColorDrawable)view.getBackground()) != null && ((ColorDrawable)view.getBackground()).getColor() == selectedColor;

                int finalRadius = (int)Math.hypot(view.getWidth()/2, view.getHeight()/2);

                if (isSelected) {
                    // Reverse animation
                    // TODO: clipping issue when animation finished
                    view.setBackgroundColor(backgroundColor);
                    Animator anim = ViewAnimationUtils.createCircularReveal(view,
                            (int) view.getWidth() / 2,
                            (int) view.getHeight() / 2,
                            finalRadius,
                            0);
                    view.setBackgroundColor(selectedColor);

                    // Restore the original background when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setBackgroundColor(backgroundColor);
                        }
                    });
                    anim.start();
                } else {
                    // Revelation
                    Animator anim = ViewAnimationUtils.createCircularReveal(view,
                            (int) view.getWidth() / 2,
                            (int) view.getHeight() / 2,
                            0,
                            finalRadius);
                    view.setBackgroundColor(selectedColor);
                    anim.start();
                }
            }
        }
    }

    public void addItem(int position, RecordingItem newItem) {
        mDataset.add(position, newItem);
        notifyItemInserted(position);
    }

    public void addItem(RecordingItem newItem) {
        mDataset.add(newItem);
        notifyItemInserted(mDataset.size());
    }

    public void removeItem(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<RecordingItem> myDataset) {
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy - HH:mm"); // TODO: Replace that by LocalDateTime on java 8

        holder.mDurationText.setText(String.valueOf(mDataset.get(position).getDuration()));
        holder.mDateText.setText(dateFormat.format(mDataset.get(position).getDate()));

        // TODO: Restore also the state (selected or not) >> change background accordingly

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
