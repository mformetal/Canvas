package milespeele.canvas.paint;

import android.graphics.Paint;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 11/10/15.
 */
public class PaintStore extends Paint {

    private PaintStoreListener listener;
    public interface PaintStoreListener {
        void onColorChanged(int newColor);
    }

    public PaintStore(int color, float width) {
        set(PaintStyles.normal(color, width));
    }

    public void setListener(PaintStoreListener listener) {
        this.listener = listener;
    }

    public void changePaint(Paint newPaint) {
        set(newPaint);
    }

    @Override
    public void setColor(int color) {
        super.setColor(color);
        if (listener != null) {
            listener.onColorChanged(color);
        }
    }
}
