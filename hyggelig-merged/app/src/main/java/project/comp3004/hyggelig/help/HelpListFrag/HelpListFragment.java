package project.comp3004.hyggelig.help.HelpListFrag;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.help.HelpDB.TopicList;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.example.myfirstapp.HelpListFrag.HelpTopicListContent;
//import com.example.myfirstapp.HelpListFrag.HelpTopicListContent.HelpTopic;
//import com.example.myfirstapp.R;

import java.util.HashMap;
import java.util.Map;

/**
 * A fragment representing a list of HelpTopics.
 * Activities containing this fragment MUST implement both {@link OnListFragmentInteractionListener}
 * interfaces.
 * Help topics are stored as text in the HelpDB.db file. The database is accessed through the Android
 * Room classes in the HelpDb package.
 */
public class HelpListFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private TopicList[] topics;
    private boolean showCategories;

    /**So this currently get a string from when the user taps on a topic
     * This should probably be changed to something like an ID
     * @param topicList
     */
    public HelpListFragment(TopicList[] topicList, boolean showCategories) {
        this.topics = topicList;
        this.showCategories = showCategories;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_fragment_item_list, container, false);

        //Set the HelpTopicListContent with whatever this fragment was initialized to
        HelpTopicListContent topicList = new HelpTopicListContent();
        topicList.setTopics(topics);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new HelpListRecyclerViewAdapter(topicList.ITEMS, mListener,
                    showCategories));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(HelpTopicListContent.HelpTopic topic);
        void onListFragmentInteraction(String category);
    }
}
