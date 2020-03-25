package project.comp3004.hyggelig.password;
import project.comp3004.hyggelig.R;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;



import java.util.Random;


public class PasswordGenerator extends AppCompatActivity {

    private Button generatePwButton;
    private String generatedPw;
    private RadioButton alphabetButton;
    private RadioButton alphabetButtonNum;
    private RadioButton alphabetButtonNumSym;
    private RadioButton alphabetButtonNumSymF;
    private EditText pwLengthField;
    private DatabaseHelper passwordDb;
    private Button storePWButton;
    private Button savePwButton;
    private Button cancelSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_generator);
        passwordDb = new DatabaseHelper(this);
        final TextView textView = findViewById(R.id.generatedPass);
        generatePwButton = findViewById(R.id.genPass);
        alphabetButton = findViewById(R.id.alphabet);
        storePWButton = findViewById(R.id.storePass);
        alphabetButtonNum = findViewById(R.id.alphabetNum);
        alphabetButtonNumSym = findViewById(R.id.alphabetNumSym);
        alphabetButtonNumSymF = findViewById(R.id.alphabetNumSymF);
        pwLengthField = findViewById(R.id.pwLength);
        generatePwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwLength = (pwLengthField.getText().toString());
                char arr[] = new char[Integer.parseInt(pwLength)];
                //generate with only numbers
                if (alphabetButton.isChecked()) {
                    passwordGen x = new passwordGen();
                    String[] argv = {"1", pwLength, "1", ""};
                    x.newPassword(argv);
                    generatedPw = x.getNewPassword(0);

                }
                //generate with number
                else if (alphabetButtonNum.isChecked()) {
                    for (int i = 0; i < Integer.parseInt(pwLength); i++) {
                        Random r = new Random();
                        int choice = r.nextInt(3);
                        if (choice == 0) {
                            arr[i] = (char) (r.nextInt(26) + 'a');
                        } else if (choice == 1) {
                            arr[i] = (char) (r.nextInt(26) + 'A');
                        } else {
                            arr[i] = (char) (r.nextInt(10) + '0');
                        }

                    }
                    generatedPw = new String(arr);


                }
                //generate with symbols
                else if (alphabetButtonNumSym.isChecked()) {
                    passwordGen x = new passwordGen();
                    String[] argv = {"2", pwLength, "2", ""};
                    x.newPassword(argv);
                    generatedPw = x.getNewPassword(0);

                }
                //generate with foreign keys
                else if (alphabetButtonNumSymF.isChecked()) {
                    passwordGen x = new passwordGen();
                    String[] argv = {"3", pwLength, "3", ""};
                    x.newPassword(argv);
                    generatedPw = x.getNewPassword(0);

                }
                //display generated password
                textView.setText("Generated Password: " + generatedPw);

                //storing generated password event handler
                storePWButton.setVisibility(View.VISIBLE);
                storePWButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setContentView(R.layout.password_popup_input_dialog);
                        cancelSaveButton = findViewById(R.id.button_cancel_user_data);
                        savePwButton = findViewById(R.id.button_save_user_data);
                        savePwButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditText pwName = findViewById(R.id.userName);
                                String passwordName = pwName.getText().toString();
                                System.out.println(passwordDb.addData(passwordName,generatedPw));
                                finish();
                            }
                        });
                        cancelSaveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }


                });


            }
        });

    }
}
