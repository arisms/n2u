package fi.tut.cs.social.proximeety.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.woxthebox.draglistview.DragListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import fi.tut.cs.social.proximeety.Adapters.CluesViewGridOtherAdapter;
import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Clue;
import fi.tut.cs.social.proximeety.classes.Connection;
import fi.tut.cs.social.proximeety.classes.HTTPRequest;
import fi.tut.cs.social.proximeety.classes.N2U_Application;

public class OtherProfileGridFragment extends Fragment {
    static private final String TAG = "OtherProfileGridFr";

    static MainActivity mainActivity;
    private DragListView mDragListView;
    List<Clue> mClues;
    CluesViewGridOtherAdapter gridAdapter;
    Boolean initialized = false;
    ArrayList<Pair<Long, Clue>> mItemArray;
    public ProgressBar loadingPB;
    View view;
    public Vibrator vibrator;
    public Connection mConnection;
    public static String connectionUsername;
    public static String connectionId;

    private TextView usernameTV;
    private Button face2faceBN;
    private Button chatBN;
    private TextView face2faceTV;
    private ImageButton backBN;
    private ImageView newMessageIV;
    private ImageView avatarIV;

    //public final String SOCKET_SERVER_URL = "http://social.cs.tut.fi:10002";
    private Socket mSocket;
    AlertDialog.Builder builder;
    private String correctAnswer = "";
    private int answersCounter = 0;
    AlertDialog dialog = null;
    public boolean requestF2F = false;
    private boolean F2Fcomplete;

    public OtherProfileGridFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        mainActivity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.fragment_other_profile_view_grid, container, false);

        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        vibrator = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

        loadingPB = (ProgressBar) view.findViewById(R.id.loadingPB);
        loadingPB.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(mainActivity, R.color.N2U_grey), PorterDuff.Mode.SRC_IN);

        F2Fcomplete = false;

        /************************ Grid View ************************/

        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);

        mDragListView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {

            }
        });
        mDragListView.setDragEnabled(false);

        mItemArray = new ArrayList<>();

        mClues = new ArrayList<Clue>();

        mDragListView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        gridAdapter = new CluesViewGridOtherAdapter(mainActivity, mItemArray, R.id.grid_item_other_layout, true);

        mDragListView.setAdapter(gridAdapter, true);

        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(null);
        mDragListView.setScrollingEnabled(true);

        usernameTV = (TextView) view.findViewById(R.id.usernameTV);
        usernameTV.setTypeface(mainActivity.futuraBold);
        usernameTV.setText(connectionUsername);

        Log.d(TAG, "mConnection: " + mConnection.faceToFace);

        // Set buttons' width to half the size of the screen
        WindowManager wm = (WindowManager) container.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                width/2,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Button to initiate face2face confirmation
        face2faceBN = (Button) view.findViewById(R.id.face2faceBN);
        params.setMargins(0, 0, 4, 0);
        face2faceBN.setLayoutParams(params);
        face2faceBN.setTypeface(mainActivity.futura);
        face2faceBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If the user is not in proximity do not allow face2face confirmation
                // Or if the last Face2Face was too soon
//                if (!mainActivity.compareDates(mConnection.lastMet) || !mainActivity.compareDates(mConnection.lastFaceToFace)) {
                if (!mainActivity.compareDates(mConnection.lastMet)) {
                    Toast.makeText(getActivity(), "Could not initiate face2face confirmation. " +
                            "The user is not in your proximity.", Toast.LENGTH_LONG).show();
                }
                else if((!mainActivity.compareDatesF2F(mConnection.lastFaceToFace) || F2Fcomplete) && mConnection.faceToFace > 0) {
                    Toast.makeText(getActivity(), "You cannot have another Face2Face confirmation so soon! " +
                            "Please wait until the next time you meet " +
                            connectionUsername + "!"
                            , Toast.LENGTH_LONG).show();
                }
                else {

                    if (mSocket.connected()) {
                        // Show confirmation dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Are you sure you want to initiate face to face interaction with "
                                + connectionUsername + "?")
                                .setTitle("Initiate face-to-face interaction.");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                initiateFace2Face();
                            }
                        })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        Log.d(TAG, "face2face dialog canceled.");
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else
                        Toast.makeText(getActivity(), "Could not connect to the server.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Button to open the chat fragment for conversation with current contact
        chatBN = (Button) view.findViewById(R.id.chatBN);
        params.setMargins(4, 0, 4, 0);
        chatBN.setLayoutParams(params);
        chatBN.setTypeface(mainActivity.futura);
        chatBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.onChatButton(mConnection, connectionUsername, connectionId);
                newMessageIV.clearAnimation();
            }
        });

        newMessageIV = (ImageView) view.findViewById(R.id.new_messageIV);
        // Check for new messages
        if(mainActivity.checkMessages(connectionId)) {
            //chatBN.setText("Messages (!)");
            newMessageIV.setVisibility(View.VISIBLE);
            final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
            animation.setDuration(2000); // duration - half a second
            animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
            animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
            //animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
            newMessageIV.startAnimation(animation);
        }
        else {
            chatBN.setText("Messages");
            newMessageIV.setVisibility(View.INVISIBLE);
        }

        // Check for new clues
        if(mainActivity.checkClues(mConnection._id)) {
            mainActivity.mNewClues.remove(mConnection._id);
            mainActivity.updateNewClues();
        }


        // Back button
        backBN = (ImageButton) view.findViewById(R.id.backBN);
        backBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.onConnectionsButton();
            }
        });

        // Connect to the Socket server
        mSocket = ((N2U_Application) mainActivity.getApplication()).getSocket();
        Log.d(TAG, "connectionId:" + mConnection._id);

        if(!mSocket.connected()) {
            Log.d(TAG, "Socket.connect - in OtherProfileGridFragment");
            mSocket.connect();
        }
        mSocket.on("face2face", onNewF2FMessage);
        mSocket.on("userOffline", onUserOffline);
        ((N2U_Application) mainActivity.getApplication()).setSocket(mSocket);
        Log.d(TAG, "Socket.set - in OtherProfileGridFragment");

        if(mainActivity.otherProfileTutorialShown) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String tutorialTitle = "Welcome to " + "<b>" + connectionUsername + "</b>" + "'s profile!" ;
            String tutorialText = "If you find one of " + connectionUsername + "'s whispers interesting," +
                    " tap and hold on it to let them know!<br><br>" +
                    "With " + "<b>" + "Messages" + "</b>" + " you can chat with others to take the interaction further," +
                    " or even to suggest a face-to-face meeting.<br><br>" +
                    "Meeting face-to-face with another user merits achievements! Press " + "<b>" + "Face2Face" + "</b>"
                    + " to play together a mini-game that requires you to actually be face-to-face.<br><br>" +
                    "Have fun interacting with people nearby!";
            builder.setMessage(Html.fromHtml(tutorialText))
                    .setTitle(Html.fromHtml(tutorialTitle));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //initiateFace2Face();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            mainActivity.mEditor.putBoolean("otherProfileTutorial", false);
            mainActivity.otherProfileTutorialShown = false;
            mainActivity.mEditor.commit();
        }

        mainActivity.getCluesForOtherGrid();

        if(requestF2F) {
            Toast.makeText(getActivity(), "Tap on the Face2Face button to initiate confirmation!", Toast.LENGTH_LONG).show();
            requestF2F = false;
        }

        face2faceBN.setText("Face2Face" + " (" + mConnection.faceToFace + ")");

        Log.d(TAG, "OTHER FRAGMENT: " + mConnection._id + " - " + mConnection.lastMet);

        avatarIV = (ImageView) view.findViewById(R.id.avatarIV);
        int hash = mConnection._id.hashCode() % 3;
        if(hash == -2)
            avatarIV.setImageResource(R.mipmap.avatar01);
        else if(hash == 0 || hash == 1)
            avatarIV.setImageResource(R.mipmap.avatar02);
        else if(hash == 2 || hash == -1)
            avatarIV.setImageResource(R.mipmap.avatar03);


        HashMap<String, String> msg = new HashMap<String, String>();
        msg.put("id", mainActivity.myProfile.getId());
        msg.put("username", mainActivity.myProfile.getUsername());
        msg.put("text", mainActivity.getCurrentDate());
        JSONObject msgJSON = new JSONObject(msg);
        mSocket.emit("connect message", msgJSON);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");

        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("face2face", onNewF2FMessage);
        mSocket.off("userOffline", onUserOffline);

        ((N2U_Application) mainActivity.getApplication()).setSocket(mSocket);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View mView = view;
        view.post(new Runnable() {
            @Override
            public void run() {
                if((mConnection.faceToFace > 0) && !updateAvatar())
                    downloadAvatar();
            }
        });

    }

    public void refreshGrid() {

        mItemArray.clear();
        gridAdapter.notifyDataSetChanged();
        mainActivity.getCluesForOtherGrid();
    }

    public void addItemToList(Clue clue, int position) {
        Log.d(TAG, "addItemToList " + " position " + position + " mConnection.timesMet " + mConnection.timesMet + " mainActivity.myProfile.getClues() " + mainActivity.myProfile.getClues());
        if(position < mConnection.timesMet) {
            if(position == mainActivity.myProfile.getClues()) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                String tutorialTitle = "Share more whispers!" ;
//                String tutorialText = connectionUsername + " has shared more whispers than you! " +
//                        "Add more whispers in your profile to be able to see " + connectionUsername + "'s complete profile.";
//                builder.setMessage(Html.fromHtml(tutorialText))
//                        .setTitle(Html.fromHtml(tutorialTitle));
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        //initiateFace2Face();
//                    }
//                });
//                AlertDialog dialog = builder.create();
//                dialog.show();
            }
            else if(position < mainActivity.myProfile.getClues()){
                mItemArray.add(new Pair<>(Long.valueOf(clue.getOrderNumber()), clue));
                gridAdapter.notifyDataSetChanged();
            }
        }
    }

    public void otherUserHasMoreWhispers() {

//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        String tutorialTitle = connectionUsername + " has not shared enough whispers!" ;
//        String tutorialText = "You have met " + connectionUsername + " enough times to receive up to "
//                + mConnection.timesMet + " whispers, but they haven't shared enough yet.\n"
//                + "Encourage other users to share more whispers by sending them a message!";
//        builder.setMessage(Html.fromHtml(tutorialText))
//                .setTitle(Html.fromHtml(tutorialTitle));
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                //initiateFace2Face();
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();

    }

    public void initiateFace2Face() {
        Log.d(TAG, "initiateFace2Face()" + mConnection.toString());

        HashMap<String, String> msg = new HashMap<String, String>();

        msg.put("senderUsername", mainActivity.myProfile.getUsername());
        msg.put("senderId", mainActivity.myProfile.getId());
        msg.put("recipient", connectionId);
        msg.put("connectionId", mConnection._id);
        msg.put("connectionUser1Id", mConnection.user1Id);
        msg.put("connectionUser2Id", mConnection.user2Id);
        msg.put("connectionLastFaceToFace", mainActivity.dateToString(mConnection.lastFaceToFace));
        msg.put("connectionLastMet", mainActivity.dateToString(mConnection.lastMet));
        msg.put("connectionLastUpdate", mainActivity.dateToString(mConnection.lastUpdate));
        msg.put("connectionFaceToFace", Integer.toString(mConnection.faceToFace));
        msg.put("connectionTimesMet", Integer.toString(mConnection.timesMet));
        msg.put("type", "f2fInit");
        JSONObject msgJSON = new JSONObject(msg);
        mSocket.emit("face2face", msgJSON);

    }

    private void disconnectSocket() {
        Log.d(TAG, "disconnectSocket()");

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("face2face", onNewF2FMessage);
        mSocket.off("userOffline", onUserOffline);

        Log.d(TAG, "disconnectSocket() - setting socketConnected to false");
        mSocket.close();
        ((N2U_Application) mainActivity.getApplication()).setSocket(mSocket);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onConnectError");
            disconnectSocket();
        }
    };

    private Emitter.Listener onUserOffline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(mainActivity, "User is not available. Try again later!", Toast.LENGTH_LONG).show();
                }
            });
        }
    };


    private Emitter.Listener onNewF2FMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            final JSONObject msgJSON = (JSONObject) args[0];
            Log.d(TAG, "Received message from server: " + msgJSON);
            Log.d(TAG, "mConnection._id: " + mConnection._id);

            if(N2U_Application.isActivityVisible()) {
                try {
                    if((msgJSON.getString("type").equals("f2fInit")
                            && msgJSON.getString("recipient").equals(mainActivity.myProfile.getId()))
                            && (msgJSON.getString("connectionId").equals(mConnection._id))
                            ) {

                        // Show dialog to confirm f2f interaction
                        builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("User " + msgJSON.getString("senderUsername") + " wants to initiate a face-to-face confirmation. " +
                                "Do you accept?")
                                .setTitle("Initiate face-to-face interaction.");
                        MediaPlayer mp = MediaPlayer.create(mainActivity.getApplicationContext(), R.raw.knock);
                        mp.start();
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                // Generate a random number between 1 and 15
                                Random rand = new Random();
                                int  seed = rand.nextInt(5) + 1;

                                // Send message to start face2face interaction
                                HashMap<String, String> msg = new HashMap<String, String>();
                                msg.put("senderId", mainActivity.myProfile.getId());
                                msg.put("senderUsername", mainActivity.myProfile.getUsername());
                                try {
                                    msg.put("recipient", msgJSON.getString("senderId"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                msg.put("seed", Integer.toString(seed));
                                msg.put("type", "f2fStart");
                                JSONObject msgJSON = new JSONObject(msg);
                                mSocket.emit("face2face", msgJSON);

                                showImageMain(seed);

                            }
                        })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        Log.d(TAG, "face2face dialog canceled.");
                                    }
                                });
                        mainActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }
                    else if(msgJSON.getString("type").equals("f2fStart")
                            && msgJSON.getString("recipient").equals(mainActivity.myProfile.getId())) {

                        int seed = Integer.parseInt(msgJSON.getString("seed"));
                        showImageSecondary(seed);

                    }
                    else if(msgJSON.getString("type").equals("f2fEnd")
                            && msgJSON.getString("recipient").equals(mainActivity.myProfile.getId())) {

                        //mainActivity.connectionsGridItemOnClick(mConnection, connectionUsername, connectionId);
//                    mainActivity.onBackPressed();
//                    mainActivity.connectionsGridItemOnClick(mConnection, connectionUsername, connectionId);
                        dialog.dismiss();
                        if(msgJSON.getString("success").equals("true")) {
                            mConnection.faceToFace++;

                            mainActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), "CORRECT!", Toast.LENGTH_SHORT).show();
                                    face2faceBN.setText("Face2Face" + " (" + mConnection.faceToFace + ")");
                                }
                            });

                            // Update the shared preferences and check for achievement
                            //if(mConnection.faceToFace > mainActivity.face2face_total) {
                                mainActivity.face2face_total++;
                                mainActivity.mEditor.putInt("face2face_total", mainActivity.face2face_total);
                                mainActivity.mEditor.commit();
                                mainActivity.progressAchievements("face2face_total");
                                F2Fcomplete = true;
                                downloadAvatar();
                            //}
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void showImageMain(final int seed) {
        LayoutInflater li = LayoutInflater.from(mainActivity);
        final View dialogView = li.inflate(R.layout.dialog_image_main, null);

        answersCounter = 0;

        // Show the corresponding image depending on the randomly generated number
        if(1<= seed && seed <= 5)
            dialogView.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.n2u_01));
        else if(6<= seed && seed <= 10)
            dialogView.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.n2u_02));
        else
            dialogView.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.n2u_03));

        // Determine the correct button
        correctAnswer = "";
        switch (seed) {
            case 1:
                correctAnswer = "button4";
                break;
            case 2:
                correctAnswer = "button2";
                break;
            case 3:
                correctAnswer = "button5";
                break;
            case 4:
                correctAnswer = "button3";
                break;
            case 5:
                correctAnswer = "button1";
                break;
            case 6:
                correctAnswer = "button3";
                break;
            case 7:
                correctAnswer = "button5";
                break;
            case 8:
                correctAnswer = "button4";
                break;
            case 9:
                correctAnswer = "button1";
                break;
            case 10:
                correctAnswer = "button2";
                break;
            case 11:
                correctAnswer = "button3";
                break;
            case 12:
                correctAnswer = "button5";
                break;
            case 13:
                correctAnswer = "button4";
                break;
            case 14:
                correctAnswer = "button2";
                break;
            case 15:
                correctAnswer = "button1";
                break;
        }

        Button button1 = (Button) dialogView.findViewById(R.id.person1BN);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(correctAnswer.equals("button1"))
                    onF2FAnswer(dialog, true);
                else
                    onF2FAnswer(dialog, false);
            }
        });

        Button button2 = (Button) dialogView.findViewById(R.id.person2BN);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals("button2"))
                    onF2FAnswer(dialog, true);
                else
                    onF2FAnswer(dialog, false);
            }
        });

        Button button3 = (Button) dialogView.findViewById(R.id.person3BN);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(correctAnswer.equals("button3"))
                    onF2FAnswer(dialog, true);
                else
                    onF2FAnswer(dialog, false);
            }
        });

        Button button4 = (Button) dialogView.findViewById(R.id.person4BN);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(correctAnswer.equals("button4"))
                    onF2FAnswer(dialog, true);
                else
                    onF2FAnswer(dialog, false);
            }
        });

        Button button5 = (Button) dialogView.findViewById(R.id.person5BN);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals("button5"))
                    onF2FAnswer(dialog, true);
                else
                    onF2FAnswer(dialog, false);
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setView(dialogView);

        final AlertDialog.Builder builder = alertDialogBuilder;
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void onF2FAnswer(AlertDialog dialog, boolean correct) {

        Boolean endFaceToFace = false;
        Boolean success = false;

        // If the answer to the face2face confirmation was correct
        if(correct) {
            MediaPlayer mp = MediaPlayer.create(mainActivity.getApplicationContext(), R.raw.applause);
            mp.start();
            Toast.makeText(getActivity(), "CORRECT!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // Update the connection between the users on the Server
            HashMap<String, String> params = new HashMap<String, String>();
            HTTPRequest request;

            mConnection.faceToFace++;
            mConnection.lastFaceToFace = mainActivity.stringToDate(mainActivity.getCurrentDate());

            params.put("_id", mConnection._id);
            params.put("_user1Id", mConnection.user1Id);
            params.put("_user2Id", mConnection.user2Id);
            params.put("timesMet", Integer.toString(mConnection.timesMet));
            params.put("faceToFace", Integer.toString(mConnection.faceToFace));
            params.put("lastFaceToFace", mainActivity.dateToString(mConnection.lastFaceToFace));
            params.put("lastMet", mainActivity.dateToString(mConnection.lastMet));
            params.put("lastUpdate", mainActivity.dateToString(mConnection.lastUpdate));
            JSONObject req = new JSONObject(params);

            request = new HTTPRequest("UpdateConnection",
                    mainActivity.HOST + mainActivity.PORT + "/connections/update",
                    Request.Method.PUT,
                    req);
            mainActivity.mHTTPHelper.sendToServer(request);

            // Update the face2face TextView
            face2faceBN.setText("Face2Face" + " (" + mConnection.faceToFace + ")");

            // Update the shared preferences and check for achievement
            //if(mConnection.faceToFace > mainActivity.face2face_total) {
                mainActivity.face2face_total++;
                mainActivity.mEditor.putInt("face2face_total", mainActivity.face2face_total);
                mainActivity.mEditor.commit();
                mainActivity.progressAchievements("face2face_total");
                F2Fcomplete = true;
                downloadAvatar();
            //}

            endFaceToFace = true;
            success = true;
        }
        else {
            MediaPlayer mp = MediaPlayer.create(mainActivity.getApplicationContext(), R.raw.buzz);
            mp.start();
            vibrator.vibrate(120);
            answersCounter++;
            if (answersCounter == 2) {
                dialog.dismiss();
                Toast.makeText(getActivity(), "Wrong answer! Try again...", Toast.LENGTH_SHORT).show();
                endFaceToFace = true;
            } else
                Toast.makeText(getActivity(), "Wrong answer! You have one more try...", Toast.LENGTH_SHORT).show();
        }

        // Emit message to end face2face confirmation
        if(endFaceToFace) {
            HashMap<String, String> msg = new HashMap<String, String>();

            msg.put("senderUsername", mainActivity.myProfile.getUsername());
            msg.put("senderId", mainActivity.myProfile.getId());
            msg.put("recipient", connectionId);                     /* !!!!!!!!!!!! RECIPIENT ID??? !!!!!!!!!!!! */
            msg.put("connectionId", mConnection._id);
            msg.put("type", "f2fEnd");
            if(success)
                msg.put("success", "true");
            else
                msg.put("success", "false");
            JSONObject msgJSON = new JSONObject(msg);
            mSocket.emit("face2face", msgJSON);
        }
    }

    public void downloadAvatar() {

        HashMap<String, String> params = new HashMap<>();
        HTTPRequest request;

        params.put("_id", connectionId);
        JSONObject req = new JSONObject(params);

        request = new HTTPRequest("OtherFragmentGrid - UpdateAvatar",
                mainActivity.HOST + mainActivity.PORT + "/profiles/pics/" + connectionId,
                Request.Method.GET,
                req);
        mainActivity.mHTTPHelper.sendToServer(request);
    }

    public boolean updateAvatar() {

        String filename = connectionId;
        File file = new File(mainActivity.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = mainActivity.openFileInput(filename);

                Bitmap avatarBMP = BitmapFactory.decodeStream(fileInputStream);

                int width = avatarIV.getMeasuredWidth();
                int height = avatarIV.getMeasuredHeight();
                Log.d(TAG, "OtherProfileGridFragment - updateAvatar(), width - height: " + width + " " + height);

                avatarIV.setImageBitmap(avatarBMP);
                fileInputStream.close();

                // Adjust image size
                avatarIV.getLayoutParams().height = height;
                avatarIV.getLayoutParams().width = width;

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            }
            return true;
        }
        return false;
    }

    public boolean likeClue(Clue clue) {

        // Get the owner of the clue
        boolean update = false;

        // If the current clue is contained in the list of liked clues
        if(mainActivity.mLikedClues.size() > 0) {
            if (mainActivity.mLikedClues.get(clue.getId()) != null) {
                return false;
            } else {
                update = true;
            }
        }
        else
            update = true;


        if (update){

            // Update the clue on the server, and add it to the users list
            String message = "OtherFragmentGrid - UpdateClue";
            String route = "/clues/" + clue.getId() + "/update";
            int method = Request.Method.PUT;

            HashMap<String, String> params = new HashMap<String, String>();
            HTTPRequest request;

            params.put("_id", clue.getId());
            params.put("ownerId", clue.getOwnerId());
            params.put("question", clue.getQuestion());
            params.put("answer", clue.getAnswer());
            params.put("updated", mainActivity.getCurrentDate());
            params.put("orderNumber", Integer.toString(clue.getOrderNumber()));
            params.put("likes", Integer.toString(clue.getLikes() + 1));
            JSONObject req = new JSONObject(params);

            request = new HTTPRequest(message,
                    mainActivity.HOST + mainActivity.PORT + route,
                    method,
                    req);

            mainActivity.mHTTPHelper.sendToServer(request);

            mainActivity.updateLikedClues(clue);

            // Send push notification to the owner of the clue
            if(!mSocket.connected()) {
                mSocket.connect();
            }
            HashMap<String, String> msg = new HashMap<String, String>();

            msg.put("senderUsername", mainActivity.myProfile.getUsername());
            msg.put("senderId", mainActivity.myProfile.getId());
            msg.put("recipientId", connectionId);
            msg.put("clueId", clue.getId());
            msg.put("clueText", clue.getQuestion() + " " + clue.getAnswer());
            JSONObject msgJSON = new JSONObject(msg);
            mSocket.emit("likeNotification", msgJSON);

            // Update the likes_given in the shared preferences and check for the achievement
            mainActivity.likes_given = mainActivity.mSettings.getInt("likes_given", mainActivity.likes_given);
            mainActivity.likes_given++;
            mainActivity.mEditor.putInt("likes_given", mainActivity.likes_given);
            mainActivity.mEditor.commit();
            mainActivity.progressAchievements("likes_given");

            // ********************************************************
            // Store like to the database on the server
            String tag = "Add Like";
            String route2 = "/likes";
            int method2 = Request.Method.POST;

            HashMap<String, String> params2 = new HashMap<String, String>();
            HTTPRequest request2;

            params2.put("_ownerId", mainActivity.myProfile.getId());
            params2.put("_clueId", clue.getId());
            params2.put("date",  mainActivity.getCurrentDate());
            JSONObject req2 = new JSONObject(params2);

            request2 = new HTTPRequest(tag,
                    mainActivity.HOST + mainActivity.PORT + route2,
                    method2,
                    req2);

            mainActivity.mHTTPHelper.sendToServer(request2);
            // ********************************************************
        }

        return true;

    }

    public void showImageSecondary(int seed) {
        LayoutInflater li = LayoutInflater.from(mainActivity);
        View dialogView = li.inflate(R.layout.dialog_image_secondary, null);
        ImageView imageIV = (ImageView) dialogView.findViewById(R.id.imageIV);

        if(seed == 1 || seed == 6 || seed == 11)
            imageIV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.person_01));
        else if(seed == 2 || seed == 7 || seed == 12)
            imageIV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.person_02));
        else if(seed == 3 || seed == 8 || seed == 13)
            imageIV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.person_03));
        else if(seed == 4 || seed == 9 || seed == 14)
            imageIV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.person_04));
        else if(seed == 5 || seed == 10 || seed == 15)
            imageIV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.person_05));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setView(dialogView);

        final AlertDialog.Builder builder = alertDialogBuilder;
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                dialog = builder.create();
                dialog.show();
            }
        });
    }

    public interface OtherProfileGridFragmentListener {
        void getCluesForOtherGrid();
        void onChatButton(Connection connection, String username, String id);
    }
}
