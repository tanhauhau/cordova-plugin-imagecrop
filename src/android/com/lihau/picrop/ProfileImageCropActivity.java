package com.lihau.picrop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.adamstyrc.cookiecutter.CookieCutterImageView;
import com.adamstyrc.cookiecutter.ImageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by lhtan on 31/1/16.
 */
public class ProfileImageCropActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_RESULT = "result";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FakeR fakeR = new FakeR(getApplicationContext());

        setContentView(fakeR.getId("layout", "picrop_main"));
        final CookieCutterImageView cookieCutter = (CookieCutterImageView) findViewById(fakeR.getId("id", "ivCookieCutter"));
        Button chooseButton = (Button) findViewById(fakeR.getId("id", "chooseBtn"));
        Button cancelButton = (Button) findViewById(fakeR.getId("id", "cancelBtn"));

        try {
            Point screenSize = ImageUtils.getScreenSize(this);
            String imageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);
            Bitmap bmp = getScaledBitmap(imageUri, screenSize.x, screenSize.y);

            cookieCutter.setImageBitmap(bmp);
            cookieCutter.invalidate();
        } catch (IOException e) {
            returnErrorMessage("NO_SUCH_FILE");
        }

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap cropped = cookieCutter.getCroppedBitmap();
                    String savedUri = saveBitmap(cropped);
                    Intent result = new Intent();
                    result.putExtra(EXTRA_RESULT, savedUri);
                    setResult(RESULT_OK, result);
                    finish();
                } catch (IOException e) {
                    returnErrorMessage("UNABLE_TO_SAVE");
                    Log.e(ProfileImageCrop.LOG_TAG, "UNABLE_TO_SAVE", e);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnErrorMessage("USER_CANCELLED");
            }
        });
    }

    private void returnErrorMessage(String msg) {
        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT, msg);
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }

    private String saveBitmap(Bitmap cropped) throws IOException {
        File croppedFile = getTempFile("profile_pic");
        FileOutputStream stream = new FileOutputStream(croppedFile);
        cropped.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return croppedFile.getAbsolutePath();
    }

    private Bitmap getScaledBitmap(String imageUri, int width, int height) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(imageUri, this), null, options);
        // Calculate inSampleSize
        options.inSampleSize = ImageUtils.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(imageUri, this), null, options);
    }

    private File getTempFile(String filename) throws IOException {
        File folder = getApplicationContext().getCacheDir();
        int i = 0;
        File tempFile = new File(folder, filename + ".png");
        while(tempFile.exists()){
            tempFile = new File(folder, filename + (i++) + ".png");
        }
        return tempFile;
    }
    /*
      FileHelper.java from apache/cordova-plugin-camera

         Licensed to the Apache Software Foundation (ASF) under one
         or more contributor license agreements.  See the NOTICE file
         distributed with this work for additional information
         regarding copyright ownership.  The ASF licenses this file
         to you under the Apache License, Version 2.0 (the
         "License"); you may not use this file except in compliance
         with the License.  You may obtain a copy of the License at
           http://www.apache.org/licenses/LICENSE-2.0
         Unless required by applicable law or agreed to in writing,
         software distributed under the License is distributed on an
         "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
         KIND, either express or implied.  See the License for the
         specific language governing permissions and limitations
         under the License.
   */
    static class FileHelper {
        @SuppressWarnings("deprecation")
        public static String getRealPath(Uri uri, Context context) {
            String realPath = null;

            if (Build.VERSION.SDK_INT < 11)
                realPath = FileHelper.getRealPathFromURI_BelowAPI11(context, uri);

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = FileHelper.getRealPathFromURI_API11to18(context, uri);

                // SDK > 19 (Android 4.4)
            else
                realPath = FileHelper.getRealPathFromURI_API19(context, uri);

            return realPath;
        }
        public static String getRealPath(String uriString, Context context) {
            return FileHelper.getRealPath(Uri.parse(uriString), context);
        }

        @SuppressLint("NewApi")
        public static String getRealPathFromURI_API19(Context context, Uri uri) {
            String filePath = "";
            try {
                String wholeID = DocumentsContract.getDocumentId(uri);

                // Split at colon, use second item in the array
                String id = wholeID.indexOf(":") > -1 ? wholeID.split(":")[1] : wholeID.indexOf(";") > -1 ? wholeID
                        .split(";")[1] : wholeID;

                String[] column = {MediaStore.Images.Media.DATA};

                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";

                Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
                        sel, new String[]{id}, null);

                int columnIndex = cursor.getColumnIndex(column[0]);

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            } catch (Exception e) {
                filePath = "";
            }
            return filePath;
        }

        @SuppressLint("NewApi")
        public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
            String[] proj = {MediaStore.Images.Media.DATA};
            String result = null;

            try {
                CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();

                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    result = cursor.getString(column_index);
                }
            } catch (Exception e) {
                result = null;
            }
            return result;
        }

        public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
            String[] proj = {MediaStore.Images.Media.DATA};
            String result = null;

            try {
                Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);

            } catch (Exception e) {
                result = null;
            }
            return result;
        }
        public static InputStream getInputStreamFromUriString(String uriString, Context context) throws IOException {
            InputStream returnValue = null;
            if (uriString.startsWith("content")) {
                Uri uri = Uri.parse(uriString);
                returnValue = context.getContentResolver().openInputStream(uri);
            } else if (uriString.startsWith("file://")) {
                int question = uriString.indexOf("?");
                if (question > -1) {
                    uriString = uriString.substring(0, question);
                }
                if (uriString.startsWith("file:///android_asset/")) {
                    Uri uri = Uri.parse(uriString);
                    String relativePath = uri.getPath().substring(15);
                    returnValue = context.getAssets().open(relativePath);
                } else {
                    // might still be content so try that first
                    try {
                        returnValue = context.getContentResolver().openInputStream(Uri.parse(uriString));
                    } catch (Exception e) {
                        returnValue = null;
                    }
                    if (returnValue == null) {
                        returnValue = new FileInputStream(getRealPath(uriString, context));
                    }
                }
            } else {
                returnValue = new FileInputStream(uriString);
            }
            return returnValue;
        }
    }
    public static class FakeR {
        private Context context;
        private String packageName;

        public FakeR(Activity activity) {
            context = activity.getApplicationContext();
            packageName = context.getPackageName();
        }

        public FakeR(Context context) {
            this.context = context;
            packageName = context.getPackageName();
        }

        public int getId(String group, String key) {
            return context.getResources().getIdentifier(key, group, packageName);
        }
    }
}
