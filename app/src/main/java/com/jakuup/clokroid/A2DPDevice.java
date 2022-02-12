package com.jakuup.clokroid;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class A2DPDevice implements Serializable {

    private final BluetoothDevice myDevice;

    A2DPDevice(BluetoothDevice device) {
        myDevice = device;
    }

    BluetoothDevice getBluetoothDevice() {
        return myDevice;
    }
}
