package milespeele.canvas.event;

import com.parse.ParseException;

/**
 * Created by Miles Peele on 8/16/2015.
 */
public class EventParseError {

    private static final int NOT_PARSE_ERROR = -1;

    public ParseException e;

    public EventParseError(ParseException e) {
        this.e = e;
    }

    public EventParseError(Throwable throwable) {

    }

    public int getErrorCode() {
        return (e != null) ? e.getCode() : NOT_PARSE_ERROR;
    }
}
