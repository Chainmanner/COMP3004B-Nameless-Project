package project.comp3004.hyggelig;
import project.comp3004.hyggelig.bitcoin.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hyggelig_main);

        Intent testIntent = new Intent(this.getApplicationContext(), Bitcoin_MainActivity.class);
        startActivity(testIntent);
    }
}