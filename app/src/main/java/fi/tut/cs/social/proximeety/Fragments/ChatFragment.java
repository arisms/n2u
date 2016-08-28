package fi.tut.cs.social.proximeety.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import fi.tut.cs.social.proximeety.Adapters.MessageAdapter;
import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Connection;
import fi.tut.cs.social.proximeety.classes.HTTPRequest;
import fi.tut.cs.social.proximeety.classes.Message;
import fi.tut.cs.social.proximeety.classes.N2U_Application;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    public final String SOCKET_SERVER_URL = "http://social.cs.tut.fi:10002";
//    public final String SOCKET_SERVER_URL = "http:///192.168.1.2:3001";

    private Button sendBN;
    private TextView inputTV;
    private TextView usernameTV;
    private MainActivity mainActivity;
    private RecyclerView mMessagesView;
    private ImageButton backBN;

    View view;
    private Socket mSocket;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean initialized = false;

    public Connection mConnection;
    public String connectionUsername;
    public String connectionId;



    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        mainActivity = (MainActivity) getActivity();

        mAdapter = new MessageAdapter((MainActivity) getActivity(), mMessages);

        mSocket = ((N2U_Application) mainActivity.getApplication()).getSocket();

        if(!mSocket.connected()) {
            Log.d(TAG, "Socket.connect - in ChatFragment");
            mSocket.connect();
        }
        mSocket.on("chat message", onNewMessage);

        inputTV = (TextView) view.findViewById(R.id.inputTV);
        inputTV.setTypeface(mainActivity.futura);

        sendBN = (Button) view.findViewById(R.id.sendBN);
        sendBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(inputTV.getText().toString());
            }
        });
        sendBN.setTypeface(mainActivity.futura);

        backBN = (ImageButton) view.findViewById(R.id.backBN);
        backBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mainActivity.onConnectionsButton();
                if(mainActivity.checkMessages(connectionId)) {
                    mainActivity.mNewMessages.remove(connectionId);
                    mainActivity.updateNewMessages();
                }
                goBack();
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check for new messages
        if(mainActivity.checkMessages(connectionId)) {
            mainActivity.mNewMessages.remove(connectionId);
            mainActivity.updateNewMessages();
        }

        usernameTV = (TextView) view.findViewById(R.id.usernameTV);
        usernameTV.setTypeface(mainActivity.futura);
        usernameTV.setText("Chat with " + connectionUsername);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mMessages.clear();
        mAdapter.notifyDataSetChanged();

        // Get conversation from the server
        String message = "ChatFragment - GetConversation";
        String route = "/messages/conversation/senderId=" + mainActivity.myProfile.getId()
                + "&recipientId=" + connectionId;

        HTTPRequest request = new HTTPRequest(message,
                mainActivity.HOST + mainActivity.PORT + route,
                Request.Method.GET,
                null
        );
        mainActivity.mHTTPHelper.sendToServerArray(request);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        mSocket.off("chat message", onNewMessage);
    }

    public void goBack() {
        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        mainActivity.connectionsGridItemOnClick(mConnection, connectionUsername, connectionId);
    }

    public void addMyMessage(String username, String message, Date timeSent) {
        mMessages.add(new Message.Builder(Message.MY_MESSAGE)
                .username(username).message(message).timeSent(timeSent).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    public void addOtherMessage(String username, String message, Date timeSent) {
        mMessages.add(new Message.Builder(Message.OTHER_MESSAGE)
                .username(username).message(message).timeSent(timeSent).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void sendMessage(String message) {
        addMyMessage(mainActivity.myProfile.getUsername(), message, mainActivity.stringToDate(mainActivity.getCurrentDate()));

        // Send message to the socket server
        HashMap<String, String> msg = new HashMap<String, String>();
        msg.put("senderId", mainActivity.myProfile.getId());
        msg.put("senderUsername", mainActivity.myProfile.getUsername());
        msg.put("recipientId", connectionId);
        msg.put("recipientUsername", connectionUsername);
        msg.put("text", message);
        JSONObject msgJSON = new JSONObject(msg);
        mSocket.emit("chat message", msgJSON);

        inputTV.setText("");

        // Store message to the database on the server
        String tag = "ChatFragment - SendMessage";
        String route = "/messages";
        int method = Request.Method.POST;

        HashMap<String, String> params = new HashMap<String, String>();
        HTTPRequest request;

        params.put("_senderId", mainActivity.myProfile.getId());
        params.put("_recipientId", connectionId);
        params.put("messageText", message);
        params.put("timeSent", mainActivity.getCurrentDate());
        JSONObject req = new JSONObject(params);

        request = new HTTPRequest(tag,
                                  mainActivity.HOST + mainActivity.PORT + route,
                                  method,
                                  req);

        mainActivity.mHTTPHelper.sendToServer(request);

        mainActivity.getTotalMessages();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onConnectError");
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject msgJSON = (JSONObject) args[0];
            Log.d(TAG, "Received message from server: " + msgJSON);

            try {
                if (msgJSON.getString("recipientId").equals(mainActivity.myProfile.getId()))
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                addOtherMessage(msgJSON.getString("senderUsername"), msgJSON.getString("text"),
                                        mainActivity.stringToDate(msgJSON.getString("timeSent")));
                                mainActivity.getTotalMessages();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };
}
