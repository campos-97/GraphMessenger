package project.datos.tec.graphmessanger.gui.messaging;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import project.datos.tec.graphmessanger.R;
import project.datos.tec.graphmessanger.logic.communication.bluetooth.Piconet;
import project.datos.tec.graphmessanger.logic.communication.communication.Chat;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;
    private View view;

    private ScrollView scrollView;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view =  inflater.inflate(R.layout.fragment_contacts, container, false);

        scrollView = (ScrollView)view.findViewById(R.id.scroll_view);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        // Find the ListView resource.
        mainListView = (ListView) view.findViewById(R.id.mainListView);

        // Create and a List of orders names.
        final String[] orders = new String[]{};
        final ArrayList<String> orderList = new ArrayList<String>();
        orderList.addAll(Arrays.asList(orders));

        // Create ArrayAdapter using the FinalOrder list.
        listAdapter = new ArrayAdapter<String>(getContext(), R.layout.contact_row, R.id.firstLine, orderList);

        for(Chat chat : SharedData.instance().getChats()){
            if(chat.isConected()){
                listAdapter.add(chat.getFriendName());
            }
        }

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                Intent intent = new Intent(getContext(), MessagingActivity.class);
                intent.putExtra("Device-ID",String.valueOf(pos));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //SET ADAPTER
        mainListView.setAdapter(listAdapter);


        final Handler handler = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                if(msg.arg1 == 1){
                    int count = listAdapter.getCount();
                    for(int i = count ; i < SharedData.instance().getChats().size() ; i++){
                        listAdapter.add(SharedData.instance().getChats().get(i).getFriendName());
                        listAdapter.notifyDataSetChanged();
                    }
                }
                super.handleMessage(msg);
            }
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while(System.currentTimeMillis() - start < 2000){
                    updateChats(handler);
                }
            }
        };
        thread.start();

        return this.view;
    }

    private synchronized void updateChats(Handler handler) {
        if(SharedData.instance().getChats().size() > listAdapter.getCount()){
            android.os.Message msg = handler.obtainMessage();
            msg.arg1 = 1;
            handler.sendMessage(msg);
        }
    }
}
