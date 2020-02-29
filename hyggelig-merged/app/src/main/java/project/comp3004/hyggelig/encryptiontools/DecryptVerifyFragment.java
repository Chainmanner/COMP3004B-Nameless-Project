package project.comp3004.hyggelig.encryptiontools;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import project.comp3004.hyggelig.R;

public class DecryptVerifyFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
    private EncryptionTools_MainActivity instance;

    // Encrypt/sign modes
    private final boolean MODE_DECRYPT = false;
    private final boolean MODE_VERIFY = true;
    private boolean execMode;

    // Encryption type - true if symmetric, false if asymmetric (public key).
    private boolean symmetricDecrypt;

    // The public/private key selected in the respective Spinner, depending on if we're encrypting or signing.
    private int selectedKey = 0;

    private TableRow getfile_row;
    private TableRow enc_cipher_row;
    private TableRow sign_algo_row;
    private TableRow privkey_row;
    private TableRow pubkey_row;
    private TableRow password_row;  // TODO: Give the option to generate a password.
    private TableRow execute_row;

    private Button getfile;
    private Spinner enc_cipher;
    private Spinner sign_algo;
    private Spinner privkey;
    private Spinner pubkey;
    private EditText password;
    private Button execute;

    private Uri targetFileURI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View theView = inflater.inflate(R.layout.encryptiontools_decrypt_file_layout, container, false);

        Toolbar toolbar = theView.findViewById(R.id.toolbar);
        instance = (EncryptionTools_MainActivity) getActivity();
        if (instance != null) {
            instance.setSupportActionBar(toolbar);
            if (instance.getSupportActionBar() != null) {
                instance.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                instance.getSupportActionBar().setDisplayShowHomeEnabled(true);
                instance.getSupportActionBar().setTitle("Encrypt a File");
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {   // This actually makes the back button go back.
                    Navigation.findNavController(v).popBackStack();
                }
            });
        }

        RadioButton dec_radio = theView.findViewById(R.id.dec_radio);
        if ( dec_radio != null ) dec_radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDecryptionOptions(v);
            }
        });
        RadioButton verify_radio = theView.findViewById(R.id.verify_radio);
        if ( verify_radio != null ) verify_radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSigningOptions(v);
            }
        });

        getfile_row = theView.findViewById(R.id.getfile_row);
        enc_cipher_row = theView.findViewById(R.id.enc_cipher_row);
        sign_algo_row = theView.findViewById(R.id.sign_algo_row);
        pubkey_row = theView.findViewById(R.id.pubkey_row);
        privkey_row = theView.findViewById(R.id.privkey_row);
        password_row = theView.findViewById(R.id.password_row);
        execute_row = theView.findViewById(R.id.execute_row);

        enc_cipher = theView.findViewById(R.id.enc_cipher);
        pubkey = theView.findViewById(R.id.pubkey);
        sign_algo = theView.findViewById(R.id.sign_algo);
        privkey = theView.findViewById(R.id.privkey);

        getfile = theView.findViewById(R.id.getfile);
        execute = theView.findViewById(R.id.execute);

        password = theView.findViewById(R.id.password);

        if (getfile_row != null) getfile_row.setVisibility(View.GONE);
        if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
        if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
        if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
        if (privkey_row != null) privkey_row.setVisibility(View.GONE);
        if (password_row != null) password_row.setVisibility(View.GONE);
        if (execute_row != null) execute_row.setVisibility(View.GONE);

        Spinner filetype_menu = theView.findViewById(R.id.filetype);
        if (filetype_menu != null) filetype_menu.setOnItemSelectedListener(this);
        if (enc_cipher != null) enc_cipher.setOnItemSelectedListener(this);
        if (sign_algo != null) sign_algo.setOnItemSelectedListener(this);
        if (pubkey != null) pubkey.setOnItemSelectedListener(this);
        if (privkey != null) privkey.setOnItemSelectedListener(this);

        if (getfile != null)
            getfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickFile();
                }
            });

        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeAction();
            }
        });

        return theView;
    }

    // Shows options that are applicable to both encryption and signing.
    private void showMutualOptions(View v) {
        Log.w("hyggelig", "showMutualOptions");
        if (getfile_row != null) getfile_row.setVisibility(View.VISIBLE);
        if (execute_row != null) execute_row.setVisibility(View.VISIBLE);
    }

    private void showDecryptionOptions(View v) {
        Log.w("hyggelig", "showEncryptionOptions");

        //initAndHideAllOptions(v);
        showMutualOptions(v);

        if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
        if (privkey_row != null) privkey_row.setVisibility(View.GONE);

        if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.VISIBLE);
        //Spinner enc_cipher = v.findViewById(R.id.enc_cipher);
        if (enc_cipher != null)
            handleSpanners(v, enc_cipher.getSelectedItemPosition(), R.id.enc_cipher);
        //if ( password_row != null ) password_row.setVisibility(View.VISIBLE);

        if ( execute != null )
            execMode = MODE_DECRYPT;

        selectedKey = 0;
    }

    // Show the options applicable for signing data.
    private void showSigningOptions(View v) {
        Log.w("hyggelig", "showSigningOptions");
        //initAndHideAllOptions(v);
        showMutualOptions(v);

        if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
        if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
        if (password_row != null) password_row.setVisibility(View.GONE);

        if (sign_algo_row != null) sign_algo_row.setVisibility(View.VISIBLE);
        //Spinner sign_algo = v.findViewById(R.id.sign_algo);
        if (sign_algo != null)
            handleSpanners(v, sign_algo.getSelectedItemPosition(), R.id.sign_algo);

        if ( execute != null )
            execMode = MODE_VERIFY;

        selectedKey = 0;
    }

    // Handles the UI's Spinners.
    private void handleSpanners(View v, int position, int ID)
    {
        Log.w("hyggelig", "handleSpanners");
        Log.w("hyggelig", "pos " + position);
        Log.w("hyggelig", "id " + ID);

        switch(ID)
        {
            case R.id.enc_cipher:
                Log.w("hyggelig", "enc_cipher");

                // Symmetric - show password prompt
                if (position == 0) {
                    Log.w("hyggelig", "pos 0");

                    symmetricDecrypt = true;

                    if (privkey_row != null) privkey_row.setVisibility(View.GONE);

                    if (password_row != null) password_row.setVisibility(View.VISIBLE);

                    selectedKey = 0;
                }
                // Asymmetric - show password prompt
                else if (position == 1) {
                    Log.w("hyggelig", "pos 1");

                    symmetricDecrypt = false;

                    if (password_row != null) password_row.setVisibility(View.GONE);

                    if (privkey_row != null) privkey_row.setVisibility(View.VISIBLE);

                    selectedKey = 0;
                }
                break;
            case R.id.sign_algo:
                Log.w("hyggelig", "sign_algo");

                if (pubkey_row != null) pubkey_row.setVisibility(View.VISIBLE);

                // RSA
                if (position == 0) {
                    Log.w("hyggelig", "pos 0");

                    // TODO: Get RSA private keys.
                }
                // DSA
                else if (position == 1) {
                    Log.w("hyggelig", "pos 1");

                    // TODO: Get DSA private keys.
                }
                break;
            case R.id.privkey:
            case R.id.pubkey:
                Log.w("hyggelig", "pub/privkey = " + position);
                selectedKey = position;
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
    {
        Log.w("hyggelig", "onItemSelected");
        Log.w("hyggelig", "actual pos " + position);
        handleSpanners(v, position, parent.getId());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // Nothing to do here.
    }

    // Open a file manager to have the user select a file.
    private void pickFile() {
        Intent pickFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickFileIntent.setType("*/*");
        pickFileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, "true");
        // Need to make sure we actually have something that can handle this action.
        if (instance != null && pickFileIntent.resolveActivity(instance.getPackageManager()) != null)
            startActivityForResult(Intent.createChooser(pickFileIntent, "Select a File Manager"), 0);
    }

    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent resultIntent)
    {
        Log.w("hyggelig", "onActivityResult - invoked");

        if ( resultIntent != null && requestcode == 0 )
        {
            Log.w("hyggelig", "SELECT_FILE");
            Log.w("hyggelig", "" + resultIntent.getData());
            targetFileURI = resultIntent.getData();
            if ( getfile != null )
                getfile.setText("File selected!");
        }
    }

    private boolean decryptFile()
    {
        // TODO: Make this work.
        alertError("You followed the correct steps, but decryption is NYI.");
        return true;
    }

    private boolean verifyFile()
    {
        alertError("You followed the correct steps, but verification is NYI.");
        return true;
    }

    private void alertError(String msg)
    {
        new AlertDialog.Builder(instance)
                .setTitle("Error")
                .setMessage(msg)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }

    // TODO: When encryption is available, finish this up.
    private void executeAction()
    {
        Log.w("hyggelig", "executeAction");
        Log.w("hyggelig", "mode = " + execMode);

        if ( targetFileURI == null )
        {
            Log.w("hyggelig", "no file URI provided");
            alertError("No file selected");
            return;
        }

        if ( execMode == MODE_DECRYPT ) // Encryption, could be symmetric or asymmetric.
        {
            if ( symmetricDecrypt )
            {
                if ( password != null )
                {
                    final String encPass = password.getText().toString();
                    Log.w("hyggelig", "password = " + encPass); // FIXME: REMOVE WHEN DONE
                    if ( encPass.equals("") )   // Blank password.
                    {
                        alertError("No password provided");
                    }
                    else    // Password filled in; attempt to decrypt.
                    {
                        decryptFile();
                    }
                }
            }
            else    // Asymmetric encryption
            {
                if ( selectedKey == 0 ) // No key selected.
                {
                    alertError("No private key selected");
                }
                else
                {
                    alertError("You did fine, but asymmetric decryption is NYI.");
                    // TODO
                }
            }
        }
        else if ( execMode == MODE_VERIFY )   // Signing.
        {
            if ( selectedKey == 0 )
            {
                alertError("No public key selected");
            }
            else
            {
                alertError("You did fine, but the rest of this dialog is NYI until we can fetch public keys.");
                // TODO
            }
        }
    }
}
