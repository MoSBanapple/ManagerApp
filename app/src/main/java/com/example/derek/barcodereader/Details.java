package com.example.derek.barcodereader;

/**
 * Created by Derek on 6/27/2018.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Details extends AppCompatActivity implements View.OnClickListener {
    private TextView productDisplay, detailsDisplay;
    private ImageView productImage;
    private EditText priceInput, stockInput;
    private Button changeButton, removeButton;
    private String code, name, imageURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Details");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Details");


        Intent intent = getIntent();
        code = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);


        // Capture the layout's TextView and set the string as its text
        productDisplay = (TextView) findViewById(R.id.product_display);
        detailsDisplay = (TextView) findViewById(R.id.description_display);
        productImage = (ImageView) findViewById(R.id.product_image);
        changeButton = (Button)findViewById(R.id.change_button);
        removeButton = (Button)findViewById(R.id.remove_button);
        priceInput = (EditText)findViewById(R.id.price_input);
        stockInput = (EditText)findViewById(R.id.stock_input);
        changeButton.setOnClickListener(this);
        removeButton.setOnClickListener(this);


        new GetDetails().execute(code, "", "");

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.change_button){
            new UpdateProduct().execute(code, "", "");
        }
        if (view.getId() == R.id.remove_button){
            new RemoveProduct().execute(code, "", "");
        }
    }

    public void toMainScreen(String input) throws IOException {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("test", input);

        startActivity(intent);
    }


    class GetDetails extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String url = "https://storeproject-209402.appspot.com/products/" + barcode;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

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
                new RequestDetails().execute(code, "", "");
                return;
            }

            try {//modify
                JSONObject result = new JSONObject(output);
                name = result.getString("name");
                productDisplay.setText(name);
                detailsDisplay.setText(result.getString("description"));
                imageURL = result.getString("image");
                new GetPrice().execute(code, "", "");
                new GetStock().execute(code, "", "");
                new DownloadImageTask(productImage).execute(imageURL);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    class GetPrice extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String url = "https://storeproject-209402.appspot.com/prices/" + barcode;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

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
                        "Invalid price", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            try {//modify
                JSONObject result = new JSONObject(output);
                priceInput.setText(result.getString("price"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    class GetStock extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String url = "https://storeproject-209402.appspot.com/stocks/" + barcode;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

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
                        "Invalid stock", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            try {//modify
                JSONObject result = new JSONObject(output);
                stockInput.setText(result.getString("stock"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }







    class RequestDetails extends AsyncTask<String, String, String> {
        boolean err = false;
        protected String doInBackground(String... inputs) {
            String output = "";
            String item = inputs[0].replace(' ', '+');
            //String key = "AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y";
            //String engine = "005773736382830971489:6njleywqa3i";
            //String url = "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" + engine + "&q=" + item;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";
            String key = "YDGBRILWFMOSTNIVWFERZIHADYNRMXOLPYXMXYCIRDNPZREGPENHLUSQFWCEGVWY";
            String params = "token=" + key + "&country=us&source=google-shopping&currentness=daily_updated&completeness=one_page&key=gtin&values=" + item;
            String url = "https://api.priceapi.com/jobs";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
//                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
//                osw.write(params);
                byte[] postDataBytes = params.getBytes("UTF-8");
                con.getOutputStream().write(postDataBytes);
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
                boolean finished = false;
                String jobID = new JSONObject(output).getString("job_id");
                while (!finished){
                    url = "https://api.priceapi.com/jobs/" + jobID + "?token=" + key;
                    obj = new URL(url);
                    con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.connect();
                    in = con.getInputStream();
                    isw = new InputStreamReader(in);
                    data = isw.read();
                    output = "";
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        output += current;
                    }
                    JSONObject result = new JSONObject(output);
                    if (result.getString("status").equals("finished")){
                        finished = true;
                    }
                }
                url = "https://api.priceapi.com/products/bulk/" + jobID + "?token=" + key;
                obj = new URL(url);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                in = con.getInputStream();
                isw = new InputStreamReader(in);
                data = isw.read();
                output = "";
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }

            } catch (Exception e) {
                e.printStackTrace();
                err = true;
                return e.toString();
            }

            return output;

        }

        protected void onPostExecute(String output) {
            if (output.length() == 0) {
                productDisplay.setText("Online results not found");
                return;
            }
            if (err){
                productDisplay.setText(output);
                return;
            }
            try {
                JSONObject result = new JSONObject(output);
                JSONArray products = result.getJSONArray("products");

                //for (int i = 0; i < products.length(); i++) {
                    JSONObject targetResult = products.getJSONObject(0);
                    productDisplay.setText(targetResult.getString("name"));
                    detailsDisplay.setText(targetResult.getString("description"));

                    imageURL = targetResult.getString("image_url");
                    new DownloadImageTask(productImage).execute(imageURL);


                //}
            } catch (JSONException e) {
                productDisplay.setText(e.toString());
                e.printStackTrace();
            }
            //contentTxt.setText(details);
            try {
                //productDisplay.setText(details);
            } catch (Exception e) {
                productDisplay.setText(e.toString());
            }


        }
    }



    class UpdateProduct extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String url = "https://storeproject-209402.appspot.com/products";
            try {
                url += "?name=" + URLEncoder.encode(productDisplay.getText().toString(), "UTF-8");
                url += "&code=" + barcode;
                url += "&image=" + imageURL;
                url += "&description=" + URLEncoder.encode(detailsDisplay.getText().toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
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
            url = "https://storeproject-209402.appspot.com/prices";
            url += "?code=" + barcode;
            url += "&price=" + priceInput.getText().toString();
            output = "";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
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

            url = "https://storeproject-209402.appspot.com/stocks";
            url += "?code=" + barcode;
            url += "&stock=" + stockInput.getText().toString();
            output = "";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
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

            Toast toast = Toast.makeText(getApplicationContext(),
                    "Product added/modified", Toast.LENGTH_SHORT);
            toast.show();
            try {//modify
                toMainScreen(name);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class RemoveProduct extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String url = "https://storeproject-209402.appspot.com/products/" + barcode;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("DELETE");
                con.setDoInput(true);
                con.setDoOutput(true);
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
            url = "https://storeproject-209402.appspot.com/prices/" + barcode;

            output = "";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("DELETE");
                con.setDoInput(true);
                con.setDoOutput(true);
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

            url = "https://storeproject-209402.appspot.com/stocks/" + barcode;
            output = "";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("DELETE");
                con.setDoInput(true);
                con.setDoOutput(true);
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
                        "Not found", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            Toast toast = Toast.makeText(getApplicationContext(),
                    "Product deleted", Toast.LENGTH_SHORT);
            toast.show();
            try {//modify
                toMainScreen(name);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }




}
