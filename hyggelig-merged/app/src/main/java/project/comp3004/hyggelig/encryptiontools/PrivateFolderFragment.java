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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivateFolderFragment extends Fragment
{
	private EncryptionTools_MainActivity instance;

	private RecyclerView fileList;

	private Button addFile;

	private String privateFolderPassword = "";
	private final String passwordFilename = "/privatefolder_password";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.w("hyggelig", "onCreateView");
		View theView = inflater.inflate(R.layout.encryptiontools_private_folder_layout, container, false);

		instance = (EncryptionTools_MainActivity)this.getActivity();

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

		addFile = theView.findViewById(R.id.addFile);
		addFile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addFilePrompt();
			}
		});

		fileList = theView.findViewById(R.id.fileList);
		fileList.setVerticalScrollBarEnabled(true);
		fileList.setLayoutManager(new LinearLayoutManager(instance.getApplicationContext()));
		fileList.setAdapter(new PrivateFilesAdapter(null, this));

		//showFiles(theView);

		return theView;
	}

	private void setPrivateFolderPassword(String password)
	{
		privateFolderPassword = password;
	}
	private String getPrivateFolderPassword()
	{
		return privateFolderPassword;
	}

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

	@Override
	public void onActivityResult(int reqcode, int rescode, Intent resultIntent)
	{
		Log.w("hyggelig", "onActivityResult " + reqcode + " " + rescode);


		if ( reqcode == 0 && resultIntent != null )
		{
			Uri targetURI = resultIntent.getData();
			if ( targetURI != null )
			{
				Log.w("hyggelig", "Adding file with URI " + targetURI);
				encryptAndAddFile(targetURI, getPrivateFolderPassword());
			}
		}
	}

	private boolean encryptAndAddFile(Uri targetFile, String password)
	{
		Log.w("hyggelig", "encryptAndAddFile " + targetFile.getPath());
		Log.w("hyggelig", "password = " + password);	// FIXME: REMOVE WHEN DONE

		Cursor theCursor = instance.getContentResolver().query(targetFile, null, null, null, null);
		if ( theCursor == null )
			return false;
		theCursor.moveToFirst();
		String name = theCursor.getString( theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) );
		int size = (int)theCursor.getLong( theCursor.getColumnIndex(OpenableColumns.SIZE) );
		theCursor.close();

		try
		{
			byte[] contents = new byte[size];

			InputStream fileIS = instance.getContentResolver().openInputStream(targetFile);
			fileIS.read(contents);
			fileIS.close();

			byte[] contentsEncrypted = aes.encrypt(contents, 256, getPrivateFolderPassword());

			File newFile = new File(instance.getPrivateFolderPath() + name);
			newFile.createNewFile();

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

		showFiles();

		return true;
	}

	private boolean decryptAndExportFile(String filename, String password)
	{
		Log.w("hyggelig", "decryptAndExportFile " + filename);
		Log.w("hyggelig", "password = " + password);	// FIXME: REMOVE WHEN DONE

		String outputDir = instance.getOutputDirPath() + "PrivateFolderExport/";
		try
		{
			File exportedFile = new File(outputDir + filename);
			exportedFile.createNewFile();

			File fileToExport = new File(instance.getPrivateFolderPath() + filename);
			byte[] contents = new byte[(int)fileToExport.length()];

			FileInputStream fileIS = new FileInputStream(fileToExport);
			fileIS.read(contents);
			fileIS.close();

			byte[] contentsPlaintext = aes.decrypt(contents, 256, getPrivateFolderPassword());

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

	private boolean deleteFile(String filename)
	{
		Log.w("hyggelig", "deleteFile " + filename);

		File fileToDelete = new File(instance.getPrivateFolderPath() + filename);
		fileToDelete.delete();

		showFiles();

		return true;
	}

	// Deletes all the files and then clears the RecyclerView's adapter at the end, whereas deleteFile() deletes a file and then regenerates the adapter.
	private void deleteAllFiles()
	{
		try
		{
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
				Log.w("hyggelig", "No files in " + privatefolderDir.getAbsolutePath());
				return;
			}

			for ( File curFile : privatefolderDir.listFiles() )
			{
				curFile.delete();
			}
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while wiping all files in the private folder: " + e.getMessage());
			e.printStackTrace();
		}

		fileList.swapAdapter(new PrivateFilesAdapter(null, this), true);
	}

	// Need this to go back to the Encryption Tools menu after clicking the cancel in the password prompt. Can't do it directly from said prompt.
	private void goBack()
	{
		if ( this.getView() != null )
			Navigation.findNavController(this.getView()).popBackStack();
	}

	private void alertError(String msg)
	{
		new AlertDialog.Builder(instance)
				.setTitle("Error")
				.setMessage(msg)
				.setNegativeButton(android.R.string.ok, null)
				.show();
	}

	@Override
	public void onStart()
	{
		Log.w("hyggelig", "onStart");

		if ( this.getPrivateFolderPassword().equals("") )	// No password in memory; prompt for it.
		{
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

				OKButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String thePassword = passPrompt.getText().toString();
						if ( thePassword.equals("") )
						{
							alertError("Password field is empty");
							return;
						}
						byte[] passFileContents = new byte[(int)passFile.length()];
						try
						{
							FileInputStream fileIS = new FileInputStream(passFile);
							fileIS.read(passFileContents);
							fileIS.close();

							MessageDigest md = MessageDigest.getInstance("SHA-256");
							byte[] passDigest = md.digest(thePassword.getBytes());
							// We could, of course, try to decrypt the file, see if we're successful, and check the password hash.
							// However, that's a little more complicated, so we're sticking with the possibly-suboptimal-yet-simpler solution.
							byte[] encryptedDigest = aes.encrypt(passDigest, 256, thePassword);

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
				CANCELButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						promptDialog.dismiss();
						goBack();
					}
				});
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

				Button OKButton = askDialog.findViewById(R.id.button_OK);
				OKButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String thePassword = passPrompt.getText().toString();
						if ( thePassword.equals("") )
						{
							alertError("Password field is empty");
						}
						else if ( thePassword.equals(passReprompt.getText().toString()) )
						{
							Log.w("hyggelig", "private folder password = " + thePassword);	// FIXME: REMOVE WHEN DONE
							try
							{
								passFile.createNewFile();

								MessageDigest md = MessageDigest.getInstance("SHA-256");
								byte[] passDigest = md.digest(thePassword.getBytes());
								byte[] encryptedDigest = aes.encrypt(passDigest, 256, thePassword);

								FileOutputStream fileOS = new FileOutputStream(passFile);
								fileOS.write(encryptedDigest);
								fileOS.close();

								Log.w("hyggelig", "Password file successfully initialized");
								new AlertDialog.Builder(instance)
										.setTitle("Password Set")
										.setMessage("Your password to your private folder has been set! Do not forget it, as you cannot retrieve your files if you do.")
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

	// Don't think we need this, but I'm keeping this here just in case I'm proven wrong.
	/*@Override
	public void onStop()
	{
		Log.w("hyggelig", "onStop");

		// TODO

		super.onStop();
	}*/

	@Override
	public void onDestroyView()
	{
		Log.w("hyggelig", "onDestroyView");

		// Clear the stored password. We're done here.
		this.setPrivateFolderPassword("");

		super.onDestroyView();
	}

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

		@Override
		public int getItemCount()
		{
			return (fileNames == null ? 0 : fileNames.length);
		}

		class FileViewHolder extends RecyclerView.ViewHolder
		{
			private String correspondingFilename;

			private TextView filenameView;
			private TextView filesizeView;

			public FileViewHolder(View v)
			{
				super(v);

				filenameView = v.findViewById(R.id.fileName);
				filesizeView = v.findViewById(R.id.fileSize);

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
										// It's not the most efficient, but it's simpler.
									}
								})
								.setNegativeButton(android.R.string.no, null)
								.show();
					}
				});
			}

			public void bindItem(String filename)
			{
				correspondingFilename = filename;
				filenameView.setText(filename);

				File theFile = new File(mainFragment.instance.getPrivateFolderPath() + filename);
				filesizeView.setText("Size: " + android.text.format.Formatter.formatFileSize(mainFragment.instance, theFile.length()));
			}
		}
	}
}
