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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.didisoft.pgp.KeyPairInformation;
import com.google.android.material.tabs.TabItem;

import com.didisoft.pgp.KeyStore;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class KeyringFragment extends Fragment {

    private EncryptionTools_MainActivity instance;

    //private TabItem publicKeys;
    //private TabItem privateKeys;
    private TabLayout keyTypeRow;

    private RecyclerView keyList;

    private Button importKey;
    private Button generateKeypair;

    private boolean showingSecretKeys = false;

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

        keyTypeRow = theView.findViewById(R.id.keyTypeRow);
        if ( keyTypeRow != null )
        	keyTypeRow.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
				@Override
				public void onTabSelected(TabLayout.Tab tab) {
					switch ( tab.getPosition() )
					{
						case 0:	// PUBLIC
							showPublicKeys(tab.parent.getRootView());
							showingSecretKeys = false;
							break;
						case 1:	// PRIVATE
							showPrivateKeys(tab.parent.getRootView());
							showingSecretKeys = true;
							break;
					}
				}

				@Override
				public void onTabUnselected(TabLayout.Tab tab) {
					// Nothing to do here.
				}

				@Override
				public void onTabReselected(TabLayout.Tab tab) {
					// Nothing to do here.
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

		keyList = theView.findViewById(R.id.keyList);
		keyList.setVerticalScrollBarEnabled(true);
		keyList.setLayoutManager(new LinearLayoutManager(instance.getApplicationContext()));
		keyList.setAdapter(new KeysAdapter(null, false));

        showPublicKeys(theView);

        return theView;
    }

    private void showPublicKeys(View v)
    {
    	Log.w("hyggelig", "showPublicKeys");
    	String pubkeyDirPath = instance.getPubkeysPath();
        File pubkeyDir = new File(pubkeyDirPath);
        if ( !pubkeyDir.isDirectory() )
		{
			Log.w("hyggelig", pubkeyDir.getAbsolutePath() + " somehow is not a directory");
			return;
		}
        if ( pubkeyDir.list().length == 0 )
		{
			Log.w("hyggelig", "No files in " + pubkeyDir.getAbsolutePath());
		}

		int dirSize = pubkeyDir.list().length;
        KeyStore tempStore = new KeyStore();
        KeyPairInformation[] showThese = new KeyPairInformation[dirSize];
        int i = 0;
        for ( String curName : pubkeyDir.list() )
		{
			Log.w("hyggelig", curName);
			try
			{
				showThese[i] = tempStore.importPublicKey(pubkeyDirPath + curName)[0];
				i++;
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}

        Log.w("hyggelig", Arrays.toString(pubkeyDir.list()));
		KeysAdapter theAdapter = new KeysAdapter(showThese, false);
		keyList.swapAdapter(theAdapter, true);
		keyList.getAdapter().notifyDataSetChanged();
    }

    private void showPrivateKeys(View v)
    {
    	Log.w("hyggelig", "showPrivateKeys");
		String privkeyDirPath = instance.getPrivkeysPath();
		File privkeyDir = new File(privkeyDirPath);
		if ( !privkeyDir.isDirectory() )
		{
			Log.w("hyggelig", privkeyDir.getAbsolutePath() + " somehow is not a directory");
			return;
		}
		if ( privkeyDir.list().length == 0 )
		{
			Log.w("hyggelig", "No files in " + privkeyDir.getAbsolutePath());
		}

		int dirSize = privkeyDir.list().length;
		KeyStore tempStore = new KeyStore();
		KeyPairInformation[] showThese = new KeyPairInformation[dirSize];
		int i = 0;
		for ( String curName : privkeyDir.list() )
		{
			Log.w("hyggelig", curName);
			try
			{
				showThese[i] = tempStore.importPrivateKey(privkeyDirPath + curName)[0];
				i++;
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}

		Log.w("hyggelig", Arrays.toString(privkeyDir.list()));
		KeysAdapter theAdapter = new KeysAdapter(showThese, true);
		keyList.swapAdapter(theAdapter, true);
		keyList.getAdapter().notifyDataSetChanged();
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
			String outFilePath = "";
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

				String keyContentsString = new String(keyContents, "ASCII");
				Log.w("hyggelig", keyContentsString);	// FIXME: REMOVE WHEN DONE
				if ( keyContentsString.contains("-----BEGIN PGP PUBLIC KEY BLOCK-----")
						&& keyContentsString.contains("-----END PGP PUBLIC KEY BLOCK-----") )
				{
					outFilePath = instance.getPubkeysPath();
				}
				else if ( keyContentsString.contains("-----BEGIN PGP PRIVATE KEY BLOCK-----")
						&& keyContentsString.contains("-----END PGP PRIVATE KEY BLOCK-----") )
				{
					outFilePath += instance.getPrivkeysPath();
				}
				else
				{
					throw new Exception("File is not an OpenPGP key or is in an invalid format");
				}
				outFilePath += filename;

				File newFile = new File(outFilePath);
				newFile.createNewFile();
				FileOutputStream fileOS = new FileOutputStream(newFile);
				fileOS.write(keyContents);
				fileOS.close();
			}
			catch ( Exception e )
			{
				Log.w("hyggelig", "Error while copying key file! " + e.getMessage());
				e.printStackTrace();
				return;
			}

			Log.w("hyggelig", "File " + outFilePath + " written successfully");
			// Refresh the list of keys.
			if ( showingSecretKeys )
				showPrivateKeys(this.getView());
			else
				showPublicKeys(this.getView());
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
        if ( current == null )
		{
			Log.w("hyggelig", "current is null!");
			return;
		}

		ConstraintLayout theLayout = holder.itemView.findViewById(R.id.constraint_layout);
        if ( position % 2 == 0 )
		{
			theLayout.setBackgroundColor(Color.LTGRAY);
		}
        else
		{
			theLayout.setBackgroundColor(Color.WHITE);
		}

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
                finalName += "[" + (showSecretKeys ? "priv" : "pub") + "] ";
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
        return keyPairs == null ? 0 : keyPairs.length;
    }
}