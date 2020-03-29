package project.comp3004.hyggelig.help.HelpTextFrag;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
    private static final String HELP_TEXT = "";

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout and set the text for this fragment
        View view = inflater.inflate(R.layout.fragment_help_text, container, false);
        ((TextView)view.findViewById(R.id.helpTextView)).setText(helpText);
        return view;
    }
}
