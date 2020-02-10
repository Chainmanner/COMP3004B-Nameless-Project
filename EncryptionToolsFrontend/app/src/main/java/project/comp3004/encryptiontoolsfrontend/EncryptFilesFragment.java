package project.comp3004.encryptiontoolsfrontend;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class EncryptFilesFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View theView = inflater.inflate(R.layout.encrypt_files_layout, container, false);

        // This adds a back button.
        Toolbar toolbar = theView.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        if ( ((MainActivity)getActivity()).getSupportActionBar() != null )
        {
            ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((MainActivity)getActivity()).getSupportActionBar().setTitle("Encrypt a File");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // This actually makes the back button go back.
                Navigation.findNavController(v).navigate(R.id.action_encryptFilesFragment2_to_encryptionToolsMainMenuFragment3);
            }
        });

        // TEST CODE - IT WORKS!
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //                <category android:name="android.intent.category.OPENABLE"></category>
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        //intent.putExtra(Intent.EXTRA_MIME_TYPES, "*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, "true");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent2 = Intent.createChooser(intent, "Choose a file");
        PackageManager pm = ((MainActivity)getActivity()).getPackageManager();
        Log.w("hyggelig", "" + (pm.queryIntentActivities(intent2, 0).size()));
        startActivityForResult(intent2, 0);

        return theView;
    }
}