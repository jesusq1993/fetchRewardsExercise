package com.example.fetchrewardexercise;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import com.example.fetchrewardexercise.databinding.ActivityMainBinding;
import org.json.JSONArray;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    ArrayList<DataObject> arrayList = new ArrayList<>();
    ArrayList<String> dataObjectList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    //DataObject class that stores listID, list, and name
    //uses Comparable interface to be able to sort ArrayList by either listID, list, or Name
    public class DataObject implements Comparable<DataObject>{
        int id;
        int listID;
        String name;

        public DataObject(int id, int listID, String name) {
            this.id = id;
            this.listID = listID;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getListID() {
            return listID;
        }

        public void setListID(int listID) {
            this.listID = listID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(DataObject object) {
            return listID - object.getListID();
        }
    }

    class NameComparator implements Comparator<DataObject>{
        @Override
        public int compare(DataObject dataObject1, DataObject dataObject2) {
            return Integer.parseInt(dataObject1.getName().substring(5)) - Integer.parseInt(dataObject2.getName().substring(5));
        }
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

                JSONArray jsonArray = new JSONArray(result);

                //Everytime data is fetched clear the arraylist and input the fetched data
                //Loop through all objects and only add to arrayList the objects that don't have null or empty string
                arrayList.clear();
                for(int i = 0; i < jsonArray.length();i++){
                    if(!jsonArray.getJSONObject(i).getString("name").equals("")){
                        if(!jsonArray.getJSONObject(i).isNull("name")) {
                            /*String id = Integer.toString(jsonArray.getJSONObject(i).getInt("id"));
                            String listID = Integer.toString(jsonArray.getJSONObject(i).getInt("listId"));
                            String name = jsonArray.getJSONObject(i).getString("name");

                            String objectInfo = "ID: " + id + "\nList ID: " + listID + "\nName: " + name;
                            arrayList.add(objectInfo);*/

                            int id = jsonArray.getJSONObject(i).getInt("id");
                            int listID = jsonArray.getJSONObject(i).getInt("listId");
                            String name = jsonArray.getJSONObject(i).getString("name");

                            DataObject dataObject = new DataObject(id,listID,name);
                            arrayList.add(dataObject);
                        }
                    }

                }

                //ArrayList sort by listID
                Collections.sort(arrayList);

                //ArrayList sort by name
                sortByName(arrayList);

                //convert to string so that it can be added to listView
                addObjectDataList(arrayList);

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
        arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, dataObjectList);
        activityMainBinding.listView.setAdapter(arrayAdapter);
    }

    //Initiates background thread to get JSON Data
    public void fetchData(View view) {
        FetchData fetchData = new FetchData();
        fetchData.start();
    }

    //adds all data to new String ArrayList
    public void addObjectDataList(ArrayList<DataObject> objects){
        for(int i = 0; i < objects.size(); i++){
            dataObjectList.add(Integer.toString(objects.get(i).getListID()) + "        " + Integer.toString(objects.get(i).getId()) + "        " + objects.get(i).getName());
        }
    }

    //seperate listID objects into individual ArrayList and sort each by name.
    //clear string arraylist and all sorted items
    private void sortByName(ArrayList<DataObject> arrayList) {
        ArrayList<DataObject> listID1 = new ArrayList<>();
        ArrayList<DataObject> listID2 = new ArrayList<>();
        ArrayList<DataObject> listID3 = new ArrayList<>();
        ArrayList<DataObject> listID4 = new ArrayList<>();

        for(DataObject object: arrayList){
            switch (object.getListID()){
                case 1:
                    listID1.add(object);
                    break;
                case 2:
                    listID2.add(object);
                    break;
                case 3:
                    listID3.add(object);
                    break;
                case 4:
                    listID4.add(object);
                    break;
            }
        }
        Collections.sort(listID1, new NameComparator());
        Collections.sort(listID2, new NameComparator());
        Collections.sort(listID3, new NameComparator());
        Collections.sort(listID4, new NameComparator());

        arrayList.clear();

        arrayList.addAll(listID1);
        arrayList.addAll(listID2);
        arrayList.addAll(listID3);
        arrayList.addAll(listID4);

    }

}