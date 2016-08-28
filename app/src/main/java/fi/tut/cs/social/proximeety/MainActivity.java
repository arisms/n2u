package fi.tut.cs.social.proximeety;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fi.tut.cs.social.proximeety.Fragments.AchievementsGridFragment;
import fi.tut.cs.social.proximeety.Fragments.ChatFragment;
import fi.tut.cs.social.proximeety.Fragments.ConnectionsGridFragment;
import fi.tut.cs.social.proximeety.Fragments.HeaderFragment;
import fi.tut.cs.social.proximeety.Fragments.LogInRegisterFragment;
import fi.tut.cs.social.proximeety.Fragments.MyProfileViewGridFragment;
import fi.tut.cs.social.proximeety.Fragments.OtherProfileGridFragment;
import fi.tut.cs.social.proximeety.classes.Achievement;
import fi.tut.cs.social.proximeety.classes.BootBroadcastReceiver;
import fi.tut.cs.social.proximeety.classes.Clue;
import fi.tut.cs.social.proximeety.classes.Connection;
import fi.tut.cs.social.proximeety.classes.HTTPRequest;
import fi.tut.cs.social.proximeety.classes.N2UService;
import fi.tut.cs.social.proximeety.classes.N2U_Application;
import fi.tut.cs.social.proximeety.classes.Profile;
import fi.tut.cs.social.proximeety.classes.User;
import fi.tut.cs.social.proximeety.helpers.BluetoothHelper;
import fi.tut.cs.social.proximeety.helpers.HTTPHelper;

public class MainActivity extends FragmentActivity implements LogInRegisterFragment.LogInRegisterFragmentListener,
        HeaderFragment.HeaderFragmentListener,
        MyProfileViewGridFragment.MyProfileViewGridFragmentListener,
        ConnectionsGridFragment.ConnectionsGridFragmentListener, OtherProfileGridFragment.OtherProfileGridFragmentListener {
    public final String TAG = "MainActivity";   //Debugging

    public static final String HOST = "http://social.cs.tut.fi:";
    public static final String PORT = "10001";
//    public static final String HOST = "http://192.168.1.2:";
//    public static final String PORT = "3000";

    public static final long PROXIMITY_SCAN_INTERVAL = 60000;  //1 minute
    public static final long UPDATE_MIN_INTERVAL = 7200000;      //2 hours
    public static final long PROXIMITY_INTERACTION_INTERVAL = 360000;  //6 minutes

    // Local variables
    // TODO: static variables for helpers/classes/fragments(?)
    private JSONObject mSingleResult = null;
    private JSONArray mResults = new JSONArray();
    public static HTTPHelper mHTTPHelper;
    public BluetoothHelper mBluetoothHelper;
    public SharedPreferences mSettings;
    public SharedPreferences.Editor mEditor;

    public static final String START_F2F = "fi.tut.cs.social.proximeety.START_F2F";
    public static final String IMAGE_SELECTED = "fi.tut.cs.social.proximeety.IMAGE_SELECTED";
    public AlertDialog.Builder builder;
    public AlertDialog tutorialDialog = null;
    private static String usernameF2F;
    private static String idF2F;

    public static Profile myProfile;
    public static List<Clue> myInfo;
    public boolean loggedIn = false;
    private static boolean registered = false;
    public static boolean avatarUploaded = false;
    private int cluesSaved;
    public final int CLUES_LIMIT = 5;
    public static Boolean otherProfileTutorialShown;
    public static List<Connection> myConnections;
    public static List<User> mUsers;
    public static Map<String, Boolean> mLikedClues;
    public static Map<String, Boolean> mNewMessages;
    public static Map<String, Boolean> mNewClues;
    public static Map<String, Achievement> mAchievements;
    public static Boolean dateStored;

    public static int connections_total, connections_daily, achievements_total, likes_per_item,
            messages_received, messages_sent, face2face_total, likes_given, whispers_received, days_used;
    public static Date lastDayDate;
    public static String lastDayString;

    public String myPictureStringUri;
    public Typeface futura;
    public Typeface futuraThin;
    public Typeface futuraBold;

    public Vibrator vibrator;
    long[] pattern = { 0, 100, 100, 80 };

    // Fragments
    public LogInRegisterFragment mLogInRegisterFragment;
    public HeaderFragment mHeaderFragment;
    public OtherProfileGridFragment mOtherProfileGridFragment;
    public ChatFragment mChatFragment;
    public MyProfileViewGridFragment mMyProfileViewGridFragment;
    public ConnectionsGridFragment mConnectionsGridFragment;
    public AchievementsGridFragment mAchievementsGridFragment;
    public Fragment currentFragment;
    Fragment previousFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // Define Typefaces
        futura = Typeface.createFromAsset(getAssets(), "fonts/Futura-Medium.ttf");
        futuraThin = Typeface.createFromAsset(getAssets(), "fonts/Futura-Thin.ttf");
        futuraBold = Typeface.createFromAsset(getAssets(), "fonts/Futura-Bold.ttf");

        // Create helper classes
        mHTTPHelper = new HTTPHelper(this);
        mBluetoothHelper = new BluetoothHelper(this);

        // Create myProfile
        myInfo = new ArrayList<Clue>();
        myProfile = new Profile(null, null, null, mBluetoothHelper.init(), 0, false);
        myConnections = new ArrayList<Connection>();
        mUsers = new ArrayList<User>();
        mLikedClues = new HashMap<String, Boolean>();
        mNewMessages = new HashMap<String, Boolean>();
        mNewClues = new HashMap<String, Boolean>();
        mAchievements = new HashMap<String, Achievement>();

        getAchievements();
        createAchievements();

        // Create the fragment instances
        mLogInRegisterFragment = new LogInRegisterFragment();
        mHeaderFragment = new HeaderFragment();
        mChatFragment = new ChatFragment();
        mMyProfileViewGridFragment = new MyProfileViewGridFragment();
        mConnectionsGridFragment = new ConnectionsGridFragment();
        mOtherProfileGridFragment = new OtherProfileGridFragment();
        mAchievementsGridFragment = new AchievementsGridFragment();

        // Open the Default SharedPreferences file (create if it doesn't exist) to read myProfile
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSettings.edit();
        myProfile.setUsername(mSettings.getString("username", ""));
        myProfile.setPassword(mSettings.getString("password", ""));
        myProfile.setDeviceId(mSettings.getString("deviceId", myProfile.getDeviceId()));
        myProfile.setId(mSettings.getString("_id", ""));
        myProfile.setActive(Boolean.valueOf(mSettings.getString("active", "")));
        myProfile.setClues(Integer.parseInt(mSettings.getString("clues", "0")));
        myPictureStringUri = mSettings.getString("myPictureUri", "");
        otherProfileTutorialShown = mSettings.getBoolean("otherProfileTutorial", true);
        dateStored = mSettings.getBoolean("dateStored", false);
        avatarUploaded = mSettings.getBoolean("avatarUploaded", false);

        // Achievements data
        connections_total = mSettings.getInt("connections_total", 0);
        connections_daily = mSettings.getInt("connections_daily", 0);
        achievements_total = mSettings.getInt("achievements_total", 0);
        likes_per_item = mSettings.getInt("likes_per_item", 0);
        messages_received = mSettings.getInt("messages_received", 0);
        messages_sent = mSettings.getInt("messages_sent", 0);
        face2face_total = mSettings.getInt("face2face_total", 0);
        likes_given = mSettings.getInt("likes_given", 0);
        whispers_received = mSettings.getInt("whispers_received", 0);
        days_used = mSettings.getInt("days_used", 0);
        lastDayString = mSettings.getString("last_day", getCurrentDate());

        // If myProfile was read successfully from SharedPreferences, use info to log-in
        // Otherwise, go to mLogInRegisterFragment to allow user to log-in/Register
        if(!myProfile.getId().equals("")) {
            initiate();
        }
        else {
            showTutorialAbout();
        }

        // Register the broadcast receiver to get Intents from the service
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START_F2F);
        intentFilter.addAction(IMAGE_SELECTED);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // Update days used
        lastDayDate = stringToDate(lastDayString);
        if(compareDays(lastDayDate)) {
            days_used++;
            mEditor.putInt("days_used", days_used);
            mEditor.putString("last_day", getCurrentDate());
            mEditor.commit();
            progressAchievements("days_used");
        }
        //Log.d(TAG, "Dates: " + lastDayString + " " + lastDayDate + " " + days_used);
        if(!dateStored) {
            mEditor.putString("last_day", lastDayString);
            dateStored = true;
            mEditor.putBoolean("dateStored", dateStored);
            mEditor.commit();
        }

        // TODO: debug this!
        getAllUsers(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        N2U_Application.activityResumed();

//        getAllUsers(1);
//        if(currentFragment == mConnectionsListFragment)
//            mConnectionsListFragment.refreshConnections();
    }

    @Override
    protected void onPause() {
        super.onPause();
        N2U_Application.activityPaused();
    }

    // TODO: Implement!
    @Override
    public void onBackPressed() {
        //Log.d(TAG, "onBackPressed()");
        if(currentFragment == mOtherProfileGridFragment || currentFragment == mMyProfileViewGridFragment
                || currentFragment == mAchievementsGridFragment) {
            onConnectionsButton();
        }
        else if(currentFragment == mConnectionsGridFragment) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        else if(currentFragment == mChatFragment)
            mChatFragment.goBack();
    }

    public void initiate() {
//        Log.d(TAG, "initiate()");
//        for(int i=0; i<mUsers.size(); i++)
//            Log.d(TAG, "user: " + i + ". " + mUsers.get(i).getUsername());

        // Trigger the service
        Intent startServiceIntent = new Intent(this, N2UService.class);
        startServiceIntent.putExtra("Trigger", "Activity");
        startService(startServiceIntent);

        // Get list of liked clues
        getLikedClues();

        // Go to my connections - Grid
        showHeader();

        getSupportFragmentManager().beginTransaction().add(R.id.header_layout, mHeaderFragment).commit();

        if(registered)
            onHomeButton();
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mConnectionsGridFragment).commit();
            currentFragment = mConnectionsGridFragment;
        }

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.getStringExtra("MFS") != null)
                if(intent.getStringExtra("MFS").equals("ConnectionUpdates")) {
                    Log.d(TAG, "FROM SERVICE! - NOTIFICATION");
                }

            // If the activity was started for F2F
            if(intent.getStringExtra("F2F") != null) {
                //Toast.makeText(getApplicationContext(), "Will go to profile of:" + intent.getStringExtra("F2F"), Toast.LENGTH_SHORT).show();

                // Perform a request to the server for the connection between the current user and the one who requested the F2F

                HTTPRequest request = new HTTPRequest("GetConnection",
                        HOST + PORT + "/connections/" + intent.getStringExtra("connectionId"),
                        Request.Method.GET,
                        null
                );
                mHTTPHelper.sendToServer(request);

                usernameF2F = intent.getStringExtra("senderUsername");
                idF2F = intent.getStringExtra("senderId");
            }
            if(intent.getStringExtra("Achievement") != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mAchievementsGridFragment).commit();
                currentFragment = mAchievementsGridFragment;
            }
            if(intent.getStringExtra("days_used") != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mConnectionsGridFragment).commit();
                currentFragment = mConnectionsGridFragment;
            }
        }

        getTotalMessages();
    }

    public void goToOtherProfileForF2F(JSONObject response) {
//        Log.d(TAG, "goToOtherProfileForF2F" + response);

        Connection connection = null;
        try {
            connection = new Connection(response.getString("_id"),
                    response.getString("_user1Id"),
                    response.getString("_user2Id"),
                    Integer.parseInt(response.getString("timesMet")),
                    Integer.parseInt(response.getString("faceToFace")),
                    stringToDate(response.getString("lastFaceToFace")),
                    stringToDate(response.getString("lastMet")),
                    stringToDate(response.getString("lastUpdate")),
                    Boolean.valueOf(response.getString("blocked"))
                    );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mOtherProfileGridFragment.mConnection = connection;
        mOtherProfileGridFragment.connectionUsername = usernameF2F;
        mOtherProfileGridFragment.connectionId = idF2F;

        connectionsGridItemOnClick(connection, usernameF2F, idF2F);

    }

    public void checkBluetoothConditions() {

        // Check android version of the phone
        if(Build.VERSION.RELEASE.startsWith("6") || Build.VERSION.RELEASE.startsWith("7")) {

            String macAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
//            Log.d(TAG, "Bluetooth deviceID secure: " + macAddress);
            myProfile.setDeviceId(macAddress);
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("This application is not compatible with your device's operating system version.")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            finish();
//                        }
//                    });
//            builder.setTitle("Sorry!");
//            // Create the AlertDialog object and return it
//            builder.setCancelable(false);
//            AlertDialog dialog = builder.create();
//            dialog.show();
        }
        else {
            // Attempt to get the right Bluetooth Mac address
            myProfile.setDeviceId(mBluetoothHelper.init());
//            Log.d(TAG, "Bluetooth deviceID: " + myProfile.getDeviceId());

            if (myProfile.getDeviceId().equals("02:00:00:00:00:00")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("There was a problem with acquiring your device's ID. " +
                        "Please try disabling/enabling Bluetooth before you start the app.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.setTitle("Bluetooth Adapter Error!");
                // Create the AlertDialog object and return it
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }


    public void getAllUsers(int refresh) {
        String message;
        if (refresh == 1)
            message = "GetAllUsers - Refresh";
        else
            message = "GetAllUsers";

        //mUsers.clear();
        // Get a list of all users from the server
        HTTPRequest request = new HTTPRequest(message,
                HOST + PORT + "/profiles/",
                Request.Method.GET,
                null
        );
        mHTTPHelper.sendToServerArray(request);
    }

    public void logIn(JSONObject response) {
        try {
//            Log.d(TAG, "Response: " + response + " " + response.getString("_id"));

            // If the _id is -1, username and password were not found in the database
            if (response.getString("_id").equals("-1"))
                mLogInRegisterFragment.wrongCredentials("both", "not_found");
            else {
                // Update myProfile and SharedPreferences with session info
                myProfile.setId(response.getString("_id"));
                myProfile.setUsername(response.getString("username"));
                myProfile.setPassword(response.getString("password"));
                myProfile.setActive(Boolean.valueOf(response.getString("active")));
                myProfile.setClues(Integer.parseInt(response.getString("clues")));
                mEditor.putString("username", myProfile.getUsername());
                mEditor.putString("password", myProfile.getPassword());
                mEditor.putString("deviceId", myProfile.getDeviceId());
                mEditor.putString("_id", myProfile.getId());
                mEditor.putString("clues", Integer.toString(myProfile.getClues()));
                mEditor.commit();

                loggedIn = true;
                Toast.makeText(getApplicationContext(), "Log in successful.", Toast.LENGTH_SHORT).show();

                initiate();
            }
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public void register(JSONObject response) {
        try {
//            Log.d(TAG, "Response: " + response + " " + response.getString("_id"));

            // If the returned _id is -1, username already exists
            if(response.getString("_id").equals("-1")) {
                mLogInRegisterFragment.wrongCredentials("username", "exists");
            }
            else {
                // Update myProfile and SharedPreferences with session info
                myProfile.setId(response.getString("_id"));
                myProfile.setUsername(response.getString("username"));
                myProfile.setPassword(response.getString("password"));
                myProfile.setActive(false);
                myProfile.setClues(0);
                mEditor.putString("username", myProfile.getUsername());
                mEditor.putString("password", myProfile.getPassword());
                mEditor.putString("deviceId", myProfile.getDeviceId());
                mEditor.putString("_id", myProfile.getId());
                mEditor.putString("active", "false");
                mEditor.putString("clues", Integer.toString(myProfile.getClues()));
                mEditor.putBoolean("otherProfileTutorial", true);
                otherProfileTutorialShown = true;
                mEditor.commit();

                loggedIn = true;
                Toast.makeText(getApplicationContext(), "Register successful.", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "Profile: " + myProfile.getUsername());



                registered = true;
                initiate();
            }
            } catch (JSONException e) { e.printStackTrace(); }
    }

    public void populateCluesGrid(JSONArray response) {

        JSONObject tempObject;
        try {
            mMyProfileViewGridFragment.loadingPB.setVisibility(View.INVISIBLE);
            if(response.length() == 0) {
                //mMyProfileViewGridFragment.infoTV.setVisibility(View.VISIBLE);
                mMyProfileViewGridFragment.showInfo(true);
            }
            for(int i=0; i<response.length(); i++) {
                tempObject = (JSONObject) response.get(i);

                // Create a new Clue object to populate a list of one of the Fragments
                Clue clue = new Clue(tempObject.getString("_id"),
                        tempObject.getString("_ownerId"),
                        tempObject.getString("question"),
                        tempObject.getString("answer"),
                        Integer.parseInt(tempObject.getString("orderNumber")),
                        stringToDate(tempObject.getString("updated")),
                        Integer.parseInt(tempObject.getString("likes")));
                //Integer.parseInt(tempObject.getString("orderNumber")),

                // Check if _id from response matches the one from myProfile
                if(tempObject.getString("_ownerId").equals(myProfile.getId())) {
                    mMyProfileViewGridFragment.addItemToList(clue);

                    if(i==0 && myInfo != null)
                        myInfo.clear();

                    myInfo.add(clue);
                }
            }
//            tempObject = (JSONObject) response.get(0);
//            Log.d(TAG, "id: " + tempObject.getString("_id"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onClueSavedGrid(JSONObject response, boolean updatedFlag) {
        //Toast.makeText(getApplicationContext(), "Information saved successfully.", Toast.LENGTH_LONG).show();

        if(!updatedFlag) {
            try {
                JSONObject tempObject = (JSONObject) response;

                // Create a new Clue object to populate a list of one of the Fragments
                Clue clue = new Clue(tempObject.getString("_id"),
                        tempObject.getString("_ownerId"),
                        tempObject.getString("question"),
                        tempObject.getString("answer"),
                        Integer.parseInt(tempObject.getString("orderNumber")),
                        stringToDate(tempObject.getString("updated")),
                        Integer.parseInt(tempObject.getString("likes"))
                        );

                // Check if _id from response matches the one from myProfile
//                Log.d(TAG, "IDs: " + tempObject.getString("_ownerId") + " " + myProfile.getId());
                if (tempObject.getString("_ownerId").equals(myProfile.getId())) {
                    mMyProfileViewGridFragment.addItemToList(clue);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mMyProfileViewFragment).commit();
            //getSupportFragmentManager().beginTransaction().add(R.id.header_layout, mHeaderFragment).commit();
            mMyProfileViewGridFragment.refreshGrid();
        }
    }

    public void onClueSavedGridOther(JSONObject response) {

        if(currentFragment == mOtherProfileGridFragment) {

            try {
                JSONObject tempObject = (JSONObject) response;

                // Create a new Clue object to populate a list of one of the Fragments
                Clue clue = new Clue(tempObject.getString("_id"),
                        tempObject.getString("_ownerId"),
                        tempObject.getString("question"),
                        tempObject.getString("answer"),
                        Integer.parseInt(tempObject.getString("orderNumber")),
                        stringToDate(tempObject.getString("updated")),
                        Integer.parseInt(tempObject.getString("likes")));

                // Check if _id from response matches the one from fragment
                //Log.d(TAG, "IDs: " + tempObject.getString("_ownerId") + " " + myProfile.getId());
                if (tempObject.getString("_ownerId").equals(mOtherProfileGridFragment.connectionId)) {
                    mOtherProfileGridFragment.refreshGrid();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void populateConnectionsGrid(JSONArray response) {
        myConnections.clear();
        boolean connectionsMoreThanZero = false;

        JSONObject tempObject;
        try {
            mConnectionsGridFragment.loadingPB.setVisibility(View.INVISIBLE);   // Hide the progress bar
            if(response.length() == 0 ) {
                //mConnectionsGridFragment.infoTV.setVisibility(View.VISIBLE);
                mConnectionsGridFragment.showInfo(true);
            }
            else if(response.length() > 0) {
                mConnectionsGridFragment.totalConnections = response.length();
                mConnectionsGridFragment.showInfo(false);
            }

            for(int i=0; i<response.length(); i++) {
                tempObject = (JSONObject) response.get(i);
//                Log.d(TAG, "populateConnectionsGrid - object: " + tempObject.toString());

                if(tempObject.getString("blocked").equals("false")) {
                    connectionsMoreThanZero = true;
                    // Create a new connection to populate the list in ConnectionsListFragment
                    Connection connection = new Connection(tempObject.getString("_id"),
                            tempObject.getString("_user1Id"),
                            tempObject.getString("_user2Id"),
                            Integer.parseInt(tempObject.getString("timesMet")),
                            Integer.parseInt(tempObject.getString("faceToFace")),
                            stringToDate(tempObject.getString("lastFaceToFace")),
                            stringToDate(tempObject.getString("lastMet")),
                            stringToDate(tempObject.getString("lastUpdate")),
                            Boolean.getBoolean(tempObject.getString("blocked"))
                    );
                    mConnectionsGridFragment.addItemToList(connection);
                    myConnections.add(connection);

//                    if(i == response.length()-1)
//                        mConnectionsGridFragment.scrollTo(2,2);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!connectionsMoreThanZero)
            mConnectionsGridFragment.showInfo(true);

        setMyConnections(myConnections);
    }

    public void loadConversation(JSONArray response) {

        JSONObject tempObject;
        try {
            for(int i=0; i<response.length(); i++) {
                tempObject = (JSONObject) response.get(i);
//                Log.d(TAG, "loadConversation(): " + tempObject.toString());

                if(tempObject.getString("_senderId").equals(myProfile.getId()))
                    mChatFragment.addMyMessage(myProfile.getUsername(), tempObject.getString("messageText"), stringToDate(tempObject.getString("timeSent")));
                else
                    mChatFragment.addOtherMessage(mChatFragment.connectionUsername, tempObject.getString("messageText"), stringToDate(tempObject.getString("timeSent")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//        Log.d("onActivityResult", "requestCode: " + requestCode + ", resultCode: " + resultCode);

        if(requestCode == mBluetoothHelper.REQUEST_ENABLE_BT) {
            // If enabling Bluetooth was denied, exit the app
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Enabling bluetooth was denied", Toast.LENGTH_LONG).show();
                finish();
            }
            // If bluetooth was enabled, ask to enable discoverability
            else if(resultCode == Activity.RESULT_OK && !mBluetoothHelper.getDiscoverableStatus()) {
                mBluetoothHelper.enableDiscoverability();
            }
        }
        else if(requestCode == mBluetoothHelper.REQUEST_ENABLE_DISCOVERABILITY) {
            // If enabling discoverability was denied, exit the app
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Enabling discoverability was denied", Toast.LENGTH_LONG).show();
                finish();
            }
            // If discoverability was enabled, start scanning for nearby devices.
            else {
                Log.d(TAG, "Bluetooth OK");
                //scheduleAlarm();
            }
        }
    }

    public void activateUser() {


//        Log.d(TAG, "Activating user: " + myProfile.getUsername());
        myProfile.setActive(true);
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("_id", myProfile.getId());
        params.put("username", myProfile.getUsername());
        params.put("password", myProfile.getPassword());
        params.put("deviceId", myProfile.getDeviceId());
        params.put("active", Boolean.toString(myProfile.getActive()));
        params.put("clues", Integer.toString(myProfile.getClues()));
        JSONObject req = new JSONObject(params);


        HTTPRequest request;
        request = new HTTPRequest("UpdateUser",
                HOST + PORT + "/profiles/" + myProfile.getId() + "/edit",
                Request.Method.PUT,
                req);
        mHTTPHelper.sendToServer(request);

        // Update shared preferences
        mEditor.putString("active", "true");
        mEditor.commit();
    }

    public void deactivateUser() {

//        Log.d(TAG, "Deactivating user: " + myProfile.getUsername());
        myProfile.setActive(true);
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("_id", myProfile.getId());
        params.put("username", myProfile.getUsername());
        params.put("password", myProfile.getPassword());
        params.put("deviceId", myProfile.getDeviceId());
        params.put("active", "false");
        params.put("clues", Integer.toString(myProfile.getClues()));
        JSONObject req = new JSONObject(params);


        HTTPRequest request;
        request = new HTTPRequest("UpdateUser",
                HOST + PORT + "/profiles/" + myProfile.getId() + "/edit",
                Request.Method.PUT,
                req);
        mHTTPHelper.sendToServer(request);

        // Update shared preferences
        mEditor.putString("active", "false");
        mEditor.commit();
    }

    public List<Connection> getMyConnections() {

        List<Connection> connections = new ArrayList<Connection>();

        // Open the file to read the list of connections for the current user
        String filename = "N2Uconnections";
        File file = new File(this.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = this.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                connections = (List<Connection>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return connections;
    }

    public void setMyConnections(List<Connection> connections) {

        // Write the list of connections to the shared file
        String filename = "N2Uconnections";
        try {
            File file = new File(getApplicationContext().getFilesDir(), filename);

            // Create the file if it doesn't already exist
            if (!file.exists()) {
//                Log.d(TAG, "setMyConnections() - file does not exist");
                file.createNewFile();
            }
            // Write to the file
            FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(connections);
            objectOutputStream.close();
            outputStream.close();
            Log.d(TAG, "setMyConnections() - updates saved to file");

        } catch (IOException e) {
            Log.e(TAG, "Unable to access file, " + e.toString());
        }

    }

    public void hideHeader() {

        FrameLayout headerLayout = (FrameLayout) this.findViewById(R.id.header_layout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) headerLayout.getLayoutParams();
        params.weight = 0;
        headerLayout.setLayoutParams(params);

        FrameLayout mainLayout = (FrameLayout) this.findViewById(R.id.main_layout);
        params = (LinearLayout.LayoutParams) mainLayout.getLayoutParams();
        params.weight = 1;
        mainLayout.setLayoutParams(params);
    }

    public void showHeader() {

        FrameLayout headerLayout = (FrameLayout) this.findViewById(R.id.header_layout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) headerLayout.getLayoutParams();
        params.weight = (float) .09;
        headerLayout.setLayoutParams(params);

        FrameLayout mainLayout = (FrameLayout) this.findViewById(R.id.main_layout);
        params = (LinearLayout.LayoutParams) mainLayout.getLayoutParams();
        params.weight = (float) .91;
        mainLayout.setLayoutParams(params);

//        if(currentFragment == mAchievementsGridFragment)
//            mHeaderFragment.
    }

    public void updateLikedClues(Clue clue) {

        mLikedClues.put(clue.getId(), true);
        File file = new File(getApplicationContext().getFilesDir(), "N2U_likedclues");

        try {
            // Create the file if it doesn't already exist
            if (!file.exists()) {
                Log.d(TAG, "setMyConnections() - file does not exist");
                file.createNewFile();
            }
            // Write to the file
            FileOutputStream outputStream = getApplicationContext().openFileOutput("N2U_likedclues", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(mLikedClues);
            objectOutputStream.close();
            outputStream.close();
        }
        catch (IOException e) {
        }
    }

    public void getLikedClues() {
        String filename = "N2U_likedclues";
        File file = new File(this.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = this.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                mLikedClues = (HashMap<String, Boolean>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

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

    public Boolean checkMessages(String id) {

        getNewMessages();

        if(mNewMessages.size() > 0) {

            if(mNewMessages.get(id) != null) {
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }

    public Boolean checkClues(String id) {

//        Log.d(TAG, "checkClues for id: " + id);
        //getNewClues();
        String filename = "N2U_newClues";
        File file = new File(this.getFilesDir(), filename);
        HashMap<String, Boolean> tempNewClues = new HashMap<>();


        if (file.exists()) {
            try {
                FileInputStream fileInputStream = this.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                tempNewClues = (HashMap<String, Boolean>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
//        Log.d(TAG, "tempNewClues.size: " + tempNewClues.size() + " " + tempNewClues);

//        for(int i=0; i<tempNewClues.size(); i++)
//            Log.d(TAG, "tempNewClues: " + tempNewClues.get(id) + " id: " + id);

        if(tempNewClues.size() > 0) {

            if(tempNewClues.get(id) != null) {
                return true;
            }
            else return false;
        }
        else
            return false;
    }

    public void getAchievements() {
        String filename = "N2U_achievements";
        File file = new File(this.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = this.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                mAchievements = (HashMap<String, Achievement>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.d(TAG, "getAchievements() - file does not exist");
        }
    }

    public void updateAchievements() {

        String filename = "N2U_achievements";
        try {
            File file = new File(getApplicationContext().getFilesDir(), filename);

            // Create the file if it doesn't already exist
            if (!file.exists()) {
                Log.d(TAG, "updateAchievements() - file does not exist");
                file.createNewFile();
            }
            // Write to the file
            FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(mAchievements);
            objectOutputStream.close();
            outputStream.close();
            Log.d(TAG, "updateAchievements() - updates saved to file");

        } catch (IOException e) {
            Log.e(TAG, "Unable to access file, " + e.toString());
        }
    }

    public boolean createAchievements() {

        if(mAchievements.size() == 0) {
            Achievement tempAchievement = null;

            tempAchievement = new Achievement("connections_total", "Hermit", "Total profiles collected:", 0, 5);
            mAchievements.put("connections_total", tempAchievement);

//            tempAchievement = new Achievement("connections_daily", "Lurking", "Profiles collected in one day:", 0, 3);
//            mAchievements.put("connections_daily", tempAchievement);

            tempAchievement = new Achievement("likes_per_item", "Pathetic", "Likes received for one whisper:", 0, 5);
            mAchievements.put("likes_per_item", tempAchievement);

            tempAchievement = new Achievement("messages_received", "Not a word", "Messages received:", 0, 5);
            mAchievements.put("messages_received", tempAchievement);

            tempAchievement = new Achievement("messages_sent", "Illiterate", "Messages sent:", 0, 10);
            mAchievements.put("messages_sent", tempAchievement);

            tempAchievement = new Achievement("face2face_total", "Loner", "Face-to-Face meetings:", 0, 3);
            mAchievements.put("face2face_total", tempAchievement);

            tempAchievement = new Achievement("likes_given", "Meh...", "Whispers liked:", 0, 7);
            mAchievements.put("likes_given", tempAchievement);

            tempAchievement = new Achievement("whispers_received", "Minimalist", "Times met with others:", 0, 15);
            mAchievements.put("whispers_received", tempAchievement);

            tempAchievement = new Achievement("days_used", "Nice to meet you!", "Days using the app:", 0, 1);
            mAchievements.put("days_used", tempAchievement);

            updateAchievements();
            return true;
        }

        return false;
    }

    public boolean progressAchievements(String type) {
        boolean progressFlag = false;

        switch(type) {
            case "connections_total" :
                // If the rank of the achievement is lower than the maximum (3)
                mAchievements.get(type).setCurrentValue(connections_total);
                if(mAchievements.get(type).getRank() < 3) {

                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(connections_total >= mAchievements.get(type).getMaxValue()) {

                        // 0, 5, 8, 12 -> 0, 5, 20, 50
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(20);
                                mAchievements.get(type).setTitle("Villager");
                                break;
                            case 1:
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(50);
                                mAchievements.get(type).setTitle("Townsman");
                                break;
                            case 2:
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Cosmopolitan");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            case "likes_per_item" :
                // If the rank of the achievement is lower than the maximum (3)
                mAchievements.get(type).setCurrentValue(likes_per_item);
                if(mAchievements.get(type).getRank() < 3) {

                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(likes_per_item >= mAchievements.get(type).getMaxValue()) {

                        // 0, 3, 5, 10 -> 0, 5, 15, 30
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(15);
                                mAchievements.get(type).setTitle("Rising star");
                                break;
                            case 1:
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(30);
                                mAchievements.get(type).setTitle("Popular");
                                break;
                            case 2:
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Kardashian");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            case "face2face_total" :
                // If the rank of the achievement is lower than the maximum (3)
                Log.d(TAG, "face2face_total");
                mAchievements.get(type).setCurrentValue(face2face_total);
                if(mAchievements.get(type).getRank() < 3) {

//                    Log.d(TAG, "face2face_total value: " + face2face_total + " maxValue: " + mAchievements.get(type).getMaxValue());
                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(face2face_total >= mAchievements.get(type).getMaxValue()) {

                        //  0, 1, 5, 10 -> 0, 3, 15, 30
//                        Log.d(TAG, "face2face_total, rank: " + mAchievements.get(type).getRank());
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                Log.d(TAG, "face2face_total, case 0");
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(10);
                                mAchievements.get(type).setTitle("Introvert");
                                break;
                            case 1:
                                Log.d(TAG, "face2face_total, case 0");
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(20);
                                mAchievements.get(type).setTitle("Extrovert");
                                break;
                            case 2:
                                Log.d(TAG, "face2face_total, case 0");
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Party animal");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            case "likes_given" :
                // If the rank of the achievement is lower than the maximum (3)
                mAchievements.get(type).setCurrentValue(likes_given);
                if(mAchievements.get(type).getRank() < 3) {


                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(likes_given >= mAchievements.get(type).getMaxValue()) {

                        //  0, 7, 10, 20 -> 0, 7, 20, 50
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(20);
                                mAchievements.get(type).setTitle("Liker(t) scale");
                                break;
                            case 1:
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(50);
                                mAchievements.get(type).setTitle("Love is all around me");
                                break;
                            case 2:
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Everything is awesome!");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            case "whispers_received" :
                // If the rank of the achievement is lower than the maximum (3)
                mAchievements.get(type).setCurrentValue(whispers_received);
                if(mAchievements.get(type).getRank() < 3) {

                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(whispers_received >= mAchievements.get(type).getMaxValue()) {

                        //  0, 8, 15, 30 -> 0, 15, 40, 80
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(40);
                                mAchievements.get(type).setTitle("Hobbyist");
                                break;
                            case 1:
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(80);
                                mAchievements.get(type).setTitle("Pro");
                                break;
                            case 2:
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Hoarder");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            case "days_used" :
                // If the rank of the achievement is lower than the maximum (3)
                mAchievements.get(type).setCurrentValue(days_used);
                if(mAchievements.get(type).getRank() < 3) {

                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(days_used >= mAchievements.get(type).getMaxValue()) {

                        //  0, 1, 4, 7 -> 0, 1, 7, 14
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(7);
                                mAchievements.get(type).setTitle("Welcome back!");
                                break;
                            case 1:
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(14);
                                mAchievements.get(type).setTitle("Still interested?");
                                break;
                            case 2:
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Regular customer");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            case "messages_sent" :
                // If the rank of the achievement is lower than the maximum (3)
                mAchievements.get(type).setCurrentValue(messages_sent);
                Log.d(TAG, "progressAchievements messages_sent - " + " mAchievements.get(type).getCurrentValue" + mAchievements.get(type).getCurrentValue()
                        + " mAchievements.get(type).getRank() " + mAchievements.get(type).getRank()
                        + " mAchievements.get(type).getMaxValue() " + mAchievements.get(type).getMaxValue()
                    );
                if(mAchievements.get(type).getRank() < 3) {

                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(messages_sent >= mAchievements.get(type).getMaxValue()) {

                        //  0, 2, 10, 20 -> 0, 10, 50, 100
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(50);
                                mAchievements.get(type).setTitle("Scribbler");
                                break;
                            case 1:
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(100);
                                mAchievements.get(type).setTitle("Author");
                                break;
                            case 2:
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Spambot");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            case "messages_received" :
                // If the rank of the achievement is lower than the maximum (3)
                mAchievements.get(type).setCurrentValue(messages_received);
                if(mAchievements.get(type).getRank() < 3) {

                    // If the current value is greater than the maximum,
                    // create a notification, and update the max value, rank and title
                    if(messages_received >= mAchievements.get(type).getMaxValue()) {

                        //  0, 1, 5, 10 -> 0, 5, 15, 30
                        switch (mAchievements.get(type).getRank()) {
                            case 0:
                                mAchievements.get(type).setRank(1);
                                mAchievements.get(type).setMaxValue(15);
                                mAchievements.get(type).setTitle("You’ve got mail!");
                                break;
                            case 1:
                                mAchievements.get(type).setRank(2);
                                mAchievements.get(type).setMaxValue(30);
                                mAchievements.get(type).setTitle("PO Box");
                                break;
                            case 2:
                                mAchievements.get(type).setRank(3);
                                mAchievements.get(type).setTitle("Need a spam filter?");
                                break;
                        }
                        progressFlag = true;
                    }
                }
                break;
            default:
                break;
        }

        if(progressFlag) {
            achievements_total++;
            mEditor.putInt("achievements_total", achievements_total);
            mEditor.commit();

            createAchievementNotification("Achievement unlocked!", mAchievements.get(type).getTitle());

            updateAchievements();

            // ********************************************************
            // Store achievement to the database on the server
            String tag = "Add Achievement";
            String route = "/achievements";
            int method = Request.Method.POST;

            HashMap<String, String> params = new HashMap<String, String>();
            HTTPRequest request;

            params.put("_ownerId", myProfile.getId());
            params.put("type", type);
            params.put("title",  mAchievements.get(type).getTitle());
            params.put("text", mAchievements.get(type).getText());
            params.put("rank", String.valueOf(mAchievements.get(type).getRank()));
            params.put("date", getCurrentDate());
            JSONObject req = new JSONObject(params);

            request = new HTTPRequest(tag,
                    HOST + PORT + route,
                    method,
                    req);

            mHTTPHelper.sendToServer(request);
            // ********************************************************

            return true;
        }
        else {
            updateAchievements();
            return false;
        }
    }

    public void createAchievementNotification(String title, String message) {

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
        resultIntent.putExtra("Achievement", title);
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
        mNotificationManager.notify(1, mBuilder.build());
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, -1);
//        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep);
//        mp.start();
    }

    public void getTotalMessages() {
        // Get total number of sent messages
        HTTPRequest request = new HTTPRequest("SentMessages",
                HOST + PORT + "/messages/conversation/senderId=" + myProfile.getId(),
                Request.Method.GET,
                null
        );

        mHTTPHelper.sendToServerArray(request);

        // Get total number of received messages
        request = new HTTPRequest("ReceivedMessages",
                HOST + PORT + "/messages/conversation/recipientId=" + myProfile.getId(),
                Request.Method.GET,
                null
        );

        mHTTPHelper.sendToServerArray(request);

    }

    public void onGetSentMessages(JSONArray response) {
        //if(messages_sent < response.length()) {
            messages_sent = response.length();
            mEditor.putInt("messages_sent", messages_sent);
            mEditor.commit();

            // Check for achievements
            progressAchievements("messages_sent");
        //}
    }

    public void onGetReceivedMessages(JSONArray response) {
        //if(messages_received < response.length()) {
            messages_received = response.length();
            mEditor.putInt("messages_received", messages_received);
            mEditor.commit();

            // Check for achievements
            progressAchievements("messages_received");
        //}
    }


    /* ************************************************************************************************************* */
    /************************************************ Fragment Interfaces ********************************************/

    // LogInRegisterFragment: when the Log In button is pressed
    @Override
    public void onLogInButton(String username, String password) {
//        Log.d(TAG, "onLogInButton " + username + " " + password);

        HTTPRequest request = new HTTPRequest("LogIn",
                HOST + PORT + "/profiles/login/" + username + "/" + password,
                Request.Method.GET,
                null
        );

        mHTTPHelper.sendToServer(request);
    }

    // LogInRegisterFragment: when the Register button is pressed
    @Override
    public void onRegisterButton(String username, String password) {
//        Log.d(TAG, "onRegisterInButton " + username + " " + password + " community: " + myProfile.getCommunity() + " age: " + myProfile.getAge() + " gender: " + myProfile.getGender());

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);
        params.put("deviceId", myProfile.getDeviceId());
        params.put("active", Boolean.toString(false));
        params.put("clues", Integer.toString(myProfile.getClues()));
        params.put("email", myProfile.getEmail());
        params.put("age", Integer.toString(myProfile.getAge()));
        params.put("gender", myProfile.getGender());
        params.put("community", myProfile.getCommunity());
        JSONObject req = new JSONObject(params);

        HTTPRequest request = new HTTPRequest("Register",
                HOST + PORT + "/profiles",
                Request.Method.POST,
                req);

        mHTTPHelper.sendToServer(request);
    }

    // MyProfileViewGridFragment
    @Override
    public void getCluesForUserGrid() {
        Log.d(TAG, "getCluesForUserGrid()");

        HTTPRequest request = new HTTPRequest("GetCluesGrid",
                HOST + PORT + "/clues/ownerId=" + myProfile.getId(),
                Request.Method.GET,
                null
        );
//        Log.d(TAG, "Request: " + request.toString());
        mHTTPHelper.sendToServerArray(request);
    }


    // HeaderFragment
    @Override
    public void onConnectionsButton() {
        Log.d(TAG, "onConnectionsButton");

        // Go to list of connections
        if(currentFragment != mConnectionsGridFragment) {
            if(currentFragment == mMyProfileViewGridFragment) {
                if(mMyProfileViewGridFragment.atLeastThreeWhispers()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    String tutorialTitle = "Please share more whispers!";
                    String tutorialText = "The more whispers you share, the more you can receive.";
                    builder.setMessage(Html.fromHtml(tutorialText))
                            .setTitle(Html.fromHtml(tutorialTitle));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mConnectionsGridFragment).commit();
                            currentFragment = mConnectionsGridFragment;
                            mHeaderFragment.updateIcons("myConnections");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mConnectionsGridFragment).commit();
                    currentFragment = mConnectionsGridFragment;
                    mHeaderFragment.updateIcons("myConnections");
                }
            }
            else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mConnectionsGridFragment).commit();
                currentFragment = mConnectionsGridFragment;
                mHeaderFragment.updateIcons("myConnections");
            }
        }
        else
            mConnectionsGridFragment.refreshFragment();
        showHeader();
    }

    // HeaderFragment
    @Override
    public void onHomeButton() {
        Log.d(TAG, "onHomeButton");

        // Go to my profile
        if(currentFragment != mMyProfileViewGridFragment) {
            currentFragment = mMyProfileViewGridFragment;
            mHeaderFragment.updateIcons("myProfile");
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mMyProfileViewGridFragment).commit();
        }
    }

    // HeaderFragment
    @Override
    public void onAchievementsButton() {
        if(currentFragment == mMyProfileViewGridFragment) {
            if(mMyProfileViewGridFragment.atLeastThreeWhispers()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String tutorialTitle = "Please share more whispers!";
                String tutorialText = "The more whispers you share, the more you can receive.";
                builder.setMessage(Html.fromHtml(tutorialText))
                        .setTitle(Html.fromHtml(tutorialTitle));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mAchievementsGridFragment).commit();
                        currentFragment = mAchievementsGridFragment;
                        mHeaderFragment.updateIcons("myStats");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mAchievementsGridFragment).commit();
                currentFragment = mAchievementsGridFragment;
                mHeaderFragment.updateIcons("myStats");
            }
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mAchievementsGridFragment).commit();
            currentFragment = mAchievementsGridFragment;
            mHeaderFragment.updateIcons("myStats");
        }
    }

    @Override
    public void getConnectionsGridForUser() {
        HTTPRequest request = new HTTPRequest("GetConnectionsGrid",
                HOST + PORT + "/connections/userId=" + myProfile.getId(),
                Request.Method.GET,
                null
        );
        mHTTPHelper.sendToServerArray(request);
    }

    public void connectionsGridItemOnClick(Connection connection, String username, String id) {
//        Log.d(TAG, "connectionsListItemOnClick() " + connection._id + " - " + connection.lastMet);

        mOtherProfileGridFragment.mConnection = connection;
        mOtherProfileGridFragment.connectionUsername = username;
        mOtherProfileGridFragment.connectionId = id;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //fragmentTransaction.setCustomAnimations(getResources()., R.animator.fade_out);
        fragmentTransaction.setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom);
        //fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        fragmentTransaction.replace(R.id.main_layout, mOtherProfileGridFragment);
        fragmentTransaction.commit();

        //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mOtherProfileGridFragment).commit();
        currentFragment = mOtherProfileGridFragment;

        hideHeader();
    }

    @Override
    public void getCluesForOtherGrid() {

        HTTPRequest request = new HTTPRequest("GetCluesOtherGrid",
                HOST + PORT + "/clues/ownerId=" + mOtherProfileGridFragment.connectionId,
                Request.Method.GET,
                null
        );
        mHTTPHelper.sendToServerArray(request);
    }

    @Override
    public void onChatButton(Connection connection, String username, String id) {

        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mChatFragment).commit();
        currentFragment = mChatFragment;
        mChatFragment.mConnection = connection;
        mChatFragment.connectionUsername = username;
        mChatFragment.connectionId = id;
    }

    public void populateCluesGridOther(JSONArray response) {

        JSONObject tempObject;
        try {
            mOtherProfileGridFragment.loadingPB.setVisibility(View.INVISIBLE);
//            if(response.length() == 0)
//                mOtherProfileFragment.infoTV.setVisibility(View.VISIBLE);

            for(int i=0; i<response.length(); i++) {
                tempObject = (JSONObject) response.get(i);

                // Create a new Clue object to populate a list of one of the Fragments
                Clue clue = new Clue(tempObject.getString("_id"),
                        tempObject.getString("_ownerId"),
                        tempObject.getString("question"),
                        tempObject.getString("answer"),
                        Integer.parseInt(tempObject.getString("orderNumber")),
                        stringToDate(tempObject.getString("updated")),
                        Integer.parseInt(tempObject.getString("likes")));
                //Integer.parseInt(tempObject.getString("orderNumber")),

                // If size
                mOtherProfileGridFragment.addItemToList(clue, i);
            }
            // If the number of the other user's whispers is greater
            // than the number of whispers the user has shared
            if((response.length() < myProfile.getClues()) && (response.length() < mOtherProfileGridFragment.mConnection.timesMet)) {
                mOtherProfileGridFragment.otherUserHasMoreWhispers();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*********************************** FROM SERVICE ***********************************/
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "broadcastReceiver - onReceive()");

            if(intent.getAction().equals(START_F2F)) {

                Bundle args = new Bundle();
                args.putString("connectionId", intent.getStringExtra("connectionId"));
                args.putString("senderId", intent.getStringExtra("senderId"));
                args.putString("senderUsername", intent.getStringExtra("senderUsername"));


                // If the user is not currently on the sender's profile
                if(currentFragment != mOtherProfileGridFragment ||
                        (currentFragment == mOtherProfileGridFragment && !(mOtherProfileGridFragment.mConnection._id.equals(intent.getStringExtra("connectionId"))) )) {

                    F2FDialogFragment dialogFragment = new F2FDialogFragment();
                    dialogFragment.setArguments(args);
                    dialogFragment.show(getSupportFragmentManager(), "dialogFragmentTag");

                }
            }
            else if(intent.getAction().equals(IMAGE_SELECTED)) {
                Log.d(TAG, "intent action = IMAGE_SELECTED");
                if(currentFragment == mMyProfileViewGridFragment) {
                    Log.d(TAG, "intent action = IMAGE_SELECTED");
                    mMyProfileViewGridFragment.updateAvatar();
                    if(!avatarUploaded)
                        mMyProfileViewGridFragment.uploadImage();
                }
            }
        }
    };

    public static class F2FDialogFragment extends DialogFragment {

        private String username;
        private String connectionId;
        private String senderId;

        public F2FDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.knock2);
            mp.start();

            Bundle bundle = this.getArguments();
            this.username = bundle.getString("senderUsername");
            this.connectionId = bundle.getString("connectionId");
            this.senderId = bundle.getString("senderId");

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("User " + username + " wants to initiate a Face2Face meeting.\nGo to their profile?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HTTPRequest request = new HTTPRequest("GetConnection",
                                    HOST + PORT + "/connections/" + connectionId,
                                    Request.Method.GET,
                                    null
                            );
                            mHTTPHelper.sendToServer(request);

                            usernameF2F = username;
                            idF2F = senderId;
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


    public void showTutorialAbout() {
        LayoutInflater li = LayoutInflater.from(this);
        final View dialogView = li.inflate(R.layout.dialog_tutorial_about, null);

        TextView titleTV = (TextView) dialogView.findViewById(R.id.titleTV);
        titleTV.setTypeface(futuraBold);
        TextView about1TV = (TextView) dialogView.findViewById(R.id.about1TV);
        about1TV.setTypeface(futura);
        TextView about2TV = (TextView) dialogView.findViewById(R.id.about2TV);
        about2TV.setTypeface(futura);
        TextView about3TV = (TextView) dialogView.findViewById(R.id.about3TV);
        about3TV.setTypeface(futura);
        TextView about4TV = (TextView) dialogView.findViewById(R.id.about4TV);
        about4TV.setTypeface(futura);
//        TextView about5TV = (TextView) dialogView.findViewById(R.id.about5TV);
//        about5TV.setTypeface(futura);

        Button nextBN = (Button) dialogView.findViewById(R.id.nextBN);
        nextBN.setTypeface(futura);
        nextBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showTutorialResearch();
                showTutorialConsent();
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);

        final AlertDialog.Builder builder = alertDialogBuilder;
        tutorialDialog = builder.create();
        tutorialDialog.show();
    }

    public void showTutorialResearch() {
        tutorialDialog.dismiss();
        LayoutInflater li = LayoutInflater.from(this);
        final View dialogView = li.inflate(R.layout.dialog_tutorial_research, null);

        TextView titleTV = (TextView) dialogView.findViewById(R.id.titleTV);
        titleTV.setTypeface(futuraBold);
        TextView research1TV = (TextView) dialogView.findViewById(R.id.research1TV);
        research1TV.setTypeface(futura);
        TextView research2TV = (TextView) dialogView.findViewById(R.id.research2TV);
        research2TV.setTypeface(futura);
        TextView research3TV = (TextView) dialogView.findViewById(R.id.research3TV);
        research3TV.setTypeface(futura);

        Button nextBN = (Button) dialogView.findViewById(R.id.nextBN);
        nextBN.setTypeface(futura);
        nextBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTutorialConsent();
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);

        final AlertDialog.Builder builder = alertDialogBuilder;
        tutorialDialog = builder.create();
        tutorialDialog.show();
    }

    public void showTutorialConsent() {
        tutorialDialog.dismiss();
        LayoutInflater li = LayoutInflater.from(this);
        final View dialogView = li.inflate(R.layout.dialog_tutorial_consent, null);

        // Set typefaces for TextViews
        // ------------------------------------------------------
        TextView titleTV = (TextView) dialogView.findViewById(R.id.titleTV);
        titleTV.setTypeface(futuraBold);

        TextView researchTitle = (TextView) dialogView.findViewById(R.id.researchTitle);
        researchTitle.setTypeface(futuraBold);
        TextView researchText = (TextView) dialogView.findViewById(R.id.researchText);
        researchText.setTypeface(futura);

        TextView dataCollectionTitle = (TextView) dialogView.findViewById(R.id.dataCollectionTitle);
        dataCollectionTitle.setTypeface(futuraBold);
        TextView dataCollectionText = (TextView) dialogView.findViewById(R.id.dataCollectionText);
        dataCollectionText.setTypeface(futura);

        TextView anonymityTitle = (TextView) dialogView.findViewById(R.id.anonymityTitle);
        anonymityTitle.setTypeface(futuraBold);
        TextView anonymityText = (TextView) dialogView.findViewById(R.id.anonymityText);
        anonymityText.setTypeface(futura);

        TextView stopTitle = (TextView) dialogView.findViewById(R.id.stopTitle);
        stopTitle.setTypeface(futuraBold);
        TextView stopText = (TextView) dialogView.findViewById(R.id.stopText);
        stopText.setTypeface(futura);

//        TextView backgroundTitle = (TextView) dialogView.findViewById(R.id.backgroundTitle);
//        backgroundTitle.setTypeface(futuraBold);
//        TextView backgroundText = (TextView) dialogView.findViewById(R.id.backgroundText);
//        backgroundText.setTypeface(futura);
//
//        TextView genderTV = (TextView) dialogView.findViewById(R.id.genderTV);
//        genderTV.setTypeface(futuraBold);
//        TextView maleTV = (TextView) dialogView.findViewById(R.id.maleTV);
//        maleTV.setTypeface(futura);
//        TextView femaleTV = (TextView) dialogView.findViewById(R.id.femaleTV);
//        femaleTV.setTypeface(futura);
//        TextView otherTV = (TextView) dialogView.findViewById(R.id.otherTV);
//        otherTV.setTypeface(futura);
//
//        TextView ageTV = (TextView) dialogView.findViewById(R.id.ageTV);
//        ageTV.setTypeface(futuraBold);
//
//        TextView communityTV = (TextView) dialogView.findViewById(R.id.communityTV);
//        communityTV.setTypeface(futuraBold);
//
//        TextView emailTV = (TextView) dialogView.findViewById(R.id.emailTV);
//        emailTV.setTypeface(futuraBold);

        TextView agreementTV = (TextView) dialogView.findViewById(R.id.agreementTV);
        agreementTV.setTypeface(futura);

        // ------------------------------------------------------

        // EditTexts and other input forms
//        final RadioButton maleRB = (RadioButton) dialogView.findViewById(R.id.maleRB);
//        final RadioButton femaleRB = (RadioButton) dialogView.findViewById(R.id.femaleRB);
//        final RadioButton otherRB = (RadioButton) dialogView.findViewById(R.id.otherRB);
//        final EditText ageET = (EditText) dialogView.findViewById(R.id.ageET);
//        ageET.setTypeface(futura);
//        final Spinner communitySP = (Spinner) dialogView.findViewById(R.id.communitySP);
//        final EditText emailET = (EditText) dialogView.findViewById(R.id.emailET);
//        emailET.setTypeface(futura);
        final CheckBox agreementCB = (CheckBox) dialogView.findViewById(R.id.agreementCB);
//
//        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
//                R.array.communities_array, android.R.layout.simple_spinner_item);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        communitySP.setAdapter(spinnerAdapter);
//
//        maleRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (maleRB.isChecked()) {
//                    femaleRB.setChecked(false);
//                    otherRB.setChecked(false);
//                }
//            }
//        });
//        femaleRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if(femaleRB.isChecked()) {
//                    maleRB.setChecked(false);
//                    otherRB.setChecked(false);
//                }
//            }
//        });
//        otherRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if(otherRB.isChecked()) {
//                    maleRB.setChecked(false);
//                    femaleRB.setChecked(false);
//                }
//            }
//        });

        Button nextBN = (Button) dialogView.findViewById(R.id.nextBN);
        nextBN.setTypeface(futura);
        nextBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (agreementCB.isChecked()) {

                    myProfile.setGender("");
                    myProfile.setAge(0);
                    myProfile.setCommunity("");
                    myProfile.setEmail("");
                    tutorialDialog.dismiss();
                    getSupportFragmentManager().beginTransaction().add(R.id.main_layout, mLogInRegisterFragment).commit();
                    currentFragment = mLogInRegisterFragment;
                    hideHeader();

                    checkBluetoothConditions();

                } else
                    Toast.makeText(getApplicationContext(), "You must agree with the research terms.",
                            Toast.LENGTH_SHORT).show();
            }
        });
//        nextBN.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                // Check if all the required fields have been entered
//                if( (maleRB.isChecked() || femaleRB.isChecked()) &&
//                        (ageET.getText().length() > 0) &&
//                        (emailET.getText().length() > 0)) {
//
//                    if(agreementCB.isChecked()) {
//
//                        if(!emailET.getText().toString().contains("@")) {
//                            Toast.makeText(getApplicationContext(), "Please provide a valid email address.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            if(Integer.parseInt(ageET.getText().toString()) < 18 || Integer.parseInt(ageET.getText().toString()) > 99) {
//                                Toast.makeText(getApplicationContext(), "You have to insert your real age!",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                            else {
//                                if (maleRB.isChecked())
//                                    myProfile.setGender("Male");
//                                else if (femaleRB.isChecked())
//                                    myProfile.setGender("Female");
//                                else
//                                    myProfile.setGender("Other");
//                                myProfile.setAge(Integer.parseInt(ageET.getText().toString()));
//                                myProfile.setCommunity(communitySP.getSelectedItem().toString());
//                                myProfile.setEmail(emailET.getText().toString());
//
//                                tutorialDialog.dismiss();
//                                getSupportFragmentManager().beginTransaction().add(R.id.main_layout, mLogInRegisterFragment).commit();
//                                currentFragment = mLogInRegisterFragment;
//                                hideHeader();
//
//                                checkBluetoothConditions();
//                            }
//                        }
//                    }
//                    else
//                        Toast.makeText(getApplicationContext(), "You must agree with the research terms.",
//                                Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(getApplicationContext(), "All required fields must be filled.", Toast.LENGTH_SHORT)
//                            .show();
//                }
//            }
//        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);

        final AlertDialog.Builder builder = alertDialogBuilder;
        tutorialDialog = builder.create();
        tutorialDialog.show();
    }


    /*********************************** DATE FUNCTIONS ***************************************/

    //TODO: Update with push notification
    public boolean compareDates(Date date) {
        Date now = new Date();

        long diff = now.getTime() - date.getTime();

        //Log.d(TAG, "Now: " + dateToString(now) + ", date: " + dateToString(date));

        if(diff <= PROXIMITY_INTERACTION_INTERVAL)
            return true;
        else
            return false;

    }

    public long getDatesDiff(Date date) {
        Date now = new Date();

        long diff = now.getTime() - date.getTime();

        return diff;
    }

    public boolean compareDatesF2F(Date date) {
        Date now = new Date();

        long diff = now.getTime() - date.getTime();

//        Log.d(TAG, "F2F now: " + dateToString(now) + ", date: " + dateToString(date) + " diff: " + diff);

        if(diff > UPDATE_MIN_INTERVAL)
            return true;
        else
            return false;

    }

    public String datesDifference(Date date) {
        Date now = new Date();

        long minutes = 0;
        long hours = 0;
        long days = 0;

        String result = "";

        long diff = now.getTime() - date.getTime();

        minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        hours = TimeUnit.MILLISECONDS.toHours(diff);
        days = TimeUnit.MILLISECONDS.toDays(diff);

//        Log.d(TAG, "diff: " + diff + " " + minutes + " " + hours + " " + days);

        if(days == 1)
            result = "1 day ago";
        else if(days > 1)
            result = Long.toString(days) + " days ago";
        else if(hours == 1)
            result = "1 hour ago";
        else if(hours > 1)
            result = Long.toString(hours) + " hours ago";
        else if(minutes > TimeUnit.MILLISECONDS.toMinutes(PROXIMITY_INTERACTION_INTERVAL))
            result = Long.toString(minutes) + " mins ago";
        else if(compareDates(date))
            result = "now!";

        return result;
    }

    public String datesDifferenceChat(Date date) {
        Date now = new Date();

        long minutes = 0;
        long hours = 0;
        long days = 0;

        String result = "";

        long diff = now.getTime() - date.getTime();

        minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        hours = TimeUnit.MILLISECONDS.toHours(diff);
        days = TimeUnit.MILLISECONDS.toDays(diff);

//        Log.d(TAG, "diff: " + diff + " " + minutes + " " + hours + " " + days);

        if(days == 1)
            result = "1 day ago";
        else if(days > 1)
            result = Long.toString(days) + " days ago";
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            result = sdf.format(date);
        }

        return result;
    }

    public boolean compareDays(Date date) {
        // Returns true if current date is 1 or more days ahead
        // than date

        Date now = new Date();
        long days = 0;

        long diff = now.getTime() - date.getTime();
        days = TimeUnit.MILLISECONDS.toDays(diff);

        if(days >= 1)
            return true;
        else
            return false;
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
































