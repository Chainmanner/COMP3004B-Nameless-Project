package project.comp3004.hyggelig.encryptiontools;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.publickey.PublicKey;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

// Fragment to display the keyring, which shows the user's stored public and private keys.
// This is also where the user goes to add, remove, or export keys.
// Authored by Gabriel Valachi (101068875).
public class KeyringFragment extends Fragment
{
	// Main instance of the Encryption Tools activity.
    private EncryptionTools_MainActivity instance;

    private TabLayout keyTypeRow;

    // List of keys currently being displayed.
    private RecyclerView keyList;

    private Button importKey;
    private Button generateKeypair;

    // Are we currently displaying secret keys?
    private boolean showingSecretKeys = false;

    // The following are only used by generateKeypairDialog().
	boolean keyWillExpire = false;
	long daysAfterToday = 0;

	// Called when the fragment is created.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View theView = inflater.inflate(R.layout.encryptiontools_keyring_layout, container, false);

        instance = (EncryptionTools_MainActivity)getActivity();

        // Gets the toolbar and adds a back button.
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

        // Creates the listener for the tab row that selects the type of keys to display.
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

        // Button click listeners.
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

        // Initializes the RecyclerView that shows all the relevant keys.
		keyList = theView.findViewById(R.id.keyList);
		keyList.setVerticalScrollBarEnabled(true);
		keyList.setLayoutManager(new LinearLayoutManager(instance.getApplicationContext()));
		keyList.setAdapter(new KeysAdapter(null, false));

        showPublicKeys();	// Since the public keys tab is selected by default, show the public keys first.

        return theView;
    }

    // Shows the public keys stored on this device.
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

    // Shows the private keys stored on this device.
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

    // Opens a new activity for the user to select a key to import.
    private void importKeyPrompt()
    {
		Intent pickFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
		pickFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
		pickFileIntent.setType("*/*");
		pickFileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, "true");
		if ( instance != null && pickFileIntent.resolveActivity(instance.getPackageManager()) != null )
			startActivityForResult(Intent.createChooser(pickFileIntent, "Select a File Manager"), 0);
    }

    // Called when the activity spawned in importKeyPrompt() returns with a selected file.
	// Try to import the file if it's a valid OpenPGP key.
    @Override
	public void onActivityResult(int reqcode, int rescode, Intent resultIntent)
	{
		if ( resultIntent != null && reqcode == 0 && resultIntent.getData() != null )
		{
			Uri targetURI = resultIntent.getData();

			// Get the name and size of the key being imported.
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

			// Copy the file over into internal storage, in the appropriate key directory.
			byte[] keyContents = new byte[filesize];
			String outFilePath = "";
			try
			{
				// Get the contents of the file.
				InputStream fileIS = instance.getContentResolver().openInputStream(targetURI);
				if ( fileIS == null )
				{
					Log.w("hyggelig", "fileIS is null");
					return;
				}
				fileIS.read(keyContents);
				fileIS.close();

				// Check to make sure that we're importing an OpenPGP public or private key.
				String keyContentsString = new String(keyContents, "ASCII");
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

				// Write the contents of the imported file to a new file in internal storage.
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
			// Refresh the list of keys, now that we just added one.
			if ( showingSecretKeys )
				showPrivateKeys();
			else
				showPublicKeys();
		}
	}

	// Show the dialog to generate an OpenPGP keypair.
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

    	// Creates the handler for the date selector.
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
								Log.w("hyggelig", year + " " + month + " " + dayOfMonth);
								selectedDate.set(year, month, dayOfMonth);
								long difference = selectedDate.getTime().getTime() - Calendar.getInstance().getTime().getTime();
								
								// We need to make sure the date selected is later than the current day.
								if ( difference <= 0 )
								{
									alertError("Invalid date selected");
								}
								else
								{
									// Date selected is valid; convert it to days after today and feed it back to the user.
									daysAfterToday = TimeUnit.MILLISECONDS.toDays(difference);
									daysAfterToday = (daysAfterToday < 1 ? 1 : daysAfterToday);
									Log.w("hyggelig", "daysAfterToday = " + daysAfterToday);
									pickedDateAndTime.setText("Expires: " + selectedDate.getTime().toString());
									pickedDateAndTime.setVisibility(View.VISIBLE);
								}
							}
						},
						selectedDate.get(Calendar.YEAR),
						selectedDate.get(Calendar.MONTH),
						selectedDate.get(Calendar.DAY_OF_MONTH)
				).show();
			}
		});

    	// Sets the handler for the button to actually generate the keypair.
    	generateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String filename = keyFilename.getText().toString();
				String username = nameOfUser.getText().toString();
				String password = privKeyPass.getText().toString();

				// Make sure the required fields are not blank.
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

				// Parameters are OK, now let's generate the keypair.
				// Generally, computationally intensive things like this shouldn't be done on the main thread, but this isn't too slow.
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
				// Refresh the key list, now that we've just generated a new keypair.
				if ( showingSecretKeys )
					showPrivateKeys();
				else
					showPublicKeys();
			}
		});

		genDialog.show();
    }

    // Alerts the user of an error.
    // TODO: I really gotta move this into a utilities class or something.
	private void alertError(String msg)
	{
		new AlertDialog.Builder(instance)
				.setTitle("Error")
				.setMessage(msg)
				.setNegativeButton(android.R.string.ok, null)
				.show();
	}

	// The adapter that handles the elements in the key list.
	class KeysAdapter extends RecyclerView.Adapter<KeysAdapter.ImplementedViewHolder>
	{
		private String[] keyNames;	// Filenames of the keys being displayed.
		private boolean showSecretKeys;	// If true, we're showing private keys.

		class ImplementedViewHolder extends RecyclerView.ViewHolder
		{
			public ImplementedViewHolder(View v)
			{
				super(v);
			}
		}

		public KeysAdapter(String[] theKeys, boolean secrets)
		{
			keyNames = theKeys;
			showSecretKeys = secrets;
		}
		
		@Override
		public KeysAdapter.ImplementedViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View theView = LayoutInflater.from(parent.getContext()).inflate(R.layout.encryptiontools_keyring_keyitem, parent, false);

			return new KeysAdapter.ImplementedViewHolder(theView);
		}

		// Called for each item to be displayed in this list.
		// This sets the key information - user ID, fingerprint, expiry date, etc. - for the respective key in the list.
		// It also sets the click handler for the item, and the click handlers for the buttons in the UI that pops up after clicking this item.
		@Override
		public void onBindViewHolder(KeysAdapter.ImplementedViewHolder holder, int position)
		{
			final int thePosition = position;	// This is here because I'd rather not modify the function declaration.
			String curFile = keyNames[position];

			// Gets the information about the keys being represented.
			final String[] keyInfo;	// See PublicKey.getKeyInfo() for info on the contents of this.
			try
			{
				// We have different directories for public and private keys, and different ways of fetching them.
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

			// Changes the background color depending on the item's position, to differentiate between items.
			ConstraintLayout theLayout = holder.itemView.findViewById(R.id.constraint_layout);
			if ( position % 2 == 0 )
			{
				theLayout.setBackgroundColor(Color.LTGRAY);
			}
			else
			{
				theLayout.setBackgroundColor(Color.WHITE);
			}

			// Sets the click listener for this item in the list being displayed.
			// When this item is clicked, a menu appears, asking the user if they want to export the key, delete it, or do neither.
			theLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Dialog opsDialog = new Dialog(instance);
					opsDialog.setContentView(R.layout.encryptiontools_keyring_keyoptions);

					final TextView storedKeyFilename = opsDialog.findViewById(R.id.storedKeyFilename);
					storedKeyFilename.setText( "File: " + keyNames[thePosition] );
					final TextView keyUser = opsDialog.findViewById(R.id.keyUser);
					keyUser.setText( "User ID: " + keyInfo[0] );

					// Sets the handler for the export key button in the brought-up menu.
					// This just copies the key from internal storage to Hyggelig/EncryptionTools/ExportedKeys/ (external storage).
					final Button exportKeyButton = opsDialog.findViewById(R.id.exportKeyButton);
					exportKeyButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// Key file from internal storage
							File importedKey = new File((showSecretKeys ? instance.getPrivkeysPath()
																		: instance.getPubkeysPath()) + keyNames[thePosition]);
							// Output key file
							File exportedKey = new File(instance.getOutputDirPath() + "ExportedKeys/" + keyNames[thePosition]);

							// Now we copy the internally-stored key to an external directory.
							try
							{
								byte[] keyContents = new byte[(int)importedKey.length()];

								FileInputStream fileIS = new FileInputStream(importedKey);
								fileIS.read(keyContents);
								fileIS.close();

								exportedKey.createNewFile();
								FileOutputStream fileOS = new FileOutputStream(exportedKey);
								fileOS.write(keyContents);
								fileOS.close();
							}
							catch ( Exception e )
							{
								Log.w("hyggelig", "Error while exporting key " + keyNames[thePosition] + ": " + e.getMessage());
								alertError("Error while exporting key " + keyNames[thePosition] + ": " + e.getMessage());
								e.printStackTrace();
								return;
							}

							new AlertDialog.Builder(instance)
									.setTitle("Success")
									.setMessage("Key successfully exported to "+ exportedKey.getAbsolutePath() + "!")
									.setNegativeButton(android.R.string.ok, null)
									.show();
							opsDialog.dismiss();
						}
					});

					// Sets up the handler for the delete button in the popup menu.
					// Prompts the user if they want to delete the key, and if so, deletes it from internal storage.
					final Button deleteKeyButton = opsDialog.findViewById(R.id.deleteKeyButton);
					deleteKeyButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							new AlertDialog.Builder(instance)
									.setTitle("Warning")
									.setMessage("Are you sure you want to delete this key?")
									.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											File fileToDelete = new File((showSecretKeys ? instance.getPrivkeysPath()
																							: instance.getPubkeysPath())
																			+ keyNames[thePosition]);
											if ( fileToDelete.delete() )
											{
												new AlertDialog.Builder(instance)
														.setTitle("Success")
														.setMessage("Key deleted!")
														.setNegativeButton(android.R.string.ok, null)
														.show();
												opsDialog.dismiss();

												if ( showSecretKeys )
													showPrivateKeys();
												else
													showPublicKeys();
											}
											else
												alertError("Failed to delete specified key due to unknown error");
										}
									})
									.setNegativeButton(android.R.string.no, null)
									.show();
						}
					});

					// Sets the handler for the cancel button in the popup menu.
					// Just dismisses the menu.
					final Button cancelKeyDialog = opsDialog.findViewById(R.id.cancelKeyDialog);
					cancelKeyDialog.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							opsDialog.dismiss();
						}
					});

					opsDialog.show();
				}
			});

			// Displays the user ID, plus some other info, in the following format:
			//	[REVOKED] (if revoked)
			//	[key type]
			//	[algorithm]
			//	User ID
			// If the key is revoked, this appears red.
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

			// Displays the key's fingerprint.
			TextView keyFingerprint = holder.itemView.findViewById(R.id.keyFingerprint);
			if ( keyFingerprint != null )
			{
				keyFingerprint.setText("Fingerprint: " + keyInfo[1]);
			}

			// Displays the key's expiration date.
			// If this date has passed, this text appears in red.
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

		// Gets the number of items in this adapter.
		// Note that if keyNames is null, returns zero to prevent crashing.
		@Override
		public int getItemCount()
		{
			return keyNames == null ? 0 : keyNames.length;
		}
	}
}
