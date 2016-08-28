package fi.tut.cs.social.proximeety.Adapters;

import android.content.Context;
import android.graphics.Point;
import android.os.Vibrator;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Clue;

/**
 * Created by Aris on 29/11/15.
 */
public class CluesViewGridAdapter extends DragItemAdapter<Pair<Long, Clue>, CluesViewGridAdapter.ViewHolder> {
    static private final String TAG = "CluesViewGridAdapter";

    private int mGrabHandleId;
    static View view;
    int mPosition;
    ArrayList<Pair<Long, Clue>> mList;
    MainActivity mActivity;
    public Vibrator vibrator;


    public CluesViewGridAdapter(MainActivity mainActivity, ArrayList<Pair<Long, Clue>> list, int grabHandleId, boolean dragOnLongPress) {
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

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.clues_view_grid_item, parent, false);
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
        holder.mText.setText(text);
        if(text.length() > 60 && text.length() < 80)
            holder.mText.setTextSize(17);
        else if(text.length() < 60)
            holder.mText.setTextSize(20);
        else
            holder.mText.setTextSize(15);

        holder.itemView.setTag(text);
        holder.mClue = mItemList.get(position).second;
        holder.mLikes.setText(Integer.toString(mItemList.get(position).second.getLikes()));

        // Set likes per item and check for achievement
        if(mItemList.get(position).second.getLikes() > mActivity.likes_per_item) {
            mActivity.likes_per_item = mItemList.get(position).second.getLikes();
            mActivity.mEditor.putInt("likes_per_item", mActivity.likes_per_item);

            mActivity.progressAchievements("likes_per_item");
        }

        if(mItemList.get(position).second.getLikes() > 0)
            holder.likedIV.setImageResource(R.drawable.liked);

    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ViewHolder extends DragItemAdapter<Pair<Long, Clue>, CluesViewGridAdapter.ViewHolder>.ViewHolder {
        public TextView mText;
        public Clue mClue;
        public TextView mLikes;
        public ImageView likedIV;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            mText = (TextView) itemView.findViewById(R.id.clueTV);
            mText.setTypeface(mActivity.futura);
            mLikes = (TextView) itemView.findViewById(R.id.likesTV);
            mLikes.setTypeface(mActivity.futura);
            likedIV = (ImageView) itemView.findViewById(R.id.likedIV);

            //clueId = (TextView) itemView.findViewById(R.id.clueidTV);
            //Log.d(TAG, "Viewholder constructor");
        }

        @Override
        public void onItemClicked(View view) {
            //Toast.makeText(view.getContext(), "Item clicked - " + mClue.getId(), Toast.LENGTH_SHORT).show();



            mActivity.mMyProfileViewGridFragment.createDialog(mClue);
        }

        @Override
        public boolean onItemLongClicked(View view) {
            //Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

}
