package project.comp3004.encryptiontoolsfrontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.provider.MediaStore.EXTRA_OUTPUT;

// TODO: When this is done, clean it up for God's sake.
public class EncryptFilesFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private MainActivity instance;

    // Custom intent codes
    private final int SELECT_FILE = 0;
    private final int TAKE_PICTURE = 1;
    private final int RECORD_VIDEO = 2;

    // Encrypt/sign modes
    private final boolean MODE_ENCRYPT = false;
    private final boolean MODE_SIGN = true;
    private boolean execMode;

    // Encryption type - true if symmetric, false if asymmetric (public key).
    private boolean symmetricEncrypt;

    // The public/private key selected in the respective Spinner, depending on if we're encrypting or signing.
    private int selectedKey = 0;

    // TODO: Move ALL references to UI elements as class references, because there's no guarantee we'll always find them.
    private TableRow filetype_row;
    private TableRow getfile_row;
    private TableRow preview_row;
    private TableRow enc_cipher_row;
    private TableRow sign_algo_row;
    private TableRow pubkey_row;
    private TableRow privkey_row;
    private TableRow password_row;  // TODO: Give the option to generate a password.
    private TableRow deleteorig_row;
    private TableRow execute_row;

    private Spinner enc_cipher;
    private Spinner pubkey;
    private Spinner sign_algo;
    private Spinner privkey;

    private Button getfile;
    private Button execute;

    private ImageView preview;

    private EditText password;

    private CheckBox deleteOrig;

    // The file we'll be working on.
    private Uri targetFileURI;

    private File baseExternDir;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.encrypt_files_layout, container, false);

        // This adds a back button.
        Toolbar toolbar = theView.findViewById(R.id.toolbar);
        instance = (MainActivity) getActivity();
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
                    Navigation.findNavController(v).popBackStack();//.navigate(R.id.action_encryptFilesFragment2_to_encryptionToolsMainMenuFragment3);
                }
            });

            baseExternDir = instance.getApplicationContext().getExternalFilesDir("temp");
        }

        RadioButton enc_radio = theView.findViewById(R.id.enc_radio);
        if (enc_radio != null) enc_radio.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    showEncryptionOptions(v);
                                                                }
                                                            }
        );
        RadioButton sign_radio = theView.findViewById(R.id.sign_radio);
        if (enc_radio != null) sign_radio.setOnClickListener(new View.OnClickListener() {
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
        if (filetype_menu != null) filetype_menu.setOnItemSelectedListener(this);
        Spinner enc_cipher_menu = theView.findViewById(R.id.enc_cipher);
        if (enc_cipher_menu != null) enc_cipher_menu.setOnItemSelectedListener(this);
        Spinner sign_algo_menu = theView.findViewById(R.id.sign_algo);
        if (sign_algo_menu != null) sign_algo_menu.setOnItemSelectedListener(this);
        Spinner pubkey_menu = theView.findViewById(R.id.pubkey);
        if (pubkey_menu != null) pubkey_menu.setOnItemSelectedListener(this);
        Spinner privkey_menu = theView.findViewById(R.id.privkey);
        if (privkey_menu != null) privkey_menu.setOnItemSelectedListener(this);

        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeAction();
            }
        });

        return theView;
    }

    // Hides all options except for action_row.
    private void initAndHideAllOptions(View v) {
        Log.w("hyggelig", "initAndHideAllOptions");
        filetype_row = v.findViewById(R.id.filetype_row);
        getfile_row = v.findViewById(R.id.getfile_row);
        preview_row = v.findViewById(R.id.preview_row);
        enc_cipher_row = v.findViewById(R.id.enc_cipher_row);
        sign_algo_row = v.findViewById(R.id.sign_algo_row);
        pubkey_row = v.findViewById(R.id.pubkey_row);
        privkey_row = v.findViewById(R.id.privkey_row);
        password_row = v.findViewById(R.id.password_row);
        deleteorig_row = v.findViewById(R.id.deleteorig_row);
        execute_row = v.findViewById(R.id.execute_row);

        enc_cipher = v.findViewById(R.id.enc_cipher);
        pubkey = v.findViewById(R.id.pubkey);
        sign_algo = v.findViewById(R.id.sign_algo);
        privkey = v.findViewById(R.id.privkey);

        getfile = v.findViewById(R.id.getfile);
        execute = v.findViewById(R.id.execute);

        preview = v.findViewById(R.id.preview);

        password = v.findViewById(R.id.password);

        deleteOrig = v.findViewById(R.id.deleteorig);

        if (filetype_row != null) filetype_row.setVisibility(View.GONE);
        if (getfile_row != null) getfile_row.setVisibility(View.GONE);
        if (preview_row != null) preview_row.setVisibility(View.GONE);
        if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.GONE);
        if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
        if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);
        if (privkey_row != null) privkey_row.setVisibility(View.GONE);
        if (password_row != null) password_row.setVisibility(View.GONE);
        if (deleteorig_row != null) deleteorig_row.setVisibility(View.GONE);
        if (execute_row != null) execute_row.setVisibility(View.GONE);
    }

    // Shows options that are applicable to both encryption and signing.
    private void showMutualOptions(View v) {
        Log.w("hyggelig", "showMutualOptions");
        //TableRow filetype_row = v.findViewById(R.id.filetype_row);
        if (filetype_row != null) filetype_row.setVisibility(View.VISIBLE);
        //TableRow getfile_row = v.findViewById(R.id.getfile_row);
        if (getfile_row != null) getfile_row.setVisibility(View.VISIBLE);
        if (execute_row != null) execute_row.setVisibility(View.VISIBLE);
    }

    // Show the options applicable for encrypting data.
    private void showEncryptionOptions(View v) {
        Log.w("hyggelig", "showEncryptionOptions");

        //initAndHideAllOptions(v);
        showMutualOptions(v);

        if (sign_algo_row != null) sign_algo_row.setVisibility(View.GONE);
        if (privkey_row != null) privkey_row.setVisibility(View.GONE);

        if (enc_cipher_row != null) enc_cipher_row.setVisibility(View.VISIBLE);
        if (deleteorig_row != null) deleteorig_row.setVisibility(View.VISIBLE);
        //Spinner enc_cipher = v.findViewById(R.id.enc_cipher);
        if (enc_cipher != null)
            handleSpanners(v, enc_cipher.getSelectedItemPosition(), R.id.enc_cipher);
        //if ( password_row != null ) password_row.setVisibility(View.VISIBLE);

        if ( execute != null )
            execMode = MODE_ENCRYPT;

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
        if (deleteorig_row != null) deleteorig_row.setVisibility(View.GONE);

        if (sign_algo_row != null) sign_algo_row.setVisibility(View.VISIBLE);
        //Spinner sign_algo = v.findViewById(R.id.sign_algo);
        if (sign_algo != null)
            handleSpanners(v, sign_algo.getSelectedItemPosition(), R.id.sign_algo);

        // NOTE: Kind of inefficient to reset the listener every time, but okay.
        if ( execute != null )
            execMode = MODE_SIGN;

        selectedKey = 0;
    }

    // Makes the UI react to the options selected in spinners.
    // For example, if a symmetric cipher is shown, hide the public key spinner and show the password prompt.
    private void handleSpanners(View v, int position, int ID) {
        Log.w("hyggelig", "handleSpanners");
        Log.w("hyggelig", "pos " + position);
        Log.w("hyggelig", "id " + ID);
        switch (ID) {
            // File type
            case (R.id.filetype): {
                Log.w("hyggelig", "filetype");
                if ( getfile != null )
                    getfile.setText("Click here to select...");
                targetFileURI = null;   // Clear this out just to be safe.

                if (position == 0) {
                    if (getfile != null)
                        getfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pickFile();
                            }
                        });
                } else if (position == 1) {
                    if (getfile != null)
                        getfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                takePicture();
                            }
                        });
                } else if (position == 2) {
                    if (getfile != null)
                        getfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                recordVideo();
                            }
                        });
                }
            }
            break;
            // Encryption ciphers
            case (R.id.enc_cipher): {
                Log.w("hyggelig", "enc_cipher");

                // Symmetric - show password prompt
                if (position == 0) {
                    Log.w("hyggelig", "pos 0");

                    symmetricEncrypt = true;

                    if (pubkey_row != null) pubkey_row.setVisibility(View.GONE);

                    if (password_row != null) password_row.setVisibility(View.VISIBLE);

                    selectedKey = 0;
                }
                // Asymmetric - show password prompt
                else if (position == 1) {
                    Log.w("hyggelig", "pos 1");

                    symmetricEncrypt = false;

                    if (password_row != null) password_row.setVisibility(View.GONE);

                    if (pubkey_row != null) pubkey_row.setVisibility(View.VISIBLE);

                    selectedKey = 0;
                }
            }
            break;
            // Public key selection
            case (R.id.pubkey): {
                Log.w("hyggelig", "pubkey = " + position);
                selectedKey = position;
            }
            break;
            // Signing algorithm
            case (R.id.sign_algo): {
                Log.w("hyggelig", "sign_algo");

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
            }
            break;
            // Private key selection
            case (R.id.privkey): {
                Log.w("hyggelig", "privkey = " + position);
                selectedKey = position;
            }
            break;
        }
    }

    // For the spinners (drop-down menus).
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        Log.w("hyggelig", "onItemSelected");
        Log.w("hyggelig", "actual pos " + position);
        handleSpanners(v, position, parent.getId());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
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
            startActivityForResult(Intent.createChooser(pickFileIntent, "Select a File Manager"), SELECT_FILE);
    }

    // Opens a camera program to have the user take a picture.
    // Why the hell does using this sometimes terminate the app?
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // TODO: I'll have to do this later, because I've had it with this. Programming for Android is absurdly complicated.
        File tempFolder = new File(baseExternDir, "temp");
        File img = new File(tempFolder, "temp-pic.jpg");
        Uri imgURI = Uri.fromFile(img);
        takePictureIntent.putExtra(EXTRA_OUTPUT, imgURI); // TODO: Maybe give the user the option to avoid saving the file, and instead use a low-res thumbnail.
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (instance != null && takePictureIntent.resolveActivity(instance.getPackageManager()) != null)
            startActivityForResult(Intent.createChooser(takePictureIntent, "Select a Camera App"), TAKE_PICTURE);
    }

    // Opens a camera program to have the user record a video.
    private void recordVideo() {
        Intent recordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        recordVideoIntent.putExtra(EXTRA_OUTPUT, "temp-vid");
        if (instance != null && recordVideoIntent.resolveActivity(instance.getPackageManager()) != null)
            startActivityForResult(Intent.createChooser(recordVideoIntent, "Select a Camera App"), RECORD_VIDEO);
    }

    // Responds primarily to the file choosing Intents.
    // TODO: Make use of handleSpanners() because sometimes, the activity is reset, even though the result is returned.
    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent resultIntent) {
        Log.w("hyggelig", "onActivityResult - invoked");

        if (resultIntent != null)
            Log.w("hyggelig", "" + resultIntent.getData());
        else
            Log.w("hyggelig", "resultIntent is null");

        // TODO: Incomplete.
        if (resultIntent != null) {
            Bundle extras = resultIntent.getExtras();
            switch (requestcode) {
                case SELECT_FILE:
                    Log.w("hyggelig", "SELECT_FILE");
                    if (preview_row != null)
                        preview_row.setVisibility(View.GONE);
                    Log.w("hyggelig", "" + resultIntent.getData());
                    targetFileURI = resultIntent.getData();
                    if ( getfile != null )
                        getfile.setText("File selected!");
                    // TODO: If the file selected is an image, preview it.
                    break;
                case TAKE_PICTURE:  // TODO - Make this work somehow.
                    Log.w("hyggelig", "TAKE_PICTURE");
                    if (preview_row != null)
                        preview_row.setVisibility(View.VISIBLE);
                    if (extras != null) {
                        Bitmap thumbnail;// = MediaStore.Images.Media.getBitmap(instance.getContentResolver(), (Uri)extras.get(EXTRA_OUTPUT));//(Bitmap)extras.get("data");
                        try {
                            thumbnail = MediaStore.Images.Media.getBitmap(instance.getApplicationContext().getContentResolver(), (Uri) extras.get(EXTRA_OUTPUT));
                            if (thumbnail != null)
                                Log.w("hyggelig", "data = " + extras);
                            if (preview != null)
                                preview.setImageBitmap(thumbnail);
                        } catch (FileNotFoundException e) {
                            Log.w("hyggelig", "file not found");
                        } catch (IOException e) {
                            Log.w("hyggelig", "oh no");
                        }
                    } else {
                        Log.w("hyggelig", "TAKEN PICTURE RETURNED NULL");
                    }
                    break;
                case RECORD_VIDEO:  // TODO
                    Log.w("hyggelig", "RECORD_VIDEO");
                    break;
                default:
                    Log.w("hyggelig", "good lord! What did you do?");
                    break;
            }
        }

        super.onActivityResult(requestcode, resultcode, resultIntent);
    }

    private boolean encryptFile()
    {
        // TODO: Make this work.
        alertError("You followed the correct steps, but encryption is NYI.");
        return true;
    }

    private boolean signFile()
    {
        // TODO: Make this work.
        alertError("You followed the correct steps, but signing is NYI.");
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
    // TODO: Also, react to the checkbox asking if the user wants to delete the original.
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

        if ( execMode == MODE_ENCRYPT ) // Encryption, could be symmetric or asymmetric.
        {
            if ( symmetricEncrypt )
            {
                if ( password != null )
                {
                    final String encPass = password.getText().toString();
                    Log.w("hyggelig", "password = " + encPass); // FIXME: REMOVE WHEN DONE
                    if ( encPass.equals("") )   // Blank password.
                    {
                        alertError("No password provided");
                    }
                    else    // Password filled in; reprompt.
                    {
                        final EditText reprompt = new EditText(instance);
                        reprompt.setHint("KEEP THIS SECRET");
                        reprompt.setGravity(Gravity.CENTER_HORIZONTAL);
                        reprompt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        AlertDialog.Builder dialog = new AlertDialog.Builder(instance)
                                .setTitle("Re-enter Password")
                                .setView(reprompt)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String repromptPass = reprompt.getText().toString();
                                        Log.w("hyggelig", "reprompt = " + repromptPass);  // FIXME: REMOVE WHEN DONE
                                        if ( repromptPass.equals("") )
                                        {
                                            alertError("No password re-entered");
                                        }
                                        else if ( !repromptPass.equals(encPass) )
                                        {
                                            alertError("Passwords do not match");
                                        }
                                        else    // Passwords match. Do the encryption.
                                        {
                                            encryptFile();
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null);
                        dialog.show();
                    }
                }
            }
            else    // Asymmetric encryption
            {
                if ( selectedKey == 0 ) // No key selected.
                {
                    alertError("No public key selected");
                }
                else
                {
                    alertError("You did fine, but public key encryption is NYI.");
                    // TODO
                }
            }
        }
        else if ( execMode == MODE_SIGN )   // Signing.
        {
            if ( selectedKey == 0 )
            {
                alertError("No private key selected");
            }
            else
            {
                alertError("You did fine, but the rest of this dialog is NYI until we can fetch private keys.");
                // TODO
            }
        }
    }
}
