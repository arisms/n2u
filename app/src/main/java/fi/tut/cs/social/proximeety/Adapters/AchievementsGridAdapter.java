package fi.tut.cs.social.proximeety.Adapters;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
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

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import fi.tut.cs.social.proximeety.Fragments.AchievementsGridFragment;
import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Achievement;
import fi.tut.cs.social.proximeety.classes.Connection;

public class AchievementsGridAdapter extends DragItemAdapter<Pair<Long, Achievement>, AchievementsGridAdapter.ViewHolder> {
    static private final String TAG = "ConnectionsGridAdapter";

    private int mGrabHandleId;
    static View view;
    int mPosition;
    ArrayList<Pair<Long, Achievement>> mList;
    MainActivity mainActivity;



    public AchievementsGridAdapter(MainActivity mainActivity, ArrayList<Pair<Long, Achievement>> list, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mGrabHandleId = grabHandleId;

        this.mainActivity = mainActivity;
        setHasStableIds(true);
        setItemList(list);
        mList = list;
        Log.d(TAG, "list size: " + list.size());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.achievements_grid_item, parent, false);

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

        holder.mTitle.setText(mItemList.get(position).second.getTitle());
        holder.mText.setText(mItemList.get(position).second.getText());
        holder.numbersTV.setText(mItemList.get(position).second.getCurrentValue() + "/" +
                mItemList.get(position).second.getMaxValue());

        if(mItemList.get(position).second.getRank() == 0) {
            holder.rankIV.setImageResource(R.mipmap.empty_token);
        }
        else if(mItemList.get(position).second.getRank() == 1) {
            holder.rankIV.setImageResource(R.mipmap.bronze_token);
        }
        else if(mItemList.get(position).second.getRank() == 2) {
            holder.rankIV.setImageResource(R.mipmap.silver_token);
        }
        else if(mItemList.get(position).second.getRank() == 3) {
            holder.rankIV.setImageResource(R.mipmap.gold_token);
            holder.numbersTV.setText(String.valueOf(mItemList.get(position).second.getCurrentValue()));
        }


        holder.itemView.setTag(holder.mText);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ViewHolder extends DragItemAdapter<Pair<Long, Achievement>, AchievementsGridAdapter.ViewHolder>.ViewHolder {
        public TextView mTitle;
        public TextView mText;
        public TextView numbersTV;
        public ImageView rankIV;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);

            mTitle = (TextView) itemView.findViewById(R.id.titleTV);
            mTitle.setTypeface(mainActivity.futuraBold);
            mText = (TextView) itemView.findViewById(R.id.textTV);
            mText.setTypeface(mainActivity.futura);
            rankIV = (ImageView) itemView.findViewById(R.id.rankIV);
            numbersTV = (TextView) itemView.findViewById(R.id.numbersTV);
            numbersTV.setTypeface(mainActivity.futura);
        }

        @Override
        public void onItemClicked(View view) {

        }

        @Override
        public boolean onItemLongClicked(View view) {
            //Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

}
