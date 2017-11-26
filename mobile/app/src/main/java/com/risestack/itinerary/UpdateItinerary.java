package com.risestack.itinerary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_itinerary);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            id= extras.getString("sub");
        } else {
            id= (String) savedInstanceState.getSerializable("sub");
        }
        Log.i("Itinerary_ID",id);

        try {
            get("itinerary/"+id);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @SuppressLint("StaticFieldLeak")
    public void get(String handle) throws IOException {

        final String final_url = URL + handle;
        final Context context = getApplicationContext();
        new AsyncTask<String, Void, JSONArray>() {
            @Override
            protected JSONArray doInBackground(String... elements) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(final_url)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    String jsonBody = response.body().string();
                    Log.i(LOG_TAG, String.format("API Response %s", jsonBody));
                    return new JSONArray(jsonBody);
                } catch (Exception exception) {
                    Log.w(LOG_TAG, exception);
                }
                return null;
            }

            protected void onPostExecute(JSONArray responseData) {
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
    public void post(String handle, String json) throws IOException {

        final String final_url = URL+handle;
        final String final_json = json;
        final Context context = getApplicationContext();
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... elements) {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, final_json);
                Request request = new Request.Builder()
                        .url(final_url)
                        .post(body)
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

                    CharSequence text = responseData.optString("name", null)+" has been added to your Itinerary";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        }.execute();
    }
    public void update_itinerary(View button) throws JSONException, IOException {


        //creates JSON object
        JSONObject joItinerary = new JSONObject();
        joItinerary.put("owner",sub_key);
        joItinerary.put("name",textEntry_name.getText().toString());
        joItinerary.put("street",textEntry_street.getText().toString());
        joItinerary.put("city",textEntry_city.getText().toString());
        joItinerary.put("zip",textEntry_zip.getText().toString());
        joItinerary.put("duration_of_stay",textEntry_duration_of_stay.getText().toString());
        //posts data
        post("itinerary",joItinerary.toString());


    }


    public void update_ui(JSONArray data) throws JSONException {


    }



    }

