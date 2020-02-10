package project.comp3004.encryptiontoolsfrontend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navframe);
    }

    // TEST CODE - IT WORKS
    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent resultIntent)
    {
        Log.w("hyggelig", "onActivityResult - invoked");

        if ( resultIntent != null && resultIntent.getDataString() != null )
            Log.w("hyggelig", resultIntent.getDataString());
        else
            Log.w("hyggelig", "something is null");

        super.onActivityResult(requestcode, resultcode, resultIntent);
    }
}