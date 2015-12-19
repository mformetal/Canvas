package milespeele.canvas.util;

import android.graphics.Canvas;
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

    public Circle(float cx, float cy, float rad) {
        centerX = cx;
        centerY = cy;
        radius = rad;
    }

    public boolean contains(float x, float y) {
        return Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2) <= radius * radius;
    }

    public static boolean contains(float x, float y, float radius) {
        return x * x + y * y <= radius * radius;
    }

    public float angle(float x, float y) {
        return (float) Math.toDegrees(Math.atan2(centerY - y, centerX - x));
    }

    public float getCenterX() { return centerX; }

    public float getCenterY() { return centerY; }

    public float setCenterX(float newX) { return centerX = newX; }

    public float setCenterY(float newY) { return centerY = newY; }
}
