package miles.canvas.data.event;

/**
 * Created by milespeele on 8/8/15.
 */
public class EventColorChosen {

    public int color;
    public boolean fill;

    public EventColorChosen(int color, boolean toFill) {
        this.color = color;
        this.fill = toFill;
    }
}
