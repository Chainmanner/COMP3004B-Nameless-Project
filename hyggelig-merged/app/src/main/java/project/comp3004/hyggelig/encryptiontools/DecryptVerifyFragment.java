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

import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;

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

public class DecryptVerifyFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
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
	private TableRow password_row;  // TODO: Give the option to generate a password.
	private TableRow execute_row;

	private Button getfile;
	private Spinner enc_cipher;
	private Spinner sign_algo;
	private Spinner privkey;
	private Spinner pubkey;
	private EditText password;
	private Button execute;

	private Uri targetFileURI;

	private String outputDirPath;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View theView = inflater.inflate(R.layout.encryptiontools_decrypt_file_layout, container, false);

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

		RadioButton dec_radio = theView.findViewById(R.id.dec_radio);
		if ( dec_radio != null ) dec_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDecryptionOptions(v);
			}
		});
		RadioButton verify_radio = theView.findViewById(R.id.verify_radio);
		if ( verify_radio != null ) verify_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSigningOptions(v);
			}
		});

		getfile_row = theView.findViewById(R.id.getfile_row);
		enc_cipher_row = theView.findViewById(R.id.enc_cipher_row);
		sign_algo_row = theView.findViewById(R.id.sign_algo_row);
		pubkey_row = theView.findViewById(R.id.pubkey_row);
		privkey_row = theView.findViewById(R.id.privkey_row);
		password_row = theView.findViewById(R.id.password_row);
		execute_row = theView.findViewById(R.id.execute_row);

		enc_cipher = theView.findViewById(R.id.enc_cipher);
		pubkey = theView.findViewById(R.id.pubkey);
		sign_algo = theView.findViewById(R.id.sign_algo);
		privkey = theView.findViewById(R.id.privkey);

		getfile = theView.findViewById(R.id.getfile);
		execute = theView.findViewById(R.id.execute);

		password = theView.findViewById(R.id.password);

		if (getfile_row != null) getfile_row.setVisibility(View.GONE);
		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
		if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
		if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);
		if (password_row != null) password_row.setVisibility(View.GONE);
		if (execute_row != null) execute_row.setVisibility(View.GONE);

		Spinner filetype_menu = theView.findViewById(R.id.filetype);
		if (filetype_menu != null) filetype_menu.setOnItemSelectedListener(this);
		if (enc_cipher != null) enc_cipher.setOnItemSelectedListener(this);
		if (sign_algo != null) sign_algo.setOnItemSelectedListener(this);
		if (pubkey != null) pubkey.setOnItemSelectedListener(this);
		if (privkey != null) privkey.setOnItemSelectedListener(this);

		if (getfile != null)
			getfile.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile();
				}
			});

		// Get the filenames of the public and private keys, then populate the key names in the Spinners.
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

		execute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeAction();
			}
		});

		return theView;
	}

	// Shows options that are applicable to both encryption and signing.
	private void showMutualOptions(View v) {
		Log.w("hyggelig", "showMutualOptions");
		if (getfile_row != null) getfile_row.setVisibility(View.VISIBLE);
		if (execute_row != null) execute_row.setVisibility(View.VISIBLE);
	}

	private void showDecryptionOptions(View v) {
		Log.w("hyggelig", "showEncryptionOptions");

		//initAndHideAllOptions(v);
		showMutualOptions(v);

		if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);
		if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);

		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.VISIBLE);
		//Spinner enc_cipher = v.findViewById(R.id.enc_cipher);
		if (enc_cipher != null)
			handleSpanners(v, enc_cipher.getSelectedItemPosition(), R.id.enc_cipher);
		//if ( password_row != null ) password_row.setVisibility(View.VISIBLE);

		if ( execute != null )
			execMode = MODE_DECRYPT;

		selectedKey = 0;
	}

	// Show the options applicable for signing data.
	private void showSigningOptions(View v) {
		Log.w("hyggelig", "showSigningOptions");
		//initAndHideAllOptions(v);
		showMutualOptions(v);

		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
		if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);
		if (password_row != null) password_row.setVisibility(View.GONE);

		//if (sign_algo_row != null) sign_algo_row.setVisibility(View.VISIBLE);
		//Spinner sign_algo = v.findViewById(R.id.sign_algo);
		if (sign_algo != null)
			handleSpanners(v, sign_algo.getSelectedItemPosition(), R.id.sign_algo);

		if ( execute != null )
			execMode = MODE_VERIFY;

		selectedKey = 0;
	}

	// Handles the UI's Spinners.
	private void handleSpanners(View v, int position, int ID)
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
				}
				// Asymmetric - show password prompt
				else if (position == 1) {
					Log.w("hyggelig", "pos 1");

					symmetricDecrypt = false;

					if (password_row != null) password_row.setVisibility(View.GONE);

					if (privkey_row != null) privkey_row.setVisibility(View.VISIBLE);

					selectedKey = 0;
				}
				break;
			case R.id.privkey:
			case R.id.pubkey:
				Log.w("hyggelig", "pub/privkey = " + position);
				selectedKey = position;
				break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		Log.w("hyggelig", "onItemSelected");
		Log.w("hyggelig", "actual pos " + position);
		handleSpanners(v, position, parent.getId());
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

	private boolean decryptFile(String keyPassword)
	{
		if ( targetFileURI == null )
			return false;
		if ( password == null ) // We need this for the symmetric encryption password.
			return false;

		Cursor theCursor = instance.getContentResolver().query(targetFileURI, null, null, null, null);
		if ( theCursor == null )
			return false;
		theCursor.moveToFirst();
		String name = theCursor.getString( theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) );
		int size = (int)theCursor.getLong( theCursor.getColumnIndex(OpenableColumns.SIZE) );
		theCursor.close();

		// Fetch the file to be decrypted.
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

		Log.w("hyggelig", Arrays.toString(contents));   // FIXME: REMOVE WHEN DONE

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
				encBytes = aes.decrypt(contents, 256, password.getText().toString());

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
				// Since Android doesn't like me using file paths very much, we're gonna need to make a temporary file.
				File tempFile = new File(instance.getFilesDir().getAbsolutePath() + "/dec_temp");
				tempFile.createNewFile();
				FileOutputStream tempFileOS = new FileOutputStream(tempFile);
				tempFileOS.write(contents);
				tempFileOS.close();

				String[] params = {tempFile.getAbsolutePath(), instance.getPrivkeysPath() + privkeys[selectedKey], keyPassword, encOutPath + newName};
				int returnStatus = PublicKey.decrypt(params);
				tempFile.delete();
				switch (returnStatus)
				{
					case 0:
						break;
					case -1:
						throw new Exception("Not enough arguments");
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

	private boolean verifyFile()
	{
		if ( targetFileURI == null )
			return false;

		Cursor theCursor = instance.getContentResolver().query(targetFileURI, null, null, null, null);
		if ( theCursor == null )
			return false;
		theCursor.moveToFirst();
		String name = theCursor.getString( theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) );
		int size = (int)theCursor.getLong( theCursor.getColumnIndex(OpenableColumns.SIZE) );
		theCursor.close();

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
			// We'll need to create a temporary file, since Android doesn't like it when we use file paths.
			File tempFile = new File(instance.getFilesDir().getAbsolutePath() + "/sign_temp");
			tempFile.createNewFile();
			FileOutputStream tempFileOS = new FileOutputStream(tempFile);
			tempFileOS.write(contents);
			tempFileOS.close();

			result = PublicKey.verify(tempFile.getAbsolutePath(), instance.getPubkeysPath() + pubkeys[selectedKey],signOutPath + newName);
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while signing the file! " + e.getMessage());
			alertError("Error while signing the file! " + e.getMessage());
			e.printStackTrace();
			return false;
		}

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

	// TODO: I really gotta move this into a utilities class or something.
	private void alertError(String msg)
	{
		new AlertDialog.Builder(instance)
				.setTitle("Error")
				.setMessage(msg)
				.setNegativeButton(android.R.string.ok, null)
				.show();
	}

	// TODO: When encryption is available, finish this up.
	private void executeAction()
	{
		Log.w("hyggelig", "executeAction");
		Log.w("hyggelig", "mode = " + execMode);

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
					Log.w("hyggelig", "password = " + encPass); // FIXME: REMOVE WHEN DONE
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
