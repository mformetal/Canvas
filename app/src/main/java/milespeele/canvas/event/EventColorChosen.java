package milespeele.canvas.event;

/**
 * Created by milespeele on 8/8/15.
 */
public class EventColorChosen {

    public int color;
    public int opacity;
    public String which;

    public EventColorChosen(int color, int opacity, String which) {
        this.color = color;
        this.opacity = opacity;
        this.which = which;
    }
}
