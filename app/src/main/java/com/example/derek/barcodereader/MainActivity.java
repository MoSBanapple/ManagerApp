package com.example.derek.barcodereader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import org.json.*;
import org.w3c.dom.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private Button scanButton;
    private TableLayout itemTable;
    private int counter;
    Map<Integer, String> links;
    public static final String EXTRA_MESSAGE = "test";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        counter = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanButton = (Button)findViewById(R.id.scan_button);

        itemTable = (TableLayout)findViewById(R.id.item_table);


        links = new HashMap<Integer, String>();
        scanButton.setOnClickListener(this);

        new GetInfo().execute("", "", "");


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }

        if (links.get(v.getId()) != null){
            try {
                showDetails(links.get(v.getId()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            //new RequestCode().execute(scanContent, "", "");
            while (scanContent.length() < 14){
                scanContent = '0' + scanContent;
            }
            try {
                showDetails(scanContent);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }





    public void showDetails(String input) throws IOException {
        Intent intent = new Intent(this, Details.class);
        intent.putExtra(EXTRA_MESSAGE, input);

        startActivity(intent);
    }

    private TableRow makeRow(){
        TableRow newRow = new TableRow(this);
        itemTable.addView(newRow);
        return newRow;
    }
    private TextView addTextView(String s){
        TextView newText = new TextView(this);
        newText.setText(s);
        newText.setMaxWidth(650);
        newText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        newText.setOnClickListener(this);
        newText.setId(counter);
        counter++;
        return newText;
    }





    class GetInfo extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String url = "https://storeproject-209402.appspot.com/products";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return output;

        }

        protected void onPostExecute(String output) {
            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            String name = "";
            try {//modify
                JSONObject result = new JSONObject(output);
                JSONArray products = result.getJSONArray("products");
                for (int i = 0; i < products.length(); i++){
                    JSONObject target = products.getJSONObject(i);
                    String targetCode = target.getString("barcode");
                    new GetNameStockPrice().execute(targetCode, "", "");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class GetNameStockPrice extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String code = inputs[0];
            String url = "https://storeproject-209402.appspot.com/products/" + code;
            String result = "";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject jsonResult = new JSONObject(output);
                result += jsonResult.getString("name") + ";";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            url = "https://storeproject-209402.appspot.com/stocks/" + code;
            output = "";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject jsonResult = new JSONObject(output);
                result += jsonResult.getString("stock") + ";";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            url = "https://storeproject-209402.appspot.com/prices/" + code;
            output = "";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject jsonResult = new JSONObject(output);
                result += jsonResult.getString("price");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result + ";" + code;

        }

        protected void onPostExecute(String output) {

            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            String[] split = output.split(";");
            String name = split[0];
            String stock = split[1];
            String price = split[2];
            String barcode = split[3];
            TableRow newRow = makeRow();
            TextView nameView = addTextView(name);
            nameView.setClickable(true);
            links.put(nameView.getId(), barcode);
            newRow.addView(nameView);
            newRow.addView(addTextView(""));
            TextView newStock = addTextView(stock);
            newRow.addView(newStock);
            newRow.addView(addTextView(""));
            newRow.addView(addTextView(price));

        }
    }









/*
    class RequestCode extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String code = inputs[0];
            String key = "3k4y8z2qyslfnlpl2ul2ro6encrrrp";
            String url = "https://api.barcodelookup.com/v2/products?barcode=" + code + "&formatted=n&key=" + key;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return output;

        }

        protected void onPostExecute(String output) {
            if (output.length() == 0){
                contentTxt.setText(output);
                return;
            }
            String details = "Not found";
            String name = "";
            try {
                JSONObject result = new JSONObject(output);
                JSONArray products = result.getJSONArray("products");
                JSONObject target = products.getJSONObject(0);
                name = target.getString("product_name");
                if (target.getString("product_name").length() == 0){
                    name = target.getString("title");
                }
                details = name + "\n\n";
                String description = target.getString("description");
                details += description + "\n\n";
                JSONArray stores = target.getJSONArray("stores");
                details += "Stores:\n";
                for (int i = 0; i < stores.length(); i++){
                    JSONObject targetStore = stores.getJSONObject(i);
                    details += "   " + targetStore.getString("store_name") + " " +
                            targetStore.getString("currency_symbol") + targetStore.getString("store_price")
                            + ", " + targetStore.getString("product_url") + "\n";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            contentTxt.setText(details);
            try {
                showDetails(details);
                //showDetails("Equate daily moisturizing lotion, 18 oz\n other stuff");
            } catch (Exception e){
                contentTxt.setText(e.toString());
            }





        }
    }
*/



}
