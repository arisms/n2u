package fi.tut.cs.social.proximeety.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.List;

import fi.tut.cs.social.proximeety.Adapters.AchievementsGridAdapter;
import fi.tut.cs.social.proximeety.Adapters.CluesViewGridOtherAdapter;
import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Achievement;
import fi.tut.cs.social.proximeety.classes.Clue;
import fi.tut.cs.social.proximeety.classes.Connection;
import fi.tut.cs.social.proximeety.classes.N2U_Application;

public class AchievementsGridFragment extends Fragment {
    static private final String TAG = "MyProfileViewGridFr";

    static MainActivity mainActivity;
    private DragListView mDragListView;
    AchievementsGridAdapter gridAdapter;

    ArrayList<Pair<Long, Achievement>> mItemArray;
    public ProgressBar loadingPB;

    private TextView titleTV;
    private TextView profilesTV, profilesNumberTV;
    private TextView achievementsTV, achievementsNumberTV;
    private TextView face2faceTV, face2faceNumberTV;


    View view;

    public TextView usernameTV;

    public AchievementsGridFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        mainActivity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.fragment_achievements_grid, container, false);

        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        loadingPB = (ProgressBar) view.findViewById(R.id.loadingPB);

        /************************ Grid View ************************/

        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);

        mDragListView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {

            }
        });
        mDragListView.setDragEnabled(false);

        mItemArray = new ArrayList<>();

        mDragListView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        gridAdapter = new AchievementsGridAdapter(mainActivity, mItemArray, R.id.grid_item_achievements_layout, true);

        mDragListView.setAdapter(gridAdapter, true);

        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(null);
        mDragListView.setScrollingEnabled(true);

        // mSettings.getString("username", "")

        titleTV = (TextView) view.findViewById(R.id.titleTV);
        titleTV.setTypeface(mainActivity.futuraBold);

        profilesTV = (TextView) view.findViewById(R.id.profilesTextTV);
        profilesTV.setTypeface(mainActivity.futura);
        profilesNumberTV = (TextView) view.findViewById(R.id.profilesNumberTV);
        profilesNumberTV.setTypeface(mainActivity.futuraBold);
        profilesNumberTV.setText(String.valueOf(mainActivity.mSettings.getInt("connections_total", mainActivity.connections_total)));

        achievementsTV = (TextView) view.findViewById(R.id.achievementsTextTV);
        achievementsTV.setTypeface(mainActivity.futura);
        achievementsNumberTV = (TextView) view.findViewById(R.id.achievementsNumberTV);
        achievementsNumberTV.setTypeface(mainActivity.futuraBold);
        achievementsNumberTV.setText(String.valueOf(mainActivity.mSettings.getInt("achievements_total", mainActivity.achievements_total)));

        face2faceTV = (TextView) view.findViewById(R.id.face2faceTextTV);
        face2faceTV.setTypeface(mainActivity.futura);
        face2faceNumberTV = (TextView) view.findViewById(R.id.face2faceNumberTV);
        face2faceNumberTV.setTypeface(mainActivity.futuraBold);
        face2faceNumberTV.setText(String.valueOf(mainActivity.mSettings.getInt("face2face_total", mainActivity.face2face_total)));

        // Copy achievements from main Activity to itemArray
//        for(int i=0; i<mainActivity.mAchievements.size(); i++) {
//            mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get(i)));
//        }
        int i=0;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("connections_total")));
//        i++;
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("connections_daily")));
        i++;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("likes_per_item")));
        i++;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("messages_received")));
        i++;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("messages_sent")));
        i++;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("face2face_total")));
        i++;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("likes_given")));
        i++;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("whispers_received")));
        i++;
        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(i), mainActivity.mAchievements.get("days_used")));




//        Achievement tempAchievement = new Achievement("Hermit", "Total profiles collected: ", 2, 5);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(0), tempAchievement));
//        tempAchievement = new Achievement("Lurking", "Profiles collected in one day: ", 0, 3);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(1), tempAchievement));
//        tempAchievement = new Achievement("Pathetic", "Likes received for one item: ", 1, 3);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(2), tempAchievement));
//        tempAchievement = new Achievement("(0)", "Messages received: ", 0, 1);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(3), tempAchievement));
//        tempAchievement = new Achievement("Illiterate", "Messages sent: ", 2, 2);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(4), tempAchievement));
//        tempAchievement = new Achievement("Loner", "Face-to-Face meetings: ", 0, 1);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(5), tempAchievement));
//        tempAchievement = new Achievement("Meh...", "Pieces liked: ", 0, 7);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(6), tempAchievement));
//        tempAchievement = new Achievement("Minimalist", "Pieces received: ", 3, 8);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(7), tempAchievement));
//        tempAchievement = new Achievement("Nice to meet you!", "Days using the app: ", 0, 1);
//        mItemArray.add(new Pair<Long, Achievement>(Long.valueOf(8), tempAchievement));

        loadingPB.setVisibility(View.INVISIBLE);

        gridAdapter.notifyDataSetChanged();

        //((N2U_Application) mainActivity.getApplication()).getSocket().disconnect();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View mView = view;
        view.post(new Runnable() {
            @Override
            public void run() {
                if(mainActivity.currentFragment == mainActivity.mAchievementsGridFragment) {
                    mainActivity.showHeader();
                    mainActivity.mHeaderFragment.updateIcons("myStats");
                }
            }
        });

    }
}
