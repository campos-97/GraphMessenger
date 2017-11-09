package project.datos.tec.graphmessanger.logic.datamanagement;

import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import project.datos.tec.graphmessanger.logic.communication.communication.Chat;

/**
 * Created by josea on 11/20/2016.
 */

public class SharedData {

    private static SharedData instance = null;

    public void SharedData(){

    }
    public static SharedData instance(){
        if(instance == null){
            instance = new SharedData();
        }
        return instance;
    }

    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private BluetoothDevice server =  null;
    private ArrayList<Chat> chats = new ArrayList<>();

    private String userName = null;
    private String userID = null;
    private Drawable userImg = null;
    private String userMAC = null;

    public ArrayList<BluetoothDevice> getBluetoothDevices() {
        return bluetoothDevices;
    }

    public void addBluetoothDevice(BluetoothDevice device){
        bluetoothDevices.add(device);
    }

    public BluetoothDevice getServer() {
        return server;
    }

    public void setServer(BluetoothDevice server) {
        this.server = server;
    }

    public ArrayList<Chat> getChats() {
        return chats;
    }

    public void setChats(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    public void addChat(Chat chat){
        this.chats.add(chat);
    }

    public void removeChat(String id){
        for(Chat chat : this.chats){
            if(chat.getChatID().equals(id)){
                this.chats.remove(chat);
            }
        }
    }

    public Chat getChat(String chatID){
        for(Chat chat : this.chats){
            if(chat.getChatID().equals(chatID)){
                return chat;
            }
        }
        return null;
    }

    public String getUserMAC() {
        return userMAC;
    }

    public void setUserMAC(String userMAC) {
        this.userMAC = userMAC;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Drawable getUserImg() {
        return userImg;
    }

    public void setUserImg(Drawable userImg) {
        this.userImg = userImg;
    }

    public void resetData(){
        this.userName = null;
        this.userID = null;
        this.userID = null;
        this.bluetoothDevices = new ArrayList<>();
    }
}
