package project.comp3004.hyggelig.encryptiontools;

import androidx.appcompat.app.AppCompatActivity;

//import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
//import android.util.Log;
import java.io.File;

import project.comp3004.hyggelig.R;


public class EncryptionTools_MainActivity extends AppCompatActivity {

	private String outputDirPath;
	private String pubkeysPath;
	private String privkeysPath;
	private String privfolderPath;  // TODO: This is currently of no use.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.encryptiontools_navframe);

		// Hackish method of creating the output directories on the main external storage directory.
		// It works on my phone, at least, but use with caution.
		// WARNING: Do not change ANY of the directory paths. They're hardcoded for the time being.
		File outputDir = getApplicationContext().getExternalFilesDir("nothing");
		if ( outputDir != null )
		{
			// First, create the path to the output directory.
			outputDirPath = outputDir.getAbsolutePath();
			outputDirPath = outputDirPath.substring(0, outputDirPath.indexOf("Android"));
			outputDirPath += "Hyggelig/EncryptionTools/";

			// Then, actually create the directory and subdirectories.
			File allOutputDir = new File( outputDirPath );
			allOutputDir.mkdirs();
			// Output directories for encryption/decryption and signing/verifying.
			File encOutputDir = new File(outputDirPath + "EncryptOutput/");
			encOutputDir.mkdirs();
			File decOutputDir = new File( outputDirPath + "DecryptOutput/");
			decOutputDir.mkdirs();
			File signOutputDir = new File( outputDirPath + "SignOutput/");
			signOutputDir.mkdirs();
			File verifyOutputDir = new File( outputDirPath + "VerifiedFiles/");
			verifyOutputDir.mkdirs();
			// Output directory for exported keys
			File exportedKeysDir = new File(outputDirPath + "ExportedKeys/");
			exportedKeysDir.mkdirs();
			// Output directory for stuff coming from the private folder.
			File privateFolderExportDir = new File(outputDirPath + "PrivateFolderExport/");
			privateFolderExportDir.mkdirs();
		}

		String privatePath = this.getFilesDir().getAbsolutePath();
		Log.w("hyggelig", privatePath);
		pubkeysPath = privatePath + "/pubkeys/";
		new File(pubkeysPath).mkdirs();
		privkeysPath = privatePath + "/privkeys/";
		new File(privkeysPath).mkdirs();

		privfolderPath = privatePath + "/privatefolder/";
		new File(privfolderPath).mkdirs();
	}

	protected String getOutputDirPath()
	{
		return outputDirPath;
	}
	protected String getPubkeysPath()
	{
		return pubkeysPath;
	}
	protected String getPrivkeysPath()
	{
		return privkeysPath;
	}
	protected String getPrivateFolderPath()
	{
		return privfolderPath;
	}
}