package project.comp3004.encryptiontoolsfrontend;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class EncryptFilesFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // TODO: Move all references to UI elements as class references, because there's no guarantee we'll always find them.
    private TableRow filetype_row;
    private TableRow getfile_row;
    private TableRow preview_row;
    private TableRow enc_cipher_row;
    private TableRow sign_algo_row;
    private TableRow pubkey_row;
    private TableRow privkey_row;
    private TableRow password_row;
    private TableRow execute_row;

    private Spinner enc_cipher;
    private Spinner sign_algo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View theView = inflater.inflate(R.layout.encrypt_files_layout, container, false);

        // This adds a back button.
        Toolbar toolbar = theView.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        if ( ((MainActivity)getActivity()).getSupportActionBar() != null )
        {
            ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((MainActivity)getActivity()).getSupportActionBar().setTitle("Encrypt a File");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // This actually makes the back button go back.
                Navigation.findNavController(v).popBackStack();//.navigate(R.id.action_encryptFilesFragment2_to_encryptionToolsMainMenuFragment3);
            }
        });

        RadioButton enc_radio = theView.findViewById(R.id.enc_radio);
        if ( enc_radio != null ) enc_radio.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {
                                                                      showEncryptionOptions(v);
                                                                  }
                                                              }
        );
        RadioButton sign_radio = theView.findViewById(R.id.sign_radio);
        if ( enc_radio != null ) sign_radio.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {
                                                                      showSigningOptions(v);
                                                                  }
                                                              }
        );

        // TEST CODE - IT WORKS!
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.setType("*/*");
        //intent.putExtra(Intent.EXTRA_MIME_TYPES, "*/*");
        //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, "true");
        //intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //Intent intent2 = Intent.createChooser(intent, "Choose a file");
        //PackageManager pm = ((MainActivity)getActivity()).getPackageManager();
        //Log.w("hyggelig", "" + (pm.queryIntentActivities(intent2, 0).size()));
        //startActivityForResult(intent2, 0);

        initAndHideAllOptions(theView);

        Spinner filetype_menu = theView.findViewById(R.id.filetype);
        if ( filetype_menu != null ) filetype_menu.setOnItemSelectedListener(this);
        Spinner enc_cipher_menu = theView.findViewById(R.id.enc_cipher);
        if ( enc_cipher_menu != null ) enc_cipher_menu.setOnItemSelectedListener(this);
        Spinner sign_algo_menu = theView.findViewById(R.id.sign_algo);
        if ( sign_algo_menu != null ) sign_algo_menu.setOnItemSelectedListener(this);
        Spinner pubkey_menu = theView.findViewById(R.id.pubkey);
        if ( pubkey_menu != null ) pubkey_menu.setOnItemSelectedListener(this);
        Spinner privkey_menu = theView.findViewById(R.id.privkey);
        if ( privkey_menu != null ) privkey_menu.setOnItemSelectedListener(this);

        return theView;
    }

    // Hides all options except for action_row.
    private void initAndHideAllOptions(View v)
    {
        Log.w("hyggelig", "initAndHideAllOptions");
        filetype_row = v.findViewById(R.id.filetype_row);
        getfile_row = v.findViewById(R.id.getfile_row);
        preview_row = v.findViewById(R.id.preview_row);
        enc_cipher_row = v.findViewById(R.id.enc_cipher_row);
        sign_algo_row = v.findViewById(R.id.sign_algo_row);
        pubkey_row = v.findViewById(R.id.pubkey_row);
        privkey_row = v.findViewById(R.id.privkey_row);
        password_row = v.findViewById(R.id.password_row);
        execute_row = v.findViewById(R.id.execute_row);

        enc_cipher = v.findViewById(R.id.enc_cipher);
        sign_algo = v.findViewById(R.id.sign_algo);

        if ( filetype_row != null ) filetype_row.setVisibility(View.GONE);
        if ( getfile_row != null ) getfile_row.setVisibility(View.GONE);
        if ( preview_row != null ) preview_row.setVisibility(View.GONE);
        if ( enc_cipher_row != null ) enc_cipher_row.setVisibility(View.GONE);
        if ( sign_algo_row != null ) sign_algo_row.setVisibility(View.GONE);
        if ( pubkey_row != null ) pubkey_row.setVisibility(View.GONE);
        if ( privkey_row != null ) privkey_row.setVisibility(View.GONE);
        if ( password_row != null ) password_row.setVisibility(View.GONE);
        if ( execute_row != null ) execute_row.setVisibility(View.GONE);
    }

    // Shows options that are applicable to both encryption and signing.
    private void showMutualOptions(View v)
    {
        Log.w("hyggelig", "showMutualOptions");
        //TableRow filetype_row = v.findViewById(R.id.filetype_row);
        if ( filetype_row != null ) filetype_row.setVisibility(View.VISIBLE);
        //TableRow getfile_row = v.findViewById(R.id.getfile_row);
        if ( getfile_row != null ) getfile_row.setVisibility(View.VISIBLE);
        if ( execute_row != null ) execute_row.setVisibility(View.VISIBLE);
    }

    // Show the options applicable for encrypting data.
    public void showEncryptionOptions(View v)
    {
        Log.w("hyggelig", "showEncryptionOptions");

        //initAndHideAllOptions(v);
        showMutualOptions(v);

        if ( sign_algo_row != null ) sign_algo_row.setVisibility(View.GONE);
        if ( privkey_row != null ) privkey_row.setVisibility(View.GONE);

        if ( enc_cipher_row != null ) enc_cipher_row.setVisibility(View.VISIBLE);
        //Spinner enc_cipher = v.findViewById(R.id.enc_cipher);
        if ( enc_cipher != null )
            handleSpanners(v, enc_cipher.getSelectedItemPosition(), R.id.enc_cipher);
        //if ( password_row != null ) password_row.setVisibility(View.VISIBLE);
    }

    // Show the options applicable for signing data.
    public void showSigningOptions(View v)
    {
        Log.w("hyggelig", "showSigningOptions");
        //initAndHideAllOptions(v);
        showMutualOptions(v);

        if ( enc_cipher_row != null ) enc_cipher_row.setVisibility(View.GONE);
        if ( pubkey_row != null ) pubkey_row.setVisibility(View.GONE);
        if ( password_row != null ) password_row.setVisibility(View.GONE);

        if ( sign_algo_row != null ) sign_algo_row.setVisibility(View.VISIBLE);
        //Spinner sign_algo = v.findViewById(R.id.sign_algo);
        if ( sign_algo != null )
            handleSpanners(v, sign_algo.getSelectedItemPosition(), R.id.sign_algo);
    }

    // Updates the spanners.
    private void handleSpanners(View v, int position, int ID)
    {
        Log.w("hyggelig", "handleSpanners");
        Log.w("hyggelig", "pos " + position);
        Log.w("hyggelig", "id " + ID);
        switch (ID) {
            // File type
            case (R.id.filetype): {
                // TODO
            } break;
            // Encryption ciphers
            case (R.id.enc_cipher): {
                Log.w("hyggelig", "enc_cipher");
                //curMenu = v.findViewById(R.id.enc_cipher);
                //curOption = curMenu.getSelectedItem().toString();
                //Log.w("hyggelig", curOption);

                // Symmetric - show password prompt
                if (position == 0) {
                    Log.w("hyggelig", "pos 0");

                    //TableRow sign_algo_row = v.findViewById(R.id.sign_algo_row);
                    //if ( sign_algo_row != null ) sign_algo_row.setVisibility(View.GONE);
                    //TableRow pubkey_row = v.findViewById(R.id.pubkey_row);
                    if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
                    //TableRow privkey_row = v.findViewById(R.id.privkey_row);
                    //if ( privkey_row != null ) privkey_row.setVisibility(View.GONE);

                    if (password_row != null) password_row.setVisibility(View.VISIBLE);
                }
                // Asymmetric - show password prompt
                else if (position == 1) {
                    Log.w("hyggelig", "pos 1");

                    if (password_row != null) password_row.setVisibility(View.GONE);

                    //if ( sign_algo_row != null ) sign_algo_row.setVisibility(View.VISIBLE);
                    if (pubkey_row != null) pubkey_row.setVisibility(View.VISIBLE);
                    //if ( privkey_row != null ) privkey_row.setVisibility(View.VISIBLE);
                }
            } break;
            // Public key selection
            case (R.id.pubkey): {
                Log.w("hyggelig", "pubkey");
                // TODO - Now that I think of it, probably nothing's gonna be done with this.
            } break;
            // Signing algorithm
            case (R.id.sign_algo): {
                Log.w("hyggelig", "sign_algo");
                //curMenu = v.findViewById(R.id.sign_algo);
                //curOption = curMenu.getSelectedItem().toString();
                //Log.w("hyggelig", curOption);

                if (privkey_row != null) privkey_row.setVisibility(View.VISIBLE);

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
            } break;
            // Private key selection
            case (R.id.privkey): {
                Log.w("hyggelig", "privkey");
                // TODO - Now that I think of it, probably nothing's gonna be done with this.
            } break;
        }
    }

    // For the spinners (drop-down menus).
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
    {
        Log.w("hyggelig", "onItemSelected");
        Log.w("hyggelig", "actual pos " + position);
        //Spinner curMenu;
        //String curOption;
        handleSpanners(v, position, parent.getId());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // Nothing to do here.
    }
}