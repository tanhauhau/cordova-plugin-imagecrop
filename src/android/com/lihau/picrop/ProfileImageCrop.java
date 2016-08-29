package com.lihau.picrop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by lhtan on 31/1/16.
 */
public class ProfileImageCrop extends CordovaPlugin {

    private static final int REQUEST_CROP = 1;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("crop")) {
            //Get options
            JSONObject options = args.optJSONObject(0);
            String imageUri = options.optString("imageUri");
            if(imageUri.length() == 0){
                this.callbackContext.error("No imageUri");
                return true;
            }
            try {
                cordova.getThreadPool().execute(new CropRunnable(imageUri, this, cordova));
                return true;
            }catch(Exception e){
                e.printStackTrace();
                this.callbackContext.error("Error Loading Image");
            }
            return true;
        }
        return false;
    }

    class CropRunnable implements Runnable {
        private String imageUri;
        private CordovaPlugin plugin;
        private CordovaInterface cordova;

        public CropRunnable(String imageUri, CordovaPlugin plugin, CordovaInterface cordova) {
            this.imageUri = imageUri;
            this.plugin = plugin;
            this.cordova = cordova;
        }

        @Override
        public void run() {
            try {
                byte[] byteArray = toByteArray(imageUri);

                Intent intent = new Intent(cordova.getActivity(), ProfileImageCropActivity.class);
                intent.putExtra(ProfileImageCropActivity.EXTRA_IMAGE_URI, byteArray);
                if(cordova != null){
                    cordova.startActivityForResult(plugin, intent, REQUEST_CROP);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error("Error Loading Image");
            }
        }
    }

     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(requestCode == REQUEST_CROP){
             if(resultCode == Activity.RESULT_OK) {
                 try {
                     String resultUri = data.getStringExtra(ProfileImageCropActivity.EXTRA_RESULT);
                     JSONObject resultObj = new JSONObject();
                     resultObj.put("resultUri", resultUri);
                     this.callbackContext.success(resultObj);
                     return;
                 } catch(Exception e){
                 }
             }
         }
         this.callbackContext.error("Unknown error");
     }

    private byte[] toByteArray(String imageUri) throws IOException{
        InputStream inputStream = FileHelper.getInputStreamFromUriString(imageUri, cordova);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while(-1 != (bytesRead = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
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
        private static final String LOG_TAG = "FileUtils";
        private static final String _DATA = "_data";

        /**
         * Returns the real path of the given URI string.
         * If the given URI string represents a content:// URI, the real path is retrieved from the media store.
         *
         * @param uri     the URI string of the audio/image/video
         * @param cordova the current application context
         * @return the full path to the file
         */
        @SuppressWarnings("deprecation")
        public static String getRealPath(Uri uri, CordovaInterface cordova) {
            String realPath = null;

            if (Build.VERSION.SDK_INT < 11)
                realPath = FileHelper.getRealPathFromURI_BelowAPI11(cordova.getActivity(), uri);

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = FileHelper.getRealPathFromURI_API11to18(cordova.getActivity(), uri);

                // SDK > 19 (Android 4.4)
            else
                realPath = FileHelper.getRealPathFromURI_API19(cordova.getActivity(), uri);

            return realPath;
        }

        /**
         * Returns the real path of the given URI.
         * If the given URI is a content:// URI, the real path is retrieved from the media store.
         *
         * @param uriString the URI of the audio/image/video
         * @param cordova   the current application context
         * @return the full path to the file
         */
        public static String getRealPath(String uriString, CordovaInterface cordova) {
            return FileHelper.getRealPath(Uri.parse(uriString), cordova);
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

        /**
         * Returns an input stream based on given URI string.
         *
         * @param uriString the URI string from which to obtain the input stream
         * @param cordova   the current application context
         * @return an input stream into the data at the given URI or null if given an invalid URI string
         * @throws IOException
         */
        public static InputStream getInputStreamFromUriString(String uriString, CordovaInterface cordova)
                throws IOException {
            InputStream returnValue = null;
            if (uriString.startsWith("content")) {
                Uri uri = Uri.parse(uriString);
                returnValue = cordova.getActivity().getContentResolver().openInputStream(uri);
            } else if (uriString.startsWith("file://")) {
                int question = uriString.indexOf("?");
                if (question > -1) {
                    uriString = uriString.substring(0, question);
                }
                if (uriString.startsWith("file:///android_asset/")) {
                    Uri uri = Uri.parse(uriString);
                    String relativePath = uri.getPath().substring(15);
                    returnValue = cordova.getActivity().getAssets().open(relativePath);
                } else {
                    // might still be content so try that first
                    try {
                        returnValue = cordova.getActivity().getContentResolver().openInputStream(Uri.parse(uriString));
                    } catch (Exception e) {
                        returnValue = null;
                    }
                    if (returnValue == null) {
                        returnValue = new FileInputStream(getRealPath(uriString, cordova));
                    }
                }
            } else {
                returnValue = new FileInputStream(uriString);
            }
            return returnValue;
        }

        /**
         * Removes the "file://" prefix from the given URI string, if applicable.
         * If the given URI string doesn't have a "file://" prefix, it is returned unchanged.
         *
         * @param uriString the URI string to operate on
         * @return a path without the "file://" prefix
         */
        public static String stripFileProtocol(String uriString) {
            if (uriString.startsWith("file://")) {
                uriString = uriString.substring(7);
            }
            return uriString;
        }

        public static String getMimeTypeForExtension(String path) {
            String extension = path;
            int lastDot = extension.lastIndexOf('.');
            if (lastDot != -1) {
                extension = extension.substring(lastDot + 1);
            }
            // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
            extension = extension.toLowerCase(Locale.getDefault());
            if (extension.equals("3ga")) {
                return "audio/3gpp";
            }
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        /**
         * Returns the mime type of the data specified by the given URI string.
         *
         * @param uriString the URI string of the data
         * @return the mime type of the specified data
         */
        public static String getMimeType(String uriString, CordovaInterface cordova) {
            String mimeType = null;

            Uri uri = Uri.parse(uriString);
            if (uriString.startsWith("content://")) {
                mimeType = cordova.getActivity().getContentResolver().getType(uri);
            } else {
                mimeType = getMimeTypeForExtension(uri.getPath());
            }

            return mimeType;
        }
    }
    /*
    The MIT License
    Copyright (c) 2010 Matt Kane
    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
    Code taken from: https://github.com/wildabeast/BarcodeScanner
    */
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
