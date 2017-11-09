package project.datos.tec.graphmessanger.logic.communication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * Created by josea on 11/27/2016.
 */

public class ServerRequest extends AsyncTask<String, String , String> {

    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;


    @Override
    protected String doInBackground(String... strings) {

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(strings[0]);
        PrintWriter printWriter;
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            btSocket.connect();
            OutputStream outStream = btSocket.getOutputStream();
            printWriter = new PrintWriter(new OutputStreamWriter(outStream), true);
            printWriter.println(" "+strings[1]);
            printWriter.println("\r\n");

            while (true){
                if(btSocket.getInputStream() != null){
                    InputStream tmpIn = new DataInputStream(btSocket.getInputStream());
                    BufferedReader br = null;
                    StringBuilder sb = new StringBuilder();
                    String line;
                    try {

                        br = new BufferedReader(new InputStreamReader(tmpIn));
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return sb.toString();
                }
            }
        } catch (IOException e) {
        }

        return null;
    }

    @Override
    protected void onPostExecute(String string){
        super.onPostExecute(string);
    }
}
