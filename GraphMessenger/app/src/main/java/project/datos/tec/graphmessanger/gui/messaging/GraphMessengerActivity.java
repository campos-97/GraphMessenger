package project.datos.tec.graphmessanger.gui.messaging;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;

import project.datos.tec.graphmessanger.R;
import project.datos.tec.graphmessanger.gui.custom.swipapleviews.CustomViewPager;
import project.datos.tec.graphmessanger.gui.custom.swipapleviews.ViewPagerAdapter;
import project.datos.tec.graphmessanger.gui.login.LogInActivity;
import project.datos.tec.graphmessanger.logic.communication.bluetooth.DiscoveryActivity;
import project.datos.tec.graphmessanger.logic.communication.bluetooth.Piconet;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;

import static android.R.attr.fragment;
import static android.R.attr.name;

public class GraphMessengerActivity extends AppCompatActivity {

    Toolbar toolbar;
    TabLayout tabLayout;
    CustomViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_messenger);
        this.context = this;

        //Sets Piconet context
        Piconet.instance(this);

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        if (AccessToken.getCurrentAccessToken() == null){
            goLoginScreen();
        }else {
            Profile profile = Profile.getCurrentProfile();
            SharedData.instance().setUserID(profile.getId());
            SharedData.instance().setUserName(profile.getName());
        }


        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        //To add new Fragments.
        viewPagerAdapter.addFragments(new ContactsFragment(),"Contacts");
        viewPagerAdapter.addFragments(new ContactsFragment(),"Contacts");
        viewPagerAdapter.addFragments(new ContactsFragment(),"Contacts");

        //Sets the view pager.
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setPagingEnabled(true);
        tabLayout.setupWithViewPager(viewPager);

        String macAddress = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
        SharedData.instance().setUserMAC(macAddress);
    }

    @Override
    public void onResume(){
        Log.d("piconet", "onResume: "+SharedData.instance().getUserName());
        super.onResume();
        if(SharedData.instance().getUserID() != null) {
            if(!Piconet.instance().getIsServerRunning()) {
                connectWithServer();
            }
        }
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.item_discovery :{
                        Intent intent = new Intent(context, DiscoveryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    }
                    case R.id.item_logout :{
                        LoginManager.getInstance().logOut();
                        Piconet.instance().sendMessage("08:D4:0C:29:E8:1F", "dcnn");
                        SharedData.instance().resetData();
                        Intent intent = new Intent(context, GraphMessengerActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    }
                    case R.id.item_user :{
                        fragment = new UserFragment();
                        replaceFragment(fragment);

                    }
                }
                Toast.makeText(getApplicationContext(),"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                return true;
            }
            });
        popup.inflate(R.menu.options_menu);
        popup.show();
    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_graph_messenger, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void connectWithServer(){
        Log.d("piconet", "connectWithServer: ");
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            Log.d("piconet", "connectWithServer: null");
            AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
        }else{
            if (btAdapter.isEnabled()) {
                Log.d("piconet", "connectWithServer: notnull");
                BluetoothDevice bluetoothDevice = btAdapter.getRemoteDevice("08:D4:0C:29:E8:1F");
                int rssi = getIntent().getShortExtra(bluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                Piconet.instance().addDevice(bluetoothDevice);
                Piconet.instance().sendMessage("08:D4:0C:29:E8:1F", SharedData.instance().getUserID()+"_"+SharedData.instance().getUserName()+
                        "_"+rssi+"_"+SharedData.instance().getUserMAC());
                SharedData.instance().setServer(bluetoothDevice);
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void AlertBox( String title, String message ){
        new AlertDialog.Builder(this)
                .setTitle( title )
                .setMessage( message + " Press OK to exit." )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).show();
    }
}
