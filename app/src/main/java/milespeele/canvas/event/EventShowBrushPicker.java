package milespeele.canvas.event;

/**
 * Created by milespeele on 8/7/15.
 */
public class EventShowBrushPicker {

    public float size;
    public int alpha;

    public EventShowBrushPicker(float size, int alpha) {
        this.size = size;
        this.alpha = alpha;
    }
}
