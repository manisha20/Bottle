package com.example.salazar.bottle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
/*
This class will be the first to be executed. It wil check if its a new user or old
through checking data from SharedPreference and open corresponding activity

 */

public class MainActivity extends ActionBarActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter;
    ListView deviceListView;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    String mac_id="98:D3:31:40:6D:72";
    //98:D3:31:40:6D:72
    BluetoothDevice bottle;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceListView=(ListView)findViewById(R.id.deviceListView);
        System.out.println("in onCreate");
        boolean new_user;
        new_user = checkIfNewUSer();
        if (new_user == true)
        {
            Toast.makeText(getApplicationContext(), "New User", Toast.LENGTH_LONG).show();
            //take all info (start activity for result to return here)and then activate bluetooth
            //start the get weight activity!

            //enableBluetooth();
            //pair with bottle
        }
        else {
            //call function to activate bluetooth
            enableBluetooth();
            //start water displayer activity
            //connectToDevice();
        }
    }



    public void newUserBTClicked(View view) {
        Intent newUserIntent=new Intent(this,WeightCollector.class);
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//        newUserIntent.putExtra(EXTRA_MESSAGE, message);
        startActivity(newUserIntent);
    }


    public void enableBluetooth()
    {
        Log.v("MainActivity","in enableBluetooth()");
        //get adapter object
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //enable bluetooth for mobile
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
        //BlueTooth ON!
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check if it is request for Bluetooth enabling
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode != RESULT_OK) {

                enableBluetooth();
            }

            else {
                connectToDevice();

            }

        }
    }

    private void connectToDevice() {
        Boolean flag=true;
        //Toast.makeText(getApplicationContext(),"Bluetooth ON",Toast.LENGTH_LONG).show();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac_id);
        Intent i = new Intent(this, WaterConsumptionDisplayer.class);
        i.putExtra(EXTRA_DEVICE_ADDRESS, mac_id);
        startActivity(i);





        final ArrayList<String> deviceList = new ArrayList<String>();
        //discover devices to pair with Bottle!
        //get list of already paired devices
        //Toast.makeText(getApplicationContext(),"Bluetooth ON",Toast.LENGTH_LONG).show();
        //Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //BluetoothDevice dev[]= (BluetoothDevice[]) pairedDevices.toArray();
        //bottle=dev[1];

        /*
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            //ArrayAdapter mArrayAdapter=new ArrayAdapter();
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                deviceList.add(device.getAddress());

                }

                ArrayAdapter deviceListAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,deviceList);
                deviceListView.setAdapter(deviceListAdapter);
                deviceListView.setBackgroundColor(Color.BLUE);
                deviceListView.setOnItemClickListener(mDeviceClickListener);

                for (int i = 0; i < deviceList.size(); ++i) {
                    System.out.println(deviceList.get(i));
                }

            }*/



    }

    private boolean checkIfNewUSer() {
        //check from shared PRef



       // returns true if a new user
        return false;
    }

    private Set<BluetoothDevice> getALlPAiredDevices(){


        return null;
    }
    /*
    Editor editor = sharedpreferences.edit();
    editor.putString("key", "value");
    editor.commit();
*/


    //@Override
    /*
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            //case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
              //  serverIntent = new Intent(this, DeviceListActivity.class);
                //startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                //return true;
        }
        return false;
    }*/
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {


            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            System.out.println(info);
            //String address = info.substring(info.length() - 17);
            //Toast.makeText(getApplicationContext(), address, Toast.LENGTH_LONG).show();


            // Make an intent to start next activity while taking an extra which is the MAC address.
            //Intent i = new Intent(this, WaterConsumptionDisplayer.class);
            //i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            //startActivity(i);
        }
    };


}
