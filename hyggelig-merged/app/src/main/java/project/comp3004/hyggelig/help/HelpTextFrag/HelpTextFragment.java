package project.comp3004.hyggelig.help.HelpTextFrag;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import project.comp3004.hyggelig.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HelpTextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HelpTextFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String HELP_TEXT = "";

    // TODO: Rename and change types of parameters
    private String helpText;

    public HelpTextFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param helpText
     * @return A new instance of fragment HelpTextFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HelpTextFragment newInstance(String helpText) {
        Log.d("newInst", "helpText:" + helpText );
        HelpTextFragment fragment = new HelpTextFragment();
        Bundle args = new Bundle();
        args.putString(HELP_TEXT, helpText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            helpText = getArguments().getString(HELP_TEXT);
        }
        Log.d("onCreate", "helpText:"+helpText );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help_text, container, false);
        ((TextView)view.findViewById(R.id.helpTextView)).setText(helpText);
        Log.d("createView", "helpText:"+helpText );
        return view;
    }
}
