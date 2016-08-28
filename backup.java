package fi.tut.cs.social.proximeety.classes;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

public class MultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;
    private final String mMimeType;
    private final byte[] mMultipartBody;
    
    public MultipartRequest(String url, Map<String, String> headers, String mimeType, byte[] multipartBody, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
        this.mMimeType = mimeType;
        this.mMultipartBody = multipartBody;
    }
    
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }
    
    @Override
    public String getBodyContentType() {
        return mMimeType;
    }
    
    @Override
    public byte[] getBody() throws AuthFailureError {
        return mMultipartBody;
    }
    
    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                                    response,
                                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
    
    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }
    
    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}


throws AuthFailureError {

Drawable d = new BitmapDrawable(mActivity.getResources(), bitmap);
byte[] fileData = getFileDataFromDrawable(mActivity, d);

ByteArrayOutputStream bos = new ByteArrayOutputStream();
DataOutputStream dos = new DataOutputStream(bos);
try {
// the first file
buildPart(dos, fileData, "upl");
// send multipart form data necesssary after file data
dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
// pass to multipart body
multipartBody = bos.toByteArray();
} catch (IOException e) {
e.printStackTrace();
}

String url = "http://192.168.1.2:3000/profiles/pics/upload";

MultipartRequest multipartRequest = new MultipartRequest(url, null, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
@Override
public void onResponse(NetworkResponse response) {
Toast.makeText(mActivity, "Uploaded successfully!", Toast.LENGTH_SHORT).show();
}
}, new Response.ErrorListener() {
@Override
public void onErrorResponse(VolleyError error) {
Toast.makeText(mActivity, "Upload failed!\r\n" + error.toString(), Toast.LENGTH_SHORT).show();
}
})
{
@Override
public Map<String, String> getHeaders() throws AuthFailureError {
HashMap<String, String> headers = new HashMap<String, String>();
//headers.put("Accept", "application/json");
//headers.put("enctype", "multipart/form-data");
String credentials = String.format("%s:%s","admin","cosmo");
String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
headers.put("Authorization", auth);
return headers;
}
};
MySingleton.getInstance(mContext).addToRequestQueue(multipartRequest);
Log.d(TAG, "Multipart request: " + multipartRequest.getBody());


private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName) throws IOException {
dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\""
+ fileName + "\"" + lineEnd);
dataOutputStream.writeBytes(lineEnd);

ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
int bytesAvailable = fileInputStream.available();

int maxBufferSize = 1024 * 1024;
int bufferSize = Math.min(bytesAvailable, maxBufferSize);
byte[] buffer = new byte[bufferSize];

// read file and write it into form...
int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

while (bytesRead > 0) {
dataOutputStream.write(buffer, 0, bufferSize);
bytesAvailable = fileInputStream.available();
bufferSize = Math.min(bytesAvailable, maxBufferSize);
bytesRead = fileInputStream.read(buffer, 0, bufferSize);
}

dataOutputStream.writeBytes(lineEnd);
}

private byte[] getFileDataFromDrawable(Context context, Drawable d) {
Drawable drawable = d;
Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
return byteArrayOutputStream.toByteArray();
}


public String getStringImage(Bitmap bmp){
ByteArrayOutputStream baos = new ByteArrayOutputStream();
bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
byte[] imageBytes = baos.toByteArray();
String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
return encodedImage;
}

public void uploadImage(Bitmap bmp){
final Bitmap bitmap = bmp;

//Showing the progress dialog
//final ProgressDialog loading = ProgressDialog.show(mActivity, "Uploading...", "Please wait...", false, false);
StringRequest stringRequest = new StringRequest(Request.Method.POST, mActivity.HOST + mActivity.PORT + "/profiles/pics/upload",
new Response.Listener<String>() {
@Override
public void onResponse(String s) {
//Disimiss the progress dialog
//loading.dismiss();
//Showing toast message of the response
Toast.makeText(mActivity, s , Toast.LENGTH_LONG).show();
}
},
new Response.ErrorListener() {
@Override
public void onErrorResponse(VolleyError volleyError) {
//Dismissing the progress dialog
//loading.dismiss();

//Showing toast
//                        Toast.makeText(mActivity, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
Toast.makeText(mActivity, "Error response", Toast.LENGTH_LONG).show();
}
}){
@Override
protected Map<String, String> getParams() throws AuthFailureError {
//Converting Bitmap to String
String image = getStringImage(bitmap);

//Getting Image Name
//String name = editTextName.getText().toString().trim();
String name = "asdffdsa.bmp";

//Creating parameters
Map<String,String> params = new Hashtable<String, String>();

//Adding parameters
params.put("image", image);
params.put("name", name);

//returning parameters
return params;
}
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

MySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
}


<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:background="@color/N2U_blue2"
        android:orientation="vertical"
        >

<RelativeLayout
android:id="@+id/avatarLayout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:background="@color/N2U_blue1"
        android:layout_marginBottom="4dp"
        >

<ImageView
android:id="@+id/avatarIV"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@mipmap/avatar01"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="14dp"
        />

<!--<ImageView-->
<!--android:id="@+id/backgroundIV"-->
<!--android:layout_alignParentTop="true"-->
<!--android:layout_width="wrap_content"-->
<!--android:layout_height="wrap_content"-->
<!--android:minHeight="0dp"-->
<!--android:padding="0dp"-->
<!--android:src="@drawable/my_profile_border"-->
<!--android:scaleType="matrix"-->
<!--/>-->

<TextView
android:id="@+id/usernameTV"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@color/N2U_black"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_centerHorizontal="true"
        android:layout_below="@+id/avatarIV"
        android:text="username"
        android:layout_marginTop="4dp"
        android:layout_marginBottom="8dp"
        />

</RelativeLayout>

<LinearLayout
android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_horizontal"
        android:paddingLeft="4dp"
        android:paddingRight="4dp"
        >

<Button
android:id="@+id/chatBN"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Messages"
        android:background="@color/N2U_orange"
        android:textColor="@color/white"
        android:textSize="17sp"
        style="?android:attr/borderlessButtonStyle"
        android:scaleType="fitCenter"
        android:textAllCaps="false"
        android:layout_marginLeft="4dp"
        android:layout_marginRight="4dp"
        android:gravity="center"
        android:paddingTop="5dp"
        android:paddingBottom="5dp"
        />

<Button
android:id="@+id/face2faceBN"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Face2Face"
        style="?android:attr/borderlessButtonStyle"
        android:scaleType="fitCenter"
        android:background="@color/N2U_orange"
        android:textColor="@color/white"
        android:textAllCaps="false"
        android:textSize="17sp"
        android:layout_marginRight="4dp"
        android:gravity="center"
        android:paddingTop="5dp"
        android:paddingBottom="5dp"
        />

</LinearLayout>


<RelativeLayout
android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center|top"
        android:paddingTop="4dp"
        android:paddingRight="4dp"
        >

<ProgressBar
android:id="@+id/loadingPB"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:indeterminate="true"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        />


<com.woxthebox.draglistview.DragListView
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/drag_list_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginBottom="4dp"
        />

</RelativeLayout>

</LinearLayout>





<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:paddingRight="10dp"
        android:background="@color/n2u_dark1"
        android:orientation="vertical"
        >

<RelativeLayout
android:id="@+id/avatarLayout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        >

<FrameLayout
android:id="@+id/avatarFrame"
        android:layout_width="110dp"
        android:layout_height="110dp"
        android:layout_alignParentTop="true"
        android:layout_centerInParent="true"
        >

<ImageButton
android:id="@+id/avatarBN"
        android:layout_width="110dp"
        android:layout_height="110dp"
        android:src="@drawable/avatar"
        android:layout_alignParentTop="true"
        android:layout_centerInParent="true"
        android:layout_marginTop="10dp"
        android:layout_marginBottom="5dp"
        style="?android:attr/borderlessButtonStyle"
        android:scaleType="fitCenter"
        />

<ImageView
android:layout_width="110dp"
        android:layout_height="110dp"
        android:scaleType="fitCenter"
        android:src="@drawable/avatar_frame"
        android:layout_alignParentTop="true"
        android:layout_centerInParent="true"
        android:layout_marginTop="10dp"
        android:layout_marginBottom="5dp"
        />

</FrameLayout>

<ImageView
android:id="@+id/connectionsIV"
        android:layout_width="30dp"
        android:layout_height="30dp"
        android:src="@drawable/total_connections"
        android:layout_centerVertical="true"
        android:layout_alignParentLeft="true"
        android:layout_alignTop="@+id/avatarFrame"
        android:layout_marginLeft="30dp"
        android:layout_marginTop="35dp"
        />

<TextView
android:id="@+id/connectionsTV"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="3"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/white"
        android:layout_toRightOf="@+id/connectionsIV"
        android:layout_alignBottom="@+id/connectionsIV"
        android:layout_marginLeft="10dp"
        android:paddingBottom="5dp"
        />

<ImageView
android:id="@+id/face2faceIV"
        android:layout_width="30dp"
        android:layout_height="30dp"
        android:src="@drawable/face2face"
        android:layout_marginLeft="30dp"
        android:layout_marginBottom="25dp"
        android:layout_alignParentLeft="true"
        android:layout_alignBottom="@+id/usernameTV"
        />

<TextView
android:id="@+id/face2faceTV"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="0"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/white"
        android:layout_toRightOf="@+id/face2faceIV"
        android:layout_alignBottom="@+id/face2faceIV"
        android:layout_marginLeft="10dp"
        android:paddingBottom="5dp"
        />

<TextView
android:id="@+id/usernameTV"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@color/white"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_centerHorizontal="true"
        android:layout_below="@+id/avatarFrame"
        android:text="username"
        android:layout_marginBottom="10dp"
        />

<ImageView
android:id="@+id/achievementsIV"
        android:layout_width="wrap_content"
        android:layout_height="36dp"
        android:src="@drawable/total_achievements"
        android:layout_marginLeft="10dp"
        android:layout_marginTop="35dp"
        android:layout_marginRight="5dp"
        android:layout_toRightOf="@+id/avatarFrame"
        android:layout_alignTop="@+id/avatarFrame"
        />

<TextView
android:id="@+id/achievementsTV"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="0"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/white"
        android:layout_toRightOf="@+id/achievementsIV"
        android:layout_alignBottom="@+id/connectionsIV"
        android:paddingBottom="5dp"
        />



</RelativeLayout>

<RelativeLayout
android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center|top"
        >

<TextView
android:id="@+id/infoTV"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@+id/line1IV"
        android:text="@string/empty_profile_info"
        android:layout_marginTop="10dp"
        android:layout_marginBottom="5dp"
        android:layout_marginLeft="16dp"
        android:layout_marginRight="16dp"
        android:textSize="20dp"
        android:textColor="@color/white"
        android:textStyle="italic"
        />

<ProgressBar
android:id="@+id/loadingPB"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:indeterminate="true"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        />


<com.woxthebox.draglistview.DragListView
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/drag_list_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginBottom="35dp"
        />

<ImageButton
android:layout_width="wrap_content"
        android:layout_height="30dp"
        android:layout_marginBottom="2dp"
        android:scaleType="fitCenter"
        android:paddingTop="2dp"
        android:paddingBottom="2dp"
        android:src="@mipmap/add"
        android:id="@+id/addBN"
        style="?android:attr/borderlessButtonStyle"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:paddingLeft="20dp"
        />

</RelativeLayout>

</LinearLayout>


/**********************************************************************************************************************/


<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/header_bg"
        android:gravity="top"
        android:isScrollContainer="false"
        >

<ImageButton
android:id="@+id/achievementsBN"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_alignParentTop="true"
        android:src="@mipmap/achievements_icon_72x72"
        android:layout_toLeftOf="@+id/connectionsBN"
        style="?android:attr/borderlessButtonStyle"
        />

<ImageButton
android:id="@+id/connectionsBN"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_alignParentTop="true"
        android:src="@mipmap/achievements_icon_72x72"
        android:layout_toLeftOf="@+id/avatarLayout"
        style="?android:attr/borderlessButtonStyle"
        android:scaleType="fitCenter"
        />

<FrameLayout
android:id="@+id/avatarLayout"
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:layout_alignParentRight="true"
        android:layout_alignParentTop="true"
        android:layout_marginRight="5dp"
        >

<ImageButton
android:id="@+id/avatarBN"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@mipmap/my_profile"
        style="?android:attr/borderlessButtonStyle"
        android:scaleType="centerCrop"
        />

<ImageView
android:layout_width="50dp"
        android:layout_height="50dp"
        android:scaleType="fitCenter"
        android:src="@mipmap/my_profile_frame"
        />


</FrameLayout>

</RelativeLayout>


<!--<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"-->
<!--xmlns:tools="http://schemas.android.com/tools"-->
<!--android:layout_width="match_parent"-->
<!--android:layout_height="wrap_content"-->
<!--android:orientation="horizontal"-->
<!--tools:context="fi.tut.cs.social.proximeety.Fragments.HeaderFragment"-->
<!--android:background="@drawable/header_bg"-->
<!--android:gravity="right"-->
<!--android:weightSum="1"-->
<!-->-->


<!--<ImageButton-->
<!--android:id="@+id/achievementsBN"-->
<!--android:layout_width="0dp"-->
<!--android:layout_height="wrap_content"-->
<!--android:src="@mipmap/achievements_icon"-->
<!--style="?android:attr/borderlessButtonStyle"-->
<!--android:scaleType="fitCenter"-->
<!--android:adjustViewBounds="true"-->
<!--android:layout_weight="0.2"-->
<!--/>-->

<!--<ImageButton-->
<!--android:id="@+id/connectionsBN"-->
<!--android:layout_width="0dp"-->
<!--android:layout_height="wrap_content"-->
<!--android:src="@mipmap/connections"-->
<!--style="?android:attr/borderlessButtonStyle"-->
<!--android:scaleType="fitCenter"-->
<!--android:adjustViewBounds="true"-->
<!--android:layout_weight="0.2"-->
<!--/>-->

<!--<ImageButton-->
<!--android:id="@+id/avatarBN"-->
<!--android:layout_width="0dp"-->
<!--android:layout_height="wrap_content"-->
<!--android:layout_marginRight="5dp"-->
<!--style="?android:attr/borderlessButtonStyle"-->
<!--android:src="@mipmap/my_profile"-->
<!--android:scaleType="fitCenter"-->
<!--android:adjustViewBounds="true"-->
<!--android:layout_weight="0.2"-->
<!--/>-->




<!--</LinearLayout>-->


<?xml version="1.0" encoding="utf-8"?>
<!-- Copyright 2014 Magnus Woxblom
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
        -->
<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center|top"
        android:orientation="vertical"
        >

<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:gravity="center"
        android:paddingRight="10dp"
        android:background="@color/background_dark"
        android:orientation="vertical"
        >

<ImageView
android:layout_width="wrap_content"
        android:layout_height="100dp"
        android:id="@+id/imageView"
        android:src="@drawable/avatar"
        android:layout_marginTop="10dp"
        />


<LinearLayout
android:id="@+id/symbols_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_below="@id/imageView"
        android:gravity="center"
        android:weightSum="1"
        android:layout_marginTop="10dp"
        >

<ImageView
android:layout_width="0dp"
        android:layout_height="50dp"
        android:id="@+id/imageView2"
        android:layout_gravity="bottom"
        android:src="@drawable/connections_symbol"
        android:layout_below="@+id/imageView"
        android:layout_marginBottom="1dp"
        android:layout_weight="0.33"
        />

<ImageView
android:layout_width="0dp"
        android:layout_height="50dp"
        android:id="@+id/imageView3"
        android:layout_gravity="center|bottom"
        android:src="@drawable/f2f_symbol"
        android:layout_toRightOf="@+id/imageView2"
        android:layout_below="@+id/imageView"
        android:layout_weight="0.33"
        />

<ImageView
android:layout_width="0dp"
        android:layout_height="50dp"
        android:id="@+id/imageView4"
        android:layout_gravity="bottom"
        android:src="@drawable/achievements_symbol"
        android:layout_toRightOf="@+id/imageView3"
        android:layout_below="@+id/imageView"
        android:layout_weight="0.33"
        />

</LinearLayout>

<LinearLayout
android:id="@+id/numbers_layout"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_below="@id/symbols_layout"
        android:gravity="center"
        android:weightSum="1"
        >

<TextView
android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:id="@+id/textView1"
        android:text="1"
        android:gravity="center"
        android:textColor="@color/white"
        android:textStyle="bold"
        android:textSize="20dp"
        android:src="@drawable/connections_symbol"
        android:layout_weight="0.33"
        />

<TextView
android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:id="@+id/textView2"
        android:text="0"
        android:gravity="center"
        android:textColor="@color/white"
        android:textStyle="bold"
        android:textSize="20dp"
        android:src="@drawable/connections_symbol"
        android:layout_weight="0.33"
        />

<TextView
android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:id="@+id/textView3"
        android:text="0"
        android:gravity="center"
        android:textColor="@color/white"
        android:textStyle="bold"
        android:textSize="20dp"
        android:src="@drawable/connections_symbol"
        android:layout_weight="0.33"
        />

</LinearLayout>

</LinearLayout>


<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:paddingRight="10dp"
        >

<com.woxthebox.draglistview.DragListView
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/drag_list_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</LinearLayout>

</LinearLayout>



package fi.tut.cs.social.proximeety.classes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import fi.tut.cs.social.proximeety.helpers.HTTPHelper;

public class N2YService extends Service {
    private static final String TAG = "N2YService";
    private static AlarmManager alarm = null;
    public static final long PROXIMITY_SCAN_INTERVAL = 60000;  //2 minutes
    public static final long UPDATE_MIN_INTERVAL = 300000;      //5 minutes

    private static Profile myProfile;
    private static SharedPreferences mSettings;
    private static SharedPreferences.Editor mEditor;
    private static BluetoothAdapter mBluetoothAdapter;
    private static HTTPHelper mHTTPHelper;
    public static List<BluetoothDevice> mDevices;

    public static SimpleDateFormat format;

    File file;
    String filename;
    FileOutputStream outputStream;

    public static List<Profile> mContacts;
    public static List<Connection> mConnections;
    public static List<String> updates;

    public static boolean updateFlag;
    public static boolean connectionFlag;

    public final String HOST = "http://social.cs.tut.fi:";
    public final String PORT = "10001";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");

        // Open the Default SharedPreferences file (create if it doesn't exist) to read myProfile
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        myProfile = new Profile(mSettings.getString("_id", ""),
                mSettings.getString("username", ""),
                mSettings.getString("password", ""),
                mSettings.getString("deviceId", ""));

        mHTTPHelper = new HTTPHelper(getApplicationContext());

        mDevices = new ArrayList<BluetoothDevice>();
        updates = new ArrayList<String>();

        mContacts = new ArrayList<Profile>();
        mConnections = new ArrayList<Connection>();

//        format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        format = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);

        // Cancel the Alarm
        if (isAlarmUp()) {
            Log.d(TAG, "Cancelling AlarmReceiver...");
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.cancel(pIntent);
        }
        // TODO
        unregisterReceiver(ActionFoundReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null) {
            Bundle extras = intent.getExtras();
            String trigger = extras.getString("Trigger");

            // If the Service was triggered from the AlarmReceiver, perform the appropriate periodic tasks
            if(trigger.equals("Alarm")) {
                bluetoothDiscovery();
            }
            // If the Service was triggered by the Activity or System Boot Receiver
            else if((trigger.equals("Activity") || trigger.equals("Boot"))) {

                // If myProfile was not written in the SharedPreferences file, stop the Service
                if(myProfile.getId().equals("")) {
                    Log.d(TAG, "Stopping Service");
                    stopSelf();
                }
                else {
                    // Start the AlarmReceiver if it is not already running
                    if(!isAlarmUp()) {
                        Log.d(TAG, "Scheduling alarm, profile: " + myProfile.getUsername() + " " + myProfile.getDeviceId());
                        scheduleAlarm();
                    }
                }
            }
            // If the Service was triggered by the HTTP Helper, get the JSON response
            else if(trigger.equals("HTTPHelper")) {

                if(extras.getString("ResponseType").equals("Profile")) {
                    JSONObject response = null;
                    try {
                        response = new JSONObject(extras.getString("Response"));
                        if(response != null) {
                            //Log.d(TAG, "Service: Received HTTPHelper data: " + response.getString("username"));
                            if(!response.getString("_id").equals("-1"))
                                onReceiveProfile(response);
                        }
                        else
                            Log.d(TAG, "response is null");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(extras.getString("ResponseType").equals("Connections")) {
                    JSONArray response = null;
                    try {
                        response = new JSONArray(extras.getString("Response"));
                        if(response != null) {
                            updateConnections(response);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        else {
            Log.d(TAG, "onStartCommand() - null");
        }

        return START_NOT_STICKY;
    }

    public void onReceiveProfile(JSONObject response) {
        try {
            Profile tempProfile = new Profile(response.getString("_id"),
                    response.getString("username"),
                    response.getString("password"),
                    response.getString("deviceId"));
            //Log.d(TAG, "In service: Created new profile with username: " + tempProfile.getUsername());
            mContacts.add(tempProfile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getConnectionsForUser() {
        Log.d(TAG, "getConnectionsForUser()");

        // Get All connections for current user from the server
        HTTPRequest request = new HTTPRequest("Service - GetConnections",
                HOST + PORT + "/connections/userId=" + myProfile.getId(),
                Request.Method.GET,
                null
        );
        mHTTPHelper.sendToServerArray(request);
    }

    public void updateConnections(JSONArray response) {
        Log.d(TAG, "updateConnections() " + response.toString());
        mConnections.clear();
        updates.clear();
        updateFlag = false;
        connectionFlag = false;

        // Add all retrieved connections to mConnections list
        JSONObject tempJSON;
        try {
            Date now = new Date();

            // For all the connections found in the server for the current user
            for (int i = 0; i < response.length(); i++) {

                // Create a Connection object from the JSONObject
                tempJSON = (JSONObject) response.get(i);
                Log.d(TAG, "From date: " + tempJSON.getString("lastMet") + " can get date: " + stringToDate(tempJSON.getString("lastMet")) );
                Connection tempConnection = new Connection(tempJSON.getString("_id"),
                        tempJSON.getString("_user1Id"),
                        tempJSON.getString("_user2Id"),
                        Integer.parseInt(tempJSON.getString("timesMet")),
                        format.parse(tempJSON.getString("lastMet")),
                        format.parse(tempJSON.getString("lastUpdate"))
                );

                // Search the list of devices in range, to see if the other user is currently in proximity
                for(int j=0; j<mContacts.size(); j++) {
                    if(mContacts.get(j).getId().equals(tempConnection.user1Id) || mContacts.get(j).getId().equals(tempConnection.user2Id)) {
                        Log.d(TAG, "Connection is in range: " + mContacts.get(j).getId());

                        // If the tim of the lastUpdate of the connection was after the last scan and before now,
                        // Another device has updated the document on the server. The notification flag is true
                        if(compareDates(tempConnection.lastUpdate)) {
                            // Add the id of the updated profile to the list of updates
                            if(!tempConnection.user1Id.equals(myProfile.getId()))
                                updates.add(tempConnection.user1Id);
                            else
                                updates.add(tempConnection.user2Id);

                            updateFlag = true;
                        }

                        // Update connection and add to the list
                        if(timeForUpdate(tempConnection.lastUpdate)) {
                            updateFlag = true;
                            tempConnection.lastUpdate = now;
                            tempConnection.timesMet++;
                        }
                        tempConnection.lastMet = now;
                        mConnections.add(tempConnection);

                        mContacts.remove(j);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date now = new Date();

        // The remaining elements in mContacts list are added as new connections
        if(mContacts.size() > 0)
            connectionFlag = true;
        for(int i=0; i<mContacts.size(); i++) {
            Connection tempConnection = new Connection(null,
                    myProfile.getId(),
                    mContacts.get(i).getId(),
                    1,
                    now,
                    now);
            mConnections.add(tempConnection);
        }

        // Save all connections to the server
        for(int i=0; i<mConnections.size(); i++) {

            Log.d(TAG, "Adding connection to the server: " + mConnections.get(i).user1Id + " "
                    + mConnections.get(i).user2Id + " " + mConnections.get(i).lastMet + " "
                    + mConnections.get(i).lastUpdate + " " + mConnections.get(i).timesMet);

            HashMap<String, String> params = new HashMap<String, String>();
            HTTPRequest request;

            params.put("_id", mConnections.get(i)._id);
            params.put("_user1Id", mConnections.get(i).user1Id);
            params.put("_user2Id", mConnections.get(i).user2Id);
            params.put("timesMet", Integer.toString(mConnections.get(i).timesMet));
            params.put("lastMet", mConnections.get(i).lastMet.toString());
            params.put("lastUpdate", mConnections.get(i).lastUpdate.toString());
            JSONObject req = new JSONObject(params);

            // If the _id of the connection is null, create a new connection
            if(mConnections.get(i)._id == null) {
                request = new HTTPRequest("AddConnection",
                        HOST + PORT + "/connections/",
                        Request.Method.POST,
                        req);
                mHTTPHelper.sendToServer(request);
            }
            // If the _id is NOT null, update the existing connection document on the server
            else {
                request = new HTTPRequest("UpdateConnection",
                        HOST + PORT + "/connections/update",
                        Request.Method.PUT,
                        req);
                mHTTPHelper.sendToServer(request);
            }
        }

        // Save the list of updated connections to the shared file
        filename = "N2Yupdates";
        try {
            file = new File(getApplicationContext().getFilesDir(), filename);

            // Create the file if it doesn't already exist
            if (!file.exists()) {
                Log.d(TAG, "updateConnections() - file does not exist");
                file.createNewFile();
            }
            // Write to the file
            outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(updates);
            objectOutputStream.close();
            outputStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Unable to access file, " + e.toString());
        }



    }

    public void bluetoothDiscovery() {
        Log.d(TAG, "Starting Bluetooth Discovery...");
        mContacts.clear();
        mDevices.clear();

        // Initialize Bluetooth mAdapter.
        final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Register the BroadcastReceiver for Bluetooth Discovery events
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        //TODO: Don't forget to unregister during onDestroy
        registerReceiver(ActionFoundReceiver, filter);

        mBluetoothAdapter.startDiscovery();
    }

    public void onDeviceFound(BluetoothDevice device) {
        // Perform a query to the server for a Profile with the specific deviceId
        HTTPRequest request = new HTTPRequest("Service - ProfileByDeviceId",
                HOST + PORT + "/profiles/deviceId/" + device.getAddress(),
                Request.Method.GET,
                null
        );

        mHTTPHelper.sendToServer(request);
    }

    // Custom receiver that enables the corresponding action related to Bluetooth discovery
    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When the Bluetooth discovery finds a device
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "Found device: " + device.getName() + " - " + device.getAddress());

                if(!mDevices.contains(device)) {
                    onDeviceFound(device);
                    mDevices.add(device);
                }
            }
            // When the Bluetooth discovery is finished
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "ACTION_DISCOVERY_FINISHED ");

                getConnectionsForUser();
            }
        }
    };

    public void scheduleAlarm() {
        Log.d(TAG, "scheduleAlarm");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                PROXIMITY_SCAN_INTERVAL, pIntent);

    }

    public boolean isAlarmUp() {
        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                new Intent(getApplicationContext(), AlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            return true;
        }

        return false;
    }

    public boolean timeForUpdate(Date date) {

        Date now = new Date();

        long diff = now.getTime() - date.getTime();

        Log.d(TAG, "timeForUpdate, now: " + now.toString() + ", date: " + date.toString() + ", diff: " + diff);

        if(diff >= UPDATE_MIN_INTERVAL)
            return true;
        else
            return false;
    }

    //TODO: Update with push notification
    public boolean compareDates(Date date) {
        Date now = new Date();

        long diff = now.getTime() - date.getTime();

        if(diff > 0 && diff < PROXIMITY_SCAN_INTERVAL)
            return true;
        else
            return false;
    }

    public String getCurrentDate() {

        Calendar cal = new GregorianCalendar();
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
        dateFormat.setCalendar(cal);
        String dateFormatted = dateFormat.format(cal.getTime());
        Log.d(TAG, "getCurrentDate(): " + dateFormatted);

        return dateFormatted;
    }

    public Date stringToDate(String date) {
        Date formattedDate = null;
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
        try {
            formattedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }
}




/* ************************************ DATE FORMATTING *************************************** */
Calendar cal = new GregorianCalendar();
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
        fmt.setCalendar(cal);
        String dateFormatted = fmt.format(cal.getTime());
        Log.d(TAG, "DATE: " + dateFormatted);
        String newDate = "13 Oct 2015, 01:32:51";
        try {
            Date testDate = fmt.parse(newDate);
            Log.d(TAG, "DATE2 : " + fmt.format(testDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
/* ************************************ DATE FORMATTING *************************************** */


/* ************************************ CONNECTIONS LIST FRAGMENT *************************************** */
public class ConnectionsListFragment extends ListFragment {
    private final String TAG = "ConnectionsListFragment";

    MainActivity mainActivity;
    ListView mListView;
    List<Connection> mConnections = new ArrayList<Connection>();
    ImageButton mScanBN;
    ConnectionsListAdapter mAdapter;
    ConnectionListFragmentListener mCallback;
    Cache cache;
    Runnable refresher = null;

    public ConnectionsListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.fragment_connections_list, container, false);

        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // List View
        mListView = (ListView) view.findViewById(android.R.id.list);
        mAdapter = new ConnectionsListAdapter(mainActivity, mConnections);
        mListView.setAdapter(mAdapter);
        //updateList();

        // Buttons
        mScanBN = (ImageButton) view.findViewById(R.id.scanBN);
        mScanBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Scan Button");
                    updateList();
            }
        });

        //refreshConnections();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ConnectionListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConnectionListFragmentListener");
        }
    }

    public interface ConnectionListFragmentListener {
        void addConnectionForUser(String user1Id, String user2Id);
    }

    public void addItemToList(Connection connection) {

//        if(connection.getTimesMet() > 0) {
//            mConnections.add(connection);
//            mAdapter.notifyDataSetChanged();
//        }
    }

    public void updateList() {
        // Read the shared file
//        File file = new File(mainActivity.getFilesDir(), "NextToYouCache");
//        if (file.exists()) {
//            try {
//                try {
//                    FileInputStream fileInputStream = mainActivity.openFileInput("NextToYouCache");
//                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                    cache = (Cache) objectInputStream.readObject();
//                    objectInputStream.close();
//                    fileInputStream.close();
//                } catch (ClassNotFoundException e) {
//                    Log.e(TAG, "Profile class not found while reading file " + "NextToYouCache");
//                }
//                Log.d(TAG, "Cache: " + cache.mProfile.getUsername());
//                Log.d(TAG, "Cache: " + cache.mConnection1.getUser2Username() + " - " + cache.mConnection1.getmUser2DeviceId() + " - " + cache.mConnection1.getTimesMet());
//                Log.d(TAG, "Cache: " + cache.mConnection2.getUser2Username() + " - " + cache.mConnection2.getmUser2DeviceId() + " - " + cache.mConnection2.getTimesMet());
//            } catch (IOException e) {
//                Log.e(TAG, "Unable to access file, " + e.toString());
//            }
//            mConnections.clear();
//            addItemToList(cache.mConnection1);
//            addItemToList(cache.mConnection2);
//        }
    }


    @Override
    public void onListItemClick(ListView mListView, View v, int position, long id) {
        Log.d(TAG, "OnListItemClick: " + position);
        mainActivity.connectionsListItemOnClick((Connection) mListView.getItemAtPosition(position));
    }

    public void refreshConnections() {

    }
}





/* ************************************ MAIN ACTIVITY ***************************************
   ************************************* onCreate() ***************************************** */
    // If the service is running in the background
        if(isMyServiceRunning(NextToYouService.class)) {
            Profile temp = null;

            // Read the shared file
            File file = new File(this.getFilesDir(), filename);
            if(file.exists()) {
                try {
                    try {
                        FileInputStream fileInputStream = this.openFileInput(filename);
                        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                        temp = (Profile) objectInputStream.readObject();
                        objectInputStream.close();
                        fileInputStream.close();
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "Profile class not found while reading file " + filename);
                    }
                    Log.d(TAG, "temp.username: " + temp.getUsername());
                } catch (IOException e) {
                    Log.e(TAG, "Unable to access file, " + e.toString());
                }
                myProfile.setId(temp.getId());
                myProfile.setUsername(temp.getUsername());
                myProfile.setPassword(temp.getPassword());
                myProfile.setDeviceId(temp.getDeviceId());
            }

            this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

            //initiate();
            createConnections();
        }
        else {
            // Add the fragment on initial activity setup
            getSupportFragmentManager().beginTransaction().add(R.id.main_layout, mLogInRegisterFragment).commit();
            currentFragment = mLogInRegisterFragment;
        }

public void createConnections() {
        /* ******************************************************** */
        //TODO: Not based on username
        // Add connections in the list
        String username1 = "";
        String username2 = "";
        if(myProfile.getUsername().equals("a1")) {
            username1 = "a2";
            username2 = "a3"; }
        else if(myProfile.getUsername().equals("a2")) {
            username1 = "a1";
            username2 = "a3"; }
        else if(myProfile.getUsername().equals("a3")) {
            username1 = "a1";
            username2 = "a2"; }
        else if(myProfile.getUsername().equals("b1")) {
            username1 = "b2";
            username2 = "b3"; }
        else if(myProfile.getUsername().equals("b2")) {
            username1 = "b1";
            username2 = "b3"; }
        else if(myProfile.getUsername().equals("b3")) {
            username1 = "b1";
            username2 = "b2"; }
        else if(myProfile.getUsername().equals("c1")) {
            username1 = "c2";
            username2 = "c3"; }
        else if(myProfile.getUsername().equals("c2")) {
            username1 = "c1";
            username2 = "c3"; }
        else if(myProfile.getUsername().equals("c3")) {
            username1 = "c1";
            username2 = "c2"; }

        final String u1 = username1;
        final String u2 = username2;
        Handler handler=new Handler();
        Runnable r=new Runnable() {
            public void run() {
                HTTPRequest request1 = new HTTPRequest("GetUser",
                        HOST + PORT + "/profiles/login/" + u1,
                        Request.Method.GET,
                        null
                );
                mHTTPHelper.sendToServer(request1);

                HTTPRequest request2 = new HTTPRequest("GetUser",
                        HOST + PORT + "/profiles/login/" + u2,
                        Request.Method.GET,
                        null
                );
                mHTTPHelper.sendToServer(request2);
            }
        };
        handler.postDelayed(r, 5000);
        /* ******************************************************** */
    }

    public void onGetUser(JSONObject response) {
        try {
            Connection temp = new Connection();
            temp.setUser1Username(myProfile.getUsername());
            temp.setUser2Username(response.getString("username"));
            temp.setmUser2DeviceId(response.getString("deviceId"));
            temp.setmUser2Id(response.getString("_id"));
            temp.setTimesMet(0);

            myConnections.add(temp);

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(myConnections.size() == 2) {
            // Write myConnections in the shared file
            File file;
            FileOutputStream outputStream;
            Cache cache = new Cache(myProfile, myConnections.get(0), myConnections.get(1));

            try {
                file = new File(this.getFilesDir(), "NextToYouCache");

                // Create the file if it doesn't already exist
                if (!file.exists()) {
                    Log.d(TAG, "onGetUser() - file does not exist");
                    file.createNewFile();
                }
                outputStream = this.openFileOutput("NextToYouCache", Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(cache);
                objectOutputStream.close();
                outputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            }

            initiate();
        }
    }

    public void initiate() {

        // Start the service, if it is not already running
        if(!isMyServiceRunning(NextToYouService.class)) {
            Log.d(TAG, "initiate() - Starting the service");

            Intent intent = new Intent(this, NextToYouService.class);
            intent.putExtra("_id", myProfile.getId());
            intent.putExtra("username", myProfile.getUsername());
            intent.putExtra("password", myProfile.getPassword());
            intent.putExtra("deviceId", myProfile.getDeviceId());

            startService(intent);
        }
        // Go to my connections
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mConnectionsListFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.header_layout, mHeaderFragment).commit();
        currentFragment = mConnectionsListFragment;

        // Initiate Bluetooth
//        mBluetoothHelper.init();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_HALF_HOUR, pIntent);
    }


/* ******************************************************************************************
   ****************************************************************************************** */

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        intent = getIntent();

        Log.d(TAG, "Is my service running? " + isMyServiceRunning(NextToYouService.class));

        mHTTPHelper = new HTTPHelper(this);
        mBluetoothHelper = new BluetoothHelper(this);

        // Create myProfile
        myInfo = new ArrayList<Clue>();
        myProfile = new Profile(null, null, null, mBluetoothHelper.init());

        if (savedInstanceState == null) {
            // Create the fragment instances
            mLogInRegisterFragment = new LogInRegisterFragment();
            mHeaderFragment = new HeaderFragment();
            mOtherProfileFragment = new OtherProfileFragment();
            mMyProfileViewFragment = new MyProfileViewFragment();
            mMyProfileEditFragment = new MyProfileEditFragment();
            mConnectionsListFragment = new ConnectionsListFragment();


            if(intent != null) {
                Log.d(TAG, "Intent: " + intent.getStringExtra("test"));
                myProfile.setId(intent.getStringExtra("_id"));
                myProfile.setId(intent.getStringExtra("username"));
                myProfile.setId(intent.getStringExtra("password"));

                initiate();
            }
            else {
                Log.d(TAG, "Intent = null");
                // Add the fragment on initial activity setup
                getSupportFragmentManager().beginTransaction().add(R.id.main_layout, mLogInRegisterFragment).commit();
                currentFragment = mLogInRegisterFragment;
            }
        } else {                                                                //TODO: CHECK IF REALLY NEEDED
            Log.d(TAG, "__________ELSE!!!!");
            // Or set the fragment from restored state info
            mLogInRegisterFragment = (LogInRegisterFragment) getSupportFragmentManager().findFragmentById(R.id.main_layout);
            currentFragment = mLogInRegisterFragment;
        }

    }


<Spinner
        android:id="@+id/question1SP"
        android:layout_height="40dp"
        android:layout_width="120dp"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginRight="5dp"
        android:layout_marginBottom="10dp"
        />

    <EditText
        android:id="@+id/answer1ET"
        android:layout_height="40dp"
        android:layout_width="fill_parent"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:layout_toRightOf="@id/question1SP"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginBottom="10dp"
        />

    <Spinner
        android:id="@+id/question2SP"
        android:layout_height="40dp"
        android:layout_width="wrap_content"
        android:minWidth="120dp"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginRight="5dp"
        android:layout_below="@id/question1SP"
        android:layout_marginBottom="10dp"
        />

    <EditText
        android:id="@+id/answer2ET"
        android:layout_height="40dp"
        android:layout_width="fill_parent"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:layout_toRightOf="@id/question2SP"
        android:layout_below="@id/answer1ET"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginBottom="10dp"
        />

    <Spinner
        android:id="@+id/question3SP"
        android:layout_height="40dp"
        android:layout_width="wrap_content"
        android:minWidth="120dp"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginRight="5dp"
        android:layout_below="@id/question2SP"
        android:layout_marginBottom="10dp"
        />

    <EditText
        android:id="@+id/answer3ET"
        android:layout_height="40dp"
        android:layout_width="fill_parent"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:layout_toRightOf="@id/question3SP"
        android:layout_below="@id/answer2ET"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginBottom="10dp"
        />

    <Spinner
        android:id="@+id/question4SP"
        android:layout_height="40dp"
        android:layout_width="wrap_content"
        android:minWidth="120dp"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginRight="5dp"
        android:layout_below="@id/question3SP"
        android:layout_marginBottom="10dp"
        />

    <EditText
        android:id="@+id/answer4ET"
        android:layout_height="40dp"
        android:layout_width="fill_parent"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:layout_toRightOf="@id/question4SP"
        android:layout_below="@id/answer3ET"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginBottom="10dp"
        />

    <Spinner
        android:id="@+id/question5SP"
        android:layout_height="40dp"
        android:layout_width="wrap_content"
        android:minWidth="120dp"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginRight="5dp"
        android:layout_below="@id/question4SP"
        android:layout_marginBottom="10dp"
        />

    <EditText
        android:id="@+id/answer5ET"
        android:layout_height="40dp"
        android:layout_width="fill_parent"
        android:textSize="14dp"
        android:gravity="bottom|left"
        android:layout_toRightOf="@id/question5SP"
        android:layout_below="@id/answer4ET"
        android:focusable="true"
        android:textColor="@color/blue"
        android:layout_marginBottom="10dp"
        />

final Spinner spinner1 = (Spinner) view.findViewById(R.id.question1SP);
        spinner1.setAdapter(adapter1);
        spinner1.setSelection(adapter1.getPosition(mainActivity.myInfo.get(0).getQuestion()));
        final Spinner spinner2 = (Spinner) view.findViewById(R.id.question2SP);
        spinner2.setAdapter(adapter1);
        spinner2.setSelection(adapter1.getPosition(mainActivity.myInfo.get(1).getQuestion()));
        final Spinner spinner3 = (Spinner) view.findViewById(R.id.question3SP);
        spinner3.setAdapter(adapter1);
        spinner3.setSelection(adapter1.getPosition(mainActivity.myInfo.get(2).getQuestion()));
        final Spinner spinner4 = (Spinner) view.findViewById(R.id.question4SP);
        spinner4.setAdapter(adapter1);
        spinner4.setSelection(adapter1.getPosition(mainActivity.myInfo.get(3).getQuestion()));
        final Spinner spinner5 = (Spinner) view.findViewById(R.id.question5SP);
        spinner5.setAdapter(adapter1);
        spinner5.setSelection(adapter1.getPosition(mainActivity.myInfo.get(4).getQuestion()));

        // Edit Texts
        answer1ET = (EditText) view.findViewById(R.id.answer1ET);
        answer1ET.setText(mainActivity.myInfo.get(0).getAnswer());
        answer2ET = (EditText) view.findViewById(R.id.answer2ET);
        answer2ET.setText(mainActivity.myInfo.get(1).getAnswer());
        answer3ET = (EditText) view.findViewById(R.id.answer3ET);
        answer3ET.setText(mainActivity.myInfo.get(2).getAnswer());
        answer4ET = (EditText) view.findViewById(R.id.answer4ET);
        answer4ET.setText(mainActivity.myInfo.get(3).getAnswer());
        answer5ET = (EditText) view.findViewById(R.id.answer5ET);
        answer5ET.setText(mainActivity.myInfo.get(4).getAnswer());        


// Send request to server for every answer
                String[] Questions = {  spinner1.getSelectedItem().toString(),
                                        spinner2.getSelectedItem().toString(),
                                        spinner3.getSelectedItem().toString(),
                                        spinner4.getSelectedItem().toString(),
                                        spinner5.getSelectedItem().toString()};

                String[] Answers = {    answer1ET.getText().toString(),
                                        answer2ET.getText().toString(),
                                        answer3ET.getText().toString(),
                                        answer4ET.getText().toString(),
                                        answer5ET.getText().toString()};

                for(int i=0; i<Answers.length; i++) {
                    mCallback.saveAnswerToServer(Questions[i], Answers[i], i);
                }


final String url = HOST + PORT + "/clues";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ownerId", myProfile.getId());
        params.put("question", question);
        params.put("answer", answer);
        params.put("orderNumber", Integer.toString(orderNumber));
        JSONObject req = new JSONObject(params);

        HTTPRequest request = new HTTPRequest("PostClue",
                HOST + PORT + "/...",
                Request.Method.POST,
                null
        );
        Log.d(TAG, "Request: " + request.toString());
        mHTTPHelper.sendToServer(request);


// add the request object to the queue to be executed
//ApplicationController.getInstance().addToRequestQueue(req);
RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

final String url = "http://social.cs.tut.fi:10001/profiles/55dfb3ad20fe189f349f18ad";
JsonObjectRequest jsObjRequestGET = new JsonObjectRequest
        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "VolleyError: " + error);
            }
        }) {
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        Log.d(TAG, "HEADERS: " + headers);
        return headers;
    }
};

// Access the RequestQueue through your singleton class.
MySingleton.getInstance(this).addToRequestQueue(jsObjRequestGET);


//        // Send GET request to verify the given credentials
//        final String url = host + port + "/profiles/login/" + username + "/" + password;
//        JsonObjectRequest jsObjRequestGET = new JsonObjectRequest
//                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.d(TAG, "Response: " + response + " " + response.getString("_id"));
//                            // If the _id is -1, username and password were not found in the database
//                            if(response.getString("_id").equals("-1"))
//                                mActivity.mLogInRegisterFragment.wrongCredentials("both", "not_found");
//                            else {
//                                mActivity.myProfile = new Profile();
//                                mActivity.myProfile.setId(response.getString("_id"));
//                                mActivity.myProfile.setUsername(response.getString("username"));
//                                mActivity.myProfile.setPassword(response.getString("password"));
//                                mActivity.myProfile.setDeviceId(response.getString("deviceId"));
//                                mActivity.loggedIn = true;
//
//                                Toast.makeText(mActivity.getApplicationContext(), "Log in successful.", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "Profile: " + mActivity.myProfile.getUsername());
//
//                                // Go to my profile
//                                mActivity.mMyProfileFragment = new MyProfileFragment();
//                                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mActivity.mMyProfileFragment).commit();
//                                mActivity.getSupportFragmentManager().beginTransaction().add(R.id.header_layout, mActivity.mHeaderFragment).commit();
//                                mActivity.currentFragment = mActivity.mMyProfileFragment;
//                            }
//                        } catch (JSONException e) { e.printStackTrace(); }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "VolleyError: " + error.toString());
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Accept", "application/json");
//                return headers;
//            }
//        };
//        MySingleton.getInstance(mActivity).addToRequestQueue(jsObjRequestGET);
    }


//    /***** GET request for a Single JSON object *****/
//    public final JSONObject getSingleObjectFromServer(String uri) {
//        final String requestURL = domain + port + uri;
//
//        JsonObjectRequest jsObjRequestGET = new JsonObjectRequest
//                (Request.Method.GET, requestURL, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        mResponse = response;
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "VolleyError: " + error);
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Accept", "application/json");
//                Log.d(TAG, "HEADERS: " + headers);
//                return headers;
//            }
//        };
//
//        mQueue.add(jsObjRequestGET);
//        return mResponse;
//    }
//
//    /***** GET request for an Array of JSON Objects *****/
//    public final JSONArray getObjectsFromServer(String uri) {
//        final String requestURL = domain + port + uri;
//
//
//        JsonArrayRequest jsArRequest = new JsonArrayRequest
//                (Request.Method.GET, requestURL, null, new Response.Listener<JSONArray>() {
//
//                    @Override
//                    public void onResponse(JSONArray response) {
//
//                        // Copy response to mResponseArray
//                        for(int i=0; i<response.length(); i++) {
//                            try {
//                                mResponseArray.put(response.get(i));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "VolleyError: " + error);
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Accept", "application/json");
//                return headers;
//            }
//        };
//
//
//        mQueue.add(jsArRequest);
//
//        return mResponseArray;
//    }


//    String url = "http://social.cs.tut.fi:10001/profiles/55dfba95b68e4f7d379f9901";
//    RequestQueue queue = Volley.newRequestQueue(this);
//
//        JsonObjectRequest jsObjRequestGET= new JsonObjectRequest
//                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        //mTxtDisplay.setText("Response: " + response.toString());
//                        Log.d(TAG, "Response: " + response.toString());
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "Error - Response: " + error.toString());
//
//                    }
//                }) {
//
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        HashMap<String, String> headers = new HashMap<String, String>();
//                        headers.put("Accept", "application/json");
//                        Log.d(TAG, "HEADERS: " + headers);
//                        return headers;
//                    }
//        };
////
////        queue.add(jsObjRequestGET);
//
//    url = "http://social.cs.tut.fi:10001/profiles";
//
//    //String params = "{\"username\":\"user2\",\"password\":\"user2pass\"}";
//    HashMap<String, String> params = new HashMap<String, String>();
//    params.put("username", "userX");
//    params.put("password", "passX");
//    params.put("deviceId", "deviceX");
//
//    Log.d(TAG, "Hashmap: " + params.toString());
//    Log.d(TAG,"Hashmap: " + params.get("password").toString() );
//    //Log.d(TAG,"Hashmap: " + params.toString() );
//
//    JSONObject req = new JSONObject(params);
////        try {
////            req = new JSONObject(params);
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
//
//    JsonObjectRequest jsObjRequestPOST= new JsonObjectRequest
//            (Request.Method.POST, url, req, new Response.Listener<JSONObject>() {
//
//                @Override
//                public void onResponse(JSONObject response) {
//                    //mTxtDisplay.setText("Response: " + response.toString());
//                    Log.d(TAG, "Response: " + response.toString());
//                }
//            }, new Response.ErrorListener() {
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d(TAG, "Error - Response: " + error.toString());
//
//                }
//            }) {
//
//        @Override
//        public Map<String, String> getHeaders() throws AuthFailureError {
//            HashMap<String, String> headers = new HashMap<String, String>();
//            headers.put("Accept", "application/json");
//            Log.d(TAG, "HEADERS: " + headers);
//            return headers;
//        }
//    };
//
//    queue.add(jsObjRequestPOST);