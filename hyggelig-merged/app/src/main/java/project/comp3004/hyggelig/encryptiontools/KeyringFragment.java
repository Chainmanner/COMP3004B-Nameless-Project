package project.comp3004.hyggelig.encryptiontools;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.publickey.PublicKey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabItem;

public class KeyringFragment extends Fragment {

    private EncryptionTools_MainActivity instance;

    private TabItem publicKeys;
    private TabItem privateKeys;

    private RecyclerView keyList;

    private Button importKeys;
    private Button generateKeypair;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View theView = inflater.inflate(R.layout.encryptiontools_keyring_layout, container, false);

        instance = (EncryptionTools_MainActivity)getActivity();

        Toolbar theToolbar = theView.findViewById(R.id.toolbar);
        instance.setSupportActionBar(theToolbar);
        instance.getSupportActionBar().setDisplayShowHomeEnabled(true);
        instance.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        instance.getSupportActionBar().setTitle("Keyring");

        return theView;
    }

    private void showPublicKeys()
    {
        // TODO
    }

    private void showPrivateKeys()
    {
        // TODO
    }
}
