package fi.tut.cs.social.proximeety.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Connection;

public class ConnectionsGridAdapter extends DragItemAdapter<Pair<Long, Connection>, ConnectionsGridAdapter.ViewHolder> {
    static private final String TAG = "ConnectionsGridAdapter";

    private int mGrabHandleId;
    static View view;
    int mPosition;
    ArrayList<Pair<Long, Connection>> mList;
    MainActivity mainActivity;
    AnimationDrawable proximityAnimation;
    public Connection mConnection;



    public ConnectionsGridAdapter(MainActivity mainActivity, ArrayList<Pair<Long, Connection>> list, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mGrabHandleId = grabHandleId;

        this.mainActivity = mainActivity;
        setItemList(list);
        mList = list;
//        Log.d(TAG, "list size: " + list.size());
        setHasStableIds(true);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connections_grid_item, parent, false);
        //view.setLayoutParams(new ViewGroup.LayoutParams(view.getMeasuredWidth(), view.getMeasuredWidth()));

        WindowManager wm = (WindowManager) parent.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        view.setLayoutParams(new LinearLayout.LayoutParams(width / 2, width / 2));

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        String id;

        // Find user's profile from list of users
        // user1Id or user2Id of the connection must be the same with the id
        // of the user from the list and different from myProfile.id
//        Log.d(TAG, "mainActivity.mUsers.size(): " + mainActivity.mUsers.size());
        if(mainActivity.mUsers.size() == 0)
            mainActivity.getAllUsers(1);

        for(int i=0; i<mainActivity.mUsers.size(); i++) {
            if ((!mItemList.get(position).second.user1Id.equals(mainActivity.myProfile.getId())) && (mItemList.get(position).second.user1Id.equals(mainActivity.mUsers.get(i).getId()))
                    || (!mItemList.get(position).second.user2Id.equals(mainActivity.myProfile.getId())) && (mItemList.get(position).second.user2Id.equals(mainActivity.mUsers.get(i).getId()))) {

                mainActivity.mConnectionsGridFragment.connectionUsername.add(position, mainActivity.mUsers.get(i).getUsername());
                mainActivity.mConnectionsGridFragment.connectionId.add(position, mainActivity.mUsers.get(i).getId());

//                Log.d(TAG, "Entered if for: " + mainActivity.mConnectionsGridFragment.connectionUsername.get(position)
//                        + " " + mainActivity.mUsers.get(i).getUsername() + " " + mainActivity.mUsers.get(i).getId() + " i: " + i);

                holder.id = mainActivity.mUsers.get(i).getId();
                holder.username = mainActivity.mUsers.get(i).getUsername();
                holder.mText.setText(mainActivity.mUsers.get(i).getUsername());
                holder.whispersTV.setText(Integer.toString(mItemList.get(position).second.timesMet));
                break;
            }
        }

        // If the user is in proximity, show the animation
        //holder.proximityIV = (ImageView) view.findViewById(R.id.inProximityIV);

        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.connections_grid_item_innerLayout);

        //Log.d(TAG, "onBindViewHolder - " +  position + ". " + mItemList.get(position).first + ", " + mItemList.get(position).second._id + " -> " + mainActivity.compareDates(mItemList.get(position).second.lastMet));
        if(mainActivity.compareDates(mItemList.get(position).second.lastMet)) {
            holder.proximityIV.setVisibility(View.VISIBLE);
            holder.proximityIV.setBackgroundResource(R.drawable.proximity_animation);
            proximityAnimation = (AnimationDrawable) holder.proximityIV.getBackground();
            proximityAnimation.start();

            // Change the background color
            holder.rl.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.white));
            //holder.backgroundV.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.white));
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.white));
            holder.border.setImageResource(R.mipmap.avatar_border);
            holder.whispersTV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.whispers));
        }
        else {
            holder.proximityIV.setVisibility(View.INVISIBLE);
            holder.rl.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.N2U_grey));
            //holder.backgroundV.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.N2U_grey));
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.N2U_grey));
            holder.border.setImageResource(R.mipmap.avatar_border_grey);
            holder.whispersTV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.whispers_grey));
        }
        holder.lastMet.setText("N2U " + mainActivity.datesDifference(mItemList.get(position).second.lastMet));


        // Search the list of updates for the user
        for(int i=0; i<mainActivity.mConnectionsGridFragment.updates.size(); i++) {
//            Log.d(TAG, "updateID: " + mainActivity.mConnectionsGridFragment.updates.get(i) + " - " + mainActivity.mConnectionsGridFragment.connectionId.get(position));
            if(mainActivity.mConnectionsGridFragment.updates.get(i).equals(mainActivity.mConnectionsGridFragment.connectionId.get(position)) && mItemList.get(position).second.timesMet < 6){
                holder.updatedIV.setVisibility(View.VISIBLE);
            }
        }

        // If there are new messages for the current user, show the message icon
        if(mainActivity.checkMessages(holder.id)) {
            holder.updatedIV.setVisibility(View.VISIBLE);
        }
        else
            holder.updatedIV.setVisibility(View.INVISIBLE);

        // If there are new clues for the current user, change the clues background
        if(mainActivity.checkClues(mItemList.get(position).second._id)) {
            Log.d(TAG, "checkClues = true");
            holder.whispersTV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.whispers_blue));
            holder.whispersTV.setTextColor(ContextCompat.getColor(mainActivity, R.color.white));
        }
        else {
            Log.d(TAG, "checkClues = false");
            holder.whispersTV.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.whispers));
            holder.whispersTV.setTextColor(ContextCompat.getColor(mainActivity, R.color.N2U_black));
        }

//        holder.itemView.setTag(holder.mText);
//        holder.itemView.setTag(holder.whispersTV);
        mConnection = mItemList.get(position).second;
        holder.connection = mConnection;

        int hash = holder.connection._id.hashCode() % 3;
        if(hash == -2  || hash == 1)
            holder.avatarIV.setImageResource(R.mipmap.avatar01);
        else if(hash == 0)
            holder.avatarIV.setImageResource(R.mipmap.avatar02);
        else if(hash == 2 || hash == -1)
            holder.avatarIV.setImageResource(R.mipmap.avatar03);

        final ViewHolder tempHolder = holder;
        holder.itemView.post(new Runnable()
        {
            @Override
            public void run()
            {
                if(tempHolder.connection.faceToFace > 0) {
                    int width = tempHolder.border.getMeasuredWidth();
                    int height = tempHolder.border.getMeasuredHeight();
//                    Log.d(TAG, "width - height: " + width + " " + height);

                    String filename = tempHolder.id;
                    File file = new File(mainActivity.getFilesDir(), tempHolder.id);

                    if (file.exists()) {
                        try {
                            FileInputStream fileInputStream = mainActivity.openFileInput(filename);

                            Bitmap avatarBMP = BitmapFactory.decodeStream(fileInputStream);

                            tempHolder.avatarIV.setImageBitmap(avatarBMP);
                            tempHolder.avatarIV.getLayoutParams().height = height;
                            tempHolder.avatarIV.getLayoutParams().width = width;
                            fileInputStream.close();

                        } catch (IOException e) {
                            Log.e(TAG, "Unable to access file, " + e.toString());
                        }
                    }
                }
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return mItemList.size();
    }

    public class ViewHolder extends DragItemAdapter<Pair<Long, Connection>, ConnectionsGridAdapter.ViewHolder>.ViewHolder {
        public TextView mText;
        public TextView lastMet;
        public ImageView proximityIV;
        public ImageView updatedIV;
        public ImageView avatarIV;
        public String id;
        public String username;
        public Connection connection;
        public TextView whispersTV;
        public ImageView border;
        public RelativeLayout rl;
        public int width;
        public int height;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            mText = (TextView) itemView.findViewById(R.id.usernameTV);
            mText.setTypeface(mainActivity.futura);
            proximityIV = (ImageView) itemView.findViewById(R.id.inProximityIV);
            updatedIV = (ImageView) itemView.findViewById(R.id.updatedIV);
            lastMet = (TextView) itemView.findViewById(R.id.lastMetTV);
            lastMet.setTypeface(mainActivity.futura);
            avatarIV = (ImageView) itemView.findViewById(R.id.avatarIV);
            whispersTV = (TextView) itemView.findViewById(R.id.whispersTV);
            whispersTV.setTypeface(mainActivity.futura);
            border = (ImageView) itemView.findViewById(R.id.borderIV);
            rl = (RelativeLayout) itemView.findViewById(R.id.connections_grid_item_innerLayout);

            width = avatarIV.getMeasuredWidth();
            height = avatarIV.getMeasuredHeight();
        }

        @Override
        public void onItemClicked(View view) {

            mainActivity.connectionsGridItemOnClick(connection, username, id);
//            Log.d(TAG, "CONNECTIONS ADAPTER: " + connection._id + " - " + username + " - " + id);

        }

        @Override
        public boolean onItemLongClicked(View view) {
            mainActivity.mConnectionsGridFragment.blockConnection(connection, username, id);
            return true;
        }
    }

}
