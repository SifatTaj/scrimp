package com.scrimpapp.scrimp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.scrimpapp.scrimp.model.ChatInfo;
import com.scrimpapp.scrimp.util.ChatBubbleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {

    EditText etChatBox;
    ListView lvChats;
    ImageButton btSend;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    String chatRoomId;
    String senderName;
    int i = 1;
    String userId;
    DocumentReference chatDoc;

    ArrayList<ChatInfo> chatInfos;
    List<String> membersId;
    ChatBubbleAdapter chatBubbleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etChatBox = findViewById(R.id.etChat);
        lvChats = findViewById(R.id.lvChat);
        btSend = findViewById(R.id.btSendChat);


        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        chatInfos = new ArrayList<>();

        membersId = HomeActivity.matchesList;

        senderName = HomeActivity.userDisplayName;

        chatBubbleAdapter = new ChatBubbleAdapter(this, chatInfos);
        lvChats.setAdapter(chatBubbleAdapter);

        subscribeToChatRoom();

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> sendChatInfo = new HashMap<String, Object>();
                sendChatInfo.put("name", senderName);
                sendChatInfo.put("user_id", userId);
                sendChatInfo.put("msg", etChatBox.getText().toString());

                Map<String, Object> send = new HashMap<String, Object>();
                send.put("" + i, sendChatInfo);

                chatDoc.set(send, SetOptions.merge());

//                chatInfos.add(new ChatInfo(etChatBox.getText().toString(), "", "abu", true));
//                chatBubbleAdapter.notifyDataSetChanged();
                etChatBox.setText("");
            }
        });
    }

    protected void subscribeToChatRoom() {
        DocumentReference userInfo = firestore.collection("users").document(userId);

        userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    chatRoomId = task.getResult().getString("chat_room");
                    if (!chatRoomId.equalsIgnoreCase("")) {
                        chatDoc = firestore.collection("chat").document(chatRoomId);
                        chatDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    for (; i <= documentSnapshot.getData().size(); ++i) {
                                        String msg = documentSnapshot.getString(i + ".msg");
                                        String user = documentSnapshot.getString(i + ".user_id");
                                        String name = documentSnapshot.getString(i + ".name");
                                        chatInfos.add(new ChatInfo(msg, user, name, user.equalsIgnoreCase(userId)));
                                        chatBubbleAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }
                    else {
                        Map<String, Object> empty = new HashMap<>();
                        firestore.collection("chat").document(userId).set(empty);
                        chatDoc = firestore.collection("chat").document(userId);

                        for(String member : membersId) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("chat_room", userId);
                            firestore.collection("users").document(member).set(data, SetOptions.merge());
                        }

                        chatDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    for (; i <= documentSnapshot.getData().size(); ++i) {
                                        Log.e("data", "" + documentSnapshot.getData());
                                        String msg = documentSnapshot.getString(i + ".msg");
                                        String user = documentSnapshot.getString(i + ".user_id");
                                        String name = documentSnapshot.getString(i + ".name");
                                        chatInfos.add(new ChatInfo(msg, "", name, user.equalsIgnoreCase(userId)));
                                        chatBubbleAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

    }
}
