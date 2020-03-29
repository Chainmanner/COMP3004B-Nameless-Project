package project.comp3004.hyggelig.help;
import project.comp3004.hyggelig.R;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import project.comp3004.hyggelig.help.HelpDB.HelpDatabase;
import project.comp3004.hyggelig.help.HelpListFrag.HelpListFragment;
import project.comp3004.hyggelig.help.HelpListFrag.HelpTopicListContent;
import project.comp3004.hyggelig.help.HelpTextFrag.HelpTextFragment;

public class BrowseHelp_Activity extends AppCompatActivity implements HelpListFragment.OnListFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_browse_help);
        if (savedInstanceState == null){
            //If there isn't some saved state, push the list of help topics fragment onto the frame
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.help_fragment_frame, new HelpListFragment(HelpDatabase.getInstance(this)
                            .getCategories(), true))
                    .commit();
        }
    }

    public void onListFragmentInteraction(HelpTopicListContent.HelpTopic topic){
        // Callback for when a user taps on a help topic
        getSupportFragmentManager().beginTransaction()
                .add(R.id.help_fragment_frame, HelpTextFragment.newInstance(HelpDatabase.getInstance(this)
                        .getHelpText(topic.topicID)))
                .commit();
    }

    public void onListFragmentInteraction(String category){
        // Callback for when a user taps on a help category
        getSupportFragmentManager().beginTransaction()
                .add(R.id.help_fragment_frame, new HelpListFragment(HelpDatabase.getInstance(this)
                        .getTopics(category), false))
                .commit();
    }
}
