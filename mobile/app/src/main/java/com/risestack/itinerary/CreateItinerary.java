package com.risestack.itinerary;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.risestack.itinerary.MainApplication.LOG_TAG;
import static com.risestack.itinerary.MainApplication.URL;
import static com.risestack.itinerary.MainApplication.JSON;


public class CreateItinerary extends Activity {
    public String sub_key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_itinerary);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            sub_key= extras.getString("sub");
        } else {
            sub_key= (String) savedInstanceState.getSerializable("sub");
        }
        Log.i("Subkey",sub_key);


    }

    public void submit_itinerary(View button) throws JSONException, IOException {

        EditText textEntry_name = (EditText) findViewById(R.id.textEntry_name);
        EditText textEntry_street = (EditText) findViewById(R.id.textEntry_street);
        EditText textEntry_city  = (EditText) findViewById(R.id.textEntry_city);
        EditText textEntry_zip = (EditText) findViewById(R.id.textEntry_zip);
        EditText textEntry_duration_of_stay = (EditText) findViewById(R.id.textEntry_duration_of_stay);
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
                    Intent intent = new Intent(context, ViewItineraries.class);
                    intent.putExtra("sub", responseData.optString("owner", null));

                    context.startActivity(intent);
                }
            }
        }.execute();
    }

}
