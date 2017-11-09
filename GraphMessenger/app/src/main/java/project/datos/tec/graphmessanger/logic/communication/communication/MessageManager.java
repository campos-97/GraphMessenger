package project.datos.tec.graphmessanger.logic.communication.communication;

import android.content.Intent;
import android.util.Log;
import android.widget.Switch;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import project.datos.tec.graphmessanger.gui.messaging.GraphMessengerActivity;
import project.datos.tec.graphmessanger.logic.communication.bluetooth.Piconet;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;

/**
 * Created by josea on 11/27/2016.
 */

public class MessageManager {

    public static void processMsg(String msg){
        Gson gson = new Gson();
        Message message= gson.fromJson(msg.substring(4),Message.class);

        String[] route = message.getRoute().split("-");
        if(message.getKind().contains("ack")) {
            if (route[0].equals(SharedData.instance().getUserMAC())) {
                newMessage(message);
                message.setKind(message.getKind()+"-copy");
                String copyMsg = gson.toJson(message);
                Piconet.sendMessage(SharedData.instance().getServer().getAddress(),copyMsg);
            } else {
                String nexAddress = route[getIndexFor(route, SharedData.instance().getUserMAC())-1];
                Piconet.instance().sendMessage(nexAddress, msg);
            }
        }else{
            if (route[route.length - 1].equals(SharedData.instance().getUserMAC()) ||
                    message.getConversationID().split("-")[0].equals(SharedData.instance().getUserID())) {
                newMessage(message);

                Calendar c = Calendar.getInstance();
                long currentTime = c.getTimeInMillis();
                long lastTime = Long.valueOf(message.getTime());

                message.setKind(message.getKind()+"-"+"ack");
                message.setTime(String.valueOf(currentTime-lastTime));
                String ackMsg = gson.toJson(message);
                Piconet.sendMessage(message.getRoute().split("-")[0], ackMsg);

            } else {
                String nexAddress = route[getIndexFor(route, SharedData.instance().getUserMAC())+1];
                Piconet.instance().sendMessage(nexAddress, msg);
            }
        }

    }

    private static void newMessage(Message message){
        Log.d("piconet", "newMessage: ");
        try{
            SharedData.instance().getChat(message.getConversationID()).addMessage(message);
        }catch (NullPointerException e){
            Chat newChat = new Chat(message.getConversationID());
            newChat.setConected(true);
            newChat.setFriendName(message.getFrom());
            newChat.setFriendMAC(message.getRoute().split("-")[0]);
            SharedData.instance().addChat(newChat);
        }
    }

    public static void processMsg(String type, String msg){
        switch (type){
            case "rssi":{
                Piconet.instance().sendRSSIS();
                break;
            }
            case "dsnn":{
                LoginManager.getInstance().logOut();
                SharedData.instance().resetData();
                System.exit(0);
                break;
            }
            case "aval":{
                Log.d("piconet", "processMsg: "+msg.substring(4));
                String response = msg.substring(4);
                String[] responseArray = response.split("-");
                if(responseArray[1].contains("yes")){
                    SharedData.instance().getChat(SharedData.instance().getUserMAC()+"-"+
                    responseArray[0]).setConected(true);
                    SharedData.instance().getChat(SharedData.instance().getUserMAC()+"-"+
                    responseArray[0]).setFriendName(responseArray[2]);
                    SharedData.instance().getChat(SharedData.instance().getUserMAC()+"-"+
                            responseArray[0]).setChatID(SharedData.instance().getUserID()+"-"+responseArray[3]);
                }
                break;
            }
            case "info":{

                break;
            }
            case "shro":{
                String[] addr = msg.substring(4).split("-");
                for(Chat chat : SharedData.instance().getChats()){
                    if(addr[addr.length-1].equals(chat.getFriendMAC())){
                        chat.setShortestPath(msg.substring(4));
                    }
                }
                break;
            }
            case "test":{
                Log.d("piconet", "processMsg: TEST");
                break;
            }
        }
    }

    private static int getIndexFor(String[] strings, String target){
        for(int i = 0 ; i < strings.length ; i++){
            if(strings[i].equals(target)){
                return i;
            }
        }
        return  -1;
    }
}
