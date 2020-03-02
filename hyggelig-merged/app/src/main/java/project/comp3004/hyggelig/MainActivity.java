package project.comp3004.hyggelig;
import project.comp3004.hyggelig.bitcoin.*;
import project.comp3004.hyggelig.encryptiontools.EncryptionTools_MainActivity;
import project.comp3004.hyggelig.help.BrowseHelp_Activity;
import project.comp3004.hyggelig.password.Password_MainActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hyggelig_main);
    }

    public void beginCryptocurrency(View view){
        Intent intent = new Intent(getApplicationContext(), Cryptocurrency_Activity.class);
        startActivity(intent);
    }

    public void beginHelp(View view){
        Intent intent = new Intent(getApplicationContext(), BrowseHelp_Activity.class);
        startActivity(intent);
    }

    public void beginEncryption(View view){
        Intent intent = new Intent(getApplicationContext(), EncryptionTools_MainActivity.class);
        startActivity(intent);
    }

    public void beginPassword(View view){
        Intent intent = new Intent(getApplicationContext(), Password_MainActivity.class);
        startActivity(intent);
    }

}