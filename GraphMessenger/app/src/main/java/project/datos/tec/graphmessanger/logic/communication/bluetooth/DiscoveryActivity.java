package project.datos.tec.graphmessanger.logic.communication.bluetooth;

import android.Manifest;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import project.datos.tec.graphmessanger.R;
import project.datos.tec.graphmessanger.gui.custom.listview.CustomListView;
import project.datos.tec.graphmessanger.gui.messaging.GraphMessengerActivity;
import project.datos.tec.graphmessanger.logic.communication.communication.Chat;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;

public class DiscoveryActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter;
    private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<>();
    private String TAG = "BS";

    private CustomListView mainListView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> orderList = new ArrayList<String>();
    private Context context;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        this.context = this;

        mainListView = (CustomListView) findViewById(R.id.mainListView);
        listAdapter = new ArrayAdapter<String>(this, R.layout.rowlayout, orderList);

        //Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(ActionFoundReceiver, filter); // Don't forget to unregister during onDestroy

        // Getting the Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckBTState();

        //DELETE ORDER ON LONG CLICK
        mainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {

                Toast.makeText(context,btDeviceList.get(pos).getName(),Toast.LENGTH_SHORT).show();
                SharedData.instance().addChat(new Chat(SharedData.instance().getUserMAC()+"-"+btDeviceList.get(pos).getAddress()));
                Piconet.instance().sendMessage(SharedData.instance().getServer().getAddress(),"aval"+btDeviceList.get(pos).getAddress());

                if(isAval(btDeviceList.get(pos))){
                    Log.d("piconet", "pene");
                    SharedData.instance().addBluetoothDevice(btDeviceList.get(pos));
                    Piconet.instance().addDevice(btDeviceList.get(pos));
                }else{
                    Log.d("piconet", "vagina");
                    SharedData.instance().removeChat(SharedData.instance().getUserID()+"-"+btDeviceList.get(pos).getAddress());
                    Toast.makeText(context,btDeviceList.get(pos).getName()+" is not connected.",Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(context, GraphMessengerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        });

        //SET ADAPTER
        mainListView.setAdapter(listAdapter);
    }

    /* This routine is called when an activity completes.*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == REQUEST_ENABLE_BT) {
            CheckBTState();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }
        unregisterReceiver(ActionFoundReceiver);
    }

    private void CheckBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // If it isn't request to turn it on
        // List paired devices
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            Toast.makeText(context,"Bluetooth NOT supported. Aborting.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (btAdapter.isEnabled()) {
                Toast.makeText(context,"Bluetooth is enabled...",Toast.LENGTH_SHORT).show();

                // Starting the device discovery
                //checkBTPermissions();
                btAdapter.startDiscovery();
            } else {
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(context, device.getName(),Toast.LENGTH_SHORT).show();
                if(!existsChat(device) &&
                        !SharedData.instance().getServer().getAddress().equals(device.getAddress())){
                    Log.d(TAG, "device address: "+device.getAddress()+"  server address: "+SharedData.instance().getServer().getAddress());
                    orderList.add("Device: " + device.getName() + ", " + device);
                    listAdapter.notifyDataSetChanged();
                    btDeviceList.add(device);
                }
            } else {
                if(BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    for (int i=0; i<uuidExtra.length; i++) {
                    }
                } else {
                    if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        Toast.makeText(context,"Discovery Started.", Toast.LENGTH_SHORT).show();
                    } else {
                        if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                            Toast.makeText(context,"Discovery Finished.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    };

    private boolean existsChat(BluetoothDevice device){
        for(Chat chat :  SharedData.instance().getChats()){
            if(chat.getFriendMAC().equals(device.getAddress())){
               return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(context, GraphMessengerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    boolean flag = false;
    private boolean isAval(final BluetoothDevice device){
        Thread thread = new Thread(){
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while(System.currentTimeMillis() - start < 10000){
                    for(Chat chat : SharedData.instance().getChats()){
                        if(chat.getFriendMAC().equals(device.getAddress())){
                            if(chat.isConected()){
                                flag = true;
                                try {
                                    this.finalize();
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        };
        thread.start();

        return flag;
    }
/**
    public void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }*/

}
