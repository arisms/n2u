package fi.tut.cs.social.proximeety;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ImageCropActivity extends Activity {

    private final int GALLERY_ACTIVITY_CODE=200;
    private final int RESULT_CROP = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("ImageCropActivity", "onCreate()");

        Intent gallery_Intent = new Intent(getApplicationContext(), GalleryUtil.class);
        startActivityForResult(gallery_Intent, GALLERY_ACTIVITY_CODE);

//        btn_choose.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //Start Activity To Select Image From Gallery
//                Intent gallery_Intent = new Intent(getApplicationContext(), GalleryUtil.class);
//                startActivityForResult(gallery_Intent, GALLERY_ACTIVITY_CODE);
//                break;
//            }
//        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ACTIVITY_CODE) {
            if(resultCode == Activity.RESULT_OK){
                String picturePath = data.getStringExtra("picturePath");
                //perform Crop on the Image Selected from Gallery
                performCrop(picturePath);
            }
            else
                finish();
        }

        if (requestCode == RESULT_CROP ) {
            if(resultCode == Activity.RESULT_OK){
                Bundle extras = data.getExtras();

                Bitmap selectedBitmap = null;
                Bitmap retrievedBitmap = null;
                if(extras != null) {
                    selectedBitmap = extras.getParcelable("data");
                }
                else {
                    Log.d("ImageCropActivity", "Is null: " + data.getData());
                    try {
                        retrievedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        selectedBitmap = retrievedBitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ;
                }

                // write the cropped image to a local file and notify the main Activity
                File file = new File(getApplicationContext().getFilesDir(), "N2Uavatar");

                // Create the file if it doesn't already exist
                if (!file.exists()) {
                    Log.d("ImageCropActivity", "N2Uavatar - file does not exist");
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent imageSelectedIntent = new Intent(MainActivity.IMAGE_SELECTED);
                FileOutputStream outputStream = null;
                try {
                    //outputStream = new FileOutputStream("N2Uavatar");
                    outputStream = getApplicationContext().openFileOutput("N2Uavatar", Context.MODE_PRIVATE);
                    selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
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

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(imageSelectedIntent);

                finish();
                // Set The Bitmap Data To ImageView
                //MainActivity mainActivity =
//                image_capture1.setImageBitmap(selectedBitmap);
//                image_capture1.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            else
                finish();
        }
//
    }

    private void performCrop(String picUri) {
        try {
            //Start Crop Activity

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            File f = new File(picUri);
            Uri contentUri = Uri.fromFile(f);

            cropIntent.setDataAndType(contentUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, RESULT_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}