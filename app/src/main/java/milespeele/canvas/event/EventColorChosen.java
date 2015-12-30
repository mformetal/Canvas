package milespeele.canvas.event;

/**
 * Created by milespeele on 8/8/15.
 */
public class EventColorChosen {

    public int color;
    public boolean bool;

    public EventColorChosen(int color, boolean toFill) {
        this.color = color;
        this.bool = toFill;
    }
}
