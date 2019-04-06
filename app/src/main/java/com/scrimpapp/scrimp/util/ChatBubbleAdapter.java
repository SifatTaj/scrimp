package com.scrimpapp.scrimp.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.scrimpapp.scrimp.R;
import com.scrimpapp.scrimp.model.ChatInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChatBubbleAdapter extends ArrayAdapter {


    private final Activity context;

    private final ArrayList<ChatInfo> chatInfos;

    public ChatBubbleAdapter(Activity context, ArrayList<ChatInfo> chatInfos){

        super(context, R.layout.chat_bubble , chatInfos);
        this.context=context;
        this.chatInfos = chatInfos;
    }

    public View getView(int position, View view, ViewGroup parent) {
        ChatInfo chatInfo = chatInfos.get(position);
        if(!chatInfo.sender) {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.chat_bubble, null,true);

            TextView name = rowView.findViewById(R.id.tvBubbleName);
            TextView msg = rowView.findViewById(R.id.tvBubbleMsg);

            name.setText(chatInfo.name);
            msg.setText(chatInfo.msg);

            return rowView;
        }
        else {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.chat_bubble_sender, null,true);

            TextView msg = rowView.findViewById(R.id.tvSenderBubbleMsg);

            msg.setText(chatInfo.msg);

            return rowView;
        }

    }
}
