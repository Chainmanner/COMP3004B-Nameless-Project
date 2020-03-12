package project.comp3004.hyggelig.encryptiontools;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.publickey.PublicKey;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.didisoft.pgp.KeyPairInformation;
import com.google.android.material.tabs.TabItem;

import com.didisoft.pgp.KeyStore;

import java.io.InputStream;

public class KeyringFragment extends Fragment {

    private EncryptionTools_MainActivity instance;

    private TabItem publicKeys;
    private TabItem privateKeys;

    private RecyclerView keyList;

    private Button importKey;
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
        theToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // This actually makes the back button go back.
                Navigation.findNavController(v).popBackStack();
            }
        });

        publicKeys = theView.findViewById(R.id.showPublicKeys);
        if ( publicKeys != null )
            publicKeys.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPublicKeys();
                }
            });
        privateKeys = theView.findViewById(R.id.showPrivateKeys);
        if ( privateKeys != null )
            privateKeys.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPrivateKeys();
                }
            });

        importKey = theView.findViewById(R.id.importKey);
        if ( importKey != null )
            importKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    importKeyPrompt();
                }
            });
        generateKeypair = theView.findViewById(R.id.generateKeypair);
        if ( generateKeypair != null )
            generateKeypair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    generateKeypairDialog(v);
                }
            });

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

    private void importKeyPrompt()
    {
		Intent pickFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
		pickFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
		pickFileIntent.setType("*/*");
		pickFileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, "true");
		if ( instance != null && pickFileIntent.resolveActivity(instance.getPackageManager()) != null )
			startActivityForResult(Intent.createChooser(pickFileIntent, "Select a File Manager"), 0);
    }

    @Override
	public void onActivityResult(int reqcode, int rescode, Intent resultIntent)
	{
		if ( resultIntent != null && reqcode == 0 && resultIntent.getData() != null )
		{
			Uri targetURI = resultIntent.getData();

			Cursor theCursor = instance.getContentResolver().query(targetURI, null, null, null, null);
			if ( theCursor == null )
			{
				Log.w("hyggelig", "Cursor null");
				return;
			}
			theCursor.moveToFirst();
			String filename = theCursor.getString( theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) );
			int filesize = (int)theCursor.getLong( theCursor.getColumnIndex(OpenableColumns.SIZE) );
			theCursor.close();

			// Copy the file over.
			byte[] keyContents = new byte[filesize];
			try
			{
				InputStream fileIS = instance.getContentResolver().openInputStream(targetURI);
				if ( fileIS == null )
				{
					Log.w("hyggelig", "fileIS is null");
					return;
				}
				fileIS.read(keyContents);
				fileIS.close();

				// TODO: Determine what type of key this is.
			}
			catch ( Exception e )
			{
				Log.w("hyggelig", "Error while copying key file! " + e.getMessage());
				e.printStackTrace();
			}


		}
	}

    private void generateKeypairDialog(View v)
    {
        // TODO
    }
}


class KeysAdapter extends RecyclerView.Adapter<KeysAdapter.ImplementedViewHolder>
{
    private KeyPairInformation[] keyPairs;
    private boolean showSecretKeys;

    class ImplementedViewHolder extends RecyclerView.ViewHolder
    {
        public ImplementedViewHolder(View v)
        {
            super(v);
        }
    }

    public KeysAdapter(KeyPairInformation[] theKeyPairs, boolean secrets)
    {
        keyPairs = theKeyPairs;
        showSecretKeys = secrets;
    }

    @Override
    public KeysAdapter.ImplementedViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View theView = LayoutInflater.from(parent.getContext()).inflate(R.layout.encryptiontools_keyring_keyitem, parent, false);

        return new KeysAdapter.ImplementedViewHolder(theView);
    }

    @Override
    public void onBindViewHolder(KeysAdapter.ImplementedViewHolder holder, int position)
    {
        KeyPairInformation current = keyPairs[position];

        TextView keyName = holder.itemView.findViewById(R.id.keyName);
        if ( keyName != null )
        {
            String finalName = "";
            if ( current.isRevoked() )
            {
				finalName += "[REVOKED]";
				keyName.setTextColor(Color.RED);
			}
            else
            {
                finalName += "[" + (showSecretKeys ? "pub" : "priv") + "] ";
                finalName += "[" + current.getAlgorithm() + "] ";
            }
            finalName += current.getUserID();
            keyName.setText(finalName);
        }

        TextView keyFingerprint = holder.itemView.findViewById(R.id.keyFingerprint);
        if ( keyFingerprint != null )
        {
            keyFingerprint.setText("Fingerprint: " + current.getFingerprint());
        }

        TextView keyExpiration = holder.itemView.findViewById(R.id.keyExpiration);
        if ( keyExpiration != null )
        {
            String finalString = (current.isExpired() ? "Expired " : "Expires ") + "on ";
            finalString += current.getExpirationDate().toString();
            if ( current.isExpired() )
                keyExpiration.setTextColor(Color.RED);
            keyExpiration.setText(finalString);
        }
    }

    @Override
    public int getItemCount()
    {
        return keyPairs.length;
    }
}