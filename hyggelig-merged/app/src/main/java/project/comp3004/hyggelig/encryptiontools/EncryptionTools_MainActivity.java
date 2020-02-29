package project.comp3004.hyggelig.encryptiontools;

import androidx.appcompat.app.AppCompatActivity;

//import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import project.comp3004.hyggelig.R;


public class EncryptionTools_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encryptiontools_navframe);

        // Unfortunately, we may need to store temporary files.
        //File tempFolder = new File(this.getApplicationContext().getFilesDir(), "temp");
        //tempFolder.mkdirs();
        //Log.w("hyggelig", this.getApplicationContext().getFilesDir().getAbsolutePath());
    }
}