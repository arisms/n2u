package fi.tut.cs.social.proximeety.classes;

import android.app.Application;
import android.support.v4.app.Fragment;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by Aris on 10/02/16.
 */
public class N2U_Application extends Application {

    private static Socket mSocket;
    private static boolean activityVisible;
    private static Fragment currentFragment;
    private final String SOCKET_SERVER_URL = "http://social.cs.tut.fi:10002";
//    private final String SOCKET_SERVER_URL = "http://192.168.1.2:3001";

    // Connect to the Socket server
    {
        try {
            mSocket = IO.socket(SOCKET_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        //mSocket.io().reconnection(false);
    }

    public Socket getSocket() {
        return mSocket;
    };

    public void setSocket(Socket socket) {
        this.mSocket = socket;
    };

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public void setCurrentFragment(Fragment fragment) {
        this.currentFragment = fragment;
    }

    public static Fragment getCurrentFragment() {
        return currentFragment;
    }
}
