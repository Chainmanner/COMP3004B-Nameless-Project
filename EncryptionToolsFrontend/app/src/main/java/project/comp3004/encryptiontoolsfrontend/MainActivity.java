package project.comp3004.encryptiontoolsfrontend;

import androidx.appcompat.app.AppCompatActivity;

//import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
//import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navframe);

        // Unfortunately, we may need to store temporary files.
        //File tempFolder = new File(this.getApplicationContext().getFilesDir(), "temp");
        //tempFolder.mkdirs();
        //Log.w("hyggelig", this.getApplicationContext().getFilesDir().getAbsolutePath());
    }
}