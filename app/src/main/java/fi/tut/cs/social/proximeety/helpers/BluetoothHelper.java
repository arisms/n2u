package fi.tut.cs.social.proximeety.helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fi.tut.cs.social.proximeety.MainActivity;

public class BluetoothHelper {
    public final String TAG = "BluetoothHelper";    // Debugging

    private BluetoothAdapter mBluetoothAdapter;
    MainActivity mainActivity;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_DISCOVERABILITY = 2;

    public BluetoothHelper(MainActivity mActivity) {
        this.mainActivity = mActivity;
    }

    /** Initialize Bluetooth **/
    public String init() {
        final BluetoothManager bluetoothManager =(BluetoothManager) mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Check whether Bluetooth is enabled
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
        else
        {
            if(!getDiscoverableStatus())
                enableDiscoverability();
            else {
                Log.d(TAG, "Bluetooth OK");
                //mainActivity.scheduleAlarm();
            }

        }


        return mBluetoothAdapter.getAddress();
    }



    public void enableDiscoverability() {
        Log.d(TAG, "enableDiscoverability()");


        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        mainActivity.startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABILITY);
    }

    public boolean getDiscoverableStatus() {
        if(mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
            return true;
        else
            return false;
    }


}
