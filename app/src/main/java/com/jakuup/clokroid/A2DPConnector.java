package com.jakuup.clokroid;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


public class A2DPConnector extends HandlerThread {
    private static final String TAG = "A2DPConnector";
    private final Logger mLogger;
    private final Context mContext;
    private final Object syncLooper = new Object();
    private final Object syncProxy = new Object();

    private Set<BluetoothDevice> bluetoothDevices = new HashSet<>();

    private BluetoothA2dp bluetoothProxy;

    private Method methodConnect;
    private Method methodDisconnect;

    private static final int MSG_ID_ENUM_DEVICES = 0;
    private static final int MSG_ID_CONNECT = 1;
    private static final int MSG_ID_DISCONNECT = 2;

    private WeakReference<A2DPConnectorCallback> myCallback;

    private class MyHandler extends Handler {

        private BluetoothDevice currentBluetoothDevice;

        MyHandler(Looper looper) {
            super(looper);
            currentBluetoothDevice = null;
        }

        private final BroadcastReceiver bluetoothConnectionReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                    if (state == BluetoothA2dp.STATE_CONNECTED) {
                        mLogger.kick(TAG + ": connected");
                        myCallback.get().notifyA2DPConnected(true);
                    } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                        currentBluetoothDevice = null;
                        myCallback.get().notifyA2DPConnected(false);
                        mLogger.kick(TAG + ": disconnected");
                    } /*
                    else if (state == BluetoothA2dp.STATE_CONNECTING) {
                        Log.d(TAG, "Connecting...");
                    } else if (state == BluetoothA2dp.STATE_DISCONNECTING) {
                        Log.d(TAG, "Disconnecting...");
                    } */
                }
            }
        };

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            waitForProxy();
            switch (msg.what) {
                case MSG_ID_ENUM_DEVICES: {
                    StringBuilder result = new StringBuilder();
                    for (BluetoothDevice device : bluetoothDevices) {
                        if (!result.toString().equals("")) {
                            result.append(";");
                        }
                        result.append(device.getName());
                    }
                    myCallback.get().notifyA2DPDevicesNames(result.toString());
                    mLogger.write(TAG, "Notified bounded devices: [" + result.toString() + "]");
                    break;
                }
                case MSG_ID_CONNECT: {
                    if (currentBluetoothDevice == null) {
                        Bundle bundle = msg.getData();
                        currentBluetoothDevice = ((A2DPDevice) bundle.getSerializable("device")).getBluetoothDevice();

                        mContext.registerReceiver(bluetoothConnectionReceiver, new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED));
                        try {
                            methodConnect.setAccessible(true);
                            methodConnect.invoke(bluetoothProxy, currentBluetoothDevice);
                        } catch (IllegalAccessException e) {
                            mLogger.write(TAG, "Illegal access");
                            Log.e(TAG, e.toString());
                        } catch (InvocationTargetException e) {
                            mLogger.write(TAG, "Unable to invoke 'connect'");
                            Log.e(TAG, e.toString());
                        }
                    }
                    break;
                }
                case MSG_ID_DISCONNECT: {
                    if (currentBluetoothDevice != null) {
                        mContext.unregisterReceiver(bluetoothConnectionReceiver);
                        try {
                            methodDisconnect.setAccessible(true);
                            methodDisconnect.invoke(bluetoothProxy, currentBluetoothDevice);
                        } catch (IllegalAccessException e) {
                            mLogger.write(TAG, "Illegal access");
                            Log.e(TAG, e.toString());
                        } catch (InvocationTargetException e) {
                            mLogger.write(TAG, "Unable to invoke 'connect'");
                            Log.e(TAG, e.toString());
                        }
                        currentBluetoothDevice = null;
                        myCallback.get().notifyA2DPConnected(false);
                    }
                    break;
                }
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
    private MyHandler handler;

    private boolean needToWaitForLooper = true;

    private void waitForLooper() {
        if (needToWaitForLooper) {
            synchronized (syncLooper) {
                try {
                    syncLooper.wait();
                    needToWaitForLooper = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean needToWaitForProxy = true;

    private void waitForProxy() {
        if (needToWaitForProxy) {
            synchronized (syncProxy) {
                try {
                    syncProxy.wait();
                    needToWaitForProxy = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public A2DPConnector(android.content.Context context) {
        super("A2DPConnectorThread");

        mContext = context;
        mLogger = new Logger();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {

                BroadcastReceiver bluetoothEnabledReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                            if (state == BluetoothAdapter.STATE_ON) {
                                getProxyAndMethods(bluetoothAdapter);
                            }
                        }
                    }
                };

                mContext.registerReceiver(bluetoothEnabledReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                bluetoothAdapter.enable();
            } else {
                getProxyAndMethods(bluetoothAdapter);
            }
        }
        else {
            mLogger.write(TAG, "Bluetooth is not supported in the hardware");
        }
    }

    private void getProxyAndMethods(BluetoothAdapter bluetoothAdapter) {
        BluetoothProfile.ServiceListener a2dpProfileListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    bluetoothProxy = (BluetoothA2dp) proxy;
                    synchronized (syncProxy) {
                        syncProxy.notify();
                    }
                }
            }
            @Override
            public void onServiceDisconnected(int profile) {
            }
        };

        bluetoothAdapter.getProfileProxy(mContext, a2dpProfileListener, BluetoothProfile.A2DP);
        bluetoothDevices = bluetoothAdapter.getBondedDevices();
        try {
            methodConnect = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
            methodDisconnect = BluetoothA2dp.class.getDeclaredMethod("disconnect", BluetoothDevice.class);
        } catch (NoSuchMethodException e) {
            mLogger.write(TAG, "No 'connect and/or disconnect' method in BluetoothA2dp");
        }
    }

    public void setCallback(A2DPConnectorCallback callback) {
        myCallback = new WeakReference<>(callback);
    }

    public void getBoundedDevices() {
        waitForLooper();
        handler.sendEmptyMessage(MSG_ID_ENUM_DEVICES);
    }

    public void connect(String name) {
        waitForLooper();
        for (BluetoothDevice device : bluetoothDevices) {
            if (device.getName().equals(name)) {
                Bundle bundle = new Bundle();
                A2DPDevice a2DPDevice = new A2DPDevice(device);
                bundle.putSerializable("device", a2DPDevice);

                Message msg = Message.obtain();
                msg.what = MSG_ID_CONNECT;
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }
    }

    public void disconnect() {
        waitForLooper();
        handler.sendEmptyMessage(MSG_ID_DISCONNECT);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = new MyHandler(getLooper());
        synchronized (syncLooper) {
            syncLooper.notify();
        }
    }
}
