package fi.tut.cs.social.proximeety.Adapters;

import android.content.Context;
import android.graphics.Point;
import android.media.Image;
import android.os.Vibrator;
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
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Clue;

/**
 * Created by Aris on 29/11/15.
 */
public class CluesViewGridOtherAdapter extends DragItemAdapter<Pair<Long, Clue>, CluesViewGridOtherAdapter.ViewHolder> {
    static private final String TAG = "CluesViewGridAdapter";

    private int mGrabHandleId;
    static View view;
    int mPosition;
    ArrayList<Pair<Long, Clue>> mList;
    MainActivity mActivity;
    public Vibrator vibrator;


    public CluesViewGridOtherAdapter(MainActivity mainActivity, ArrayList<Pair<Long, Clue>> list, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mGrabHandleId = grabHandleId;

        mActivity = mainActivity;
        setHasStableIds(true);
        setItemList(list);
        mList = list;
        Log.d(TAG, "list size: " + list.size());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.clues_other_view_grid_item, parent, false);
        //view.setLayoutParams(new ViewGroup.LayoutParams(view.getMeasuredWidth(), view.getMeasuredWidth()));

        WindowManager wm = (WindowManager) parent.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        view.setLayoutParams(new LinearLayout.LayoutParams(width/2, width/2));

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).second.getQuestion() + " " + mItemList.get(position).second.getAnswer();

        //holder.likedIV.setAlpha((float)0.7);

        holder.mText.setText(text);
        holder.mText.setTypeface(mActivity.futura);
        if(text.length() > 60 && text.length() < 80)
            holder.mText.setTextSize(17);
        else if(text.length() < 60)
            holder.mText.setTextSize(20);
        else
            holder.mText.setTextSize(15);

        holder.itemView.setTag(text);
        holder.mClue = mItemList.get(position).second;
        //holder.likesTV.setTypeface(mActivity.futura);
        Log.d(TAG, "getLikes: " + mItemList.get(position).second.getLikes());

        // If the clue is in the list of liked clues
        if(mActivity.mLikedClues.size() > 0)
            if(mActivity.mLikedClues.get(mItemList.get(position).second.getId()) != null)
                holder.likedIV.setImageResource(R.drawable.liked);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ViewHolder extends DragItemAdapter<Pair<Long, Clue>, CluesViewGridOtherAdapter.ViewHolder>.ViewHolder {
        public TextView mText;
        public Clue mClue;
        public ImageView likedIV;
        //public TextView likesTV;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            mText = (TextView) itemView.findViewById(R.id.clueTV);
            likedIV = (ImageView) itemView.findViewById(R.id.likedIV);
            //likesTV = (TextView) itemView.findViewById(R.id.likesTV);
        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), "Tap and hold to like an item.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {

            if(mActivity.mOtherProfileGridFragment.likeClue(mClue)) {
                vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(60);
                likedIV.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.liked));
                Toast.makeText(view.getContext(), "You have liked " + mActivity.mOtherProfileGridFragment.connectionUsername + "'s whisper.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(view.getContext(), "You have already liked this whisper!", Toast.LENGTH_SHORT).show();
            }


            mActivity.mOtherProfileGridFragment.likeClue(mClue);
            return true;
        }
    }

}
