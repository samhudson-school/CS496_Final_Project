package com.risestack.itinerary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.risestack.itinerary.MainApplication.JSON;
import static com.risestack.itinerary.MainApplication.LOG_TAG;
import static com.risestack.itinerary.MainApplication.URL;

public class UpdateItinerary extends Activity {
    public String id;
    public String sub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_itinerary);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            id= extras.getString("id");
        } else {
            id= (String) savedInstanceState.getSerializable("id");
        }

        try {
            get("itinerary/"+id);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @SuppressLint("StaticFieldLeak")
    public void get(String handle) throws IOException {

        final String final_url = URL + handle;
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... elements) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(final_url)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    String jsonBody = response.body().string();
                    Log.i(LOG_TAG, String.format("API Response %s", jsonBody));
                    return new JSONObject(jsonBody);
                } catch (Exception exception) {
                    Log.w(LOG_TAG, exception);
                }
                return null;
            }
            @Override
            protected void onPostExecute(JSONObject responseData) {
                if (responseData != null) {
                    try {
                        update_ui(responseData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    public void patch(String handle, String json) throws IOException {

        final String final_url = URL+handle;
        final String final_json = json;
        final String final_sub = sub;
        final Context context = getApplicationContext();
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... elements) {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, final_json);
                Request request = new Request.Builder()
                        .url(final_url)
                        .patch(body)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if(response.code()!=200) {
                        Log.w(LOG_TAG, "Error");
                    }
                } catch (Exception exception) {
                    Log.w(LOG_TAG, exception);
                }
                return null;
            }
            @Override
            protected void onPostExecute(JSONObject responseData) {
                CharSequence text = "Itinerary updated!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Intent intent = new Intent(UpdateItinerary.this, ViewItineraries.class);
                intent.putExtra("sub", final_sub);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                UpdateItinerary.this.startActivity(intent);
            }

        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    public void delete(String handle) throws IOException {

        final String final_url = URL+handle;
        final String final_sub = sub;
        final Context context = getApplicationContext();
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... elements) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(final_url)
                        .delete()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if(response.code()!=200) {
                        Log.w(LOG_TAG, "Error");
                    }
                } catch (Exception exception) {
                    Log.w(LOG_TAG, exception);
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject responseData) {
                CharSequence text = "Itinerary deleted!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Intent intent = new Intent(UpdateItinerary.this, ViewItineraries.class);
                intent.putExtra("sub", final_sub);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                UpdateItinerary.this.startActivity(intent);
            }



        }.execute();
    }
    public void update_itinerary(View button) throws JSONException, IOException {
        //look textEntry fields
        EditText textEntry_name = (EditText) findViewById(R.id.textEntry_name);
        EditText textEntry_street = (EditText) findViewById(R.id.textEntry_street);
        EditText textEntry_city  = (EditText) findViewById(R.id.textEntry_city);
        EditText textEntry_zip = (EditText) findViewById(R.id.textEntry_zip);
        EditText textEntry_duration_of_stay = (EditText) findViewById(R.id.textEntry_duration_of_stay);

        //creates JSON object
        JSONObject joItinerary = new JSONObject();
        joItinerary.put("name",textEntry_name.getText().toString());
        joItinerary.put("street",textEntry_street.getText().toString());
        joItinerary.put("city",textEntry_city.getText().toString());
        joItinerary.put("zip",textEntry_zip.getText().toString());
        joItinerary.put("duration_of_stay",textEntry_duration_of_stay.getText().toString());
        //posts data
        patch("itinerary/"+this.id,joItinerary.toString());





    }
    public void delete_itinerary(View button) throws JSONException, IOException {
        //posts data
        delete("itinerary/"+this.id);




    }


    public void update_ui(JSONObject data) throws JSONException {
        //look textEntry fields
        EditText textEntry_name = (EditText) findViewById(R.id.textEntry_name);
        EditText textEntry_street = (EditText) findViewById(R.id.textEntry_street);
        EditText textEntry_city  = (EditText) findViewById(R.id.textEntry_city);
        EditText textEntry_zip = (EditText) findViewById(R.id.textEntry_zip);
        EditText textEntry_duration_of_stay = (EditText) findViewById(R.id.textEntry_duration_of_stay);

        JSONObject dataObject = data;
        this.sub = dataObject.optString("owner");
        String name = dataObject.optString("name");
        String street = dataObject.optString("street");
        String city = dataObject.optString("city");
        String zip = dataObject.optString("zip");
        String duration_of_stay = dataObject.optString("duration_of_stay");

        textEntry_name.setText(name);
        textEntry_street.setText(street);
        textEntry_city.setText(city);
        textEntry_zip.setText(zip);
        textEntry_duration_of_stay.setText(duration_of_stay);



    }



    }

