package com.example.myfirstapp.HelpListFrag;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myfirstapp.HelpListFrag.HelpListFragment.OnListFragmentInteractionListener;
import com.example.myfirstapp.HelpListFrag.HelpTopicListContent.HelpTopic;
import com.example.myfirstapp.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link HelpTopic} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HelpListRecyclerViewAdapter extends RecyclerView.Adapter<HelpListRecyclerViewAdapter.ViewHolder> {

    private final List<HelpTopic> mValues;
    private final OnListFragmentInteractionListener mListener;

    public HelpListRecyclerViewAdapter(List<HelpTopic> topics, OnListFragmentInteractionListener listener) {
        mValues = topics;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mContentView.setText(mValues.get(position).topic);
        holder.mTopic = mValues.get(position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mTopic);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public HelpTopic mTopic;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.help_topic);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
