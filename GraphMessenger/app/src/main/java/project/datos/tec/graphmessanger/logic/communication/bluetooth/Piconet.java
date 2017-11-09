package project.datos.tec.graphmessanger.logic.communication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.facebook.share.model.ShareOpenGraphAction;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import project.datos.tec.graphmessanger.logic.communication.communication.MessageManager;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;


/**
 * Created by josea on 11/18/2016.
 */

public class Piconet {

    private final static String TAG = "piconet";

    private final String PICONET = "ANDROID_PICONET_BLUETOOTH";

    private static HashMap<String, BluetoothSocket> mBtSockets;
    private final BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, Thread> mBtConnectionThreads;
    private ArrayList<UUID> mUuidList;
    private ArrayList<String> mBtDeviceAddresses;
    private ArrayList<BluetoothDevice> bluetoothDevices;


    private Context context;
    private static Piconet instance = null;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    if(msg.obj.toString().equals("data")) {
                        MessageManager.processMsg(msg.getData().getString("msg"));
                        Toast.makeText(context, msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
                    }else{
                        MessageManager.processMsg(msg.obj.toString(),msg.getData().getString("msg"));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Piconet(Context context){
        this.context = context;

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBtSockets = new HashMap<String, BluetoothSocket>();
        mBtConnectionThreads = new HashMap<String, Thread>();
        mUuidList = new ArrayList<UUID>();
        mBtDeviceAddresses = new ArrayList<String>();
        bluetoothDevices = new ArrayList<>();

        // Allow up to 7 devices to connect to the server
        mUuidList.add(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        mUuidList.add(UUID.fromString("54d1cc90-1169-11e2-892e-0800200c9a66"));
        mUuidList.add(UUID.fromString("6acffcb0-1169-11e2-892e-0800200c9a66"));
        mUuidList.add(UUID.fromString("7b977d20-1169-11e2-892e-0800200c9a66"));
        mUuidList.add(UUID.fromString("815473d0-1169-11e2-892e-0800200c9a66"));
        mUuidList.add(UUID.fromString("503c7434-bc23-11de-8a39-0800200c9a66"));
        mUuidList.add(UUID.fromString("503c7435-bc23-11de-8a39-0800200c9a66"));

        Thread connectionProvier = new Thread(new ConnectionProvider());
        connectionProvier.start();
    }

    public static Piconet instance(Context... contexts){
        if(instance == null){
            instance = new Piconet(contexts[0]);
        }
        return instance;
    }

    public void addDevice(BluetoothDevice device){
        if(!isPaired(device.getAddress())){
            pair(device);
        }
        BluetoothDevice remoteDevice = mBluetoothAdapter
                .getRemoteDevice(device.getAddress());
        connect(remoteDevice);
    }

    private class ConnectionProvider implements Runnable{

        @Override
        public void run() {
            try {
                for (int i=0; i<mUuidList.size(); i++) {
                    BluetoothServerSocket myServerSocket = mBluetoothAdapter
                            .listenUsingRfcommWithServiceRecord(PICONET, mUuidList.get(i));
                    Log.d(TAG, " ** Opened connection for uuid " + i + " ** ");

                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    Log.d(TAG, " ** Waiting connection for socket " + i + " ** ");
                    BluetoothSocket myBTsocket = myServerSocket.accept();
                    Log.d(TAG, " ** Socket accept for uuid " + i + " ** ");
                    try {
                        // Close the socket now that the
                        // connection has been made.
                        myServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, " ** IOException when trying to close serverSocket ** ");
                    }

                    if (myBTsocket != null) {
                        String address = myBTsocket.getRemoteDevice().getAddress();

                        mBtSockets.put(address, myBTsocket);
                        mBtDeviceAddresses.add(address);

                        //Thread mBtConnectionThread = new Thread(new BluetoohConnection(myBTsocket));
                        Thread mBtConnectionThread = new BluetoohConnection(myBTsocket);
                        mBtConnectionThread.start();

                        Log.i(TAG," ** Adding " + address + " in mBtDeviceAddresses ** ");
                        mBtConnectionThreads.put(address, mBtConnectionThread);
                    } else {
                        Log.e(TAG, " ** Can't establish connection ** ");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, " ** IOException in ConnectionService:ConnectionProvider ** ", e);
            }
        }
    }

    private class BluetoohConnection extends Thread {
        private String senderAddress;

        private final InputStream mmInStream;

        public BluetoohConnection(BluetoothSocket btSocket) {

            InputStream tmpIn = null;

            try {
                tmpIn = new DataInputStream(btSocket.getInputStream());
            } catch (IOException e) {
                Log.e(TAG, " ** IOException on create InputStream object ** ", e);
            }
            mmInStream = tmpIn;
        }
        @Override
        public void run() {
            //byte[] buffer = new byte[1];
            String message = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(mmInStream));
            while (true) {
                try {
                    Log.d(TAG, "run: ");
                    String line = br.readLine();
                    if(line.length() > 0){
                        onReceive(line);
                    }
                    Log.d(TAG, "run: msg" + line);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param receiveMessage
     */
    private void onReceive(String receiveMessage) {
        Log.d(TAG, "onReceive: ");
        if (receiveMessage != null && receiveMessage.length() > 0) {
            Log.i(TAG, " $$$$ " + receiveMessage + " $$$$ ");
            Bundle bundle = new Bundle();
            bundle.putString("msg", receiveMessage);
            Message message = new Message();
            message.what = 1;
            message.setData(bundle);
            message.obj = receiveMessage.substring(0,4);
            handler.sendMessage(message);
        }
    }

    /**
     * @param device
     * @param uuidToTry
     * @return
     */
    private BluetoothSocket getConnectedSocket(BluetoothDevice device, UUID uuidToTry) {
        BluetoothSocket myBtSocket;
        try {
            myBtSocket = device.createRfcommSocketToServiceRecord(uuidToTry);
            myBtSocket.connect();
            return myBtSocket;
        } catch (IOException e) {
            Log.e(TAG, "IOException in getConnectedSocket", e);
        }
        return null;
    }

    private void connect(BluetoothDevice device) {
        BluetoothSocket myBtSocket = null;
        String address = device.getAddress();
        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(address);
        bluetoothDevices.add(remoteDevice);
        // Try to get connection through all uuids available
        for (int i = 0; i < mUuidList.size() && myBtSocket == null; i++) {
            // Try to get the socket 2 times for each uuid of the list
            for (int j = 0; j < 2 && myBtSocket == null; j++) {
                Log.d(TAG, " ** Trying connection..." + j + " with " + device.getName() + ", uuid " + i + "...** ");
                myBtSocket = getConnectedSocket(remoteDevice, mUuidList.get(i));
                if (myBtSocket == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException in connect", e);
                    }
                }
            }
        }
        if (myBtSocket == null) {
            Log.e(TAG, " ** Could not connect ** ");
            return;
        }
        Log.d(TAG, " ** Connection established with " + device.getName() +"! ** ");
        mBtSockets.put(address, myBtSocket);
        mBtDeviceAddresses.add(address);
        Thread mBluetoohConnectionThread = new Thread(new BluetoohConnection(myBtSocket));
        mBluetoohConnectionThread.start();
        mBtConnectionThreads.put(address, mBluetoohConnectionThread);

    }

    public void bluetoothBroadcastMessage(String message) {
        for (int i = 0; i < mBtDeviceAddresses.size(); i++) {
            sendMessage(mBtDeviceAddresses.get(i), message);
        }
    }

    public void sendRSSIS(){
        Intent intent = new Intent(context,Piconet.class);
        String msg = "rssi";
        for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
            msg += bluetoothDevice.getAddress().toString() + "_" +
                    intent.getShortExtra(bluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE) + ",";
        }
        sendMessage(SharedData.instance().getServer().getAddress(),msg);
    }

    public static void sendMessage(String destination, String message) {

        BluetoothSocket myBsock = mBtSockets.get(destination);
        PrintWriter printWriter;
        if (myBsock != null) {
            try {
                OutputStream outStream = myBsock.getOutputStream();
                printWriter = new PrintWriter(new OutputStreamWriter(outStream), true);
                printWriter.println(message);
                printWriter.println("\r\n");
                Log.d(TAG, "sendMessage: "+message);
                byte[] msgBuffer = message.getBytes();
                //outStream.write(msgBuffer);
                final int pieceSize = 16;
                for (int i = 0; i < message.length(); i += pieceSize) {
                    byte[] send = message.substring(i,
                            Math.min(message.length(), i + pieceSize)).getBytes();
                    //outStream.write(send);

                }
                // we put at the end of message a character to sinalize that message
                // was finished
                byte[] terminateFlag = new byte[1];
                terminateFlag[0] = 0; // ascii table value NULL (code 0)
                //outStream.write(new byte[1]);
            } catch (IOException e) {
                Log.d(TAG, "line 278", e);
            }
        }
    }

    public boolean isPaired(String address) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device != null) {
                    if(device.getAddress().matches(address)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void pair(BluetoothDevice device){
        try {
            device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getIsServerRunning(){
        if(mBtSockets.size() > 0){
            return true;
        }
        return false;
    }

}
