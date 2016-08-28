package fi.tut.cs.social.proximeety.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        // Launch the specified service when this message is received
        Intent startServiceIntent = new Intent(context, N2UService.class);

        startServiceIntent.putExtra("Trigger", "Boot");

        context.startService(startServiceIntent);
    }
}