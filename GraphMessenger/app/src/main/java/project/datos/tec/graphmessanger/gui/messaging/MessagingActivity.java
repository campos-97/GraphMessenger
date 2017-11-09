package project.datos.tec.graphmessanger.gui.messaging;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import project.datos.tec.graphmessanger.R;
import project.datos.tec.graphmessanger.gui.custom.listview.MsgsArrayAdapter;
import project.datos.tec.graphmessanger.logic.communication.bluetooth.Piconet;
import project.datos.tec.graphmessanger.logic.communication.communication.Chat;
import project.datos.tec.graphmessanger.logic.communication.communication.Message;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;

public class MessagingActivity extends AppCompatActivity {

    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;
    private MsgsArrayAdapter adapter;
    private View view;

    private Button mButton;
    private EditText mEdit;
    private ScrollView scrollView;
    private TextView friendName;

    private Context context;

    private int deviceID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        this.context = this;

        this.deviceID = Integer.valueOf(getIntent().getStringExtra("Device-ID"));

        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.mainListView);

        friendName = (TextView)findViewById(R.id.friend_name);
        friendName.setText(SharedData.instance().getChats().get(deviceID).getFriendName());

        // Create and a List of orders names.
        final String[] orders = new String[]{};
        final ArrayList<String> orderList = new ArrayList<String>();
        orderList.addAll(Arrays.asList(orders));

        // Create ArrayAdapter using the FinalOrder list.
        listAdapter = new ArrayAdapter<String>(context, R.layout.msg_right, R.id.textView, orderList);
        adapter = new MsgsArrayAdapter(getApplicationContext(),R.layout.msg_row);

        // Add more Orders.
        Message msg =  new Message("hola");
        msg.setUserID("ddddddd");
        adapter.add(msg);
        Log.d("piconet", "onCreate: chat"+SharedData.instance().getChats().get(deviceID).getFriendName());
        for(Message message : SharedData.instance().getChats().get(deviceID).getMessages()){
            adapter.add(message);
        }

        //DELETE ORDER ON LONG CLICK
        mainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                return true;
            }
        });

        //SET ADAPTER
        mainListView.setAdapter(adapter);
        mainListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mainListView.setAdapter(adapter);
        //to scroll the list view to bottom on data change
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mainListView.setSelection(adapter.getCount() - 1);
            }
        });

        mButton = (Button) findViewById(R.id.send_btn);
        mEdit   = (EditText)findViewById(R.id.input_text);

        mButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        if(!mEdit.getText().toString().matches("")) {

                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String strDate = sdf.format(c.getTime());
                            String[] time = strDate.split(" ");
                            String currentTime = String.valueOf(c.getTimeInMillis());

                            Message msg = new Message(mEdit.getText().toString(), time[0],
                                    SharedData.instance().getChats().get(deviceID).getChatID(),
                                    currentTime,SharedData.instance().getChats().get(deviceID).getFriendName(),
                                    SharedData.instance().getUserName(),
                                    SharedData.instance().getUserName(),"msgg");

                            Piconet.instance().sendMessage(SharedData.instance().getServer().getAddress(),
                                    "rssi"+SharedData.instance().getBluetoothDevices().get(deviceID).getAddress());

                            if(hasPath(SharedData.instance().getBluetoothDevices().get(deviceID))) {

                                msg.setRoute(SharedData.instance().getChats().get(deviceID).getChatID());

                                Gson gson = new Gson();
                                String jmsg = gson.toJson(msg);

                                Piconet.instance().sendMessage(SharedData.instance().getBluetoothDevices()
                                        .get(deviceID).getAddress(), "data" + jmsg);
                                adapter.add(msg);
                                adapter.notifyDataSetChanged();
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                            mEdit.setText("");
                        }
                    }
                });

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                if(msg.arg1 == 1){
                    int count = adapter.getCount();
                    for(int i = count ; i < SharedData.instance().getChats().get(deviceID).getMessages().size() ; i++){
                        adapter.add(SharedData.instance().getChats().get(deviceID).getMessages().get(i));
                    }
                }
                super.handleMessage(msg);
            }
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while(System.currentTimeMillis() - start < 10000){
                    updateMsgs(handler);
                }
            }
        };
        thread.start();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(context, GraphMessengerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    boolean flag = false;
    private boolean hasPath(final BluetoothDevice device){
        Thread thread = new Thread(){
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while(System.currentTimeMillis() - start < 10000){
                    for(Chat chat : SharedData.instance().getChats()){
                        if(chat.getFriendMAC().equals(device.getAddress())){
                            if(chat.getShortestPath() != null){
                                flag = true;
                            }
                        }
                    }
                }
            }
        };
        thread.start();

        return flag;
    }

    private synchronized void updateMsgs(Handler handler) {
        if(SharedData.instance().getChats().get(deviceID).getMessages().size() > adapter.getCount()){
            android.os.Message msg = handler.obtainMessage();
            msg.arg1 = 1;
            handler.sendMessage(msg);
        }
    }
}
