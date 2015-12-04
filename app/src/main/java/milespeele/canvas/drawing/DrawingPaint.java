package milespeele.canvas.drawing;

import android.graphics.Paint;
import android.os.Handler;

import java.util.Random;

import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/4/15.
 */
public class DrawingPaint extends Paint {

    private static final int[] rainbow = ViewUtils.rainbow();
    private static final Random random = new Random();
    private static final Handler mHandler = new Handler();

    public DrawingPaint() {
        super();

        Runnable mRunnable = new Runnable() {
            public void run() {
                setColor(rainbow[random.nextInt(rainbow.length)]);
                mHandler.postDelayed(this, 100);
            }
        };
        mRunnable.run();
    }

    public DrawingPaint(Paint paint) {
        super(paint);
    }
}
