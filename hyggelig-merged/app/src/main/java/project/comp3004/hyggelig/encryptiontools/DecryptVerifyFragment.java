package project.comp3004.hyggelig.encryptiontools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;

import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.aes.aes;
import project.comp3004.hyggelig.publickey.PublicKey;

// Fragment for the Decrypt/Verify File menu, which - you guessed it! - deals with decrypting and verifying files.
// NOTE: Some elements, such as the preview for pictures/videos, are not in use.
//		 However, they're kept in case we'll end up implementing them in the future.
// Authored by Gabriel Valachi (101068875).
public class DecryptVerifyFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
	// Main instance of the Encryption Tools activity.
	private EncryptionTools_MainActivity instance;

	// Encrypt/sign modes
	private final boolean MODE_DECRYPT = false;
	private final boolean MODE_VERIFY = true;
	private boolean execMode;

	// Encryption type - true if symmetric, false if asymmetric (public key).
	private boolean symmetricDecrypt;

	// The public/private key selected in the respective Spinner, depending on if we're encrypting or signing.
	private int selectedKey = 0;
	// Just like in EncryptFilesFragment, we need to know the paths of the keys.
	private String[] pubkeys;
	private String[] privkeys;

	private TableRow getfile_row;
	private TableRow enc_cipher_row;
	private TableRow sign_algo_row;
	private TableRow privkey_row;
	private TableRow pubkey_row;
	private TableRow password_row;  // TODO: Give the option to grab a password from the Password Manager.
	private TableRow execute_row;

	private Button getfile;
	private Spinner enc_cipher;
	//private Spinner sign_algo;
	private Spinner privkey;
	private Spinner pubkey;
	private EditText password;
	private Button execute;

	// URI of the input file.
	private Uri targetFileURI;

	// Path of the directory to store files output by any activity under the Encryption Tools.
	private String outputDirPath;

	// Called when this fragment is created.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View theView = inflater.inflate(R.layout.encryptiontools_decrypt_file_layout, container, false);

		// Creates the toolbar and back button.
		Toolbar toolbar = theView.findViewById(R.id.toolbar);
		instance = (EncryptionTools_MainActivity)getActivity();
		instance.setSupportActionBar(toolbar);
		if (instance.getSupportActionBar() != null) {
			instance.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			instance.getSupportActionBar().setDisplayShowHomeEnabled(true);
			instance.getSupportActionBar().setTitle("Decrypt/Verify a File");
		}
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {   // This actually makes the back button go back.
				Navigation.findNavController(v).popBackStack();
			}
		});
		outputDirPath = instance.getOutputDirPath();

		// Sets up listeners for the radio buttons.
		RadioButton dec_radio = theView.findViewById(R.id.dec_radio);
		if ( dec_radio != null ) dec_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDecryptionOptions();
			}
		});
		RadioButton verify_radio = theView.findViewById(R.id.verify_radio);
		if ( verify_radio != null ) verify_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showVerificationOptions();
			}
		});

		// Gets the UI elements for future access.
		getfile_row = theView.findViewById(R.id.getfile_row);
		enc_cipher_row = theView.findViewById(R.id.enc_cipher_row);
		sign_algo_row = theView.findViewById(R.id.sign_algo_row);
		pubkey_row = theView.findViewById(R.id.pubkey_row);
		privkey_row = theView.findViewById(R.id.privkey_row);
		password_row = theView.findViewById(R.id.password_row);
		execute_row = theView.findViewById(R.id.execute_row);

		enc_cipher = theView.findViewById(R.id.enc_cipher);
		pubkey = theView.findViewById(R.id.pubkey);
		//sign_algo = theView.findViewById(R.id.sign_algo);
		privkey = theView.findViewById(R.id.privkey);

		getfile = theView.findViewById(R.id.getfile);
		execute = theView.findViewById(R.id.execute);

		password = theView.findViewById(R.id.password);

		// Hides all UI elements except for the radio buttons. These will be displayed contextually.
		if (getfile_row != null) getfile_row.setVisibility(View.GONE);
		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
		if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
		if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);
		if (password_row != null) password_row.setVisibility(View.GONE);
		if (execute_row != null) execute_row.setVisibility(View.GONE);

		// Set the Spinner listeners.
		Spinner filetype_menu = theView.findViewById(R.id.filetype);
		if (filetype_menu != null) filetype_menu.setOnItemSelectedListener(this);
		if (enc_cipher != null) enc_cipher.setOnItemSelectedListener(this);
		//if (sign_algo != null) sign_algo.setOnItemSelectedListener(this);
		if (pubkey != null) pubkey.setOnItemSelectedListener(this);
		if (privkey != null) privkey.setOnItemSelectedListener(this);

		// Set the listener for the button to select a file.
		if (getfile != null)
			getfile.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile();
				}
			});

		// Get the filenames of the public and private keys, then populate the key names in the Spinners.
		// The filenames are critical for decryption and verifying, and the key names are for the user's selection of the private/public key.
		File pubkeysDir = new File(instance.getPubkeysPath());
		if ( pubkeysDir.list() != null )    // Public keys
		{
			List<String> pubkeyPaths = new ArrayList<>();
			List<String> pubkeyContents = new ArrayList<>();
			PublicKey.getKeyNamesInDir(pubkeysDir, false, pubkeyPaths, pubkeyContents);

			pubkeys = new String[pubkeyPaths.size()];
			pubkeyPaths.toArray(pubkeys);
			Log.w("hyggelig", Arrays.toString(pubkeys));
			ArrayAdapter<String> adapter = new ArrayAdapter<>(instance, android.R.layout.simple_spinner_item, pubkeyContents);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			pubkey.setAdapter(adapter);
		}
		File privkeysDir = new File(instance.getPrivkeysPath());
		if ( privkeysDir.list() != null )   // Private keys
		{
			List<String> privkeyPaths = new ArrayList<>();
			List<String> privkeyContents = new ArrayList<>();
			PublicKey.getKeyNamesInDir(privkeysDir, true, privkeyPaths, privkeyContents);

			privkeys = new String[privkeyPaths.size()];
			privkeyPaths.toArray(privkeys);
			Log.w("hyggelig", Arrays.toString(privkeys));
			ArrayAdapter<String> adapter = new ArrayAdapter<>(instance, android.R.layout.simple_spinner_item, privkeyContents);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			privkey.setAdapter(adapter);
		}

		// Set the listener for the "Decrypt/Verify" button.
		execute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeAction();
			}
		});

		return theView;
	}

	// Shows options that are applicable to both encryption and signing.
	private void showMutualOptions() {
		Log.w("hyggelig", "showMutualOptions");
		if (getfile_row != null) getfile_row.setVisibility(View.VISIBLE);
		if (execute_row != null) execute_row.setVisibility(View.VISIBLE);
	}

	// Shows decryption-specific parameters for the user to edit.
	private void showDecryptionOptions() {
		Log.w("hyggelig", "showDecryptionOptions");

		showMutualOptions();

		if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);
		if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);

		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.VISIBLE);
		//Spinner enc_cipher = v.findViewById(R.id.enc_cipher);
		if (enc_cipher != null)
			handleSpanners(enc_cipher.getSelectedItemPosition(), R.id.enc_cipher);
		//if ( password_row != null ) password_row.setVisibility(View.VISIBLE);

		if ( execute != null )
			execMode = MODE_DECRYPT;

		selectedKey = 0;
		pubkey.setSelection(0);
		privkey.setSelection(0);
	}

	// Show the options applicable for verifying data.
	private void showVerificationOptions() {
		Log.w("hyggelig", "showVerificationOptions");

		showMutualOptions();

		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);
		if (password_row != null) password_row.setVisibility(View.GONE);

		if (pubkey_row != null) pubkey_row.setVisibility(View.VISIBLE);
		//if (sign_algo_row != null) sign_algo_row.setVisibility(View.VISIBLE);
		//Spinner sign_algo = v.findViewById(R.id.sign_algo);
		//if (sign_algo != null)
		//	handleSpanners(v, sign_algo.getSelectedItemPosition(), R.id.sign_algo);

		if ( execute != null )
			execMode = MODE_VERIFY;

		selectedKey = 0;
		pubkey.setSelection(0);
		privkey.setSelection(0);
	}

	// Handles the UI's Spinners when an item is selected.
	// Args:
	// 	position - Position of the selected item in the Spinner.
	// 	ID - Spinner's resource ID.
	private void handleSpanners(int position, int ID)
	{
		Log.w("hyggelig", "handleSpanners");
		Log.w("hyggelig", "pos " + position);
		Log.w("hyggelig", "id " + ID);

		switch(ID)
		{
			case R.id.enc_cipher:
				Log.w("hyggelig", "enc_cipher");

				// Symmetric - show password prompt
				if (position == 0) {
					Log.w("hyggelig", "pos 0");

					symmetricDecrypt = true;

					if (privkey_row != null) privkey_row.setVisibility(View.GONE);

					if (password_row != null) password_row.setVisibility(View.VISIBLE);

					selectedKey = 0;
					pubkey.setSelection(0);
					privkey.setSelection(0);
				}
				// Asymmetric - hide password prompt and show private key Spinner
				else if (position == 1) {
					Log.w("hyggelig", "pos 1");

					symmetricDecrypt = false;

					if (password_row != null) password_row.setVisibility(View.GONE);

					if (privkey_row != null) privkey_row.setVisibility(View.VISIBLE);

					selectedKey = 0;
					pubkey.setSelection(0);
					privkey.setSelection(0);
				}
				break;
			case R.id.privkey:
			case R.id.pubkey:
				Log.w("hyggelig", "pub/privkey = " + position);
				selectedKey = position;
				break;
		}
	}

	// Called when an item is selected in a Spinner.
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		Log.w("hyggelig", "onItemSelected");
		Log.w("hyggelig", "actual pos " + position);
		handleSpanners(position, parent.getId());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		// Nothing to do here.
	}

	// Open a file manager to have the user select a file.
	private void pickFile() {
		Intent pickFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
		pickFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
		pickFileIntent.setType("*/*");
		pickFileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, "true");
		// Need to make sure we actually have something that can handle this action.
		if (instance != null && pickFileIntent.resolveActivity(instance.getPackageManager()) != null)
			startActivityForResult(Intent.createChooser(pickFileIntent, "Select a File Manager"), 0);
	}

	// Called when an Activity invoked from this Fragment finishes.
	// This only catches when a file has been selected.
	@Override
	public void onActivityResult(int requestcode, int resultcode, Intent resultIntent)
	{
		Log.w("hyggelig", "onActivityResult - invoked");

		if ( resultIntent != null && requestcode == 0 )
		{
			Log.w("hyggelig", "SELECT_FILE");
			Log.w("hyggelig", "" + resultIntent.getData());
			targetFileURI = resultIntent.getData();
			if ( getfile != null )
				getfile.setText("File selected!");
		}
	}

	// Decrypts the selected file and stores the plaintext contents in Hyggelig/EncryptionTools/DecryptOutput/.
	// Args:
	// 	keyPassword - Selected private key's password. No effect when symmetric encryption is selected.
	// Returns true on success, false on failure.
	private boolean decryptFile(String keyPassword)
	{
		if ( targetFileURI == null )
			return false;
		if ( password == null ) // We need this for the symmetric encryption password.
			return false;

		// Get the name and size of the file that we'll be decrypting.
		Cursor theCursor = instance.getContentResolver().query(targetFileURI, null, null, null, null);
		if ( theCursor == null )
			return false;
		theCursor.moveToFirst();
		String name = theCursor.getString( theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) );
		int size = (int)theCursor.getLong( theCursor.getColumnIndex(OpenableColumns.SIZE) );
		theCursor.close();

		// Fetch the contents file to be decrypted.
		byte[] contents = new byte[size];
		try
		{
			InputStream fileIS = instance.getContentResolver().openInputStream(targetFileURI);
			if ( fileIS == null )
			{
				Log.w("hyggelig", "null InputStream");
				alertError("null InputStream");
				return false;
			}
			fileIS.read(contents);
			fileIS.close();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while attempting to open " + targetFileURI.getPath() + "! " + e.getMessage());
			alertError("Error while attempting to open " + targetFileURI.getPath() + "! " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		// Now decrypt the file and save it.
		byte[] encBytes;
		String encOutPath = outputDirPath + "DecryptOutput/";
		String newName = name;
		if ( newName.contains(".hyg") ) // Strip off the .hyg extension if we have it.
			newName = newName.substring(0, newName.indexOf(".hyg"));
		if ( symmetricDecrypt )
		{
			try
			{
				// Decrypt the AES-256-encrypted file.
				encBytes = aes.decrypt(contents, 256, password.getText().toString());

				// Save the decrypted contents.
				FileOutputStream fileOS = new FileOutputStream(encOutPath + newName);
				fileOS.write(encBytes);
				fileOS.close();
			}
			catch (BadPaddingException e)   // This usually happens in the case of an incorrect password.
			{
				Log.w("hyggelig", "Incorrect password");
				alertError("Incorrect password");
				e.printStackTrace();
				return false;
			}
			catch (Exception e)
			{
				Log.w("hyggelig", "Error while decrypting the file! " + e.getMessage());
				alertError("Error while decrypting the file! " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			try
			{
				// As with encryption, PublicKey.decrypt() needs an actual file on disk to decrypt.
				// Since Android doesn't like me using file paths very much, we're gonna need to make a temporary file.
				File tempFile = new File(instance.getFilesDir().getAbsolutePath() + "/dec_temp");
				tempFile.createNewFile();
				FileOutputStream tempFileOS = new FileOutputStream(tempFile);
				tempFileOS.write(contents);
				tempFileOS.close();

				// Now we actually decrypt and save the file.
				String[] params = {tempFile.getAbsolutePath(), instance.getPrivkeysPath() + privkeys[selectedKey], keyPassword, encOutPath + newName};
				int returnStatus = PublicKey.decrypt(params);
				tempFile.delete();
				switch (returnStatus)
				{
					case 0:
						break;
					case -1:	// Programmer error
						throw new Exception("Not enough arguments");
					case -2:	// Password is incorrect
						Log.w("hyggelig", "Incorrect password for the private key");
						alertError("Incorrect password for the private key");
						return false;
				}
			}
			catch ( Exception e )
			{
				Log.w("hyggelig", "Error while decrypting the file! " + e.getMessage());
				alertError("Error while decrypting the file! " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}

		Log.w("hyggelig", "File decrypted and written successfully to " + encOutPath + newName + ".hyg" + "!");
		new AlertDialog.Builder(instance)
				.setTitle("Success")
				.setMessage("File decrypted and written successfully to " + encOutPath + newName + "!")
				.setNegativeButton(android.R.string.ok, null)
				.show();

		return true;
	}

	// Verifies a file with the selected public key and stores the contents (with the signature stripped) in Hyggelig/EncryptionTools/VerifiedFiles/.
	// Return true on success, false on failure.
	private boolean verifyFile()
	{
		if ( targetFileURI == null )
			return false;

		// Get the name and size of the file we'll be verifying.
		Cursor theCursor = instance.getContentResolver().query(targetFileURI, null, null, null, null);
		if ( theCursor == null )
			return false;
		theCursor.moveToFirst();
		String name = theCursor.getString( theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) );
		int size = (int)theCursor.getLong( theCursor.getColumnIndex(OpenableColumns.SIZE) );
		theCursor.close();

		// Read in the contents of the file being verified.
		byte[] contents = new byte[size];
		try
		{
			InputStream fileIS = instance.getContentResolver().openInputStream(targetFileURI);
			if ( fileIS == null )
			{
				Log.w("hyggelig", "null InputStream");
				alertError("null InputStream");
				return false;
			}
			fileIS.read(contents);
			fileIS.close();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while working on " + targetFileURI.getPath() + "! " + e.getMessage());
			alertError("Error while working on " + targetFileURI.getPath() + "! " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		// This part actually does the signing.
		String signOutPath = instance.getOutputDirPath() + "VerifiedFiles/";
		String newName = name;
		if ( newName.contains(".hyg-sign") ) // Strip off the .hyg-sign extension if we have it.
			newName = newName.substring(0, newName.indexOf(".hyg-sign"));
		int result;
		try
		{
			// Again, PublicKey.verify() needs an actual file stored on disk.
			// We'll need to create a temporary file, since Android doesn't like it when we use file paths.
			File tempFile = new File(instance.getFilesDir().getAbsolutePath() + "/sign_temp");
			tempFile.createNewFile();
			FileOutputStream tempFileOS = new FileOutputStream(tempFile);
			tempFileOS.write(contents);
			tempFileOS.close();

			// Verify the file, store the stripped contents, and get whether or not the file has a valid signature.
			result = PublicKey.verify(tempFile.getAbsolutePath(), instance.getPubkeysPath() + pubkeys[selectedKey],signOutPath + newName);
			tempFile.delete();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while signing the file! " + e.getMessage());
			alertError("Error while signing the file! " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		// Notify the user of the file's legitimacy/validity.
		String status = "";
		String additionalInfo = "";
		switch (result)
		{
			case 0:
				status = "Success! Valid Signature";
				additionalInfo = "The signature is valid. ";
				break;
			case -1:
				status = "Error: Broken Signature";
				additionalInfo = "The signature has been forged, or the data has been corrupted. ";
				break;
			case -2:
				status = "Error: Public Key Mismatch";
				additionalInfo = "Selected public key does not correspond to signing private key. ";
				break;
			case -3:
				status = "Error: No Signature";
				additionalInfo = "This file does not contain a signature. ";
				break;
		}
		Log.w("hyggelig", status);
		Log.w("hyggelig", additionalInfo);
		Log.w("hyggelig", "File extracted and written to " + signOutPath + newName);
		new AlertDialog.Builder(instance)
				.setTitle(status)
				.setMessage(additionalInfo + "File extracted and written to " + signOutPath + newName)
				.setNegativeButton(android.R.string.ok, null)
				.show();

		return true;
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

	// Called when the user clicks on the "Decrypt/Verify" button, once they've set all necessary parameters.
	private void executeAction()
	{
		Log.w("hyggelig", "executeAction");
		Log.w("hyggelig", "mode = " + execMode);

		// Check to make sure we actually have a file to decrypt or verify selected.
		if ( targetFileURI == null )
		{
			Log.w("hyggelig", "no file URI provided");
			alertError("No file selected");
			return;
		}

		if ( execMode == MODE_DECRYPT ) // Encryption, could be symmetric or asymmetric.
		{
			if ( symmetricDecrypt )
			{
				if ( password != null )
				{
					final String encPass = password.getText().toString();
					if ( encPass.equals("") )   // Blank password.
					{
						alertError("No password provided");
					}
					else    // Password filled in; attempt to decrypt.
					{
						decryptFile("");
					}
				}
			}
			else    // Asymmetric encryption
			{
				if ( selectedKey == 0 ) // No key selected.
				{
					alertError("No private key selected");
				}
				else
				{
					// Get the private key's password.
					final EditText keyPassPrompt = new EditText(instance);
					keyPassPrompt.setHint("BLANK = NO KEY PASSWORD");
					keyPassPrompt.setGravity(Gravity.CENTER_HORIZONTAL);
					keyPassPrompt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					AlertDialog.Builder dialog = new AlertDialog.Builder(instance)
							.setTitle("Enter Private Key Password")
							.setView(keyPassPrompt)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// Go ahead with the decryption.
									decryptFile(keyPassPrompt.getText().toString());
								}
							})
							.setNegativeButton(android.R.string.cancel, null);
					dialog.show();
				}
			}
		}
		else if ( execMode == MODE_VERIFY )   // Signing.
		{
			if ( selectedKey == 0 )
			{
				alertError("No public key selected");
			}
			else
			{
				verifyFile();
			}
		}
	}
}
