package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.myfirstapp.HelpListFrag.HelpListFragment;
import com.example.myfirstapp.HelpListFrag.HelpTopicListContent;

public class BrowseHelpActivity extends AppCompatActivity implements HelpListFragment.OnListFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_help);
        if (savedInstanceState == null){
            //If there isn't some saved state, push the list of help topics fragment onto the frame
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.help_fragment_frame, new HelpListFragment("top"))
                    .commit();
        }
    }

    public void onListFragmentInteraction(HelpTopicListContent.HelpTopic topic){
        // Callback for when a user taps on a help topic
        String helpTopic = topic.toString();
        Toast toast = Toast.makeText(this, helpTopic, Toast.LENGTH_SHORT);
        toast.show();

        HelpListFragment newListFrag;

        switch(helpTopic.toLowerCase()){
            case("passwords"):
            case("file encryption"):
            case("cryptocurrency wallet"):
                newListFrag = new HelpListFragment(helpTopic.toLowerCase());
                break;
            default:
                return;
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.help_fragment_frame, newListFrag)
                .commit();
    }

}
