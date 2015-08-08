package milespeele.canvas.event;

/**
 * Created by milespeele on 8/8/15.
 */
public class EventColorChosen {

    public int color;
    public String which;

    public EventColorChosen(int color, String which) {
        this.color = color;
        this.which = which;
    }
}
