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
//import android.widget.CheckBox;
import android.widget.EditText;
//import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import project.comp3004.hyggelig.aes.aes;
import project.comp3004.hyggelig.camera.CameraActivity;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.publickey.PublicKey;

// Fragment for the Encrypt/Sign File menu, which - get this - deals with encrypting and signing files.
// NOTE: Some elements, such as the preview for pictures/videos, are not in use.
//		 However, they're kept in case we'll end up implementing them in the future.
// Authored by Gabriel Valachi (101068875).
public class EncryptFilesFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
	// Main instance of the Encryption Tools activity.
	private EncryptionTools_MainActivity instance;

	// Custom intent codes
	private final int SELECT_FILE = 0;
	private final int TAKE_PICTURE = 1;
	private final int RECORD_VIDEO = 2;

	// Encrypt/sign modes
	private final boolean MODE_ENCRYPT = false;
	private final boolean MODE_SIGN = true;
	private boolean execMode;

	// Encryption type - true if symmetric, false if asymmetric (public key).
	private boolean symmetricEncrypt;

	// The public/private key selected in the respective Spinner, depending on if we're encrypting or signing.
	private int selectedKey = 0;
	// We're also gonna need the filename of the selected key for quick access.
	private String[] pubkeys;
	private String[] privkeys;

	// Boolean to indicate that we should delete the original file after encryption.
	// Useless due to not being able to delete files, but kept nonetheless.
	//private boolean deleteOriginalFile = false;

	private TableRow filetype_row;
	private TableRow getfile_row;
	//private TableRow preview_row;
	private TableRow enc_cipher_row;
	//private TableRow sign_algo_row;
	private TableRow pubkey_row;
	private TableRow privkey_row;
	private TableRow password_row;  // TODO: Give the option to generate a password.
	//private TableRow deleteorig_row;
	private TableRow execute_row;

	private Spinner enc_cipher;
	private Spinner pubkey;
	//private Spinner sign_algo;
	private Spinner privkey;

	private Button getfile;
	private Button execute;

	//private ImageView preview;

	private EditText password;

	//private CheckBox deleteOrig;

	// The file we'll be working on, if the input source is an arbitrary file.
	private Uri targetFileURI;

	// How our target file came to be (ie. is it an arbitrary file? Or a picture or video taken in this app?).
	private int targetFileSource;

	// Directory for output files.
	private String outputDirPath;

	// Called upon creating the Fragment.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View theView = inflater.inflate(R.layout.encryptiontools_encrypt_files_layout, container, false);

		// This adds a back button.
		Toolbar toolbar = theView.findViewById(R.id.toolbar);
		instance = (EncryptionTools_MainActivity) getActivity();
		instance.setSupportActionBar(toolbar);
		if (instance.getSupportActionBar() != null) {
			instance.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			instance.getSupportActionBar().setDisplayShowHomeEnabled(true);
			instance.getSupportActionBar().setTitle("Encrypt/Sign a File");
		}
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {   // This actually makes the back button go back.
				Navigation.findNavController(v).popBackStack();//.navigate(R.id.action_encryptFilesFragment2_to_encryptionToolsMainMenuFragment3);
			}
		});

		// Import the output directory path from the Activity's variable.
		outputDirPath = instance.getOutputDirPath();

		// Click listeners for the radio buttons that prompt the user what they want to do.
		RadioButton enc_radio = theView.findViewById(R.id.enc_radio);
		if (enc_radio != null) enc_radio.setOnClickListener(new View.OnClickListener() {
																@Override
																public void onClick(View v) {
																	showEncryptionOptions();
																}
															}
		);
		RadioButton sign_radio = theView.findViewById(R.id.sign_radio);
		if (enc_radio != null) sign_radio.setOnClickListener(new View.OnClickListener() {
																 @Override
																 public void onClick(View v) {
																	 showSigningOptions();
																 }
															 }
		);

		// Get pointers to the UI elements, as we'll be dynamically controlling them.
		// Table rows
		filetype_row = theView.findViewById(R.id.filetype_row);
		getfile_row = theView.findViewById(R.id.getfile_row);
		//preview_row = theView.findViewById(R.id.preview_row);
		enc_cipher_row = theView.findViewById(R.id.enc_cipher_row);
		//sign_algo_row = theView.findViewById(R.id.sign_algo_row);
		pubkey_row = theView.findViewById(R.id.pubkey_row);
		privkey_row = theView.findViewById(R.id.privkey_row);
		password_row = theView.findViewById(R.id.password_row);
		//deleteorig_row = theView.findViewById(R.id.deleteorig_row);
		execute_row = theView.findViewById(R.id.execute_row);

		// Spinners
		enc_cipher = theView.findViewById(R.id.enc_cipher);
		pubkey = theView.findViewById(R.id.pubkey);
		//sign_algo = theView.findViewById(R.id.sign_algo);
		privkey = theView.findViewById(R.id.privkey);

		// Buttons
		getfile = theView.findViewById(R.id.getfile);
		execute = theView.findViewById(R.id.execute);

		// Image preview
		//preview = theView.findViewById(R.id.preview);

		// Password field (symmetric key encryption only)
		password = theView.findViewById(R.id.password);

		// Checkbox
		//deleteOrig = theView.findViewById(R.id.deleteorig);

		// Hide the UI elements at first and show them contextually.
		if (filetype_row != null) filetype_row.setVisibility(View.GONE);
		if (getfile_row != null) getfile_row.setVisibility(View.GONE);
		//if (preview_row != null) preview_row.setVisibility(View.GONE);
		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
		//if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
		if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);
		if (password_row != null) password_row.setVisibility(View.GONE);
		//if (deleteorig_row != null) deleteorig_row.setVisibility(View.GONE);
		if (execute_row != null) execute_row.setVisibility(View.GONE);

		// Setting the Spinners' listeners.
		Spinner filetype_menu = theView.findViewById(R.id.filetype);
		if (filetype_menu != null) filetype_menu.setOnItemSelectedListener(this);
		if (enc_cipher != null) enc_cipher.setOnItemSelectedListener(this);
		//if (sign_algo != null) sign_algo.setOnItemSelectedListener(this);
		if (pubkey != null) pubkey.setOnItemSelectedListener(this);
		if (privkey != null) privkey.setOnItemSelectedListener(this);

		// Get and store the filenames of the public and private keys, then populate the key names in the Spinners.
		// The filenames are necessary for encryption/signing operations, but the names are just for show.
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

		// Checkbox to delete the original file on encryption.
		// Removed because root permissions are required.
		//if ( deleteOrig != null )
		//	deleteOrig.setOnClickListener(new View.OnClickListener() {
		//		@Override
		//		public void onClick(View v) {
		//			deleteOriginalFile = !deleteOriginalFile;
		//			Log.w("hyggelig", "deleteOriginalFile = " + deleteOriginalFile);
		//		}
		//	});

		// Setting the click listener for the execute action button.
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
		//TableRow filetype_row = v.findViewById(R.id.filetype_row);
		if (filetype_row != null) filetype_row.setVisibility(View.VISIBLE);
		//TableRow getfile_row = v.findViewById(R.id.getfile_row);
		if (getfile_row != null) getfile_row.setVisibility(View.VISIBLE);
		if (execute_row != null) execute_row.setVisibility(View.VISIBLE);
	}

	// Show the options applicable for encrypting data.
	private void showEncryptionOptions() {
		Log.w("hyggelig", "showEncryptionOptions");

		//initAndHideAllOptions(v);
		showMutualOptions();

		//if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
		if (privkey_row != null) privkey_row.setVisibility(View.GONE);

		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.VISIBLE);
		//if (deleteorig_row != null) deleteorig_row.setVisibility(View.VISIBLE);
		//Spinner enc_cipher = v.findViewById(R.id.enc_cipher);
		if (enc_cipher != null)
			handleSpanners(enc_cipher.getSelectedItemPosition(), R.id.enc_cipher);
		//if ( password_row != null ) password_row.setVisibility(View.VISIBLE);

		if ( execute != null )
			execMode = MODE_ENCRYPT;

		selectedKey = 0;
		pubkey.setSelection(0);
		privkey.setSelection(0);
	}

	// Show the options applicable for signing data.
	private void showSigningOptions() {
		Log.w("hyggelig", "showSigningOptions");
		//initAndHideAllOptions(v);
		showMutualOptions();

		if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
		if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
		if (password_row != null) password_row.setVisibility(View.GONE);
		//if (deleteorig_row != null) deleteorig_row.setVisibility(View.GONE);

		//if (sign_algo_row != null) sign_algo_row.setVisibility(View.VISIBLE);
		//Spinner sign_algo = v.findViewById(R.id.sign_algo);
		//if (sign_algo != null)
		//    handleSpanners(v, sign_algo.getSelectedItemPosition(), R.id.sign_algo);
		if (privkey_row != null) privkey_row.setVisibility(View.VISIBLE);

		if ( execute != null )
			execMode = MODE_SIGN;

		selectedKey = 0;
		pubkey.setSelection(0);
		privkey.setSelection(0);
	}

	// Makes the UI react to the options selected in Spinners.
	// For example, if a symmetric cipher is selected, hide the public key Spinner and show the password prompt.
	// Args:
	//	position - Position of the selected item in the Spinner.
	//	ID - Spinner's resource ID
	private void handleSpanners(int position, int ID) {
		Log.w("hyggelig", "handleSpanners");
		Log.w("hyggelig", "pos " + position);
		Log.w("hyggelig", "id " + ID);
		switch (ID) {
			// File type
			case (R.id.filetype): {
				Log.w("hyggelig", "filetype");
				if ( getfile != null )
					getfile.setText("Click here to select...");
				targetFileURI = null;   // Clear this out just to be safe.

				if (position == 0) {	// User wants to encrypt an arbitrary file.
					targetFileSource = SELECT_FILE;
					if (getfile != null)
						getfile.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								pickFile();
							}
						});
				} else if (position == 1) {	// User wants to take a picture and encrypt it directly.
					targetFileSource = TAKE_PICTURE;
					if (getfile != null)
						getfile.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								takePicture();
							}
						});
				} else if (position == 2) {	// User wants to record a video and encrypt it directly.
					targetFileSource = RECORD_VIDEO;
					if (getfile != null)
						getfile.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								recordVideo();
							}
						});
				}
			}
			break;
			// Encryption ciphers
			case (R.id.enc_cipher): {
				Log.w("hyggelig", "enc_cipher");

				// Symmetric - show password prompt
				if (position == 0) {
					Log.w("hyggelig", "pos 0");

					symmetricEncrypt = true;

					if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);

					if (password_row != null) password_row.setVisibility(View.VISIBLE);

					selectedKey = 0;	// Reset the position of the selected key.
					pubkey.setSelection(0);
					privkey.setSelection(0);
				}
				// Asymmetric - hide password prompt
				else if (position == 1) {
					Log.w("hyggelig", "pos 1");

					symmetricEncrypt = false;

					if (password_row != null) password_row.setVisibility(View.GONE);

					if (pubkey_row != null) pubkey_row.setVisibility(View.VISIBLE);

					selectedKey = 0;
					pubkey.setSelection(0);
					privkey.setSelection(0);
				}
			}
			break;
			// Public key selection
			case (R.id.pubkey): {
				Log.w("hyggelig", "pubkey = " + position);
				selectedKey = position;
			}
			break;
			// Private key selection
			case (R.id.privkey): {
				Log.w("hyggelig", "privkey = " + position);
				selectedKey = position;
			}
			break;
		}
	}

	// Called when an item in a Spinner (dropdown menu) is selected.
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		Log.w("hyggelig", "onItemSelected");
		Log.w("hyggelig", "actual pos " + position);
		handleSpanners(position, parent.getId());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
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
			startActivityForResult(Intent.createChooser(pickFileIntent, "Select a File Manager"), SELECT_FILE);
	}

	// Opens CameraActivity to have the user take a picture.
	private void takePicture() {
        Intent takePictureIntent = new Intent(instance, CameraActivity.class);
        String outPath = instance.getFilesDir().getAbsolutePath() + "/temp_pic";
        takePictureIntent.putExtra("outPath", outPath);
        takePictureIntent.putExtra("recordVideo", false);
        startActivityForResult(takePictureIntent, TAKE_PICTURE);
	}

	// Opens CameraActivity to have the user record a video.
	private void recordVideo() {
		Intent recordVideoIntent = new Intent(instance, CameraActivity.class);
		String outPath = instance.getFilesDir().getAbsolutePath() + "/temp_vid";
		recordVideoIntent.putExtra("outPath", outPath);
		recordVideoIntent.putExtra("recordVideo", true);
		startActivityForResult(recordVideoIntent, RECORD_VIDEO);
	}

	// Called when an Activity spawned by this Fragment finishes and returns a response.
	// Used to trap and react to the responses from when the user picks a file, takes a picture, or records a video.
	@Override
	public void onActivityResult(int requestcode, int resultcode, Intent resultIntent) {
		Log.w("hyggelig", "onActivityResult - invoked");

		if (resultIntent != null)
			Log.w("hyggelig", "resultIntent: " + resultIntent.getData());
		else
			Log.w("hyggelig", "resultIntent is null");

		switch (requestcode) {
			case SELECT_FILE:
				Log.w("hyggelig", "SELECT_FILE");
				//if (preview_row != null)
				//	preview_row.setVisibility(View.GONE);
				Log.w("hyggelig", "" + resultIntent.getData());
				targetFileURI = resultIntent.getData();
				if ( getfile != null )
					getfile.setText("File selected!");
				// TODO: If the file selected is an image, preview it.
				break;
			case TAKE_PICTURE:
				Log.w("hyggelig", "TAKE_PICTURE");
				if ( getfile != null )
					getfile.setText("Picture taken!");
				break;
			case RECORD_VIDEO:  // TODO
				Log.w("hyggelig", "RECORD_VIDEO");
				if ( getfile != null )
					getfile.setText("Video recorded!");
				break;
			default:
				Log.w("hyggelig", "good lord! What did you do?");
				break;
		}

		super.onActivityResult(requestcode, resultcode, resultIntent);
	}

	// Encrypts the file with the specified parameters and stores the result in Hyggelig/EncryptionTools/EncryptOutput/.
	// If the source of the file is an arbitrary file, its URI will be stored in targetFileUri.
	// If it's a picture or video taken/recorded in the app, it'll be stored in a temporary file in internal storage.
	// Returns true if successful, false if not.
	private boolean encryptFile()
	{
		if ( targetFileSource == SELECT_FILE && targetFileURI == null )
			return false;
		if ( symmetricEncrypt && password == null ) // We need this for the symmetric encryption password.
			return false;

		// Get the name and size of the file, so that we know what to name the output file and how much data to read.
		String name = "";
		int size = 0;
		if ( targetFileSource == SELECT_FILE )
		{
			// Need to get the arbitrarily-selected file's data using targetFileURI.
			// Complicated, but I don't really have a choice here.
			Cursor theCursor = instance.getContentResolver().query(targetFileURI, null, null, null, null);
			if (theCursor == null)
				return false;
			theCursor.moveToFirst();
			name = theCursor.getString(theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
			size = (int) theCursor.getLong(theCursor.getColumnIndex(OpenableColumns.SIZE));
			theCursor.close();
		}
		else if ( targetFileSource == TAKE_PICTURE )
		{
			name = "secret-pic.jpg";
			size = (int)(new File(instance.getFilesDir().getAbsolutePath() + "/temp_pic")).length();
		}
		else if ( targetFileSource == RECORD_VIDEO )
		{
			name = "secret-vid.mp4";
			size = (int)(new File(instance.getFilesDir().getAbsolutePath() + "/temp_vid")).length();
		}

		// Read the contents of the input file.
		byte[] contents = new byte[size];
		try
		{
			// Create the input stream differently depending on how we got our input data.
			InputStream fileIS = null;
			if ( targetFileSource == SELECT_FILE )	// Access the file with the URI in targetFileURI.
				fileIS = instance.getContentResolver().openInputStream(targetFileURI);
			else if ( targetFileSource == TAKE_PICTURE )	// Read the temporary picture stored internally.
				fileIS = new FileInputStream(instance.getFilesDir().getAbsolutePath() + "/temp_pic");
			else if ( targetFileSource == RECORD_VIDEO )	// Read the temporary picture stored externally.
				fileIS = new FileInputStream(instance.getFilesDir().getAbsolutePath() + "/temp_vid");
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
			Log.w("hyggelig", "Error while attempting to open and read input file! " + e.getMessage());
			alertError("Error while attempting to open and read input file! " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		// Encrypt the output and store it in the application's output directory, in external storage.
		byte[] encBytes;
		String encOutPath = outputDirPath + "EncryptOutput/";
		if ( symmetricEncrypt )	// Symmetrically encrypt the file, with the user-specified password.
		{
			try
			{
				// Encrypt with AES-256.
				encBytes = aes.encrypt(contents, 256, password.getText().toString());

				// Store the encrypted file.
				FileOutputStream fileOS = new FileOutputStream(encOutPath + name + ".hyg");
				fileOS.write(encBytes);
				fileOS.close();
			}
			catch (Exception e)
			{
				Log.w("hyggelig", "Error while encrypting the file! " + e.getMessage());
				alertError("Error while encrypting the file! " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		else	// Public key encryption
		{
			try
			{
				// PublicKey.encrypt() requires an actual file on disk for encryption, so we need to access the data that way.
				// Since Android doesn't like me using file paths very much, we're gonna need to make a temporary file.
				File tempFile = new File(instance.getFilesDir().getAbsolutePath() + "/enc_temp");
				tempFile.createNewFile();
				FileOutputStream tempFileOS = new FileOutputStream(tempFile);
				tempFileOS.write(contents);
				tempFileOS.close();

				// Encrypt the file and store the encrypted file in external storage.
				String[] params = {tempFile.getAbsolutePath(), instance.getPubkeysPath() + pubkeys[selectedKey], "N/A", "N/A", encOutPath + name + ".hyg", "false", "true"};
				int returnStatus = PublicKey.encrypt(params);
				tempFile.delete();	// Delete the temporary file.
				switch (returnStatus)   // NOTE: These are programmer errors.
				{
					case 0:
						break;
					case -1:
						throw new Exception("Not enough arguments");
					case -2:
						throw new Exception("Badly-formatted arguments");
					case -3:
						Log.w("hyggelig", "No encryption public key found in the chosen key file. Is it a sign-only key?");
						alertError("No encryption public key found in the chosen key file. Is it a sign-only key?");
						return false;
				}
			}
			catch ( Exception e )
			{
				Log.w("hyggelig", "Error while encrypting the file! " + e.getMessage());
				alertError("Error while encrypting the file! " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}

		Log.w("hyggelig", "File encrypted and written successfully to " + encOutPath + name + ".hyg" + "!");
		new AlertDialog.Builder(instance)
				.setTitle("Success")
				.setMessage("File encrypted and written successfully to " + encOutPath + name + ".hyg" + "!")
				.setNegativeButton(android.R.string.ok, null)
				.show();

		// Delete any temporary files we would have made for directly encrypting a picture or a video.
		if ( targetFileSource == TAKE_PICTURE )
		{
			File temp_pic = new File(instance.getFilesDir().getAbsolutePath() + "/temp_pic");
			if ( temp_pic.exists() ) temp_pic.delete();
		}
		else if ( targetFileSource == RECORD_VIDEO )
		{
			File temp_vid = new File(instance.getFilesDir().getAbsolutePath() + "/temp_vid");
			if ( temp_vid.exists() ) temp_vid.delete();
		}

		// Delete the original file if the user asked to.
		// FIXME: This just doesn't work.
        /*if ( deleteOriginalFile )
        {
            Log.w("hyggelig", targetFileURI.getPath());
            File fileToDelete = new File(targetFileURI.getPath());
            fileToDelete.delete();
            handleSpanners(this.getView(), 0, R.id.filetype);   // Deselect the deleted file.
        }*/

		return true;
	}

	// Signs a file using the user-inputted parameters and stores it in Hyggelig/EncryptionTools/SignOutput/.
	// Args:
	// 	keyPass - The password of the selected private key.
	// Returns true on success, false on failure.
	private boolean signFile(String keyPass)
	{
		if ( targetFileSource == SELECT_FILE && targetFileURI == null )
			return false;

		// Get the input file's name and size.
		String name = "";
		int size = 0;
		if ( targetFileSource == SELECT_FILE )
		{
			// Need to get the arbitrarily-selected file's data using targetFileURI.
			Cursor theCursor = instance.getContentResolver().query(targetFileURI, null, null, null, null);
			if (theCursor == null)
				return false;
			theCursor.moveToFirst();
			name = theCursor.getString(theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
			size = (int) theCursor.getLong(theCursor.getColumnIndex(OpenableColumns.SIZE));
			theCursor.close();
		}
		else if ( targetFileSource == TAKE_PICTURE )
		{
			name = "secret-pic.jpg";
			size = (int)(new File(instance.getFilesDir().getAbsolutePath() + "/temp_pic")).length();
		}
		else if ( targetFileSource == RECORD_VIDEO )
		{
			name = "secret-vid.mp4";
			size = (int)(new File(instance.getFilesDir().getAbsolutePath() + "/temp_vid")).length();
		}

		// Read the contents of the input file.
		byte[] contents = new byte[size];
		try
		{
			InputStream fileIS = null;
			if ( targetFileSource == SELECT_FILE )
				fileIS = instance.getContentResolver().openInputStream(targetFileURI);
			else if ( targetFileSource == TAKE_PICTURE )
				fileIS = new FileInputStream(instance.getFilesDir().getAbsolutePath() + "/temp_pic");
			else if ( targetFileSource == RECORD_VIDEO )
				fileIS = new FileInputStream(instance.getFilesDir().getAbsolutePath() + "/temp_vid");
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
		String signOutPath = instance.getOutputDirPath() + "SignOutput/";
		try
		{
			// As with asymmetric encryption, we need to have an actual file on disk for signing.
			// We'll need to create a temporary file, since Android doesn't like it when we use file paths.
			File tempFile = new File(instance.getFilesDir().getAbsolutePath() + "/sign_temp");
			tempFile.createNewFile();
			FileOutputStream tempFileOS = new FileOutputStream(tempFile);
			tempFileOS.write(contents);
			tempFileOS.close();

			int retStatus = PublicKey.sign(tempFile.getAbsolutePath(), instance.getPrivkeysPath() + privkeys[selectedKey], keyPass, signOutPath + name + ".hyg-sign", true);
			tempFile.delete();
			if ( retStatus == -1 )	// If we got a -1 response, the private key's password was wrong.
			{
				Log.w("hyggelig", "Incorrect password for the private key");
				alertError("Incorrect password for the private key");
				return false;
			}
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while signing the file! " + e.getMessage());
			alertError("Error while signing the file! " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		Log.w("hyggelig", "File signed and written successfully to " + signOutPath + name + ".hyg-sign" + "!");
		new AlertDialog.Builder(instance)
				.setTitle("Success")
				.setMessage("File signed and written successfully to " + signOutPath + name + ".hyg-sign" + "!")
				.setNegativeButton(android.R.string.ok, null)
				.show();

		// Clean up the temporary picture/video, depending on which was the input source.
		if ( targetFileSource == TAKE_PICTURE )
		{
			File temp_pic = new File(instance.getFilesDir().getAbsolutePath() + "/temp_pic");
			if ( temp_pic.exists() ) temp_pic.delete();
		}
		else if ( targetFileSource == RECORD_VIDEO )
		{
			File temp_vid = new File(instance.getFilesDir().getAbsolutePath() + "/temp_vid");
			if ( temp_vid.exists() ) temp_vid.delete();
		}

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

	// Called when the user clicks on the "Encrypt/Sign" button.
	// Checks to make sure all input is valid, (re)prompts for passwords as necessary, and if all's good, does the encryption or signing.
	private void executeAction()
	{
		Log.w("hyggelig", "executeAction");
		Log.w("hyggelig", "mode = " + execMode);

		// Check to make sure we actually have a file to encrypt.
		// If we chose to select an arbitrary file, check to ensure we have a URI.
		// If we opted to take a picture or record a video, ensure the temporary file exists.
		if ( targetFileSource == SELECT_FILE && targetFileURI == null )
		{
			Log.w("hyggelig", "no file URI provided");
			alertError("No file selected");
			return;
		}
		else if ( targetFileSource == TAKE_PICTURE
				&& !(new File(instance.getFilesDir().getAbsolutePath() + "/temp_pic")).exists() )
		{
			Log.w("hyggelig", "temp_pic nonexistent");
			alertError("No picture taken");
			return;
		}
		else if ( targetFileSource == RECORD_VIDEO
				&& !(new File(instance.getFilesDir().getAbsolutePath() + "/temp_vid")).exists() )
		{
			Log.w("hyggelig", "temp_vid nonexistent");
			alertError("No video recorded");
			return;
		}

		if ( execMode == MODE_ENCRYPT ) // Encryption, could be symmetric or asymmetric.
		{
			if ( symmetricEncrypt )	// Symmetric
			{
				if ( password != null )
				{
					final String encPass = password.getText().toString();
					if ( encPass.equals("") )   // Blank password.
					{
						alertError("No password provided");
					}
					else    // Password filled in; prompt again for the password to make sure the user will remember it.
					{
						final EditText reprompt = new EditText(instance);
						reprompt.setHint("KEEP THIS SECRET");
						reprompt.setGravity(Gravity.CENTER_HORIZONTAL);
						reprompt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						AlertDialog.Builder dialog = new AlertDialog.Builder(instance)
								.setTitle("Re-enter Password")
								.setView(reprompt)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String repromptPass = reprompt.getText().toString();
										if ( repromptPass.equals("") )
										{
											alertError("No password re-entered");
										}
										else if ( !repromptPass.equals(encPass) )
										{
											alertError("Passwords do not match");
										}
										else    // Passwords match! Do the encryption.
										{
											encryptFile();
										}
									}
								})
								.setNegativeButton(android.R.string.cancel, null);
						dialog.show();
					}
				}
			}
			else    // Asymmetric encryption
			{
				if ( selectedKey == 0 ) // No key selected.
				{
					alertError("No public key selected");
				}
				else
				{
					encryptFile();
				}
			}
		}
		else if ( execMode == MODE_SIGN )   // Signing.
		{
			if ( selectedKey == 0 )
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
								signFile(keyPassPrompt.getText().toString());
							}
						})
						.setNegativeButton(android.R.string.cancel, null);
				dialog.show();
			}
		}
	}
}
