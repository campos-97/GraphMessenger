package project.datos.tec.graphmessanger.gui.custom.listview;

/**
 * Created by josea on 11/22/2016.
 */


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import project.datos.tec.graphmessanger.R;
import project.datos.tec.graphmessanger.logic.communication.communication.Message;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;

public class MsgsArrayAdapter extends ArrayAdapter<Message> {

    private TextView chatText;
    private List<Message> chatMessageList = new ArrayList<>();
    private Context context;

    @Override
    public void add(Message message) {
        chatMessageList.add(message);
        super.add(message);
    }

    public MsgsArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public Message getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Message chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (!chatMessageObj.getUserID().equals(SharedData.instance().getUserID())) {
            row = inflater.inflate(R.layout.msg_left, parent, false);
        } else {
            row = inflater.inflate(R.layout.msg_right, parent, false);
        }
        chatText = (TextView) row.findViewById(R.id.textView);
        chatText.setText(chatMessageObj.getText());
        return row;
    }
}
