package project.comp3004.hyggelig.encryptiontools;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.aes.aes;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

// Fragment that secures and displays the contents of the Private Folder.
// The Private Folder is internally stored, inaccessible by other programs (unless they have root access), and its contents are encrypted.
// Authored by Gabriel Valachi (101068875).
public class PrivateFolderFragment extends Fragment
{
	// Main instance of the Encryption Tools activity.
	private EncryptionTools_MainActivity instance;

	// Displayed list of files in the private folder.
	private RecyclerView fileList;

	private Button addFile;

	// Password of the private folder.
	private String privateFolderPassword = "";
	
	// Location of the file containing the private folder password.
	private final String passwordFilename = "/privatefolder_password";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.w("hyggelig", "onCreateView");
		View theView = inflater.inflate(R.layout.encryptiontools_private_folder_layout, container, false);

		instance = (EncryptionTools_MainActivity)this.getActivity();

		// Sets the title and back button in the toolbar.
		Toolbar theToolbar = theView.findViewById(R.id.toolbar);
		instance.setSupportActionBar(theToolbar);
		instance.getSupportActionBar().setDisplayShowHomeEnabled(true);
		instance.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance.getSupportActionBar().setTitle("Private Folder");
		theToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {   // This actually makes the back button go back.
				Navigation.findNavController(v).popBackStack();
			}
		});

		// Sets the listener for the button to add a file.
		addFile = theView.findViewById(R.id.addFile);
		addFile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addFilePrompt();
			}
		});

		// Initializes the displayed list of files to a blank adapter.
		fileList = theView.findViewById(R.id.fileList);
		fileList.setVerticalScrollBarEnabled(true);
		fileList.setLayoutManager(new LinearLayoutManager(instance.getApplicationContext()));
		fileList.setAdapter(new PrivateFilesAdapter(null, this));

		// Shows the files in the directory.
		// REMOVED: Let's not do that before the user entered their password.
		//showFiles(theView);

		return theView;
	}

	// Setter and getter for the in-memory private folder password.
	private void setPrivateFolderPassword(String password)
	{
		privateFolderPassword = password;
	}
	private String getPrivateFolderPassword()
	{
		return privateFolderPassword;
	}

	// Shows the files present in the private folder.
	// Called when the user has entered the password to the folder, or when a file is added or removed.
	private void showFiles()
	{
		Log.w("hyggelig", "showFiles");
		String privatefolderPath = instance.getPrivateFolderPath();
		File privatefolderDir = new File(privatefolderPath);
		if ( !privatefolderDir.isDirectory() )
		{
			Log.w("hyggelig", privatefolderDir.getAbsolutePath() + " somehow is not a directory");
			return;
		}
		if ( privatefolderDir.list() == null )
		{
			Log.w("hyggelig" , "privatefolderDir.list() is null");
			return;
		}
		if ( privatefolderDir.list().length == 0 )
		{
			Log.w("hyggelig", "No files in " + privatefolderDir.getAbsolutePath());
		}

		Log.w("hyggelig", Arrays.toString(privatefolderDir.list()));
		PrivateFilesAdapter theAdapter = new PrivateFilesAdapter(privatefolderDir.list(), this);
		fileList.swapAdapter(theAdapter, true);
		fileList.getAdapter().notifyDataSetChanged();
	}

	// Pulls up an activity for the user to add a file to the private folder.
	private void addFilePrompt()
	{
		Log.w("hyggelig", "addFilePrompt");

		Intent addFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
		addFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
		addFileIntent.setType("*/*");
		addFileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, "true");
		if ( instance != null && addFileIntent.resolveActivity(instance.getPackageManager()) != null )
			startActivityForResult(Intent.createChooser(addFileIntent, "Select a File Manager"), 0);
	}

	// Callback for the activity invoked in addFilePrompt().
	// Adds in the selected file.
	@Override
	public void onActivityResult(int reqcode, int rescode, Intent resultIntent)
	{
		Log.w("hyggelig", "onActivityResult " + reqcode + " " + rescode);
		
		if ( reqcode == 0 && resultIntent != null )
		{
			// If we selected a file with a valid URI, just add it in. No need to discriminate here.
			Uri targetURI = resultIntent.getData();
			if ( targetURI != null )
			{
				Log.w("hyggelig", "Adding file with URI " + targetURI);
				encryptAndAddFile(targetURI, getPrivateFolderPassword());
			}
		}
	}

	// Encrypts the input file with the given password using AES-256 and adds it to the private folder.
	// Args:
	//	targetFile - The URI of the file to import.
	//	password - The password to encrypt the file with.
	// Return true if successful, false on failure.
	private boolean encryptAndAddFile(Uri targetFile, String password)
	{
		Log.w("hyggelig", "encryptAndAddFile " + targetFile.getPath());

		// Gets the file name and size.
		Cursor theCursor = instance.getContentResolver().query(targetFile, null, null, null, null);
		if ( theCursor == null )
			return false;
		theCursor.moveToFirst();
		String name = theCursor.getString( theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) );
		int size = (int)theCursor.getLong( theCursor.getColumnIndex(OpenableColumns.SIZE) );
		theCursor.close();

		// Gets the contents of the target file, encrypts it with the password, and writes these contents to a new file in the private folder.
		try
		{
			byte[] contents = new byte[size];

			// Reads the input file.
			InputStream fileIS = instance.getContentResolver().openInputStream(targetFile);
			fileIS.read(contents);
			fileIS.close();

			// Encrypts the file.
			byte[] contentsEncrypted = aes.encrypt(contents, 256, getPrivateFolderPassword());

			// Creates the new file in the private folder.
			File newFile = new File(instance.getPrivateFolderPath() + name);
			newFile.createNewFile();

			// Writes the encrypted contents to the new file.
			FileOutputStream fileOS = new FileOutputStream(newFile);
			fileOS.write(contentsEncrypted);
			fileOS.close();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while trying to add file: " + e.getMessage());
			e.printStackTrace();
			alertError("Error while trying to add file: " + e.getMessage());
			return false;
		}

		// Now that we added a new file, refresh the list.
		showFiles();

		return true;
	}

	// Decrypts a file from the private folder and exports it to Hyggelig/EncryptionTools/PrivateFolderExport/.
	// Args:
	//	filename - The name of the file to export.
	//	password - The password to decrypt the file with.
	// Returns true on success, false on failure.
	private boolean decryptAndExportFile(String filename, String password)
	{
		Log.w("hyggelig", "decryptAndExportFile " + filename);

		// Reads the ciphertext of the private file, decrypts it with the password, and exports it.
		String outputDir = instance.getOutputDirPath() + "PrivateFolderExport/";
		try
		{
			// Creates the file that will contain the plaintext contents of the exported private file.
			File exportedFile = new File(outputDir + filename);
			exportedFile.createNewFile();
			
			File fileToExport = new File(instance.getPrivateFolderPath() + filename);
			byte[] contents = new byte[(int)fileToExport.length()];

			// Reads the contents of the file to be exported.
			FileInputStream fileIS = new FileInputStream(fileToExport);
			fileIS.read(contents);
			fileIS.close();

			// Decrypts the exported file's contents with the given password.
			byte[] contentsPlaintext = aes.decrypt(contents, 256, getPrivateFolderPassword());

			// Finally, writes the plaintext contents to the output file.
			FileOutputStream fileOS = new FileOutputStream(exportedFile);
			fileOS.write(contentsPlaintext);
			fileOS.close();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while trying to export file: " + e.getMessage());
			e.printStackTrace();
			alertError("Error while trying to export file: " + e.getMessage());
			return false;
		}

		new AlertDialog.Builder(instance)
				.setTitle("Success")
				.setMessage("File " + filename + " successfully exported to " + outputDir + filename + "!")
				.setNegativeButton(android.R.string.ok, null)
				.show();

		return true;
	}

	// Deletes a file from the private folder and updates the displayed list of files.
	// Arg:
	//	filename - The name of the file to delete.
	private void deleteFile(String filename)
	{
		Log.w("hyggelig", "deleteFile " + filename);

		File fileToDelete = new File(instance.getPrivateFolderPath() + filename);
		fileToDelete.delete();

		showFiles();
	}

	// Deletes all of the files in the private folder and then clears the RecyclerView's adapter.
	// This will also delete the password file, allowing the user to set a new password for the private folder.
	private void deleteAllFiles()
	{
		// Delete the password file so that the user can reuse the private folder.
		File passFile = new File(instance.getFilesDir().getAbsolutePath() + passwordFilename);
		passFile.delete();

		File privatefolderDir = new File(instance.getPrivateFolderPath());
		if ( !privatefolderDir.isDirectory() )
		{
			Log.w("hyggelig", privatefolderDir.getAbsolutePath() + " somehow is not a directory");
			return;
		}
		if ( privatefolderDir.listFiles() == null )
		{
			Log.w("hyggelig" , "privatefolderDir.listFiles() is null");
			return;
		}
		if ( privatefolderDir.listFiles().length == 0 )
		{
			Log.w("hyggelig", "No files to delete in " + privatefolderDir.getAbsolutePath());
			return;
		}

		// Delete ALL the files in the directory.
		for ( File curFile : privatefolderDir.listFiles() )
		{
			curFile.delete();
		}

		fileList.swapAdapter(new PrivateFilesAdapter(null, this), true);
	}

	// Return to the Encryption Tools menu. Used after the user presses cancel or wipes the private folder.
	// Can't do this directly from the password prompt.
	private void goBack()
	{
		if ( this.getView() != null )
			Navigation.findNavController(this.getView()).popBackStack();
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

	// Called when the private folder is brought into view.
	// If the password is not stored in memory, prompts for the user to either create a new password or enter their existing one.
	@Override
	public void onStart()
	{
		Log.w("hyggelig", "onStart");

		if ( this.getPrivateFolderPassword().equals("") )	// No password in memory; prompt for it.
		{
			// Check if we have a password file. If we do, prompt the user for the password.
			// If not, have the user create a new one before they can use the folder.
			final File passFile = new File(instance.getFilesDir().getAbsolutePath() + passwordFilename);
			if (passFile.exists()) {
				Log.w("hyggelig", passFile.getAbsolutePath() + " exists, prompting user for password...");

				final Dialog promptDialog = new Dialog(instance);
				promptDialog.setTitle("Enter Password");
				promptDialog.setContentView(R.layout.generic_askpass);
				promptDialog.setCanceledOnTouchOutside(false);

				final EditText passPrompt = promptDialog.findViewById(R.id.pass_prompt);
				Button OKButton = promptDialog.findViewById(R.id.button_OK);
				Button CANCELButton = promptDialog.findViewById(R.id.button_CANCEL);
				Button RESETButton = promptDialog.findViewById(R.id.button_RESET);
				
				// Sets the listener for the OK button on the prompt.
				OKButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String thePassword = passPrompt.getText().toString();
						if ( thePassword.equals("") )	// Check if the password is blank.
						{
							alertError("Password field is empty");
							return;
						}
						byte[] passFileContents = new byte[(int)passFile.length()];
						try
						{
							// Read the contents of the password file.
							FileInputStream fileIS = new FileInputStream(passFile);
							fileIS.read(passFileContents);
							fileIS.close();

							// Generate a SHA-256 hash of the user-entered password, then encrypt it with itself as the password.
							MessageDigest md = MessageDigest.getInstance("SHA-256");
							byte[] passDigest = md.digest(thePassword.getBytes());
							byte[] encryptedDigest = aes.encrypt(passDigest, 256, thePassword);

							// Compare the contents of the password file with the self-encrypted hash we just made.
							// If the two are equal, grant access.
							if ( Arrays.equals(passFileContents, encryptedDigest) )
							{
								setPrivateFolderPassword(thePassword);
								showFiles();
							}
							else
							{
								alertError("Incorrect password");
								return;
							}
						}
						catch ( Exception e )
						{
							Log.w("hyggelig", "Error while comparing passwords: " + e.getMessage() );
							e.printStackTrace();
							return;
						}
						promptDialog.dismiss();
					}
				});
				// Sets the handler for the cancel button. This dismisses the dialog and takes us back to the Encryption Tools main menu.
				CANCELButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						promptDialog.dismiss();
						goBack();
					}
				});
				// Sets the handler for the reset button.
				// First warns the user if they want to proceed, then if so, deletes the private folder contents and the password file.
				RESETButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(instance)
								.setTitle("Warning")
								.setMessage("This will clear the private folder's password, but will delete ALL the files stored in it. Are you sure you want to do this?")
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										promptDialog.dismiss();
										deleteAllFiles();
										goBack();
									}
								})
								.setNegativeButton(android.R.string.no, null)
								.show();
					}
				});

				promptDialog.show();
			}
			else
			{
				Log.w("hyggelig", passFile.getAbsolutePath() + " does not exist, creating it...");

				final Dialog askDialog = new Dialog(instance);
				askDialog.setTitle("Enter Password");
				askDialog.setContentView(R.layout.generic_newpass);
				askDialog.setCanceledOnTouchOutside(false);

				final EditText passPrompt = askDialog.findViewById(R.id.pass_prompt);
				final EditText passReprompt = askDialog.findViewById(R.id.reprompt);

				// Sets the handler for the OK button in the prompt to create a new password.
				Button OKButton = askDialog.findViewById(R.id.button_OK);
				OKButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String thePassword = passPrompt.getText().toString();
						if ( thePassword.equals("") )	// Check to make sure the password button isn't empty.
						{
							alertError("Password field is empty");
						}
						else if ( thePassword.equals(passReprompt.getText().toString()) )
						{
							// If the password reprompt matches the password first entered, then create the password file with this password.
							try
							{
								passFile.createNewFile();

								// Generate a SHA-256 hash of the password the user entered, then encrypt it with itself as the password.
								MessageDigest md = MessageDigest.getInstance("SHA-256");
								byte[] passDigest = md.digest(thePassword.getBytes());
								byte[] encryptedDigest = aes.encrypt(passDigest, 256, thePassword);

								// Write the encrypted password hash to a file.
								// From now on, we'll check for equivalence to this file before letting the user access the folder.
								FileOutputStream fileOS = new FileOutputStream(passFile);
								fileOS.write(encryptedDigest);
								fileOS.close();

								Log.w("hyggelig", "Password file successfully initialized");
								new AlertDialog.Builder(instance)
										.setTitle("Password Set")
										.setMessage("The password to your private folder has been set! Do not forget it, as you cannot retrieve your files if you do.")
										.setNegativeButton(android.R.string.ok, null)
										.show();
								setPrivateFolderPassword(thePassword);
							}
							catch ( Exception e )
							{
								Log.w("hyggelig", "Couldn't create password file: " + e.getMessage() );
								e.printStackTrace();
								return;
							}
							askDialog.dismiss();
						}
						else
						{
							alertError("Passwords do not match");
						}
					}
				});

				// Sets the handler for the cancel button.
				// When clicked, takes the user back to the Encryption Tools main menu.
				Button CANCELButton = askDialog.findViewById(R.id.button_CANCEL);
				CANCELButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						askDialog.dismiss();
						goBack();
					}
				});

				askDialog.show();
			}
		}

		super.onStart();
	}

	// Called when the fragment is destroyed.
	// When this happens, we clear the password stored in memory.
	@Override
	public void onDestroyView()
	{
		Log.w("hyggelig", "onDestroyView");

		// Clear the stored password. We're done here.
		this.setPrivateFolderPassword("");

		super.onDestroyView();
	}

	// Adapter that handles showing the files in the private folder.
	class PrivateFilesAdapter extends RecyclerView.Adapter<PrivateFilesAdapter.FileViewHolder>
	{
		private PrivateFolderFragment mainFragment;
		private String[] fileNames;

		public PrivateFilesAdapter(String[] theFiles, PrivateFolderFragment theFragment)
		{
			fileNames = theFiles;
			mainFragment = theFragment;
		}

		@Override
		public PrivateFilesAdapter.FileViewHolder onCreateViewHolder(ViewGroup parent, int type)
		{
			View theView = LayoutInflater.from(parent.getContext()).inflate(R.layout.encryptiontools_private_folder_file, parent, false);

			return new PrivateFilesAdapter.FileViewHolder(theView);
		}

		// Called in order to initialize the list item's holder at the specified position.
		// This just sets the item's background color, then hands the task over to the holder itself by passing the filename of the
		// file it represents.
		@Override
		public void onBindViewHolder(PrivateFilesAdapter.FileViewHolder holder, int position)
		{
			ConstraintLayout fileLayout = holder.itemView.findViewById(R.id.constraint_layout_file);
			if ( position % 2 == 0 )
			{
				fileLayout.setBackgroundColor(Color.LTGRAY);
			}
			else
			{
				fileLayout.setBackgroundColor(Color.WHITE);
			}
			holder.bindItem( fileNames[position] );
		}

		// Gets the number of items in this adapter.
		// NOTE: Returns zero if fileNames is null.
		@Override
		public int getItemCount()
		{
			return (fileNames == null ? 0 : fileNames.length);
		}

		// Holder for an item in the list of private files.
		class FileViewHolder extends RecyclerView.ViewHolder
		{
			private String correspondingFilename;

			private TextView filenameView;
			private TextView filesizeView;
			
			private FileViewHolder(View v)
			{
				super(v);

				filenameView = v.findViewById(R.id.fileName);
				filesizeView = v.findViewById(R.id.fileSize);

				// Sets the handler of the export button.
				// When clicked, the user is prompted to confirm that they want to export the represented file.
				// If so, then it exports the specified file.
				ImageView exportButton = v.findViewById(R.id.exportButton);
				exportButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(mainFragment.instance)
								.setTitle("Warning")
								.setMessage("Are you sure you want to export " + filenameView.getText().toString() + "? Exporting will not automatically remove the file from the private folder.")
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										mainFragment.decryptAndExportFile(correspondingFilename, mainFragment.getPrivateFolderPassword());
									}
								})
								.setNegativeButton(android.R.string.no, null)
								.show();
					}
				});
				// Sets the handler of the delete button.
				// Prompts the user if they're sure they want to delete the represented file, and if they confirm, deletes it.
				ImageView deleteButton = v.findViewById(R.id.deleteButton);
				deleteButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(mainFragment.instance)
								.setTitle("Warning")
								.setMessage("Are you sure you want to delete " + filenameView.getText().toString() + "?")
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										mainFragment.deleteFile(correspondingFilename);
										// No call to notifyItemRemoved() because deleteFile() will create a new adapter and swap the existing one with the new one.
										// Not the most efficient, but it's simpler that way. Besides, a user adds items in one at a time.
									}
								})
								.setNegativeButton(android.R.string.no, null)
								.show();
					}
				});
			}

			// Displays the name and size of the file in the layout.
			// Called from PrivateFilesAdapter.onBindViewHolder().
			private void bindItem(String filename)
			{
				correspondingFilename = filename;
				filenameView.setText(filename);

				File theFile = new File(mainFragment.instance.getPrivateFolderPath() + filename);
				filesizeView.setText("Size: " + android.text.format.Formatter.formatFileSize(mainFragment.instance, theFile.length()));
			}
		}
	}
}
