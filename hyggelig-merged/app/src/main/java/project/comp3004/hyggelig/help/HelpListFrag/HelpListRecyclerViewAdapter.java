package project.comp3004.hyggelig.help.HelpListFrag;
import project.comp3004.hyggelig.R;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import project.comp3004.hyggelig.help.HelpListFrag.HelpListFragment.OnListFragmentInteractionListener;
import project.comp3004.hyggelig.help.HelpListFrag.HelpTopicListContent.HelpTopic;

import java.util.List;

public class HelpListRecyclerViewAdapter extends RecyclerView.Adapter<HelpListRecyclerViewAdapter.ViewHolder> {

    private final List<HelpTopic> mValues;
    private final OnListFragmentInteractionListener mListener;
    private boolean showCategories;

    public HelpListRecyclerViewAdapter(List<HelpTopic> topics, OnListFragmentInteractionListener listener,
                                       boolean showCategories) {
        mValues = topics;
        mListener = listener;
        this.showCategories = showCategories;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.help_fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (showCategories){
            holder.mContentView.setText(mValues.get(position).category);
        }else{
            holder.mContentView.setText(mValues.get(position).topic);
        }
        holder.mTopic = mValues.get(position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    if(showCategories){
                        mListener.onListFragmentInteraction(holder.mTopic.category);
                    }else {
                        mListener.onListFragmentInteraction(holder.mTopic);
                    }
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
            mContentView = view.findViewById(R.id.help_topic);
        }

    }
}
