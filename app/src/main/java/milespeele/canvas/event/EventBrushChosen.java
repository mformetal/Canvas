package milespeele.canvas.event;

/**
 * Created by milespeele on 8/8/15.
 */
public class EventBrushChosen {

    public float thickness;
    public int alpha;

    public EventBrushChosen(float thickness, int alpha) {
        this.alpha = alpha;
        this.thickness = thickness;
    }
}
