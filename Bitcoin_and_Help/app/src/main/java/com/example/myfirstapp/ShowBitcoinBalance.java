package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class ShowBitcoinBalance extends AppCompatActivity{
    private static final double SATOSHI_VALUE=100000000.0;
    private static final String BLOCKCHAIN_URL = "https://blockchain.info/";
    private static final String BLOCKCHAIN_BALANCE_QUERY="balance?active=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_bitcoin_balance);

    }

    public void validateAddress (View view) {
        TextView addr_input = findViewById(R.id.blockchain_addr_input);
        String blockchain_addr = addr_input.getText().toString();
        //TODO Validation of input and address validation
        AsyncHttpTask asyncHttp = new AsyncHttpTask();
        asyncHttp.execute(blockchain_addr);
    }

    /**
     * @params  String representing the address to get the balance of
     * Make an HTTP request to blockchain.info to get the balance of a wallet
     * <p>
     *     Makes use of the HttpURLConnection class to make an HTTP request to blockchain.info.
     *     User provides the address against which the balance is checked.
     *     As part of blockchain.com's API, requests are to be limited to be maximum 1 every 10 seconds.
     *     Requests to blockchain.info return a JSON object with the following form:
     *     {
     *          "address": {
     *              "final_balance": 0,      [in Satoshi]
     *              "n_tx": 0,               [number of transactions]
     *              "total_received": 0      [in Satoshi]
     *          }
     *      }
     *      Note that multiple addresses separated by '|'s can be input into the HTTP request
     * </p>
     * @return String containing a JSON object with the balance info from blockchain.info - return null otherwise
     *
     */
    private String getBalanceJSON(String target_address){
        try {

            URL balance_query_URL = new URL(BLOCKCHAIN_URL + BLOCKCHAIN_BALANCE_QUERY
                                            + target_address);
            Log.d("URL", balance_query_URL.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) balance_query_URL.openConnection();
            BufferedReader conexion_reader = new BufferedReader(
                                                new InputStreamReader(urlConnection.getInputStream())
                                            );
            StringBuilder string_builder = new StringBuilder();

            String next_line;

            while ((next_line = conexion_reader.readLine()) != null ){
                string_builder.append(next_line+'\n');
            }
            Log.d("HttpResponse",string_builder.toString());
            return string_builder.toString();

        } catch (MalformedURLException malformed_ex) {
            malformed_ex.printStackTrace();
            Log.e("getBalanceJSON", "Malformed URL");
            // Toast.makeText(getApplicationContext(),
            //        "Malformed URL", Toast.LENGTH_SHORT)
            //       .show();
            return null;
        } catch (IOException io_ex) {
            io_ex.printStackTrace();
            Log.e("getBalanceJSON", "URL connexion exception");
            /*Toast.makeText(getApplicationContext(),
                    "IO error when opening URL connexion.", Toast.LENGTH_SHORT)
                    .show();*/
            return null;
        }
    }

    private class AsyncHttpTask extends AsyncTask<String, Void, String>    {

        @Override
        protected String doInBackground(String... blockchain_addr){
            //  doInBackground must be called with an array
            //  Good if we eventually allow for retrieval of multiple balances
            return  getBalanceJSON(blockchain_addr[0]);
        }

        protected void onPostExecute(String result) {
            try {



                TextView addr_balance = findViewById(R.id.addr_balance);
                if(result==null){
                    addr_balance.setText("Error");
                }else{
                    JSONObject jsonBalanceObject = new JSONObject(result);
                    ArrayList<JSONObject> balances = new ArrayList<>();
                    for(Iterator<String> iter = jsonBalanceObject.keys();iter.hasNext();){
                        balances.add(jsonBalanceObject.getJSONObject(iter.next()));
                    }

                    double balance = balances.get(0).getDouble("final_balance")/SATOSHI_VALUE;

                    addr_balance.setText(String.format("Address balance: %f BTC", balance));
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

}
