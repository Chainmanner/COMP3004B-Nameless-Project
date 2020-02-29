package project.comp3004.hyggelig.password;
import project.comp3004.hyggelig.R;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.database.Cursor;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;


public class PasswordManager extends AppCompatActivity {

    private DatabaseHelper passwordDb;
    private Button storePwButton;
    private Button viewPwButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_manager);
        passwordDb = new DatabaseHelper(this);
        storePwButton = findViewById(R.id.storePassword);
        viewPwButton = findViewById(R.id.viewPasswords);
        storePwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordManager.this);
                builder.setTitle("Store Password");
                final View storepass_layout = getLayoutInflater().inflate(R.layout.password_storepass_popup,null);
                builder.setView(storepass_layout);
                final AlertDialog dialog = builder.create();
                Button cancel_button = storepass_layout.findViewById(R.id.cancel_store);
                Button store_button = storepass_layout.findViewById(R.id.store_pw);
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                store_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText pw = storepass_layout.findViewById(R.id.pw);
                        EditText name = storepass_layout.findViewById(R.id.userName);
                        String passWord = pw.getText().toString();
                        String passWordName = name.getText().toString();
                        passwordDb.addData(passWordName,passWord);
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });

        viewPwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.password_viewpasswords_layout);
                ListView listView = findViewById(R.id.passwordListView);
                List<String> list1 = new ArrayList<>();
                Cursor data = passwordDb.getAllData();
                if(data.getCount() == 0) {
                    Toast.makeText(PasswordManager.this, "The Database is Empty", Toast.LENGTH_SHORT).show();
                }
                else {

                    while(data.moveToNext()) {
                        String pass = "Password Name: " + data.getString(0) + "\n" + "Password: " + data.getString(1) + "\n";
                        list1.add(pass);
                        ListAdapter listAdapter = new ArrayAdapter<String>(PasswordManager.this,android.R.layout.simple_list_item_1,list1);
                        listView.setAdapter(listAdapter);

                    }
                }


            }
        });


    }
}
