package fi.tut.cs.social.proximeety.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.classes.HTTPRequest;
import fi.tut.cs.social.proximeety.classes.MySingleton;
import fi.tut.cs.social.proximeety.classes.N2UService;
import fi.tut.cs.social.proximeety.classes.User;

public class HTTPHelper {
    private final String TAG = "HTTPHelper";    // Debugging TAG

    MainActivity mActivity = null;
    Context mContext = null;


    JSONObject mResponse = null;
    JSONArray mResponseArray = new JSONArray();
    public RequestQueue mQueue;

    public HTTPHelper(MainActivity mainActivity) {
        mActivity = mainActivity;
        mContext = mainActivity.getApplicationContext();
        mQueue = Volley.newRequestQueue(mContext);
    }

    public HTTPHelper(Context context) {
        mContext = context;
        mQueue = Volley.newRequestQueue(mContext);
    }

    public void sendToServer(final HTTPRequest request) {
//        Log.d(TAG, "SendToServer " + request.url + " - " + request.reqJSON);
        String URL = request.url.replaceAll(" ", "%20");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (request.method, URL, request.reqJSON, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.d(TAG, "Response: " + response);

                            // Depending on the type of the request, perform the corresponding action
                            switch (request.type) {
                                case "LogIn":
                                    mActivity.logIn(response);
                                    break;
                                case "Register":
                                    mActivity.register(response);
                                    break;
                                case "GetClues":
                                    //Log.d(TAG, response.toString());
                                    break;
                                case "UpdateClue":
                                    //mActivity.onClueSaved(response);
                                    break;
                                case "InsertClue":
                                    //mActivity.onClueSaved();
                                    break;
                                case "FragmentGrid - InsertClue":
                                    //Log.d(TAG, "Clue inserted from Grid Fragment");
                                    mActivity.onClueSavedGrid(response, false);
                                    break;
                                case "FragmentGrid - UpdateClue":
                                    mActivity.onClueSavedGrid(response, true);
                                    break;
                                case "OtherFragmentGrid - UpdateClue":
                                    mActivity.onClueSavedGridOther(response);
                                    break;
                                case "Service - ProfileByDeviceId":
                                    sendResponseToService(response, null, "Profile");
                                    break;
                                case "Service - ConnectionByOwnersId":
                                    sendResponseToService(response, null, "Connection");
                                    break;
                                case "UpdateUser":
//                                    Log.d(TAG, "Update User Response: " + response);
                                    break;
                                case "UpdateConnection - Block":
                                    mActivity.mConnectionsGridFragment.onConnectionBlocked();
                                    break;
                                case "UploadAvatar":
                                    //Log.d(TAG, "UploadAvatar response");
                                    mActivity.avatarUploaded = true;
                                    mActivity.mEditor.putBoolean("avatarUploaded", mActivity.avatarUploaded);
                                    mActivity.mEditor.commit();
                                    break;
                                case "OtherFragmentGrid - UpdateAvatar":
                                    Log.d(TAG, "OtherFragmentGrid - UpdateAvatar");
                                    // Decode string to bitmap
                                    byte[] decodedString = new byte[0];
                                    try {
                                        decodedString = Base64.decode(response.getString("image"), Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        // write the cropped image to a local file and notify the main Activity
                                        File file = new File(mActivity.getFilesDir(), response.getString("_id"));

                                        // Create the file if it doesn't already exist
                                        if (!file.exists()) {
                                            try {
                                                file.createNewFile();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Intent imageSelectedIntent = new Intent(MainActivity.IMAGE_SELECTED);
                                        FileOutputStream outputStream = null;
                                        try {
                                            outputStream = mActivity.openFileOutput(response.getString("_id"), Context.MODE_PRIVATE);
                                            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } finally {
                                            try {
                                                if (outputStream != null) {
                                                    outputStream.close();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    mActivity.mOtherProfileGridFragment.updateAvatar();
                                    break;
                                case "GetConnection":
//                                    Log.d(TAG, "Get Single Connection: " + response);
                                    mActivity.goToOtherProfileForF2F(response);
                                    break;
                            }
//                        }
//                        catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }, new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "VolleyError: " + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                String credentials = String.format("%s:%s","admin","cosmo");
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                headers.put("Authorization", auth);
                return headers;
            }
        };
        MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);

    }

    public void sendToServerArray(final HTTPRequest request) {
//        Log.d(TAG, "SendToServerArray " + request.url);
        String URL = request.url.replaceAll(" ", "%20");

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (request.method, URL, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
//                        for(int i=0; i<response.length(); i++) {
//                            try {
////                                Log.d(TAG, "Res: " + response.get(i));
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }

                        switch (request.type) {
                            case "GetAllUsers":
                                // Add all retrieved profiles to Activity's list of users
                                mActivity.mUsers.clear();
                                for(int i=0; i<response.length(); i++) {
                                    User tempUser = null;
                                    try {
                                        JSONObject tempJSON = (JSONObject) response.get(i);
                                        tempUser = new User(tempJSON.getString("_id"), tempJSON.getString("username"),
                                                tempJSON.getString("deviceId"), tempJSON.getInt("clues"));
//                                        Log.d(TAG, "tempUser.clues: " + tempUser.getClues() + " - tempJSON.getIntclues" + tempJSON.getInt("clues"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    mActivity.mUsers.add(tempUser);
                                }
                                break;
                            case "GetAllUsers - Refresh":
                                // Add all retrieved profiles to Activity's list of users
                                for(int i=0; i<response.length(); i++) {
                                    User tempUser = null;
                                    try {
                                        JSONObject tempJSON = (JSONObject) response.get(i);
                                        tempUser = new User(tempJSON.getString("_id"), tempJSON.getString("username"),
                                                tempJSON.getString("deviceId"), tempJSON.getInt("clues"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    mActivity.mUsers.add(tempUser);
                                }
                                mActivity.mConnectionsGridFragment.refreshFragment();
//                                if(mActivity.currentFragment == mActivity.mConnectionsListFragment) {
//                                    mActivity.mConnectionsListFragment.refreshConnections();
//                                }
                                break;
                            case "GetCluesGrid":
                                mActivity.populateCluesGrid(response);
                                break;
                            case "GetConnectionsGrid":
                                mActivity.populateConnectionsGrid(response);
                                break;
                            case "GetCluesOtherGrid":
                                mActivity.populateCluesGridOther(response);
                                break;
                            case "Service - GetConnections":
                                sendResponseToService(null, response, "Connections");
                                break;
                            case "ChatFragment - GetConversation":
                                mActivity.loadConversation(response);
                                break;
                            case "SentMessages":
                                mActivity.onGetSentMessages(response);
                                break;
                            case "ReceivedMessages":
                                mActivity.onGetReceivedMessages(response);
                                break;
                        }
                    }
                }, new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "VolleyError: " + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                String credentials = String.format("%s:%s","admin","cosmo");
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                headers.put("Authorization", auth);
                return headers;
            }
        };
        MySingleton.getInstance(mContext).addToRequestQueue(jsonArrayRequest);

    }

    public void sendResponseToService(JSONObject response, JSONArray responseArray, String option) {
        Intent i = new Intent(mContext, N2UService.class);
        switch (option) {
            case "Profile":
                i.putExtra("Trigger", "HTTPHelper");
                i.putExtra("ResponseType" , "Profile");
                i.putExtra("Response",response.toString());
                mContext.startService(i);
                break;
            case "Connections":
                i.putExtra("Trigger", "HTTPHelper");
                i.putExtra("ResponseType" , "Connections");
                i.putExtra("Response", responseArray.toString());
                mContext.startService(i);
                break;
        }
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
