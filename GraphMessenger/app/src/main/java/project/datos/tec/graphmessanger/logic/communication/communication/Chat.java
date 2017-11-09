package project.datos.tec.graphmessanger.logic.communication.communication;

import java.util.ArrayList;

/**
 * Created by josea on 11/27/2016.
 */

public class Chat {

    private String chatID = null;
    private ArrayList<Message> messages = null;
    private String friendMAC = null;
    private String friendName = null;
    private boolean conected = false;
    private String shortestPath = null;

    public Chat(String chatID) {
        this.chatID = chatID;
        this.messages =  new ArrayList<>();
        this.friendMAC = chatID.split("-")[1];
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID){
        this.chatID = chatID;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addMessage (Message message){
        this.messages.add(message);
    }

    public String getFriendMAC() {
        return friendMAC;
    }

    public void setFriendMAC(String friendMAC) {
        this.friendMAC = friendMAC;
    }

    public boolean isConected() {
        return conected;
    }

    public void setConected(boolean conected) {
        this.conected = conected;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(String shortestPath) {
        this.shortestPath = shortestPath;
    }
}
