package fi.tut.cs.social.proximeety.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    public static final int REQUEST_CODE = 12345;
//    public static final String ACTION = "fi.tut.cs.social.proximeety";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Intent i = new Intent(context, N2UService.class);
        i.putExtra("Trigger", "Alarm");
        context.startService(i);
    }
}