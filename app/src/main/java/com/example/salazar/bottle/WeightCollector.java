package com.example.salazar.bottle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
/*
* this class is collecting weight information from user
* */

public class WeightCollector extends ActionBarActivity {

    float weight=0;
    boolean flag=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_collector);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weight_collector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void weightBTOnClick(View view) {
        Intent weightIntent=new Intent(this,HeightCollector.class);
        EditText strWeight = (EditText) findViewById(R.id.weightET);
        String weightTemp = strWeight.getText().toString();
        flag=true;
        try {
            weight = Float.parseFloat(weightTemp);

        } catch (NumberFormatException e) {

            Log.i("", weightTemp + " is not a number");
            Toast.makeText(getApplicationContext(), "Please Enter Digits only!", Toast.LENGTH_LONG).show();
            flag=false;
        }

        if(weight!=0 && flag!=false  ) {

            SharedPreferences sp =
                    getSharedPreferences("MyPrefs",
                            Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat("weight", weight);
            editor.commit();
            //weightIntent.putExtra(EXTRA_MESSAGE, message);
            startActivity(weightIntent);
        }
    }

}


