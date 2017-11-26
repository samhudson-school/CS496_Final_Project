package com.risestack.itinerary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

public class ViewItineraries extends Activity{
    public String sub_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_itineraries);

        Intent intent = getIntent();
        if(intent.hasExtra("sub")) {
            if (savedInstanceState == null) {
                Bundle extras = intent.getExtras();
                sub_key = extras.getString("sub");
            } else {
                sub_key = (String) savedInstanceState.getSerializable("sub");
            }
            Log.i("Subkey", sub_key);

            //get_data
            try {
                get("itineraries/" + sub_key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @SuppressLint("StaticFieldLeak")
    public void get(String handle) throws IOException {

        final String final_url = URL + handle;
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

    public void update_ui(JSONArray data) throws JSONException {
        final Context context = getApplicationContext();
        TableLayout itinerary_table = findViewById(R.id.itinerary_table);
        TableRow headings = new TableRow(this);

        TextView h1 = new TextView(this);
        h1.setText("Activity         ");
        headings.addView(h1);

        TextView h5 = new TextView(this);
        h5.setText("Duration         ");
        headings.addView(h5);

        TextView h6 = new TextView(this);
        h6.setText("Update");
        headings.addView(h6);

        TableLayout.LayoutParams lp =
                new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);

        itinerary_table.addView(headings);




        for (int i = 0; i < data.length(); i++) {
            JSONObject dataObject = data.getJSONObject(i);
            String id = dataObject.optString("id");
            String name = dataObject.optString("name");
            String duration_of_stay = dataObject.optString("duration_of_stay");

            TableRow row = new TableRow(this);


            TextView c1 = new TextView(this);
            c1.setText(name);
            c1.setGravity(Gravity.CENTER);
            row.addView(c1);




            TextView c5 = new TextView(this);
            c5.setText(duration_of_stay);
            c5.setGravity(Gravity.CENTER);
            row.addView(c5);


            Button c6 = new Button(this);
            c6.setTag(id);
            c6.setText("Update");
            c6.setGravity(Gravity.CENTER);
            row.addView(c6);
            c6.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String id = (String) view.getTag();
                    Intent intent = new Intent(context, UpdateItinerary.class);
                    intent.putExtra("id", id);
                    context.startActivity(intent);
                }
            });
            itinerary_table.addView(row);

            }
        }

}

