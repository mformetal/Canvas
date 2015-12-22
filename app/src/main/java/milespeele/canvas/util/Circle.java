package milespeele.canvas.util;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by mbpeele on 12/17/15.
 */
public class Circle {

    private float centerX;
    private float centerY;
    private float radius;

    private RectF boundingRect;

    public Circle(float cx, float cy, float rad) {
        centerX = cx;
        centerY = cy;
        radius = rad;

        boundingRect = new RectF();
        boundingRect.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
    }

    public boolean contains(float x, float y) {
        return Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2) <= radius * radius;
    }

    public static boolean contains(float x, float y, float radius) {
        return x * x + y * y <= radius * radius;
    }

    public double angleInDegrees(float x, float y) {
        return Math.toDegrees(Math.atan2(centerY - y, centerX - x));
    }

    public RectF getBoundingRect() {
        return boundingRect;
    }

    public float getDiameter() { return radius * 2; }

    public float getRadius() { return radius; }

    public float getCenterX() { return centerX; }

    public float getCenterY() { return centerY; }

    public void setCenterX(float x) {
        centerX = x;
    }

    public void setCenterY(float y) {
        centerY = y;
    }
}
