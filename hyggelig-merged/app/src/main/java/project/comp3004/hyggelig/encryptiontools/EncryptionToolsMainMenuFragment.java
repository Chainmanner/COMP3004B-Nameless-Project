package project.comp3004.hyggelig.encryptiontools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import project.comp3004.hyggelig.R;

// Fragment for the main menu of the Encryption Tools.
// This file just sets up the click handlers for each of the activities.
// Authored by Gabriel Valachi (101068875).
public class EncryptionToolsMainMenuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // NOTE: The "false" here is VERY important. Noting this because it took me hours to find out why this wasn't working.
        View theView = inflater.inflate(R.layout.encryptiontools_main, container, false);

        theView.findViewById(R.id.encryptFiles).setOnClickListener( new View.OnClickListener()
                                                                    {
                                                                        @Override
                                                                        public void onClick(View v)
                                                                        {
                                                                            //Snackbar.make(v, "wow", Snackbar.LENGTH_SHORT).show();
                                                                            Navigation.findNavController(v).navigate(R.id.action_encryptionToolsMainMenuFragment3_to_encryptFilesFragment2);
                                                                        }
                                                                    }
        );

        theView.findViewById(R.id.decryptFiles).setOnClickListener( new View.OnClickListener()
                                                                    {
                                                                        @Override
                                                                        public void onClick(View v)
                                                                        {
                                                                            //Snackbar.make(v, "wow", Snackbar.LENGTH_SHORT).show();
                                                                            Navigation.findNavController(v).navigate(R.id.action_encryptionToolsMainMenuFragment3_to_decryptVerifyFragment);
                                                                        }
                                                                    }
        );

        theView.findViewById(R.id.keyring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_encryptionToolsMainMenuFragment3_to_keyringFragment);
            }
        });

        theView.findViewById(R.id.privateFolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_encryptionToolsMainMenuFragment3_to_privateFolderFragment);
            }
        });

        // This adds a back button.
        // REMOVED: There's nowhere to go back to.
        /*Toolbar toolbar = theView.findViewById(R.id.toolbar);
        ((EncryptionTools_MainActivity)getActivity()).setSupportActionBar(toolbar);
        if ( ((EncryptionTools_MainActivity)getActivity()).getSupportActionBar() != null )
        {
            ((EncryptionTools_MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((EncryptionTools_MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // This actually makes the back button go back.
                Navigation.findNavController(v).popBackStack();
            }
        });*/

        return theView;
    }
}
