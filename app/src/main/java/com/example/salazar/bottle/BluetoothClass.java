package com.example.salazar.bottle;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by Salazar on 01-11-2015.
 */
public class BluetoothClass {

    private final static int REQUEST_ENABLE_BT = 1;
    public void connect()
    {
        //get adapter object
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //enable bluetooth for mobile
        /*
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        */
    }





}
