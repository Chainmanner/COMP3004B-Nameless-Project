package project.comp3004.hyggelig.bitcoin;
import project.comp3004.hyggelig.R;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Activity to handle dealings with cryptocurrency </p>
 * <p>
 * Promised features:
 *      -   Generate public/private addresses to send and receive Bitcoin
 *      -   View balance on the Bitcoin ledger
 * </p>
 * <p>
 *      Currently, this makes use of a Block.io wallet. Block.io allows for 10 addresses to be associated
 *      with a wallet for free.
 * </p>
 * <p>
 *     Currently, all queries are made against the Bitcoin Testnet. API keys are available for
 *     Bitcoin, Dogecoin, and Litecoin and their respective testnets.
 * </p>
 * <p>
 *     With Volley, each activity's method now includes a success callback, and an error callback.
 *     This is still not quite as modular as I was hoping, but it feels a bit less janky than with
 *     the AsyncTasks.
 * </p>
 */
public class Cryptocurrency_Activity extends AppCompatActivity{
    //https://block.io/docs/basic
    private static final String BITCOIN_TESTNET_API_KEY = "c7fc-7d3f-10f7-930f";
    private static final double SATOSHI_VALUE = 100000000.0;
    private static final String BLOCKIO_URL_BASE = "https://block.io/api/v2/";
    private static final String WALLET_BALANCE_QUERY = "get_balance/?api_key=";
    private static final String ADDRESS_BALANCE_QUERY = "get_address_balance/?api_key=";
    private static final String NEW_ADDRESS_QUERY = "get_new_address/?api_key=";
    private static final String VALIDATE_ADDRESS = "is_valid_address/?api_key=";

    private final String generateAddressTag = "GA";
    private final String checkBalanceTag = "CB";
    private final String validateAddressTag = "VA";
    private final String walletBalanceTag = "WB";

    private  EditText balance_addr_edittext;
    private  EditText label_edittext;
    private  TextView new_addr_textview;
    private  TextView balance_textview;
    private  TextView wallet_balance_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bitcoin_cryptocurrency_linearlayout);
        balance_addr_edittext = findViewById(R.id.blockchain_addr_input);
        label_edittext = findViewById(R.id.new_addr_label_input);
        new_addr_textview = findViewById(R.id.address_text);
        balance_textview = findViewById(R.id.addr_balance);
        wallet_balance_textview = findViewById(R.id.curr_wallet_balance_text);

        HttpQueue.getHttpQueue(this.getApplicationContext()).startQueue();

        getWalletBalance();
    }

    public void getWalletBalance(){

        String wallet_balance_url =   BLOCKIO_URL_BASE + WALLET_BALANCE_QUERY + BITCOIN_TESTNET_API_KEY;

        Response.Listener<JSONObject> callback = new Response.Listener<JSONObject>() {
            /**
             * {
             *   "status" : "success",
             *   "data" : {
             *     "network" : "LTCTEST",
             *     "available_balance" : "0.25",
             *     "pending_received_balance" : "0.2"
             *   }
             * }
             */
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    JSONObject data = response.getJSONObject("data");

                    if (status.equals("fail")) {
                        wallet_balance_textview.setText("Error: " +
                                data.getString("error_message"));
                    }else{
                        wallet_balance_textview.setText(
                                String.format("Current Wallet Balance: %s \u20BF (%s pending)",
                                        data.getString("available_balance"),
                                        data.getString("pending_received_balance"))
                        );
                    }
                }catch(JSONException json_ex){
                    json_ex.printStackTrace();
                    Log.e("getWallet res", json_ex.getMessage());
                }
            }

        };

        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    JSONObject errorObject = new JSONObject(new String(error.networkResponse.data));
                    JSONObject data = errorObject.getJSONObject("data");
                    wallet_balance_textview.setText("Error: " + data.getString("error_message"));
                }catch (JSONException json_ex){
                    wallet_balance_textview.setText("");
                }
            }

        };


        JsonObjectRequest walletBalanceRequest = new JsonObjectRequest(Request.Method.GET, wallet_balance_url,
                null, callback, onError);

        //Tag requests so that they can be canceled if needed
        walletBalanceRequest.setTag(walletBalanceTag);

        HttpQueue.getHttpQueue(this.getApplicationContext()).queueRequest(walletBalanceRequest);
    }

    public void validateAddress (JsonObjectRequest callerRequest, String address, final String callerTag) {
        ///api/v2/is_valid_address/?api_key=API_KEY&address=ADDRESS
        String validate_url =   BLOCKIO_URL_BASE + VALIDATE_ADDRESS + BITCOIN_TESTNET_API_KEY
                                + "&address=" + address;

        Response.Listener<JSONObject> callback = new Response.Listener<JSONObject>() {
            /**
             * Responses are in the form:
             * {
             *   "status" : "success",
             *   "data" : {
             *     "network" : "LTC",
             *     "address" : "xxxxxxxxxxxxxx",
             *     "is_valid" : false
             *   }
             * }
             * Where the address is the one provided by the user
             */
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    JSONObject data = response.getJSONObject("data");

                    if (status.equals("fail")) {
                        HttpQueue.getHttpQueue(null).dequeueRequests(callerTag);
                        balance_textview.setText("Error: " + data.getString("error_message"));
                    } else if(!data.getBoolean("is_valid")) {
                        HttpQueue.getHttpQueue(null).dequeueRequests(callerTag);
                        balance_textview.setText("The provided address is not valid for the current " +
                                "cryptocurrency network.");
                    }
                }catch(JSONException json_ex){
                    json_ex.printStackTrace();
                    Log.e("validateAddr res", json_ex.getMessage());
                }
            }

        };
        /**
         * Example error response
         * {
         *   "status" : "fail",
         *   "data" : {
         *     "error_message" : "Your account is restricted to 10 addresses per network.
         *     Please go to https://block.io/pricing to review account limits and upgrade to a more
         *     appropriate plan."
         *   }
         * }
         */
        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                HttpQueue.getHttpQueue(null).dequeueRequests(callerTag);
                try{
                    JSONObject errorObject = new JSONObject(new String(error.networkResponse.data));
                    JSONObject data = errorObject.getJSONObject("data");
                    balance_textview.setText("Error: " + data.getString("error_message"));
                }catch (JSONException json_ex){
                    balance_textview.setText("Error creating new address :(");
                }
            }

        };


        JsonObjectRequest addressValidateRequest = new JsonObjectRequest(Request.Method.GET, validate_url,
                null, callback, onError);
        //Tag requests so that they can be canceled if needed
        addressValidateRequest.setTag(validateAddressTag);

        HttpQueue.getHttpQueue(this).queueRequest(addressValidateRequest);
        HttpQueue.getHttpQueue(this).queueRequest(callerRequest);
    }

    public void generateNewAddress(View view){
        ///api/v2/get_new_address/?api_key=API_KEY&label=LABEL
        String url =   BLOCKIO_URL_BASE + NEW_ADDRESS_QUERY + BITCOIN_TESTNET_API_KEY;
        String label = label_edittext.getText().toString().trim();
        if(label.isEmpty()){
            Log.d("genNewAddress", "no label");
        }else {
            Log.d("genNewAddress", "lbl:\'"+label+"\'");
            url+="&label="+label;
        }
        Response.Listener<JSONObject> callback = new Response.Listener<JSONObject>() {
            /**
             * Responses are in the form:
             *      {
             *         "status" : "success",
             *             "data" : {
             *                 "network" : "BTCTEST",
             *                 "user_id" : 1,
             *                 "address" : "2N7zAeSRcsEuL1ozNkhbhedeAghe2PPATCq",
             *                 "label" : "chaxa46"
             *             }
             *      }
             * Where the label is either the one provided by the user, or randomly generated
             */
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    JSONObject data = response.getJSONObject("data");

                    if (status.equals("fail")) {
                        new_addr_textview.setText("Error: " + data.getString("error_message"));
                    } else {
                        String result_text = "Address : " + data.getString("address") + "\n"
                                + "Label: " + data.getString("label");
                        new_addr_textview.setText(result_text);
                    }
                }catch(JSONException json_ex){
                    json_ex.printStackTrace();
                    Log.e("genNewAddress res", json_ex.getMessage());
                }
            }

        };
        /**
         * Example error response
         * {
         *   "status" : "fail",
         *   "data" : {
         *     "error_message" : "Your account is restricted to 10 addresses per network.
         *     Please go to https://block.io/pricing to review account limits and upgrade to a more
         *     appropriate plan."
         *   }
         * }
         */
        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    JSONObject errorObject = new JSONObject(new String(error.networkResponse.data));
                    JSONObject data = errorObject.getJSONObject("data");
                    new_addr_textview.setText("Error: " + data.getString("error_message"));
                }catch (JSONException json_ex){
                    new_addr_textview.setText("Error creating new address :(");
                }finally{
                    return;
                }
            }

        };

        JsonObjectRequest addressGenRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                                        callback, onError);
        addressGenRequest.setTag(generateAddressTag);
        HttpQueue.getHttpQueue(this).queueRequest(addressGenRequest);
    }

    public void getAddressBalance(View view){
        ///api/v2/get_address_balance/?api_key=API KEY&addresses=ADDRESS1,ADDRESS2,...
        ///api/v2/get_address_balance/?api_key=API KEY&labels=LABEL1,LABEl2,...
        String url =   BLOCKIO_URL_BASE + ADDRESS_BALANCE_QUERY + BITCOIN_TESTNET_API_KEY
                        + "&addresses=";
        String address = balance_addr_edittext.getText().toString().trim();

        if(address.isEmpty()){
            Toast.makeText(getApplicationContext(),
                    "Please input a blockchain address", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        url+=address;

        Response.Listener<JSONObject> callback = new Response.Listener<JSONObject>() {
            /**
             * Responses are in the form:
             * {
             *  "status" : "success",
             *  "data" : {
                 *  "network" : "BTCTEST",
                 *  "available_balance" : "0.0",
                 *  "pending_received_balance" : "0.0",
                 *  "balances" : [
                 *      {
                 *      "user_id" : 0,
                 *      "label" : "default",
                 *      "address" : "2N59zrv7nw26txKRiCzdcDDL9JeqJy2pxkw",
                 *      "available_balance" : "0.00000000",
                 *      "pending_received_balance" : "0.00000000"
                 *      }
             *      ]
             *  }
             * }
             */
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    JSONObject data = response.getJSONObject("data");

                    if (status.equals("fail")) {
                        balance_textview.setText("Error: " + data.getString("error_message"));
                    } else {
                        balance_textview.setText(String.format("Available Balance: %.2f BTC",
                                data.getDouble("available_balance")));
                    }
                }catch(JSONException json_ex){
                    json_ex.printStackTrace();
                    Log.e("genNewAddress res", json_ex.getMessage());
                }
            }

        };
        /**
         * Example error response
         * {
         *   "status" : "fail",
         *   "data" : {
         *     "error_message" : "Invalid value for parameter ADDRESS provided."
         *   }
         * }
         */
        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    JSONObject errorObject = new JSONObject(new String(error.networkResponse.data));
                    JSONObject data = errorObject.getJSONObject("data");
                    balance_textview.setText("Error: " + data.getString("error_message"));
                }catch (JSONException json_ex){
                    balance_textview.setText("Error parsing VolleyError :(");
                }
            }

        };

        JsonObjectRequest checkBalanceRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                callback, onError);
        checkBalanceRequest.setTag(checkBalanceTag);
        validateAddress(checkBalanceRequest,address,checkBalanceTag);

    }


}
