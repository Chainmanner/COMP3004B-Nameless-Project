package project.comp3004.hyggelig.bitcoin;
import project.comp3004.hyggelig.R;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
 *     Currently, all queries are made the the Bitcoin Testnet. API keys are available for
 *     Bitcoin, Dogecoin, and Litecoin.
 * </p>
 * <p>
 *     Improvements: Each user activity has a separate AsyncTask. Instead, having one AsyncTask
 *     that gets the JSON object and returns so that an appropriate handler can deal with the JSON
 *     would be better. Could make use of AsyncHttpClient? Or Volley?
 * </p>
 */
public class CryptocurrencyActivity extends AppCompatActivity{
    //https://block.io/docs/basic
    private static final String BITCOIN_TESTNET_API_KEY = "4b8a-d875-87c9-6be9";
    private static final double SATOSHI_VALUE=100000000.0;
    private static final String BLOCKIO_URL_BASE = "https://block.io/api/v2/";
    private static final String WALLET_BALANCE_QUERY="get_balance/?api_key=";
    private static final String ADDRESS_BALANCE_QUERY="get_address_balance/?api_key=";
    private static final String NEW_ADDRESS_QUERY="get_new_address/?api_key=";
    private static final String VALIDATE_ADDRESS = "is_valid_address/?api_key=";

    //private JSONObject jsonResponse;
    //private AsyncHttpClient async_client = new

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bitcoin_cryptocurrency);

    }

    public void validateAddress (String address) {
        ///api/v2/is_valid_address/?api_key=API_KEY&address=ADDRESS


        String validate_url =   BLOCKIO_URL_BASE + VALIDATE_ADDRESS + BITCOIN_TESTNET_API_KEY
                                + "&address=" + address;

    }

    public void generateNewAddress(View view){
        ///api/v2/get_new_address/?api_key=API_KEY&label=LABEL
        EditText label_textview = findViewById(R.id.new_addr_label_input);
        String url =   BLOCKIO_URL_BASE + NEW_ADDRESS_QUERY + BITCOIN_TESTNET_API_KEY;
        String label = label_textview.getText().toString().trim();
        if(label!=""){
            url+="&label="+label;
        }
        new makeAddressTask().execute(url);
    }

    public void getAddressBalance(View view){
        ///api/v2/get_address_balance/?api_key=API KEY&addresses=ADDRESS1,ADDRESS2,...
        ///api/v2/get_address_balance/?api_key=API KEY&labels=LABEL1,LABEl2,...
        //TODO Validate address
        EditText label_textview = findViewById(R.id.new_addr_label_input);
        String url =   BLOCKIO_URL_BASE + ADDRESS_BALANCE_QUERY + BITCOIN_TESTNET_API_KEY
                        + "&addresses=" + label_textview.getText().toString().trim();

        new getAddressBalanceTask().execute(url);
    }

    /**
     * @params  String representing the URL to send the HTTP request to
     * Make an HTTP request to block.io
     *
     * @return String containing a JSON object
     *
     */
    private String makeHttpRequest(String target_URL){
        try {
            URL balance_query_URL = new URL(target_URL);
            Log.d("URL", balance_query_URL.toString());
            HttpsURLConnection url_connection = (HttpsURLConnection) balance_query_URL.openConnection();
            BufferedReader connection_reader = new BufferedReader(
                                                new InputStreamReader(url_connection.getInputStream())
                                            );
            StringBuilder string_builder = new StringBuilder();

            String next_line;

            while ((next_line = connection_reader.readLine()) != null ){
                string_builder.append(next_line+'\n');
            }
            Log.d("HttpResponse",string_builder.toString());
            connection_reader.close();
            return string_builder.toString();

        } catch (MalformedURLException malformed_ex) {
            malformed_ex.printStackTrace();
            Log.e("makeHttpRequest", "Malformed URL");
            // Toast.makeText(getApplicationContext(),
            //        "Malformed URL", Toast.LENGTH_SHORT)
            //       .show();
            return null;
        } catch (IOException io_ex) {
            io_ex.printStackTrace();
            Log.e("makeHttpRequest", "URL connexion exception");
            /*Toast.makeText(getApplicationContext(),
                    "IO error when opening URL connexion.", Toast.LENGTH_SHORT)
                    .show();*/
            return null;
        }
    }

    /**
     * Task for validating addresses.
     * Responses are in the form:
     *  {
     *   "status" : "success",
     *   "data" : {
     *     "network" : "BTCTEST",
     *     "address" : "asdas",
     *     "is_valid" : false
     *   }
     * }
     */
    private class validAddressTask extends AsyncTask<String, Void, String>    {

        @Override
        protected String doInBackground(String... target_url){
            //  doInBackground must be called with an array
            //  Good if we eventually allow for retrieval of multiple balances
            return  makeHttpRequest(target_url[0]);
        }

        protected void onPostExecute(String result) {
            try {
                if(result!=null){
                     JSONObject response = new JSONObject(result);

                    /*ArrayList<JSONObject> balances = new ArrayList<>();
                    for(Iterator<String> iter = jsonBalanceObject.keys();iter.hasNext();){
                        balances.add(jsonBalanceObject.getJSONObject(iter.next()));
                    }

                    double balance = balances.get(0).getDouble("final_balance")/SATOSHI_VALUE;

                    addr_balance.setText(String.format("Address balance: %f BTC", balance));*/
                }


            } catch (JSONException json_ex) {
                json_ex.printStackTrace();
                Log.e("AsyncHttpTask", "JSON exception");
                /*Toast.makeText(getApplicationContext(),
                        "Error when working with JSON.", Toast.LENGTH_SHORT)
                        .show(); */
            }
        }
    }

    /**
     * Task for making new addresses.
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
    private class makeAddressTask extends AsyncTask<String, Void, String>    {

        @Override
        protected String doInBackground(String... target_url){
            //  doInBackground must be called with an array
            //  Good if we eventually allow for retrieval of multiple balances
            return  makeHttpRequest(target_url[0]);
        }

        protected void onPostExecute(String result) {
            try {
                if(result!=null){
                    TextView result_textview = findViewById(R.id.result_text);
                    JSONObject response = new JSONObject(result);

                    String status = response.getString("status");
                    JSONObject data = response.getJSONObject("data");

                     if(status=="fail"){
                         result_textview.setText("Error: " + data.getString("error_message"));
                     }else{
                         String result_text =   "Address : " + data.getString("address") + "\n"
                                                + "Label: " +data.getString("label");
                         result_textview.setText(result_text);
                     }
                    /*ArrayList<JSONObject> balances = new ArrayList<>();
                    for(Iterator<String> iter = jsonBalanceObject.keys();iter.hasNext();){
                        balances.add(jsonBalanceObject.getJSONObject(iter.next()));
                    }*/
                }


            } catch (JSONException json_ex) {
                json_ex.printStackTrace();
                Log.e("makeAddressTask", json_ex.getMessage());
                /*Toast.makeText(getApplicationContext(),
                        "Error when working with JSON.", Toast.LENGTH_SHORT)
                        .show(); */
            }
        }
    }

    /**
     * Task for getting the balance of an address.
     * Responses are in the form:
     *{
     *   "status" : "success",
     *   "data" : {
     *     "network" : "BTCTEST",
     *     "available_balance" : "0.0",
     *     "pending_received_balance" : "0.0",
     *     "balances" : [
     *       {
     *         "user_id" : 0,
     *         "label" : "default",
     *         "address" : "2N59zrv7nw26txKRiCzdcDDL9JeqJy2pxkw",
     *         "available_balance" : "0.00000000",
     *         "pending_received_balance" : "0.00000000"
     *       }
     *     ]
     *   }
     * }
     *
     */
    private class getAddressBalanceTask extends AsyncTask<String, Void, String>    {
        @Override
        protected String doInBackground(String... target_url){
            //  doInBackground must be called with an array
            //  Good if we eventually allow for retrieval of multiple balances
            return  makeHttpRequest(target_url[0]);
        }

        protected void onPostExecute(String result) {
            try {
                if(result!=null){
                    TextView result_textview = findViewById(R.id.addr_balance);
                    JSONObject response = new JSONObject(result);

                    String status = response.getString("status");
                    JSONObject data = response.getJSONObject("data");
                    //JSONArray balances = data.getJSONArray("balances");
                    if(status=="fail"){
                        result_textview.setText("Error: " + data.getString("error_message"));
                    }else{
                        result_textview.setText(String.format("Available Balance: %.2f BTC",
                                                data.getDouble("available_balance")/SATOSHI_VALUE));
                    }
                }
            } catch (JSONException json_ex) {
                json_ex.printStackTrace();
                Log.e("makeAddressTask", json_ex.getMessage());
                /*Toast.makeText(getApplicationContext(),
                        "Error when working with JSON.", Toast.LENGTH_SHORT)
                        .show(); */
            }
        }
    }
}
