package project.comp3004.hyggelig.encryptiontools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import project.comp3004.hyggelig.R;

// TODO: This could use some cleanup.
public class EncryptionToolsMainMenuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // NOTE: The "false" here is VERY important.
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

        // TODO: Navigate to private folder layout.

        // This adds a back button.
        Toolbar toolbar = theView.findViewById(R.id.toolbar);
        ((EncryptionTools_MainActivity)getActivity()).setSupportActionBar(toolbar);
        if ( ((EncryptionTools_MainActivity)getActivity()).getSupportActionBar() != null )
        {
            ((EncryptionTools_MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((EncryptionTools_MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // FIXME: Disabled for now due to crashes when using it.
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // This actually makes the back button go back.
                Navigation.findNavController(v).popBackStack();
            }
        });*/

        return theView;
    }
}
