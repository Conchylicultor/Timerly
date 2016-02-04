package com.pot.timerly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by pot on 04/02/16.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private String[] mDataset;

    // Class which correspond to each element of our RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static boolean colorInitialized = false;
        private static int backgroundColor; // Default color
        private static int selectedColor; // When item selected

        // Each data item is just a string in this case

        // Should contain:
        // - Date of recording
        // - Duration

        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);

            mTextView = (TextView) v.findViewById(R.id.duration_text);

            if(!colorInitialized) {
                backgroundColor = ContextCompat.getColor(v.getContext(), R.color.background_material_light);
                selectedColor = ContextCompat.getColor(v.getContext(), R.color.item_selected);
                colorInitialized = true;
            }
        }

        @Override
        public void onClick(final View view) {
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

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(String[] myDataset) {
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
        holder.mTextView.setText(mDataset[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
