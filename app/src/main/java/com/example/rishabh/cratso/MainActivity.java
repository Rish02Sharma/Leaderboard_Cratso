package com.example.rishabh.cratso;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.rishabh.cratso.R.id.lv;

public class MainActivity extends AppCompatActivity {

    ListView list;
    ProgressDialog pDialog;
    ArrayList<HashMap<String, String>> LeaderList =new ArrayList<>();
    private static final String TAG_NAME="name";
    private static final String TAG_ID = "user_id";
    private static final String TAG_ImageURL = "profile_pic";
    private static final String TAG_RANK = "rank";

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static String offset;
    public static String pointer;

    TextView t1=null;
    TextView t2=null;
    TextView t3=null;




 //OnClick for generating list
    public void showlist(View view){
        EditText Offset = (EditText) findViewById(R.id.offset);
        String offset = Offset.getText().toString();
        this.offset=offset;

        EditText Pointer = (EditText) findViewById(R.id.pointer);
        String pointer = Pointer.getText().toString();
        this.pointer=pointer;

       loadActivity();
    }



    // OnClick method to clear existing values of list
    public void clear(View view){
        list.setAdapter(null);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadActivity();
    }

    private void loadActivity() {
        list=(ListView)findViewById(lv);
        CheckInAsync cn = new CheckInAsync();
        cn.execute();
    }


    private class CheckInAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...params) {

            URL url1 = createUrl();

            String jsonResponse ="";

                try {
                    jsonResponse = makeHttpRequest(url1);
                } catch (IOException e) {
                    Log.e(LOG_TAG, " Error in getting back url connection.");
                }

            return jsonResponse;
        }




        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(String result) {
            if (pDialog.isShowing())
                pDialog.dismiss();

            //Showing Response of JSON
           if(t1!=null){ t1.setText("CODE 200 : Successful operation");
               extractFeatureFromJson(result);
           }
            if(t2!=null) { t2.setText("CODE 400 : BAD REQUEST"); }

          if(t3!=null) { t3.setText("CODE 404 : CategoryId not found"); }
        }

        //Editing URL
        private URL createUrl() {
            URL url = null;
            try {
                url = new URL("https://us-central1-cratso-171712.cloudfunctions.net/cratso_internship/leaderboard?pointer="+pointer+"&offset=5"+offset);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }


        // method to get response of JSON
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";


            if ( url==null){

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

                if (urlConnection.getResponseCode() == 200) {

                     t1 = (TextView)findViewById(R.id.response);
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);


                } else if (urlConnection.getResponseCode() == 400) {

                     t2 = (TextView)findViewById(R.id.response);

                } else if (urlConnection.getResponseCode() == 404) {

                     t3 = (TextView)findViewById(R.id.response);

                } else {

                    Log.e(LOG_TAG, " Error in getting back url connection.");

                }
    } catch (IOException e) {
        Log.e(LOG_TAG, " Problem retrieving the earthquake json response", e);
    } finally {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        if (inputStream != null) {
            // function must handle java.io.IOException here
            inputStream.close();
        }
    }
    return jsonResponse;
        }

        private String readFromStream(InputStream inputStream)throws IOException {
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


        private void extractFeatureFromJson(String cratsoJSON) {
            try {
                JSONObject baseJsonResponse = new JSONObject(cratsoJSON);
                JSONArray featureArray = baseJsonResponse.getJSONArray("data");


                for (int i=0; i<featureArray.length() ; i++) {

                    JSONObject firstFeature = featureArray.getJSONObject(i);

                    String NAME = firstFeature.getString("name");
                    String ID = firstFeature.getString("user_id");
                    String url = firstFeature.getString("profile_pic");
                    String rank = firstFeature.getString("rank");


                    HashMap<String,String> persons = new HashMap<String,String>();
                    persons.put(TAG_NAME,NAME);
                    persons.put(TAG_ID,ID);
                    persons.put(TAG_ImageURL,url);
                    persons.put(TAG_RANK,rank);

                    LeaderList.add(persons);
                    Log.d("Preonlist detail", String.valueOf(LeaderList));

                }

                Adapterr adpt=new Adapterr(LeaderList);
                list.setAdapter(adpt);

            } catch (JSONException e) {
                System.out.println("Not able to print");
            }

        }


        private class Adapterr extends BaseAdapter
        {
            LayoutInflater inflater;
            ArrayList<HashMap<String, String>> leader =new ArrayList<>();


            public Adapterr( ArrayList<HashMap<String, String>> leaderList) {

                this.leader =leaderList;
            }


            @Override
            public int getCount() {
                return leader.size();
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View v, ViewGroup parent) {
                if(inflater==null)
                    inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(v==null)

                    v = inflater.inflate(R.layout.apisingle, null);

                TextView t1 = (TextView) v.findViewById(R.id.NAME);
                TextView t2 = (TextView) v.findViewById(R.id.ID);
                TextView t3 = (TextView) v.findViewById(R.id.rank);

                ImageView imageView = (ImageView) v.findViewById(R.id.profile_pic);
                if (imageView == null) {
                    imageView = new ImageView(MainActivity.this);
                }

                HashMap<String, String> finalPersons = new HashMap<>();
                finalPersons = LeaderList.get(position);

                t1.setText(finalPersons.get(TAG_NAME));
                t2.setText(finalPersons.get(TAG_ID));
                t3.setText(finalPersons.get(TAG_RANK));

                String url = finalPersons.get(TAG_ImageURL);

                Picasso.with(MainActivity.this).load(url).into(imageView);

                return v;
            }
        }
    }
}