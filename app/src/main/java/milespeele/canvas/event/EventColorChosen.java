package milespeele.canvas.event;

/**
 * Created by milespeele on 8/8/15.
 */
public class EventColorChosen {

    public int color;
    public int opacity;
    public boolean which;

    public EventColorChosen(int color, int opacity, boolean which) {
        this.color = color;
        this.opacity = opacity;
        this.which = which;
    }
}
