package project.comp3004.encryptiontoolsfrontend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

// TODO: This could use some cleanup.
public class EncryptionToolsMainMenuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // NOTE: The "false" here is VERY important.
        View theView = inflater.inflate(R.layout.main, container, false);

        // TODO: All buttons must be clickable.

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

        // This adds a back button.
        Toolbar toolbar = theView.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        if ( ((MainActivity)getActivity()).getSupportActionBar() != null )
        {
            ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // This actually makes the back button go back.
                Navigation.findNavController(v).popBackStack();
            }
        });

        return theView;
    }
}
