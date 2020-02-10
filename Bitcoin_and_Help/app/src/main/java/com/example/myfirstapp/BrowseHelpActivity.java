package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.myfirstapp.dummy.DummyContent;

public class BrowseHelpActivity extends AppCompatActivity implements HelpListFragment.OnListFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_help);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.help_fragment, new HelpListFragment())
                    .commit();
        }
    }

    public void onListFragmentInteraction(DummyContent.DummyItem item){
        Toast toast = Toast.makeText(this,"Fragment Interaction", Toast.LENGTH_SHORT);
        toast.show();
    }

}
