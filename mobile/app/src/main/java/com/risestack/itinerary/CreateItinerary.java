package com.risestack.itinerary;


import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CreateItinerary extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_itinerary);

        String sub_key;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            sub_key= extras.getString("sub");
        } else {
            sub_key= (String) savedInstanceState.getSerializable("sub");
        }
        Log.i("Subkey",sub_key);


    }

    public void submit_itinerary(View button) {

        EditText textEntry_name = (EditText) findViewById(R.id.textEntry_name);
        EditText textEntry_street = (EditText) findViewById(R.id.textEntry_street);
        EditText textEntry_city  = (EditText) findViewById(R.id.textEntry_city);
        EditText textEntry_zip = (EditText) findViewById(R.id.textEntry_zip);
        EditText textEntry_duration_of_stay = (EditText) findViewById(R.id.textEntry_duration_of_stay);
        String text_name = textEntry_name.getText().toString();
        String text_street = textEntry_street.getText().toString();
        String text_city = textEntry_city.getText().toString();
        String text_zip = textEntry_zip.getText().toString();
        String text_duration_of_stay = textEntry_duration_of_stay.getText().toString();
        Log.i("text_name",text_name);
        Log.i("text_street",text_street);
        Log.i("text_city",text_city);
        Log.i("text_zip",text_zip);
        Log.i("text_duration_of_stay",text_duration_of_stay);



    }

}
