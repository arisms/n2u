package fi.tut.cs.social.proximeety.classes;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.Request;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.helpers.HTTPHelper;

public class N2UService extends Service {
    private static final String TAG = "N2UService";
    private static AlarmManager alarm = null;
    public static final long PROXIMITY_SCAN_INTERVAL = 60000;  //1 minute
    public static final long UPDATE_MIN_INTERVAL = 7200000;      //2 hours

    private static Profile myProfile;
    private static SharedPreferences mSettings;
    private static SharedPreferences.Editor mEditor;
    private static BluetoothAdapter mBluetoothAdapter;
    private static HTTPHelper mHTTPHelper;
    public static List<BluetoothDevice> mDevices;
    public static Map<String, Boolean> mNewClues;

    File file;
    String filename;
    FileOutputStream outputStream;

    public Vibrator vibrator;
    long[] pattern = { 0, 100, 100, 80 };
    int mId = 1;

    public static List<Profile> mContacts;
    public static List<Connection> mConnections;
    public static List<String> updates;
    public static Map<String, Boolean> mNewMessages;


    public static boolean updateFlag;
    public static boolean connectionFlag;

    public final String HOST = "http://social.cs.tut.fi:";
    public final String PORT = "10001";
//    public static final String HOST = "http://192.168.1.2:";
//    public static final String PORT = "3000";

//    private final String SOCKET_SERVER_URL = "http://social.cs.tut.fi:10002";
    private Socket mSocket;
    private static boolean socketConnected = false;
    Boolean[] dailies = new Boolean[5];


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");

        // Open the Default SharedPreferences file (create if it doesn't exist) to read myProfile
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        myProfile = new Profile(mSettings.getString("_id", ""),
                mSettings.getString("username", ""),
                mSettings.getString("password", ""),
                mSettings.getString("deviceId", ""),
                Integer.parseInt(mSettings.getString("clues", "")),
                Boolean.valueOf(mSettings.getString("active", "")));

        mHTTPHelper = new HTTPHelper(getApplicationContext());

        mDevices = new ArrayList<BluetoothDevice>();
        updates = new ArrayList<String>();

        mContacts = new ArrayList<Profile>();
        mConnections = new ArrayList<Connection>();
        mNewClues = new HashMap<String, Boolean>();
        mNewMessages = new HashMap<String, Boolean>();

        for(int i=0; i<dailies.length; i++) {
            dailies[i] = false;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);

        // Cancel the Alarm
        if (isAlarmUp()) {
            Log.d(TAG, "Cancelling AlarmReceiver...");
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.cancel(pIntent);
        }
        // TODO
        //unregisterReceiver(ActionFoundReceiver);

        if(mSocket.connected()) {
            disconnectSocket();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if(intent != null) {
            Bundle extras = intent.getExtras();
            String trigger = extras.getString("Trigger");

            mSocket = ((N2U_Application) this.getApplication()).getSocket();
            //Log.d(TAG, "OnstartCommand, Before if, socketConnected: " + socketConnected);
            if(!socketConnected) {
                connectSocket();
            }
            HashMap<String, String> msg = new HashMap<String, String>();
            msg.put("id", myProfile.getId());
            msg.put("username", myProfile.getUsername());
            msg.put("text", getCurrentDate());
            JSONObject msgJSON = new JSONObject(msg);
            mSocket.emit("connect message", msgJSON);

            Log.d(TAG, "trigger: " + trigger);
            // If the Service was triggered from the AlarmReceiver, perform the appropriate periodic tasks
            if(trigger.equals("Alarm")) {
                //Log.d(TAG, "trigger = Alarm");

                myProfile.setActive(Boolean.valueOf(mSettings.getString("active", "")));
                if(myProfile.getActive()) {
                    //Log.d(TAG, "myProfile = Active");
                    bluetoothDiscovery();
                }
                else {
                    //Log.d(TAG, "myProfile = Inactive");
                }
            }
            // If the Service was triggered by the Activity or System Boot Receiver
            else if((trigger.equals("Activity") || trigger.equals("Boot"))) {
                //Log.d(TAG, "trigger = Activity");

                // If myProfile was not written in the SharedPreferences file, stop the Service
                if(myProfile.getId().equals("")) {
                    Log.d(TAG, "Stopping Service");
                    stopSelf();
                }
                else {
                    // Start the AlarmReceiver if it is not already running
                    if(!isAlarmUp()) {
//                        Log.d(TAG, "Scheduling alarm, profile: " + myProfile.getUsername() + " " + myProfile.getDeviceId());
                        scheduleAlarm();
                    }

                }

            }
            // If the Service was triggered by the HTTP Helper, get the JSON response
            else if(trigger.equals("HTTPHelper")) {

                if(extras.getString("ResponseType").equals("Profile")) {
                    JSONObject response = null;
                    try {
                        response = new JSONObject(extras.getString("Response"));
                        if(response != null) {
                            //Log.d(TAG, "Service: Received HTTPHelper data: " + response.getString("username"));
                            if(!response.getString("_id").equals("-1"))
                                onReceiveProfile(response);
                        }
                        else
                            Log.d(TAG, "response is null");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(extras.getString("ResponseType").equals("Connections")) {
                    JSONArray response = null;
                    try {
                        response = new JSONArray(extras.getString("Response"));
                        if(response != null) {
                            updateConnections(response);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        else {
            Log.d(TAG, "onStartCommand() - null");
        }

        getNewMessages();

        // Check for days used
//        int days_used = mSettings.getInt("days_used", 0);
//        String lastDayString = mSettings.getString("last_day", getCurrentDate());
//
//        if(days_used == 1 && dailies[0] == false) {
//            createNotification("Discovered no-one yet?", "Try crowded places!", "days_used", "1");
//            dailies[0] = true;
//        }
//        else if(days_used == 2 && dailies[1] == false) {
//            createNotification("Whisper something interesting to get likes!", "You can edit your whispers anytime.", "days_used", "2");
//            dailies[1] = true;
//        }
//        else if(days_used == 3 && dailies[2] == false) {
//            createNotification("Friends around?", "Use N2U to discover them!", "days_used", "3");
//            dailies[2] = true;
//        }
//        else if(days_used == 4 && dailies[3] == false) {
//            createNotification("Common interest?", "Send a message to make contact with others!", "days_used", "4");
//            dailies[3] = true;
//        }
//        else if(days_used == 5 && dailies[4] == false) {
//            createNotification("Where are all the profile pics?", "Meet face-to-face to get them.", "days_used", "5");
//            dailies[4] = true;
//        }


        // Send ping to the server
        HashMap<String, String> msg = new HashMap<String, String>();
        msg.put("id", myProfile.getId());
        msg.put("username", myProfile.getUsername());
        msg.put("text", getCurrentDate());
        JSONObject msgJSON = new JSONObject(msg);
        //mSocket.emit("ping", msgJSON);
        //mSocket.connect();

        return START_NOT_STICKY;
    }

    public void onReceiveProfile(JSONObject response) {
        try {
            Profile tempProfile = new Profile(response.getString("_id"),
                    response.getString("username"),
                    response.getString("password"),
                    response.getString("deviceId"),
                    Integer.parseInt(response.getString("clues")),
                    Boolean.valueOf(response.getString("active")));
            //Log.d(TAG, "In service: Created new profile with username: " + tempProfile.getUsername());
            if(tempProfile.getActive())
                mContacts.add(tempProfile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getConnectionsForUser() {
        Log.d(TAG, "getConnectionsForUser()");

        // Get All connections for current user from the server
        HTTPRequest request = new HTTPRequest("Service - GetConnections",
                HOST + PORT + "/connections/userId=" + myProfile.getId(),
                Request.Method.GET,
                null
        );
        mHTTPHelper.sendToServerArray(request);
    }

    public void updateConnections(JSONArray response) {
//        Log.d(TAG, "updateConnections() " + response.toString());
        mConnections.clear();
        mNewClues.clear();
        updates.clear();
        updateFlag = false;
        connectionFlag = false;

        getNewClues();
        // Add all retrieved connections to mConnections list
        JSONObject tempJSON;
        try {

            // For all the connections found in the server for the current user
            for (int i = 0; i < response.length(); i++) {

                // Create a Connection object from the JSONObject
                tempJSON = (JSONObject) response.get(i);
//                Log.d(TAG, "In for, getString, boolean, valueof: " + tempJSON.getString("blocked") + " " + Boolean.getBoolean(tempJSON.getString("blocked")) + " " + Boolean.valueOf(tempJSON.getString("blocked")));
                //Log.d(TAG, "From date: " + tempJSON.getString("lastMet") + " can get date: " + stringToDate(tempJSON.getString("lastMet")) );
                Connection tempConnection = new Connection(tempJSON.getString("_id"),
                        tempJSON.getString("_user1Id"),
                        tempJSON.getString("_user2Id"),
                        Integer.parseInt(tempJSON.getString("timesMet")),
                        Integer.parseInt(tempJSON.getString("faceToFace")),
                        stringToDate(tempJSON.getString("lastFaceToFace")),
                        stringToDate(tempJSON.getString("lastMet")),
                        stringToDate(tempJSON.getString("lastUpdate")),
                        Boolean.valueOf(tempJSON.getString("blocked"))
                );

                // Search the list of devices in range, to see if the other user is currently in proximity
                for(int j=0; j<mContacts.size(); j++) {
                    if(mContacts.get(j).getId().equals(tempConnection.user1Id) || mContacts.get(j).getId().equals(tempConnection.user2Id)) {
//                        Log.d(TAG, "Connection is in range: " + mContacts.get(j).getId());


//                        if(compareDates(tempConnection.lastUpdate) && tempConnection.timesMet <= 5) {
//                            updateFlag = true;
//                        }

                        // Read number of inserted whispers from shared preferences
                        myProfile.setClues(Integer.parseInt(mSettings.getString("clues", "")));

                        // Update connection and add to the list

                        if(timeForUpdate(tempConnection.lastUpdate) && tempConnection.timesMet < myProfile.getClues()) {
//                            Log.d(TAG, "Enters if for: " + tempConnection.timesMet + " " + myProfile.getClues());
                            updateFlag = true;
                            tempConnection.lastUpdate = stringToDate(getCurrentDate());
                            tempConnection.timesMet++;
                        }
                        // If the time of the lastUpdate of the connection was after the last scan and before now,
                        // another device has updated the document on the server. The notification flag is true
                        else if(compareDates(tempConnection.lastUpdate) && tempConnection.timesMet <= myProfile.getClues()) {
                            updateFlag = true;
                        }
                        tempConnection.lastMet = stringToDate(getCurrentDate());
                        mConnections.add(tempConnection);

                        // Add the id of the updated profile to the list of updates
                        if(updateFlag) {
                            if (!tempConnection.user1Id.equals(myProfile.getId()))
                                updates.add(tempConnection.user1Id);
                            else
                                updates.add(tempConnection.user2Id);

//                            Log.d(TAG, "Adding to mNewClues: " + tempConnection._id);
                            mNewClues.put(tempConnection._id, true);
                        }

                        mContacts.remove(j);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // The remaining elements in mContacts list are added as new connections
        if(mContacts.size() > 0)
            connectionFlag = true;
        for(int i=0; i<mContacts.size(); i++) {

            Connection tempConnection = new Connection(null,
                    myProfile.getId(),
                    mContacts.get(i).getId(),
                    1,
                    0,
                    stringToDate(getCurrentDate()),
                    stringToDate(getCurrentDate()),
                    stringToDate(getCurrentDate()),
                    false
            );
            mConnections.add(tempConnection);
        }

        saveUpdates();

    }

    public void saveUpdates() {
        Log.d(TAG, "saveUpdates - list of Connections:");

        // Create notification
        String message = "";
        String title = "";
        if(updateFlag && connectionFlag) {
            title = "A new contact is next to you!";
            message = "And more whispers revealed...";
        }
        else if(updateFlag) {    // If an existing connection has been updated
            title = "More whispers revealed...";
            message = "...about someone next to you!";
        }
        else if(connectionFlag) {
            title = "A new contact is next to you!";
            message = "Find out his/her whispers...";
        }
        if(updateFlag || connectionFlag)
            createNotification(title, message, null, null);

        // Log
//        for(int i=0; i<mConnections.size(); i++) {
//            Log.d(TAG, "Adding connection to the server: " + mConnections.get(i).user1Id + " "
//                    + mConnections.get(i).user2Id + " " + mConnections.get(i).lastMet + " "
//                    +mConnections.get(i).faceToFace + " " + mConnections.get(i).lastFaceToFace
//                    + mConnections.get(i).lastUpdate + " " + mConnections.get(i).timesMet + " " + mConnections.get(i).blocked);
//        }

        // Save all connections to the server
        for(int i=0; i<mConnections.size(); i++) {

            HashMap<String, String> params = new HashMap<String, String>();
            HTTPRequest request;

            params.put("_id", mConnections.get(i)._id);
            params.put("_user1Id", mConnections.get(i).user1Id);
            params.put("_user2Id", mConnections.get(i).user2Id);
            params.put("timesMet", Integer.toString(mConnections.get(i).timesMet));
            params.put("faceToFace", Integer.toString(mConnections.get(i).faceToFace));
            params.put("lastFaceToFace", dateToString(mConnections.get(i).lastFaceToFace));
            params.put("lastMet", dateToString(mConnections.get(i).lastMet));
            params.put("lastUpdate", dateToString(mConnections.get(i).lastUpdate));
            params.put("blocked", Boolean.toString(mConnections.get(i).blocked));
            JSONObject req = new JSONObject(params);

            // If the _id of the connection is null, create a new connection
            if(mConnections.get(i)._id == null) {
                request = new HTTPRequest("AddConnection",
                        HOST + PORT + "/connections/",
                        Request.Method.POST,
                        req);
                mHTTPHelper.sendToServer(request);
            }
            // If the _id is NOT null, update the existing connection document on the server
            else {
                request = new HTTPRequest("UpdateConnection",
                        HOST + PORT + "/connections/update",
                        Request.Method.PUT,
                        req);
                mHTTPHelper.sendToServer(request);
            }
        }

        // Save the list of updated connections to the shared file
        if(updates.size() > 0) {
            filename = "N2Yupdates";
            try {
                file = new File(getApplicationContext().getFilesDir(), filename);

                // Create the file if it doesn't already exist
                if (!file.exists()) {
                    Log.d(TAG, "updateConnections() - file does not exist");
                    file.createNewFile();
                }
                // Write to the file
                outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(updates);
                objectOutputStream.close();
                outputStream.close();
                Log.d(TAG, "updateConnections() - updates saved to file");

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            }
        }

        if(mNewClues.size() > 0)
            updateNewClues();
    }

    public void getNewClues() {
        String filename = "N2U_newClues";
        File file = new File(this.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = this.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                mNewClues = (HashMap<String, Boolean>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateNewClues() {

        String filename = "N2U_newClues";
        try {
            File file = new File(getApplicationContext().getFilesDir(), filename);

            // Create the file if it doesn't already exist
            if (!file.exists()) {
                Log.d(TAG, "updateNewClues() - file does not exist");
                file.createNewFile();
            }
            // Write to the file
            FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(mNewClues);
            objectOutputStream.close();
            outputStream.close();
            Log.d(TAG, "updateNewClues() - updates saved to file");

        } catch (IOException e) {
            Log.e(TAG, "Unable to access file, " + e.toString());
        }

    }

    /* ****************************************************** */
    public void bluetoothDiscovery() {
        Log.d(TAG, "Starting Bluetooth Discovery...");
        mContacts.clear();
        mDevices.clear();

        // Initialize Bluetooth mAdapter.
        final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Register the BroadcastReceiver for Bluetooth Discovery events
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        //TODO: Don't forget to unregister during onDestroy
        registerReceiver(ActionFoundReceiver, filter);

        mBluetoothAdapter.startDiscovery();
    }

    /* ****************************************************** */
    public void createNotification(String title, String message, String extraTitle, String extraText) {
        Log.d(TAG, "createNotification()");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.notification)
                        .setContentTitle(title)
                        .setContentText(message);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        resultIntent.putExtra(extraTitle, extraText);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setLights(Color.BLUE, 500, 500);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        mNotificationManager.notify(mId, mBuilder.build());
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, -1);
//        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep);
//        mp.start();
    }

    private void createNotificationF2F(String title, String message, String connectionId, String senderUsername, String senderId) {
        Log.d(TAG, "createNotification()");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.notification)
                        .setContentTitle(title)
                        .setContentText(message);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        resultIntent.putExtra("F2F", "f2fInit");
        resultIntent.putExtra("connectionId", connectionId);
        resultIntent.putExtra("senderUsername", senderUsername);
        resultIntent.putExtra("senderId", senderId);


        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setLights(Color.BLUE, 500, 500);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        mNotificationManager.notify(mId, mBuilder.build());
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, -1);
//        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.knock2);
//        mp.start();
    }

    /* ************************************************************************************************************ */
    public void onDeviceFound(BluetoothDevice device) {
        // Perform a query to the server for a Profile with the specific deviceId
        HTTPRequest request = new HTTPRequest("Service - ProfileByDeviceId",
                HOST + PORT + "/profiles/deviceId/" + device.getAddress(),
                Request.Method.GET,
                null
        );

        mHTTPHelper.sendToServer(request);
    }

    /* ************************************************************************************************************ */
    // Custom receiver that enables the corresponding action related to Bluetooth discovery
    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When the Bluetooth discovery finds a device
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

//                Log.d(TAG, "Found device: " + device.getName() + " - " + device.getAddress());

                if(!mDevices.contains(device)) {
                    onDeviceFound(device);
                    mDevices.add(device);
                }
            }
            // When the Bluetooth discovery is finished
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "ACTION_DISCOVERY_FINISHED ");

                getConnectionsForUser();
            }
        }
    };

    /* ************************************************************************************************************ */
    public void scheduleAlarm() {
        Log.d(TAG, "scheduleAlarm");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                PROXIMITY_SCAN_INTERVAL, pIntent);

    }

    /* ************************************************************************************************************ */
    public boolean isAlarmUp() {
        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                new Intent(getApplicationContext(), AlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            return true;
        }

        return false;
    }


    /* ********************************************** SOCKET.IO ***************************************************** */

    private void connectSocket() {
        Log.d(TAG, "Socket.connect - in Service");
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_RECONNECT_ATTEMPT, onReconnectAttempt);
        mSocket.on(Socket.EVENT_RECONNECTING, onReconnecting);
        mSocket.on(Socket.EVENT_RECONNECT, onReconnect);
        mSocket.on("face2face", onFace2Face);
        mSocket.on("new chat message", onNewChatMessage);
        mSocket.on("likeNotification", onLikeNotification);
        mSocket.on("ping", onPing);
        ((N2U_Application) this.getApplication()).setSocket(mSocket);
    }

    private void disconnectSocket() {
        //Log.d(TAG, "disconnectSocket()");

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off("face2face", onFace2Face);

        //Log.d(TAG, "disconnectSocket() - setting socketConnected to false");
        socketConnected = false;
        mSocket.close();
        ((N2U_Application) this.getApplication()).setSocket(mSocket);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Log.d(TAG, "onConnectError");
            //if(socketConnected) {
                disconnectSocket();
            //}
        }
    };

    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onReconnect");
        }
    };

    private Emitter.Listener onReconnecting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onReconnecting");
        }
    };

    private Emitter.Listener onReconnectAttempt = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onReconnectAttempt");
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onConnect");

            if(!socketConnected) {
                // When connected to the service, send information about the current user
                // Send message to the server
                HashMap<String, String> msg = new HashMap<String, String>();
                msg.put("id", myProfile.getId());
                msg.put("username", myProfile.getUsername());
                msg.put("text", getCurrentDate());
                JSONObject msgJSON = new JSONObject(msg);
                mSocket.emit("connect message", msgJSON);

//                Log.d(TAG, "Socket - onConnect: " + msgJSON);
                socketConnected = true;
            }
        }
    };

    private Emitter.Listener onFace2Face = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject msgJSON = (JSONObject) args[0];
//            Log.d(TAG, "Received message from server - IN SERVICE: " + msgJSON);

            try {
                if(msgJSON.getString("type").equals("f2fInit")) {

                    // If the received message's recipient is the current user
                    if(msgJSON.getString("recipient").equals(myProfile.getId())) {

                        // If the activity is in the foreground, just send a message to the activity
                        if(N2U_Application.isActivityVisible()) {
                            Intent intent = new Intent(MainActivity.START_F2F);
                            try {
                                intent.putExtra("senderUsername", msgJSON.getString("senderUsername"));
                                intent.putExtra("senderId", msgJSON.getString("senderId"));
                                intent.putExtra("connectionId", msgJSON.getString("connectionId"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                        // If the activity is NOT in the foreground, create a notification which will start the activity
                        else {
                            // Start the activity with an Intent
                            try {
                                createNotificationF2F("Face2Face request!", msgJSON.getString("senderUsername") +
                                                " wants to meet you face to face!", msgJSON.getString("connectionId"),
                                        msgJSON.getString("senderUsername"), msgJSON.getString("senderId"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onNewChatMessage = new Emitter.Listener() {


        @Override
        public void call(Object... args) {
            Log.d(TAG, "onNewChatMessage");
            final JSONObject msgJSON = (JSONObject) args[0];

            try {
                if(msgJSON.getString("recipientId").equals(myProfile.getId())) {

                    // Write to the new messages list
                    addNewMessage(msgJSON.getString("senderId"));
                    createNotification("You have a new message!", msgJSON.getString("senderUsername") + " has sent you a chat message.", null, null);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onPing = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onLikeNotification");

            final JSONObject msgJSON = (JSONObject) args[0];

            try {
                if(msgJSON.getString("id").equals(myProfile.getId())) {

                    Log.d(TAG, "Received pong from Server");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onLikeNotification = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onLikeNotification");

            final JSONObject msgJSON = (JSONObject) args[0];

            try {
                if(msgJSON.getString("recipientId").equals(myProfile.getId())) {

                    String senderUsername = msgJSON.getString("senderUsername");
                    String clueText = msgJSON.getString("clueText");
                    createNotification(senderUsername + " likes your whisper: ", "\"" + clueText + "\"", null, null);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void getNewMessages() {
        String filename = "N2U_newMessages";
        File file = new File(this.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = this.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                mNewMessages = (HashMap<String, Boolean>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateNewMessages() {

        String filename = "N2U_newMessages";
        try {
            File file = new File(getApplicationContext().getFilesDir(), filename);

            // Create the file if it doesn't already exist
            if (!file.exists()) {
                Log.d(TAG, "updateNewMessages() - file does not exist");
                file.createNewFile();
            }
            // Write to the file
            FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(mNewMessages);
            objectOutputStream.close();
            outputStream.close();
            Log.d(TAG, "updateNewMessages() - updates saved to file");

        } catch (IOException e) {
            Log.e(TAG, "Unable to access file, " + e.toString());
        }

    }

    public void addNewMessage(String id) {
        mNewMessages.put(id, true);
        updateNewMessages();
    }

    /* ************************************************************************************************************ */


    /*********************************** DATE FUNCTIONS ***************************************/

    public boolean timeForUpdate(Date date) {

        Date now = new Date();

        long diff = now.getTime() - date.getTime();

//        Log.d(TAG, "timeForUpdate, now: " + now.toString() + ", date: " + date.toString() + ", diff: " + diff);

        if(diff >= UPDATE_MIN_INTERVAL)
            return true;
        else
            return false;
    }

    //TODO: Update with push notification
    public boolean compareDates(Date date) {
        Date now = new Date();

        long diff = now.getTime() - date.getTime();

        if(diff > 0 && diff < PROXIMITY_SCAN_INTERVAL) {
            Log.d(TAG, "compareDates returning TRUE for now: " + now.getTime() + " and date: " + date.getTime());
            return true;
        }
        else {
            Log.d(TAG, "compareDates returning FALSE for now: " + now.getTime() + " and date: " + date.getTime());
            return false;
        }
    }

    public String getCurrentDate() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss Z", Locale.ENGLISH);
        String date = sdf.format(new Date());

        return date;
    }

    public Date stringToDate(String date) {
        Date resultDate = null;

        try {
            resultDate = new SimpleDateFormat("dd MM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultDate;
    }

    public String dateToString(Date date) {
        String dateString;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss Z", Locale.ENGLISH);
        dateString = sdf.format(date);

        return dateString;
    }
}
