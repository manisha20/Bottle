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
* this class is collecting height information from user
* */
public class HeightCollector extends ActionBarActivity {

    float height=0;
    boolean flag=true;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_collector);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_height_collector, menu);
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
    public void heightBTOnClick(View view) {

        Intent heightIntent=new Intent(this,WaterConsumptionDisplayer.class);
        EditText strHeight = (EditText) findViewById(R.id.heightET);
        String heightTemp = strHeight.getText().toString();
        flag=true;
        try {
            height = Float.parseFloat(heightTemp);
        } catch (NumberFormatException e) {

            Log.i("",heightTemp+" is not a number");
            Toast.makeText(getApplicationContext(), "Please Enter Digits only!", Toast.LENGTH_LONG).show();
            flag=false;
        }
        if(height!=0 && flag!=false  ) {
            SharedPreferences sp =
                    getSharedPreferences("MyPrefs",
                            Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat("height", height);
            editor.commit();
            String mac_id="98:D3:31:40:6D:72";

            heightIntent.putExtra(EXTRA_DEVICE_ADDRESS, mac_id);
            //weightIntent.putExtra(EXTRA_MESSAGE, message);
            startActivity(heightIntent);
        }
    }
}
