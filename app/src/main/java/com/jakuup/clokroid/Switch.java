package com.jakuup.clokroid;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Switch {
    private static final String TAG = "Switch";

    private InetAddress ipAddress;
    private int tcpPort;

    Switch(String address, int port) {
        try {
            ipAddress = InetAddress.getByName(address);
            tcpPort = port;
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        send();
    }

    private void send() {
        try {
            Socket socket = new Socket(ipAddress, tcpPort);


            OutputStream out = socket.getOutputStream();
            PrintWriter output = new PrintWriter(out);

            output.println("Hello from Android");

            out.flush();
            out.close();
            socket.close();

            ClokroidApplication.kick(TAG + "[" + ipAddress + ":" + tcpPort + "]@send");
        }
        catch (IOException e) {
            ClokroidApplication.log(TAG + "[" + ipAddress + "]", "Failed to send data on the port:" + tcpPort);
            e.printStackTrace();
        }
    }
}
