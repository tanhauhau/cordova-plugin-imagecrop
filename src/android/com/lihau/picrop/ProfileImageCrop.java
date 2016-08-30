package com.lihau.picrop;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            Intent intent = new Intent(cordova.getActivity(), ProfileImageCropActivity.class);
            intent.putExtra(ProfileImageCropActivity.EXTRA_IMAGE_URI, imageUri);
            cordova.startActivityForResult(this, intent, REQUEST_CROP);
            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CROP){
            if(resultCode == Activity.RESULT_OK) {
                try {
                    String result = data.getStringExtra(ProfileImageCropActivity.EXTRA_RESULT);
                    JSONObject resultObj = new JSONObject();
                    resultObj.put("resultUri", result);
                    this.callbackContext.success(resultObj);
                    return;
                } catch(JSONException e){
                    callbackError("JSON_EXCEPTION");
                }
            } else {
                String errorMessage = data.getStringExtra(ProfileImageCropActivity.EXTRA_RESULT);
                callbackError(errorMessage);
            }
        }
        callbackError("UNKNOWN_ERROR");
    }

    private void callbackError(String message){
        try {
            JSONObject errorObject = new JSONObject();
            errorObject.put("name", "ProfileImageCrop");
            errorObject.put("code", message);
            this.callbackContext.error(errorObject);
        }catch(JSONException e){
            callbackError("JSON_EXCEPTION");
        }
    }
}
