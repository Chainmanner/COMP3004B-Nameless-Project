package project.comp3004.hyggelig.encryptiontools;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.publickey.PublicKey;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class KeyringFragment extends Fragment {

    private EncryptionTools_MainActivity instance;

    //private TabItem publicKeys;
    //private TabItem privateKeys;
    private TabLayout keyTypeRow;

    private RecyclerView keyList;

    private Button importKey;
    private Button generateKeypair;

    private boolean showingSecretKeys = false;

    // The following are only used by generateKeypairDialog().
	boolean keyWillExpire = false;
	long daysAfterToday = 0;

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
							showPublicKeys();
							showingSecretKeys = false;
							break;
						case 1:	// PRIVATE
							showPrivateKeys();
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
                    generateKeypairDialog();
                }
            });

		keyList = theView.findViewById(R.id.keyList);
		keyList.setVerticalScrollBarEnabled(true);
		keyList.setLayoutManager(new LinearLayoutManager(instance.getApplicationContext()));
		keyList.setAdapter(new KeysAdapter(null, false));

        showPublicKeys();

        return theView;
    }

    private void showPublicKeys()
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

		KeysAdapter theAdapter = new KeysAdapter(pubkeyDir.list(), false);
		keyList.swapAdapter(theAdapter, true);
		keyList.getAdapter().notifyDataSetChanged();
    }

    private void showPrivateKeys()
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

		KeysAdapter theAdapter = new KeysAdapter(privkeyDir.list(), true);
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
				showPrivateKeys();
			else
				showPublicKeys();
		}
	}

    private void generateKeypairDialog()
    {
    	Log.w("hyggelig", "generateKeypairDialog");

    	final Dialog genDialog = new Dialog(instance);
    	genDialog.setContentView(R.layout.encryptiontools_keyring_genkeys);
    	genDialog.setCanceledOnTouchOutside(false);

    	final EditText keyFilename = genDialog.findViewById(R.id.keyFilename);
    	final EditText nameOfUser = genDialog.findViewById(R.id.nameOfUser);
    	final EditText privKeyPass = genDialog.findViewById(R.id.privKeyPass);
    	final EditText privKeyConfirm = genDialog.findViewById(R.id.privKeyConfirm);

    	final CheckBox keyWillExpire_checkbox = genDialog.findViewById(R.id.keyWillExpire);
    	final Button pickDate = genDialog.findViewById(R.id.pickDate);
    	pickDate.setVisibility(View.GONE);
    	final TextView pickedDateAndTime = genDialog.findViewById(R.id.pickedDateAndTime);
    	pickedDateAndTime.setVisibility(View.GONE);

    	final Button generateButton = genDialog.findViewById(R.id.generateButton);
    	final Button cancelButton = genDialog.findViewById(R.id.cancelButton);

    	keyWillExpire = false;
    	keyWillExpire_checkbox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				keyWillExpire = !keyWillExpire;
				if ( keyWillExpire )
					pickDate.setVisibility(View.VISIBLE);
				else
				{
					pickDate.setVisibility(View.GONE);
					pickedDateAndTime.setVisibility(View.GONE);
				}
			}
		});

    	cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				genDialog.dismiss();
			}
		});

    	daysAfterToday = 1;
		final Calendar selectedDate = Calendar.getInstance();
    	pickDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(
						instance,
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
								// TODO: Check to see if the date is valid, ie. if it's after today.
								Log.w("hyggelig", year + " " + month + " " + dayOfMonth);
								selectedDate.set(year, month, dayOfMonth);
								long difference = selectedDate.getTime().getTime() - Calendar.getInstance().getTime().getTime();
								daysAfterToday = TimeUnit.MILLISECONDS.toDays(difference);
								daysAfterToday = (daysAfterToday < 1 ? 1 : daysAfterToday);
								Log.w("hyggelig", "daysAfterToday = " + daysAfterToday);
								pickedDateAndTime.setText( "Expires: " + selectedDate.getTime().toString() );
								pickedDateAndTime.setVisibility(View.VISIBLE);
							}
						},
						selectedDate.get(Calendar.YEAR),
						selectedDate.get(Calendar.MONTH),
						selectedDate.get(Calendar.DAY_OF_MONTH)
				).show();
			}
		});

    	generateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String filename = keyFilename.getText().toString();
				String username = nameOfUser.getText().toString();
				String password = privKeyPass.getText().toString();

				// TODO: Should also check for invalid characters in the filename. Then again, that'd be handled in the catch block.
				if ( filename.equals("") )
				{
					alertError("Filename field is blank");
					return;
				}
				if ( username.equals("") )
				{
					alertError("Username field is blank");
					return;
				}
				if ( !password.equals(privKeyConfirm.getText().toString()) )
				{
					alertError("Passwords do not match");
					return;
				}

				try
				{
					String[] args = {
							"2",
							username,
							password,
							"true",
							filename,
							(keyWillExpire ? "" + daysAfterToday : "0"),
							instance.getPubkeysPath(),
							instance.getPrivkeysPath()
					};
					PublicKey.generateKeyPair(args);
				}
				catch ( Exception e )
				{
					Log.w("hyggelig", "Error generating keys: " + e.getMessage());
					alertError("Error generating keys: " + e.getMessage());
					e.printStackTrace();
					return;
				}

				new AlertDialog.Builder(instance)
						.setTitle("Success")
						.setMessage("Keypair for " + username + " generated successfully!")
						.setNegativeButton(android.R.string.ok, null)
						.show();
				genDialog.dismiss();
				if ( showingSecretKeys )
					showPrivateKeys();
				else
					showPublicKeys();
			}
		});

		genDialog.show();
    }

    // TODO: I really gotta move this into a utilities class or something.
	private void alertError(String msg)
	{
		new AlertDialog.Builder(instance)
				.setTitle("Error")
				.setMessage(msg)
				.setNegativeButton(android.R.string.ok, null)
				.show();
	}

	class KeysAdapter extends RecyclerView.Adapter<KeysAdapter.ImplementedViewHolder>
	{
		private String[] keyPairs;
		private boolean showSecretKeys;

		class ImplementedViewHolder extends RecyclerView.ViewHolder
		{
			public ImplementedViewHolder(View v)
			{
				super(v);
			}
		}

		public KeysAdapter(String[] theKeys, boolean secrets)
		{
			keyPairs = theKeys;
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
			String curFile = keyPairs[position];

			String[] keyInfo;	// See PublicKey.getKeyInfo() for info on the contents of this.
			try
			{
				if ( showSecretKeys )
					keyInfo = PublicKey.getKeyInfo(instance.getPrivkeysPath() + curFile, true);
				else
					keyInfo = PublicKey.getKeyInfo(instance.getPubkeysPath() + curFile, false);
			}
			catch ( Exception e )
			{
				Log.w("hyggelig", "Error while trying to get key file info: " + e.getMessage());
				e.printStackTrace();
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
				if ( keyInfo[5].equals("true") )
				{
					finalName += "[REVOKED]";
					keyName.setTextColor(Color.RED);
				}
				else
				{
					finalName += "[" + (showSecretKeys ? "priv" : "pub") + "] ";
					finalName += "[" + keyInfo[2] + "] ";
				}
				finalName += keyInfo[0];
				keyName.setText(finalName);
			}

			TextView keyFingerprint = holder.itemView.findViewById(R.id.keyFingerprint);
			if ( keyFingerprint != null )
			{
				keyFingerprint.setText("Fingerprint: " + keyInfo[1]);
			}

			TextView keyExpiration = holder.itemView.findViewById(R.id.keyExpiration);
			if ( keyExpiration != null )
			{
				String finalString = (keyInfo[4].equals("true") ? "Expired " : "Expires ") + "on ";
				finalString += keyInfo[3];
				if ( keyInfo[4].equals("true") )
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
}