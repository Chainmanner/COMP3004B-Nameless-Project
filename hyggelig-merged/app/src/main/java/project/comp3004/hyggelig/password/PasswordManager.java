package project.comp3004.hyggelig.password;
import project.comp3004.hyggelig.R;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
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
        passwordDb = new DatabaseHelper(this); //initalize password db
        storePwButton = findViewById(R.id.storePassword);
        viewPwButton = findViewById(R.id.viewPasswords);
        //event handler for storing password buttion
        storePwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create fragment
                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordManager.this);
                builder.setTitle("Store Password");
                final View storepass_layout = getLayoutInflater().inflate(R.layout.password_storepass_popup,null);
                builder.setView(storepass_layout);
                final AlertDialog dialog = builder.create();
                Button cancel_button = storepass_layout.findViewById(R.id.cancel_store);
                Button store_button = storepass_layout.findViewById(R.id.store_pw);
                //event handlers for fragment
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
                dialog.show(); //show the fragment

            }
        });
        //view password database event handler
        viewPwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.password_viewpasswords_layout);
                ListView listView = findViewById(R.id.passwordListView);
                List<String> list1 = new ArrayList<>();
                HashMap<String,String> passDictionary = new HashMap<>();
                Cursor data = passwordDb.getAllData();
                if(data.getCount() == 0) {
                    Toast.makeText(PasswordManager.this, "The Database is Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    //populate the ListView
                    while(data.moveToNext()) {
                        String pass = data.getString(0);
                        passDictionary.put(data.getString(0),data.getString(1));
                        list1.add(pass);
                        ListAdapter listAdapter = new ArrayAdapter<String>(PasswordManager.this,android.R.layout.simple_list_item_1,list1);
                        listView.setAdapter(listAdapter);

                    }
                }

                //show a password on item click
                displayListItem(listView,passDictionary);



            }
        });




    }

    protected void displayListItem(final ListView pwList, final HashMap<String,String> passDic) {

        pwList.setClickable(true);
        pwList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = pwList.getItemAtPosition(position);
                String clickedItem = (String)o;
                clickedItem = clickedItem.substring(0).trim();
                String password = passDic.get(clickedItem);
                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordManager.this);
                final View view_pass_item = getLayoutInflater().inflate(R.layout.view_pass_item,null);
                builder.setView(view_pass_item);
                final AlertDialog dialog = builder.create();
                Button cancel_view_button = view_pass_item.findViewById(R.id.cancel_view);
                TextView password_item = view_pass_item.findViewById(R.id.password_item);
                password_item.setText("Password: " + password);
                cancel_view_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


    }
}
