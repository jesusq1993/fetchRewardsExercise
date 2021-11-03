package com.example.fetchrewardexercise;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fetchrewardexercise.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    public void fetchData(View view) {
        FetchData fetchData = new FetchData();
        fetchData.start();
    }


    // This class is in charge of fetching the data in the background thread.
    class FetchData extends Thread{

        @Override
        public void run() {

            String result = "";
            URL url;
            HttpsURLConnection httpsURLConnection = null;

            try {
                //Create URL and Httpsurlconnection to connect to webpage to extract json data
                url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = httpsURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                //While Loop with get each character one by one and keep concatenating it to the result string
                while(data!=-1){
                    char current = (char) data;
                    result+=current;
                    data = reader.read();
                }

                // used for testing purposes to make sure data is coming through
                Log.i("Content:",result);

                JSONArray jsonArray = new JSONArray(result);

                //Everytime data is fetched clear the arraylist and input the fetched data
                //Loop through all objects and only add to arrayList the objects that don't have null or empty string
                arrayList.clear();
                for(int i = 0; i < jsonArray.length();i++){
                    if(!jsonArray.getJSONObject(i).getString("name").equals("")){
                        if(!jsonArray.getJSONObject(i).isNull("name")) {
                            String id = Integer.toString(jsonArray.getJSONObject(i).getInt("id"));
                            String listID = Integer.toString(jsonArray.getJSONObject(i).getInt("listId"));
                            String name = jsonArray.getJSONObject(i).getString("name");

                            String objectInfo = "ID: " + id + "\nList ID: " + listID + "\nName: " + name;
                            arrayList.add(objectInfo);
                        }
                    }

                }

                //ArrayList
                Collections.sort(arrayList);

                //This Handler updates listView with data.
                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        arrayAdapter.notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace(); // If something goes wrong print stack trace.
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //using ViewBinding.
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);

        //Array adapter to layout data in list view
        arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
        activityMainBinding.listView.setAdapter(arrayAdapter);
    }
}