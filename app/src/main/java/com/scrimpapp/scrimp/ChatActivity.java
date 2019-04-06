package com.scrimpapp.scrimp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.scrimpapp.scrimp.model.ChatInfo;
import com.scrimpapp.scrimp.util.ChatBubbleAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    EditText etChatBox;
    ListView lvChats;
    ImageButton btSend;

    ArrayList<ChatInfo> chatInfos;
    ChatBubbleAdapter chatBubbleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etChatBox = findViewById(R.id.etChat);
        lvChats = findViewById(R.id.lvChat);
        btSend = findViewById(R.id.btSendChat);

        chatInfos = new ArrayList<>();

        chatInfos.add(new ChatInfo("hello", "", "abu", false));
        chatInfos.add(new ChatInfo("how are you", "", "abu", false));
        chatInfos.add(new ChatInfo("kothay", "", "abu", false));

        chatBubbleAdapter = new ChatBubbleAdapter(this, chatInfos);
        lvChats.setAdapter(chatBubbleAdapter);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatInfos.add(new ChatInfo(etChatBox.getText().toString(), "", "abu", true));
                chatBubbleAdapter.notifyDataSetChanged();
                etChatBox.setText("");
            }
        });
    }
}
