package fi.tut.cs.social.proximeety.Fragments;


import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.woxthebox.draglistview.DragListView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import fi.tut.cs.social.proximeety.Adapters.ConnectionsGridAdapter;
import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Connection;
import fi.tut.cs.social.proximeety.classes.HTTPRequest;

public class ConnectionsGridFragment extends Fragment {
    static private final String TAG = "ConnectionsGridFragment";

    MainActivity mainActivity;
    View view;
    private DragListView mDragListView;
    public ProgressBar loadingPB;
    ArrayList<Pair<Long, Connection>> mItemArray;
    ConnectionsGridAdapter gridAdapter;
    public static List<String> updates = new ArrayList<String>();
    private static List<Connection> myConnections = new ArrayList<Connection>();

    public static List<String> connectionUsername;
    public static List<String> connectionId;
    public static int totalConnections;

    private TextView infoTV;
    private TextView infoTitleTV;
    private ImageView infoImageIV;
    private GridLayoutManager gridLayoutManager;

    File file;
    String filename;
    FileOutputStream outputStream;


    public ConnectionsGridFragment() {
        // Required empty public constructor
        connectionUsername = new ArrayList<String>();
        connectionId = new ArrayList<String>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        view = inflater.inflate(R.layout.fragment_connections_grid, container, false);

        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        loadingPB = (ProgressBar) view.findViewById(R.id.loadingPB);
        loadingPB.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.N2U_grey), PorterDuff.Mode.SRC_IN);

        /************************ Grid View ************************/
        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);

        // Drag is not used in this view
        mDragListView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
                //onItemDragStarted()
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                //onItemDragEnded()
            }
        });
        mDragListView.setDragEnabled(false);

        mItemArray = new ArrayList<>();
        gridLayoutManager = new GridLayoutManager(getContext(), 2);


        mDragListView.setLayoutManager(gridLayoutManager); // 2: number of columns in the list


        // Get the list of connections from the file
        myConnections = mainActivity.getMyConnections();
//        for (int i = 0; i < myConnections.size(); i++) {
//            Log.d(TAG, "connection " + i + ". " + myConnections.get(i)._id);
//        }


        if(mainActivity.mUsers.size() == 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    gridAdapter = new ConnectionsGridAdapter(mainActivity, mItemArray, R.id.connections_grid_item_layout, true);

                    mDragListView.setAdapter(gridAdapter, true);
                    mDragListView.setCanDragHorizontally(false);
                    mDragListView.setCustomDragItem(null);
                    mDragListView.setScrollingEnabled(true);

                    mainActivity.getConnectionsGridForUser();
                }
            }, 2000);
        }
        else {
            gridAdapter = new ConnectionsGridAdapter(mainActivity, mItemArray, R.id.connections_grid_item_layout, true);

            mDragListView.setAdapter(gridAdapter, true);
            mDragListView.setCanDragHorizontally(false);
            mDragListView.setCustomDragItem(null);
            mDragListView.setScrollingEnabled(true);

            mainActivity.getConnectionsGridForUser();
        }

        infoTV = (TextView) view.findViewById(R.id.infoTV);
        infoTitleTV = (TextView) view.findViewById(R.id.infoTitleTV);
        infoImageIV = (ImageView) view.findViewById(R.id.infoImageIV);

        showInfo(false);


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View mView = view;

        // Count the total times met
        view.post(new Runnable() {
            @Override
            public void run() {
//                int total = 0;
//                for(int i=0; i<mItemArray.size(); i++) {
//                    total += mItemArray.get(i).second.timesMet;
//                }
//                if(total > mainActivity.whispers_received) {
//                    mainActivity.whispers_received = total;
//                    mainActivity.mEditor.putInt("whispers_received", total);
//
//                    mainActivity.progressAchievements("whispers_received");
//                }
//                Log.d(TAG, "mItemArray.size(): " + mItemArray.size());
            }
        });
    }

    public void checkForAchievements() {
        // Get the number of total connections and check for the achievement
        if(totalConnections >= mainActivity.connections_total) {

            mainActivity.connections_total = totalConnections;
            mainActivity.mEditor.putInt("connections_total", mainActivity.connections_total);

            mainActivity.progressAchievements("connections_total");
        }

        int total = 0;
        for(int i=0; i<totalConnections; i++) {
            total += mItemArray.get(i).second.timesMet;
        }
//        Log.d(TAG, "totalConnections - mItemArray.size() - mainActivity.whispers_received" + totalConnections + " " + mItemArray.size() + " " + mainActivity.whispers_received);
        if(total > mainActivity.whispers_received) {
            mainActivity.whispers_received = total;
            mainActivity.mEditor.putInt("whispers_received", total);

            mainActivity.progressAchievements("whispers_received");
        }
        mainActivity.mEditor.commit();
    }

    public void showInfo(boolean show) {

        if(show) {
            infoTV.setVisibility(View.VISIBLE);
            if(mainActivity.myProfile.getClues() > 0)
                infoTV.setText(getResources().getString(R.string.empty_connections_list));
            else if(mainActivity.myProfile.getClues() == 0) {
                //infoTV.setText(getResources().getString(R.string.empty_connections_list_no_whispers));
                infoTV.setText(mainActivity.getResources().getString(R.string.empty_connections_list_no_whispers));
            }

            infoTitleTV.setVisibility(View.VISIBLE);
            infoImageIV.setVisibility(View.VISIBLE);
        }
        else {
            infoTV.setTypeface(mainActivity.futura);
            infoTV.setVisibility(View.INVISIBLE);

            infoTitleTV.setTypeface(mainActivity.futuraBold);
            infoTitleTV.setVisibility(View.INVISIBLE);

            infoImageIV.setVisibility(View.INVISIBLE);
        }
    }

    public void hideInfo() {

    }

    public void refreshFragment() {
        getUpdates();
        mItemArray.clear();
        gridAdapter.notifyDataSetChanged();
        mainActivity.getConnectionsGridForUser();
        //gridAdapter.notifyDataSetChanged();
    }

    public void addItemToList(Connection connection) {
//        Log.d(TAG, "Adding item to connections list: " + connection._id);
        mItemArray.add(new Pair<>(Long.valueOf(mItemArray.size()), connection));

        // Sort
        Collections.sort(mItemArray, new Comparator<Pair<Long, Connection>>() {
            @Override
            public int compare(final Pair<Long, Connection> pair1, final Pair<Long, Connection> pair2) {
                if(mainActivity.getDatesDiff(pair1.second.lastMet) > mainActivity.getDatesDiff(pair2.second.lastMet))
                    return 1;
                else if(mainActivity.getDatesDiff(pair1.second.lastMet) == mainActivity.getDatesDiff(pair2.second.lastMet))
                    return 0;
                else
                    return -1;
            }
        });

        gridAdapter.notifyDataSetChanged();

        if(mItemArray.size() == totalConnections) {
            checkForAchievements();
        }
    }

    public void blockConnection(final Connection connection, String username, String id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("If you block " + username + " they will not receive updates from you, they won't be " +
                "able to view your profile or contact you in any way.\n" + "The user will be removed from your list of " +
                "connections and you will not have access to their profile.\n" +
                "This action is irreversible.")
                .setTitle("Are you sure you want to block this user?");
        builder.setPositiveButton("Block", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "Block confirmed.");

                // Update connection on the server
                HashMap<String, String> params = new HashMap<String, String>();
                HTTPRequest request;

                params.put("_id", connection._id);
                params.put("_user1Id", connection.user1Id);
                params.put("_user2Id", connection.user2Id);
                params.put("timesMet", Integer.toString(connection.timesMet));
                params.put("faceToFace", Integer.toString(connection.faceToFace));
                params.put("lastFaceToFace", mainActivity.dateToString(connection.lastFaceToFace));
                params.put("lastMet", mainActivity.dateToString(connection.lastMet));
                params.put("lastUpdate", mainActivity.dateToString(connection.lastUpdate));
                params.put("blocked", Boolean.toString(true));
                JSONObject req = new JSONObject(params);

                request = new HTTPRequest("UpdateConnection - Block",
                        mainActivity.HOST + mainActivity.PORT + "/connections/update",
                        Request.Method.PUT,
                        req);
                mainActivity.mHTTPHelper.sendToServer(request);
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Block canceled.");
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void onConnectionBlocked() {

        Toast.makeText(getContext(), "Connection blocked.", Toast.LENGTH_LONG);
        refreshFragment();
    }

    public void getUpdates() {
        mItemArray.clear();
        // Read the list of updates from the shared file
        filename = "N2Yupdates";
        file = new File(mainActivity.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = mainActivity.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                updates = (List<String>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

//        for (int i = 0; i < updates.size(); i++) {
//            Log.d(TAG, "updates " + i + ". " + updates.get(i));
//        }

        // Clear updates in the file
        try {
            file = new File(mainActivity.getFilesDir(), filename);

            // Create the file if it doesn't already exist
            if (!file.exists()) {
                Log.d(TAG, "clear updates - file does not exist");
                //file.createNewFile();
            }
            // Write to the file
            outputStream = mainActivity.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            List<String> tempUpdates = new ArrayList<String>();
            objectOutputStream.writeObject(tempUpdates);
            objectOutputStream.close();
            outputStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Unable to access file, " + e.toString());
        }

        //mainActivity.getConnectionsGridForUser();
    }

    public interface ConnectionsGridFragmentListener {
        void getConnectionsGridForUser();
    }


}
