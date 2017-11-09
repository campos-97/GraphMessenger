package project.datos.tec.graphmessanger.logic.communication.communication;

/**
 * Created by josea on 11/27/2016.
 */

public class Message {

    private String text;
    private String date;
    private String userID;
    private String conversationID;
    private String from;
    private String to;
    private String time;
    private String route;
    private String kind;

    private Message(){

    }
    public Message(String text){
        this.text = text;
    }
    public Message(String text, String date, String conversationID, String time, String to, String from, String userID , String kind) {
        this();
        this.text = text;
        this.date = date;
        this.conversationID = conversationID;
        this.time = time;
        this.to = to;
        this.from = from;
        this.kind = kind;
        this.userID = userID;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getConversationID() {
        return conversationID;
    }
    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }

    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }
}
