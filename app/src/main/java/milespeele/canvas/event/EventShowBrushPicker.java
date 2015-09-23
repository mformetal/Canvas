package milespeele.canvas.event;

/**
 * Created by milespeele on 8/7/15.
 */
public class EventShowBrushPicker {

    public float size;
    public int color;

    public EventShowBrushPicker(float size, int color) {
        this.color = color;
        this.size = size;
    }
}
