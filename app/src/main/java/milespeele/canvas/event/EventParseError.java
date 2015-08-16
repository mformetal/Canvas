package milespeele.canvas.event;

import com.parse.ParseException;

/**
 * Created by Miles Peele on 8/16/2015.
 */
public class EventParseError {

    public ParseException e;

    public EventParseError(ParseException e) {
        this.e = e;
    }

    public int getErrorCode() { return e.getCode(); }
}
