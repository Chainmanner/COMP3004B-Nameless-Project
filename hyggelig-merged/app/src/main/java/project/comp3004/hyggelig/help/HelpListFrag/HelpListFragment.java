package project.comp3004.hyggelig.help.HelpListFrag;
import project.comp3004.hyggelig.R;

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
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 * Currently generates HelpTopicListContents based on stored String arrays. Would be nicer if the
 * topics could be generated from a database or a textfile or something.
 */
public class HelpListFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private String targetTopic = "top";

    private static String[] topLevelTopics = {"Passwords","File Encryption","Cryptocurrency Wallet"};
    private static String[] passwordTopics =   {   "What is a Password Manager?", "Why shouldn't I reuse passwords?",
                                            "What makes a password \"good\"?"
                                        };
    private static String[] encryptionTopics = {   "Why should I encrypt my files?",
                                            "How does encryption work?", "What is an encryption key?"
                                        };
    private static String[] cryptoTopics = {    "What is a cryptocurrency?", "How do I get cryptocurrency?",
                                                "What is a cryptocurrency wallet?"
                                            };

    private static Map<String, String[]> helpTopics = new HashMap<String, String[]>();
    static{
        helpTopics.put("top", topLevelTopics);
        helpTopics.put(topLevelTopics[0].toLowerCase(), passwordTopics);
        helpTopics.put(topLevelTopics[1].toLowerCase(), encryptionTopics);
        helpTopics.put(topLevelTopics[2].toLowerCase(), cryptoTopics);
    }

    /**So this currently get a string from when the user taps on a topic
     * This should probably be changed to something like an ID
     * @param targetTopic
     */
    public HelpListFragment(String targetTopic) {
        this.targetTopic = targetTopic.toLowerCase();
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
        String[] topics;
        if( (topics = helpTopics.get(targetTopic))!=null) {
            topicList.setTopics(topics);
        }else{
            topicList.setTopics(topLevelTopics);
        }
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new HelpListRecyclerViewAdapter(topicList.ITEMS, mListener));
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
    }
}
