package com.facebook.peepingtom.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.peepingtom.Adapters.MessageRecyclerAdapter;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.ChatDescription;
import com.facebook.peepingtom.Models.Message;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;

public class MessageActivity extends AppCompatActivity implements DatabaseLayer.Messages {
    RecyclerView rvMessages;
    MessageRecyclerAdapter messageAdapter;
    ArrayList<Message> messageList;
    private EditText etNewMessage;
    private Button btSend;
    User mainUser;
    User otherUser;
    ChatDescription description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_message);
        otherUser = Parcels.unwrap(getIntent().getParcelableExtra("otherUser"));
        getSupportActionBar().setTitle(otherUser.getFirstName());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DAD4D4D4")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D97E7E7E")));
        messageAdapter = new MessageRecyclerAdapter(this, otherUser);
        rvMessages = (RecyclerView) findViewById(R.id.rvMessages);
        etNewMessage = (EditText) findViewById(R.id.etNewMessage);
        btSend = (Button) findViewById(R.id.btSend);
        mainUser = ((GlobalVars)getApplication()).getUser();
        description = Parcels.unwrap(getIntent().getParcelableExtra("description"));
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etNewMessage.getText().toString().trim().isEmpty()) return;
                Message message = new Message(mainUser.getUid(), etNewMessage.getText().toString(), null, new Date());
                description.setLastMessage(message);
                if (description.getChatId() == null || description.getChatId().isEmpty())
                    DatabaseLayer.submitNewMessage(mainUser.getUid(), otherUser.getUid(), message,
                                                                    description, MessageActivity.this);
                else DatabaseLayer.submitMessage(message, description, MessageActivity.this);
                //TODO notify description of new message in return intent
                etNewMessage.setText("");
            }
        });
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        rvMessages.setLayoutManager(llm);
        rvMessages.setAdapter(messageAdapter);
        messageList = new ArrayList<>();
        messageAdapter.messageList = messageList;
        if (description.getChatId() != null && !description.getChatId().isEmpty())
            DatabaseLayer.getMessages(description.getChatId(), this);
        rvMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            //scroll to bottom when keyboard pops up
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvMessages.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }, 100);
                }
            }
        });
    }

    @Override
    public void onMessageFetched(Message message) {
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.postDelayed(new Runnable() {
            @Override
            public void run() {
                rvMessages.scrollToPosition(messageList.size() - 1);
            }
        }, 100);
    }

    @Override
    public void onBackPressed()
    {
        Intent resultData = new Intent();
        resultData.putExtra("description", Parcels.wrap(description));
        setResult(MainActivity.RESULT_OK, resultData);
        finish();
    }
}
