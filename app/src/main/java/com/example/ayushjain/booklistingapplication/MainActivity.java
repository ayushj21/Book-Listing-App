package com.example.ayushjain.booklistingapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button searchButton;
    BooksAdapter adapter;
    ListView listView;
    TextView noDataFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        searchButton = (Button) findViewById(R.id.searchButton);
        noDataFound = (TextView) findViewById(R.id.no_data_found);

        adapter = new BooksAdapter(this, -1);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        searchButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute();
                }
                else   {
                    Toast.makeText(MainActivity.this, "No Internet Connection",
                            Toast.LENGTH_LONG).show();
                }
            }

        }
    );

}
    private String getUserInput() {
        return editText.getText().toString();
    }



    private class BookAsyncTask extends AsyncTask<URL, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(URL... urls) {
            URL url = createURL(getUrlForHttpRequest());
            String jsonResponse = "";

            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<Book> books = parseJSON(jsonResponse);
            return books;
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            if (books == null) {
                return;
            }
            updateUi(books);
        }

        private URL createURL(String stringUrl) {
            try {
                return new URL(stringUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }

        private String getUrlForHttpRequest() {
            final String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=search+";
            String formatUserInput = getUserInput().trim().replaceAll("\\s+","+");
            String url = baseUrl + formatUserInput;
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null){
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200){
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e("mainActivity", "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private List<Book> parseJSON(String json) {

            if (json == null) {
                return null;
            }

            List<Book> books =  QueryUtils.extractBooks(json);
            return books;
        }
    }

    private void updateUi(List<Book> books){
        if (books.isEmpty()){
            // if no books found, show a message
            noDataFound.setVisibility(View.VISIBLE);
        } else {
            noDataFound.setVisibility(View.GONE);
        }
        adapter.clear();
        adapter.addAll(books);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
