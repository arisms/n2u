package fi.tut.cs.social.proximeety.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;

public class HeaderFragment extends Fragment {
    final String TAG = "HeaderFragment";    // Debugging

    public ImageButton profileBN;
    MainActivity mainActivity;
    ImageButton connectionsBN;
    ImageButton statsBN;
    HeaderFragmentListener mCallback;
    private static String currentFragment = "";
    private static boolean viewCreated = false;

    public HeaderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);
        mainActivity = (MainActivity) getActivity();

        profileBN = (ImageButton) view.findViewById(R.id.avatarBN);
        if(!mainActivity.myPictureStringUri.equals("")) {
            profileBN.setImageURI(Uri.parse(mainActivity.myPictureStringUri));
            profileBN.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        profileBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Home Button");
                mCallback.onHomeButton();
            }
        });

        // Buttons
        connectionsBN = (ImageButton) view.findViewById(R.id.connectionsBN);
        connectionsBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Connections Button");
                mCallback.onConnectionsButton();
                //connectionsBN.setImageResource(R.mipmap.n2u_icon_selected));
            }
        });

        //ToDo
        statsBN = (ImageButton) view.findViewById(R.id.achievementsBN);
        statsBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onAchievementsButton();
            }
        });

        if(!currentFragment.equals(""))
            updateIcons(currentFragment);
        else
            updateIcons("myConnections");

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (HeaderFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement HeaderFragmentListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View mView = view;
        view.post(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "onViewCreated - currentFragment: " + currentFragment);
                switch (currentFragment) {
                    case "myProfile":
                        profileBN.setImageResource(R.mipmap.profile_icon_selected);
                        connectionsBN.setImageResource(R.mipmap.n2u_icon);
                        statsBN.setImageResource(R.mipmap.achievements_icon);
                        break;
                    case "myConnections":
                        profileBN.setImageResource(R.mipmap.profile_icon);
                        connectionsBN.setImageResource(R.mipmap.n2u_icon_selected);
                        statsBN.setImageResource(R.mipmap.achievements_icon);
                        break;
                    case "myStats":
                        profileBN.setImageResource(R.mipmap.profile_icon);
                        connectionsBN.setImageResource(R.mipmap.n2u_icon);
                        statsBN.setImageResource(R.mipmap.achievements_icon_selected);
                        break;

                }

                viewCreated = true;
            }
        });

    }

    public void updateIcons(String currentFragment) {

        Log.d(TAG, "updateIcons - currentFragment: " + currentFragment);
        this.currentFragment = currentFragment;

        if(viewCreated) {
            Log.d(TAG, "updateIcons + viewCreated - currentFragment: " + currentFragment);
            switch (currentFragment) {
                case "myProfile":
                    profileBN.setImageResource(R.mipmap.profile_icon_selected);
                    connectionsBN.setImageResource(R.mipmap.n2u_icon);
                    statsBN.setImageResource(R.mipmap.achievements_icon);
                    break;
                case "myConnections":
                    profileBN.setImageResource(R.mipmap.profile_icon);
                    connectionsBN.setImageResource(R.mipmap.n2u_icon_selected);
                    statsBN.setImageResource(R.mipmap.achievements_icon);
                    break;
                case "myStats":
                    profileBN.setImageResource(R.mipmap.profile_icon);
                    connectionsBN.setImageResource(R.mipmap.n2u_icon);
                    statsBN.setImageResource(R.mipmap.achievements_icon_selected);
                    break;

            }
        }


    }

    public interface HeaderFragmentListener {
        void onHomeButton();
        void onConnectionsButton();
        void onAchievementsButton();
    }
}
