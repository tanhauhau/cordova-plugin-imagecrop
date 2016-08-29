package com.lihau.picrop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.adamstyrc.cookiecutter.CookieCutterImageView;
import com.adamstyrc.cookiecutter.CookieCutterShape;
import com.adamstyrc.cookiecutter.ImageUtils;


/**
 * Created by lhtan on 31/1/16.
 */
public class ProfileImageCropActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_RESULT = "result";

    private ProfileImageCrop.FakeR fakeR;
    private CookieCutterImageView cookieCutter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fakeR = new ProfileImageCrop.FakeR(getApplicationContext());

        setContentView(fakeR.getId("layout", "picrop_main"));
        cookieCutter = (CookieCutterImageView) findViewById(fakeR.getId("id", "ivCookieCutter"));

        Point screenSize = ImageUtils.getScreenSize(this);
        byte[] byteArray = getIntent().getByteArrayExtra(EXTRA_IMAGE_URI);
        Bitmap bmp = getScaledBitmap(byteArray, screenSize.x, screenSize.y);

        cookieCutter.setImageBitmap(bmp);
        cookieCutter.getParams().setShape(CookieCutterShape.HOLE);
        cookieCutter.invalidate();
    }
    private Bitmap getScaledBitmap(byte[] byteArray, int width, int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height / 2> reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
