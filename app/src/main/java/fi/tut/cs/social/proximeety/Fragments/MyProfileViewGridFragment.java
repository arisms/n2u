package fi.tut.cs.social.proximeety.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.woxthebox.draglistview.DragListView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import fi.tut.cs.social.proximeety.Adapters.CluesViewGridAdapter;
import fi.tut.cs.social.proximeety.CropperActivity;
import fi.tut.cs.social.proximeety.ImageCropActivity;
import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Clue;
import fi.tut.cs.social.proximeety.classes.HTTPRequest;

public class MyProfileViewGridFragment extends Fragment {
    static private final String TAG = "MyProfileViewGridFr";

    static MainActivity mainActivity;
    private DragListView mDragListView;
    List<Clue> mClues;
    CluesViewGridAdapter gridAdapter;
    Boolean initialized = false;
    ArrayList<Pair<Long, Clue>> mItemArray;
    public ProgressBar loadingPB;
    View view;
    public Vibrator vibrator;

    private int CHOOSE_IMAGE_REQUEST = 1;
    private int REQUEST_CROP_ICON = 2;
    private final static int RESULT_SELECT_IMAGE = 3;

    TextView usernameTV;
    private TextView infoTitleTV;
    private TextView infoTV;
    private TextView infoTipsTV;
    private ImageView infoImageIV;
    private ImageButton infoBN;
    private AlertDialog infoDialog = null;
    private TextView moreWhispersTV;

    private ImageButton avatarBN;
    private Bitmap bitmap = null;
    private FloatingActionButton addBN;

    private Boolean createdClue = false;

    public MyProfileViewGridFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        mainActivity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.fragment_my_profile_view_grid, container, false);

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
                //Toast.makeText(getActivity(), "Start - position: " + mItemArray.get(position).second.getAnswer(), Toast.LENGTH_SHORT).show();
                vibrator = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(60);
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    //Toast.makeText(getActivity(), "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
                    //Log.d(TAG, "Start position element: " + mItemArray.get(fromPosition).second.getAnswer());
                    //Log.d(TAG, "End position element: " + mItemArray.get(toPosition).second.getAnswer());

                    // Update order number of clues
                    for (int i = 0; i < mItemArray.size(); i++) {
                        Log.d(TAG, "mItemArray - " + i + ": " + mItemArray.get(i).second.getAnswer());
                        Clue clue = mItemArray.get(i).second;
                        clue.setOrderNumber(i);

                        String message = "";
                        String route = "";
                        int method;

                        message = "FragmentGrid - UpdateClue";
                        route = "/clues/" + clue.getId() + "/update";
                        method = Request.Method.PUT;

                        HashMap<String, String> params = new HashMap<String, String>();
                        HTTPRequest request;

                        params.put("_id", clue.getId());
                        params.put("ownerId", clue.getOwnerId());
                        params.put("question", clue.getQuestion());
                        params.put("answer", clue.getAnswer());
                        params.put("updated", mainActivity.getCurrentDate());
                        params.put("orderNumber", Integer.toString(clue.getOrderNumber()));
                        params.put("likes", Integer.toString(clue.getLikes()));
                        JSONObject req = new JSONObject(params);

                        request = new HTTPRequest(message,
                                mainActivity.HOST + mainActivity.PORT + route,
                                method,
                                req);

                        mainActivity.mHTTPHelper.sendToServer(request);
                    }

                }
            }
        });
        mDragListView.setDragEnabled(true);

        mItemArray = new ArrayList<>();

        mClues = new ArrayList<Clue>();

        mDragListView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        gridAdapter = new CluesViewGridAdapter(mainActivity, mItemArray, R.id.grid_item_layout, true);

        mDragListView.setAdapter(gridAdapter, true);

        mDragListView.setCanDragHorizontally(true);
        mDragListView.setCustomDragItem(null);
        mDragListView.setScrollingEnabled(true);

        //mDragListView.set

        mainActivity.getCluesForUserGrid();

        initialized = true;

        usernameTV = (TextView) view.findViewById(R.id.usernameTV);
        usernameTV.setTypeface(mainActivity.futuraBold);
        usernameTV.setText(mainActivity.myProfile.getUsername());

        addBN = (FloatingActionButton) view.findViewById(R.id.addBN);
        addBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mainActivity.loadMyProfileEdit();
                Clue clue = new Clue(null,
                        mainActivity.myProfile.getId(),
                        null,
                        null,
                        gridAdapter.getItemCount(),
                        mainActivity.stringToDate(mainActivity.getCurrentDate()),
                        0);
                createDialog(clue);
            }
        });

        infoTitleTV = (TextView) view.findViewById(R.id.infoTitleTV);
        infoTV = (TextView) view.findViewById(R.id.infoTV);
        infoTipsTV = (TextView) view.findViewById(R.id.infoTipsTV);
        infoImageIV = (ImageView) view.findViewById(R.id.infoImageIV);
        moreWhispersTV = (TextView) view.findViewById(R.id.moreWhispersTV);
        moreWhispersTV.setTypeface(mainActivity.futura);
        showInfo(false);

        avatarBN = (ImageButton) view.findViewById(R.id.avatarBN);
        if(!mainActivity.myPictureStringUri.equals("")) {
            avatarBN.setImageURI(Uri.parse(mainActivity.myPictureStringUri));
            avatarBN.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        avatarBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openGallery();
                Intent intent = new Intent(getActivity(), CropperActivity.class);
                startActivity(intent);
            }
        });

        infoBN = (ImageButton) view.findViewById(R.id.infoBN);
        infoBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInformationDialog();
            }
        });

        return view;
    }

    public void refreshGrid() {
        gridAdapter.notifyDataSetChanged();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Position the FAB
        final View mView = view;
        view.post(new Runnable() {
            @Override
            public void run() {
                ImageView backgroundIV = (ImageView) mView.findViewById(R.id.backgroundIV);
                final int bgHeight = backgroundIV.getMeasuredHeight();
                final int fabHeight = addBN.getMeasuredHeight();
                final int fabWidth = addBN.getMeasuredWidth();

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) addBN.getLayoutParams();
                params.setMargins(0, bgHeight - (fabHeight / 2) - 10, 0, 0);
                addBN.setLayoutParams(params);

                final int infoWidth = infoBN.getMeasuredWidth();
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) infoBN.getLayoutParams();
                //params2.setMargins(0, 8, 12, 0);
                params2.addRule(RelativeLayout.ALIGN_END, R.id.addBN);
                params2.setMargins(0, 16, (fabWidth / 2) - (infoWidth / 2), 0);
                infoBN.setLayoutParams(params2);

                updateAvatar();
            }
        });

    }

    public void showInfo(boolean show) {

        if(show) {
            infoTitleTV.setTypeface(mainActivity.futuraBold);
            infoTitleTV.setVisibility(View.VISIBLE);
            infoTV.setTypeface(mainActivity.futura);
            infoTV.setVisibility(View.VISIBLE);
            infoTipsTV.setTypeface(mainActivity.futura);
            infoTipsTV.setVisibility(View.VISIBLE);
            infoImageIV.setVisibility(View.VISIBLE);
            moreWhispersTV.setVisibility(View.INVISIBLE);
        }
        else {
            infoTitleTV.setVisibility(View.INVISIBLE);
            infoTV.setVisibility(View.INVISIBLE);
            infoTipsTV.setVisibility(View.INVISIBLE);
            infoImageIV.setVisibility(View.INVISIBLE);

            if(mClues.size() >= 1 && mClues.size() < 3)
                moreWhispersTV.setVisibility(View.VISIBLE);
        }

    }

    public void addItemToList(Clue clue) {

        // If the list of clues is empty, register the user as active in the server
        if(mClues.size() == 0) {
            Log.d(TAG, "Adding item to list with mClues.size: 0");
            mainActivity.activateUser();
            showInfo(false);
        }

        mClues.add(clue);
        mItemArray.add(new Pair<>(Long.valueOf(clue.getOrderNumber()), clue));
        //Log.d(TAG, "Adding item to list with position: " + Long.valueOf(clue.getOrderNumber()));
        //mItemArray.add(new Pair<>(Long.valueOf(1), clue));

        gridAdapter.notifyDataSetChanged();
        if(mClues.size() >= 3) {
            moreWhispersTV.setVisibility(View.INVISIBLE);
        }
        else if(mClues.size() >= 1 && mClues.size() < 3)
            moreWhispersTV.setVisibility(View.VISIBLE);
    }

    public void createDialog(Clue newClue) {

        Log.d(TAG, "createDialog, " + newClue.getQuestion() + " - " + newClue.getAnswer());
        final Clue clue = newClue;
        createdClue = false;

        LayoutInflater li = LayoutInflater.from(mainActivity);
        View dialogView = li.inflate(R.layout.dialog_editclue, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setView(dialogView);

        MySpinnerAdapter<String> adapter = new MySpinnerAdapter(
                getContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.questions_array))
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner questionSP = (Spinner) dialogView.findViewById(R.id.questionSP);
        questionSP.setAdapter(adapter);

        final EditText answerET = (EditText) dialogView.findViewById(R.id.answerET);
        answerET.setTypeface(mainActivity.futura, 0);
        answerET.setTextSize(16);

        final TextView titleTV = (TextView) dialogView.findViewById(R.id.titleTV);
        titleTV.setTypeface(mainActivity.futura, 0);


        if(clue.getId() == null)
            questionSP.setSelection(adapter.getPosition("I am"));
        else {
            Log.d(TAG, "createDialog, set question: " + clue.getQuestion() + "...");
            questionSP.setSelection(adapter.getPosition(clue.getQuestion() + "..."));

        }

        answerET.setText(clue.getAnswer());

        alertDialogBuilder
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();


        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).
                        setTextColor(ContextCompat.getColor(getContext(), R.color.N2U_orange));
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTypeface(mainActivity.futura);
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).
                        setTextColor(ContextCompat.getColor(getContext(), R.color.N2U_orange));
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTypeface(mainActivity.futura);

                Button positiveBN = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveBN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = "";
                        String route = "";
                        int method;

                        if (questionSP.getSelectedItem().equals("<You may choose a start for your whisper>"))
                            clue.setQuestion("");
                        else
                            clue.setQuestion(questionSP.getSelectedItem().toString().substring(0,
                                    questionSP.getSelectedItem().toString().length() - 3));

                        clue.setAnswer(answerET.getText().toString());

                        String text = clue.getQuestion() + clue.getAnswer();
                        Log.d(TAG, "Length: " + text.length());
                        if (text.length() > 110) {
                            Toast.makeText(getContext(), "Text cannot be more than 110 characters long. " +
                                            "Current length: " + text.length(),
                                    Toast.LENGTH_SHORT).show();

                        }
                        else if(text.length() < 7) {
                            Toast.makeText(getContext(), "Text must be at least 7 characters long. " +
                                            "Current length: " + text.length(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {

                            if (clue.getId() == null) {
                                // Create new clue and add it to the server
                                clue.setOwnerId(mainActivity.myProfile.getId());
                                // TODO: clue.setOrderNumber(); from number of current clues + 1
                                //clue.setOrderNumber( gridAdapter.getItemCount());
                                message = "FragmentGrid - InsertClue";
                                route = "/clues";
                                method = Request.Method.POST;

                                createdClue = true;
                            } else {
                                message = "FragmentGrid - UpdateClue";
                                route = "/clues/" + clue.getId() + "/update";
                                method = Request.Method.PUT;
                            }

                            HashMap<String, String> params = new HashMap<String, String>();
                            HTTPRequest request;

                            params.put("_id", clue.getId());
                            params.put("ownerId", clue.getOwnerId());
                            params.put("question", clue.getQuestion());
                            params.put("answer", clue.getAnswer());
                            params.put("updated", mainActivity.getCurrentDate());
                            params.put("orderNumber", Integer.toString(clue.getOrderNumber()));
                            params.put("likes", Integer.toString(clue.getLikes()));
                            JSONObject req = new JSONObject(params);

                            request = new HTTPRequest(message,
                                    mainActivity.HOST + mainActivity.PORT + route,
                                    method,
                                    req);

                            mainActivity.mHTTPHelper.sendToServer(request);

                            if (createdClue) {
                                // If a new clue was created, update the user's profile on the server
                                mainActivity.myProfile.setClues(mainActivity.myProfile.getClues() + 1);
                                message = "FragmentGrid - UpdateProfile";
                                HashMap<String, String> params2 = new HashMap<String, String>();

                                params2.put("_id", mainActivity.myProfile.getId());
                                params2.put("username", mainActivity.myProfile.getUsername());
                                params2.put("password", mainActivity.myProfile.getPassword());
                                params2.put("deviceId", mainActivity.myProfile.getDeviceId());
                                params2.put("active", Boolean.toString(mainActivity.myProfile.getActive()));
                                params2.put("clues", Integer.toString(mainActivity.myProfile.getClues()));
                                params.put("likes", Integer.toString(0));
                                JSONObject req2 = new JSONObject(params2);


                                HTTPRequest request2;
                                request2 = new HTTPRequest("UpdateUser",
                                        mainActivity.HOST + mainActivity.PORT + "/profiles/" + mainActivity.myProfile.getId() + "/edit",
                                        Request.Method.PUT,
                                        req2);
                                mainActivity.mHTTPHelper.sendToServer(request2);
                                // Update shared preferences
                                mainActivity.mEditor.putString("clues", Integer.toString(mainActivity.myProfile.getClues()));
                                mainActivity.mEditor.commit();
                            }
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        // show it
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTypeface(mainActivity.futura);
        positiveButton.setTextColor(ContextCompat.getColor(mainActivity, R.color.N2U_black));
        positiveButton.setAllCaps(false);
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTypeface(mainActivity.futura);
        negativeButton.setTextColor(ContextCompat.getColor(mainActivity, R.color.N2U_black));
        negativeButton.setAllCaps(false);

    }

    public boolean atLeastThreeWhispers() {

       if(mClues.size() >= 3)
           return false;
        else
           return true;

    }

    private void showInformationDialog() {

        LayoutInflater li = LayoutInflater.from(mainActivity);
        View dialogView = li.inflate(R.layout.dialog_info, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setView(dialogView);

        TextView titleTV = (TextView) dialogView.findViewById(R.id.titleTV);
        titleTV.setTypeface(mainActivity.futuraBold);

        Button aboutBN = (Button) dialogView.findViewById(R.id.aboutBN);
        aboutBN.setTypeface(mainActivity.futura);
        aboutBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAboutInfo();
            }
        });

        Button consentBN = (Button) dialogView.findViewById(R.id.consentBN);
        consentBN.setTypeface(mainActivity.futura);
        consentBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConsentInfo();
            }
        });

        Button deactivateBN = (Button) dialogView.findViewById(R.id.deactivateBN);
        deactivateBN.setTypeface(mainActivity.futura);
        deactivateBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoDialog.dismiss();
                deactivateProfile();
            }
        });

        Button contactBN = (Button) dialogView.findViewById(R.id.contactBN);
        contactBN.setTypeface(mainActivity.futura);
        contactBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContactInfo();
            }
        });

        infoDialog = alertDialogBuilder.create();
        infoDialog.show();
    }

    private void showAboutInfo() {
        infoDialog.dismiss();

        LayoutInflater li = LayoutInflater.from(mainActivity);
        View dialogView = li.inflate(R.layout.dialog_info_about, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setView(dialogView);

        TextView about1TV = (TextView) dialogView.findViewById(R.id.about1TV);
        about1TV.setTypeface(mainActivity.futura);
        TextView about2TV = (TextView) dialogView.findViewById(R.id.about2TV);
        about2TV.setTypeface(mainActivity.futura);
        TextView about3TV = (TextView) dialogView.findViewById(R.id.about3TV);
        about3TV.setTypeface(mainActivity.futura);
        TextView about4TV = (TextView) dialogView.findViewById(R.id.about4TV);
        about4TV.setTypeface(mainActivity.futura);
        TextView about5TV = (TextView) dialogView.findViewById(R.id.about5TV);
        about5TV.setTypeface(mainActivity.futura);

        TextView research1Text = (TextView) dialogView.findViewById(R.id.research1TV);
        research1Text.setTypeface(mainActivity.futura);

        TextView research2Text = (TextView) dialogView.findViewById(R.id.research2TV);
        research2Text.setTypeface(mainActivity.futura);

//        TextView research3Text = (TextView) dialogView.findViewById(R.id.research3TV);
//        research3Text.setTypeface(mainActivity.futura);

        Button okBN = (Button) dialogView.findViewById(R.id.okBN);
        okBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoDialog.dismiss();
            }
        });

        infoDialog = alertDialogBuilder.create();
        infoDialog.show();
    }

    private void showConsentInfo() {
        infoDialog.dismiss();

        LayoutInflater li = LayoutInflater.from(mainActivity);
        View dialogView = li.inflate(R.layout.dialog_info_consent, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setView(dialogView);

        // Set typefaces
        TextView mainTitle = (TextView) dialogView.findViewById(R.id.titleTV);
        mainTitle.setTypeface(mainActivity.futuraBold);
        TextView dataTitle = (TextView) dialogView.findViewById(R.id.dataCollectionTitle);
        dataTitle.setTypeface(mainActivity.futuraBold);
        TextView dataText = (TextView) dialogView.findViewById(R.id.dataCollectionText);
        dataText.setTypeface(mainActivity.futura);

        TextView anonymityTitle = (TextView) dialogView.findViewById(R.id.anonymityTitle);
        anonymityTitle.setTypeface(mainActivity.futuraBold);
        TextView anonymityText = (TextView) dialogView.findViewById(R.id.anonymityText);
        anonymityText.setTypeface(mainActivity.futura);

        TextView stopTitle = (TextView) dialogView.findViewById(R.id.stopTitle);
        stopTitle.setTypeface(mainActivity.futuraBold);
        TextView stopText = (TextView) dialogView.findViewById(R.id.stopText);
        stopText.setTypeface(mainActivity.futura);

        Button okBN = (Button) dialogView.findViewById(R.id.okBN);
        okBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoDialog.dismiss();
            }
        });

        infoDialog = alertDialogBuilder.create();
        infoDialog.show();
    }

    private void showContactInfo() {
        infoDialog.dismiss();

        LayoutInflater li = LayoutInflater.from(mainActivity);
        View dialogView = li.inflate(R.layout.dialog_info_contact, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setView(dialogView);

        // Set typefaces
        TextView contact1TitleTV = (TextView) dialogView.findViewById(R.id.contact1TitleTV);
        contact1TitleTV.setTypeface(mainActivity.futuraBold);
        TextView contact1TextTV = (TextView) dialogView.findViewById(R.id.contact1TextTV);
        contact1TextTV.setTypeface(mainActivity.futura);

        TextView contact2TitleTV = (TextView) dialogView.findViewById(R.id.contact2TitleTV);
        contact2TitleTV.setTypeface(mainActivity.futuraBold);
        TextView contact2TextTV = (TextView) dialogView.findViewById(R.id.contact2TextTV);
        contact2TextTV.setTypeface(mainActivity.futura);

        TextView contact3TitleTV = (TextView) dialogView.findViewById(R.id.contact3TitleTV);
        contact3TitleTV.setTypeface(mainActivity.futuraBold);
        TextView contact3TextTV = (TextView) dialogView.findViewById(R.id.contact3TextTV);
        contact3TextTV.setTypeface(mainActivity.futura);

        TextView contact4TitleTV = (TextView) dialogView.findViewById(R.id.contact4TitleTV);
        contact4TitleTV.setTypeface(mainActivity.futuraBold);
        TextView contact4TextTV = (TextView) dialogView.findViewById(R.id.contact4TextTV);
        contact4TextTV.setTypeface(mainActivity.futura);

        Button okBN = (Button) dialogView.findViewById(R.id.okBN);
        okBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoDialog.dismiss();
            }
        });

        infoDialog = alertDialogBuilder.create();
        infoDialog.show();
    }

    private void deactivateProfile(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You will no longer be visible to other users, and you won't be able to receive any updates" +
                " from them.\nThis action is irreversible.")
                .setTitle("Are you sure you want to deactivate your profile?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "Deactivate confirmed.");
                mainActivity.deactivateUser();
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Deactivate canceled.");
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private static class MySpinnerAdapter<String> extends ArrayAdapter<String> {

        private MySpinnerAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        // Affects default (closed) state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);

            if(view.getText().equals("<You may choose a start for your whisper>")) {
                view.setTextSize(14);
                view.setTypeface(mainActivity.futura, 0);   // 2 for italics
                view.setTextColor(ContextCompat.getColor(mainActivity, R.color.N2U_blue2));
            }
            else {
                view.setTypeface(mainActivity.futura, 0);
                view.setTextColor(ContextCompat.getColor(mainActivity, R.color.N2U_black));
                view.setTextSize(16);
            }
            return view;
        }

        // Affects opened state of the spinner
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);

            if(view.getText().equals("<You may choose a start for your whisper>")) {
                view.setTextSize(14);
                view.setTypeface(mainActivity.futura, 2);   // 2 for italics
                view.setTextColor(ContextCompat.getColor(mainActivity, R.color.N2U_blue2));
                view.setText("Leave empty");
            }
            else {
                view.setTypeface(mainActivity.futura, 0);
                view.setTextColor(ContextCompat.getColor(mainActivity, R.color.N2U_black));
                view.setTextSize(16);
            }
            return view;
        }
    }


    public void updateAvatar() {
        int width = avatarBN.getMeasuredWidth();
        int height = avatarBN.getMeasuredHeight();

        ImageView backgroundIV = (ImageView) view.findViewById(R.id.backgroundIV);
        final int bgHeight = backgroundIV.getMeasuredHeight();
        final int bgWidth = backgroundIV.getMeasuredWidth();

        Log.d(TAG, "Width - Height: " + width + " " + height);

        String filename = "N2Uavatar";
        File file = new File(mainActivity.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = mainActivity.openFileInput(filename);

                Bitmap avatarBMP = BitmapFactory.decodeStream(fileInputStream);

                avatarBN.setImageBitmap(avatarBMP);
                fileInputStream.close();

                // Adjust image size
//                avatarBN.setMinimumWidth(width - 10);
//                avatarBN.setMinimumHeight(height - 10);
//                avatarBN.setMaxHeight(bgHeight - 10);
//                avatarBN.setMaxWidth(bgWidth - 10);
                avatarBN.getLayoutParams().height = height;
                avatarBN.getLayoutParams().width = width;

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            }
        }
    }

    public void openGallery(){

        Intent intent = new Intent(getActivity(), ImageCropActivity.class);
        startActivity(intent);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult - Fragment, requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data.getData());

    }

    public void uploadImage() {

        Bitmap bitmap = null;

        String filename = "N2Uavatar";
        File file = new File(mainActivity.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = mainActivity.openFileInput(filename);

                bitmap = BitmapFactory.decodeStream(fileInputStream);

                fileInputStream.close();
            } catch (IOException e) {

            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("_id", mainActivity.myProfile.getId());
        params.put("image",encodedImage);
        JSONObject req = new JSONObject(params);


        HTTPRequest request;
        request = new HTTPRequest("UploadAvatar",
                mainActivity.HOST + mainActivity.PORT + "/profiles/pics/upload",
                Request.Method.POST,
                req);
        mainActivity.mHTTPHelper.sendToServer(request);

    }

    public interface MyProfileViewGridFragmentListener {
        void getCluesForUserGrid();
    }


}
