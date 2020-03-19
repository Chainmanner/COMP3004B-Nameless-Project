package project.comp3004.hyggelig.encryptiontools;
import project.comp3004.hyggelig.R;
import project.comp3004.hyggelig.aes.aes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivateFolderFragment extends Fragment
{
	private EncryptionTools_MainActivity instance;

	private RecyclerView fileList;

	private Button addFile;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
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

		fileList = theView.findViewById(R.id.fileList);

		addFile = theView.findViewById(R.id.addFile);

		showFiles(theView);

		return theView;
	}

	private void showFiles(View v)
	{
		Log.w("hyggelig", "showFiles");
		String privatefolderPath = instance.getPrivateFolderPath();
		File privatefolderDir = new File(privatefolderPath);
		if ( !privatefolderDir.isDirectory() )
		{
			Log.w("hyggelig", privatefolderDir.getAbsolutePath() + " somehow is not a directory");
			return;
		}
		if ( privatefolderDir.list().length == 0 )
		{
			Log.w("hyggelig", "No files in " + privatefolderDir.getAbsolutePath());
			return;
		}

		List<File> privateFiles = new ArrayList<File>(Arrays.asList( privatefolderDir.listFiles() ));
		// TODO: Need to create the Adapter before I can go any further.
	}

	private void addFilePrompt()
	{
		// TODO
	}

	@Override
	public void onActivityResult(int reqcode, int rescode, Intent resultIntent)
	{
		// TODO
	}

	private boolean encryptAndAddFile(Uri targetFile, String password)
	{
		// TODO
		return true;
	}

	private boolean decryptAndExportFile(String filename, String password)
	{
		// TODO
		return true;
	}

	private boolean deleteFile(String filename)
	{
		// TODO
		return true;
	}

	class PrivateFilesAdapter extends RecyclerView.Adapter<PrivateFilesAdapter.FileViewHolder>
	{
		private PrivateFolderFragment mainFragment;

		// TODO

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
						mainFragment.decryptAndExportFile(correspondingFilename, mainFragment.instance.getPrivateFolderPassword());
					}
				});
				ImageView deleteButton = v.findViewById(R.id.deleteButton);
				deleteButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mainFragment.deleteFile(correspondingFilename);
						// No call to notifyItemRemoved() because deleteFile() will create a new adapter and swap the existing one with the new one.
						// It's not the most efficient, but it's simpler.
					}
				});
			}

			public void bindItem(String filename)
			{
				correspondingFilename = filename;
				filenameView.setText(filename);

				File theFile = new File(mainFragment.instance.getPrivateFolderPath() + filename);
				filesizeView.setText("" + theFile.length());
			}
		}
	}
}
