package com.adamstyrc.cookiecutter;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by adamstyrc on 04/04/16.
 */
public class CookieCutterParams {

    private int circleRadius = 400;
    private Circle circle;
    private float maxZoom = 4;
    private int minImageSize = 200;

    private int width;
    private int height;

    private HoleParams holeParams;


    Circle getCircle() {
        return circle;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    public int getCircleRadius() {
        return circleRadius;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    public int getMinImageSize() {
        return minImageSize;
    }

    public void setMinImageSize(int minImageSize) {
        this.minImageSize = minImageSize;
    }

    void updateWithView(int width, int height) {
        this.width = width;
        this.height = height;

        circle = new Circle(width / 2, height / 2, circleRadius);

        holeParams = new HoleParams();
    }

    public HoleParams getHoleParams() {
        return holeParams;
    }

    public class HoleParams {
        Path path;
        Paint paint;

        public HoleParams() {
            setPath();

            paint = new Paint();
            paint.setColor(Color.parseColor("#AA000000"));
        }

        private void setPath() {
            path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.addRect(0, 0, width, height, Path.Direction.CW);
            path.addCircle(circle.getCx(), circle.getCy(), circle.getRadius(), Path.Direction.CW);
        }
    }
}
