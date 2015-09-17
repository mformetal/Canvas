package milespeele.canvas.event;

import android.graphics.Paint;

/**
 * Created by milespeele on 8/8/15.
 */
public class EventBrushChosen {

    public float thickness;
    public Paint paint;

    public EventBrushChosen(float thickness, Paint paint) {
        this.thickness = thickness;
        this.paint = paint;
    }
}
