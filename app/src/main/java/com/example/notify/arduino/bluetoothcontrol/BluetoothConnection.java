package com.example.notify.arduino.bluetoothcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by root on 1/23/18.
 */

public class BluetoothConnection extends AsyncTask<Void, Void, Void> {
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean connectSuccess = true;
    private String address = null;
    private String TAG = this.getClass().getSimpleName();
    private static final UUID statUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothConnection(final String address) {
        this.address = address;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "Initializing BT...");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try
        {
            if (btSocket == null || !isBtConnected) {
                // Get the mobile bluetooth device
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                // Connects to the device's address and checks if it's available
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                // Create a RFCOMM (SPP) connection
                btSocket = device.createInsecureRfcommSocketToServiceRecord(statUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                // Start connection
                btSocket.connect();
            }
        }
        catch (IOException e)
        {
            connectSuccess = false; //if the try failed, you can check the exception here
            Log.e(TAG, "BT Connection Failed");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (!connectSuccess) {
            Log.e(TAG, "Connection Failed");
            throw new RuntimeException("Connection Failed");
        } else {
            Log.i(TAG, "Connection Successful");
            isBtConnected = true;
        }
    }

    public void send(String s) {
        if (btSocket == null) {
            Log.e(TAG, "Socket is not initialized.");
            throw new RuntimeException("Socket is not initialized");
        }
        try {
            btSocket.getOutputStream().write(s.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Couldn't send data");
        }
    }
}
