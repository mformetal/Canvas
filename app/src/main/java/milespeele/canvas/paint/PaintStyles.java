package milespeele.canvas.paint;

import android.graphics.BlurMaskFilter;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import java.util.Random;

import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class PaintStyles {

    private static BlurMaskFilter BLUR_MASK_FILTER = new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL);
    private static EmbossMaskFilter EMBOSS_MASK_FILTER = new EmbossMaskFilter(new float[]{1, 1, 1}, 1f, 0, 3);
    private static PathEffect DASHED_PASH_EFFECT = new DashPathEffect(new float[] {10,20}, 0);

    public static Paint randomStyle(int currentColor, float width) {
        Random random = new Random();
        int randomNum = random.nextInt(10);
        if (randomNum <= 1) {
            return normalPaint(currentColor, width);
//        } else if (randomNum <= 3) {
//        } else if (randomNum <= 5) {
//            return  neonPaint(currentColor, width);
        } else if (randomNum <= 7) {
            return dashedPaint(currentColor, width);
        } else {
            return normalPaint(currentColor, width);
        }
    }

    public static Paint normalPaint(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

//    public static Paint fadePaint(int currentColor, float width) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setDither(true);
//        paint.setColor(currentColor);
//        paint.setStrokeWidth(width);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        paint.setMaskFilter(BLUR_MASK_FILTER);
//        return paint;
//    }

//    public static Paint neonPaint(int currentColor, float width) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setDither(true);
//        paint.setColor(currentColor);
//        paint.setStrokeWidth(width);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        paint.setMaskFilter(EMBOSS_MASK_FILTER);
//        return paint;
//    }

    public static Paint dashedPaint(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(DASHED_PASH_EFFECT);
        return paint;
    }

    public static Paint eraserPaint(int color, float width) {
        Paint eraser = new Paint();
        eraser.setColor(color);
        eraser.setAntiAlias(true);
        eraser.setStrokeWidth(width * 3f);
        eraser.setStrokeJoin(Paint.Join.ROUND);
        eraser.setStrokeCap(Paint.Cap.ROUND);
        eraser.setStyle(Paint.Style.STROKE);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        return eraser;
    }
}
