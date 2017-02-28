package com.lukeb_000.lukebaxchatroom;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatRoom extends AppCompatActivity {
    private String message;
    private TextView messagesView;
    private static final String DEBUG_TAG = "HttpExample";
    private int current;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Intent intent = getIntent();
        name = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        messagesView = (TextView) findViewById(R.id.textbox);
        message = "";
        messagesView.setText(message);
        new Thread(new Runnable() {
            public void run() {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    current = 0;
                    while (true){
                        new DownloadWebpageTask().execute("http://www.lukebax.net/chat/get?currentPoint=" + current);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException i) {
                        }
                    }
                }
            }
        }).start();
    }

    public void postNewMessage(View view)throws IOException {
        EditText editText = (EditText) findViewById(R.id.edit_message);
        message = editText.getText().toString();
        message = message.replaceAll(" ","%20");
        name = name.replaceAll(" ","%20");

        new Thread(new Runnable(){
            public void run() {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    try {
                        URL url = new URL("http://www.lukebax.net/chat/put?name="+name+"&message="+message);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        conn.connect();
                        conn.getResponseCode();
                    } catch (IOException e) {
                    }
                }
            }
        }).start();

        editText.setText("");
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            decodeJSON(result);
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            int len = 1000;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        public void decodeJSON(String inputJSON) {
            try{
                JSONArray arr = new JSONArray(inputJSON);
                if (arr.length() >0) {
                    for (int i = 0; i<arr.length(); i++) {
                        messagesView.append(arr.getString(i)+ "\n");
                        current++;
                    }
                }
            }catch (JSONException j) {
            }
        }
    }
}