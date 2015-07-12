package milespeele.canvas.parse;

import com.parse.ParseException;

import milespeele.canvas.util.Logger;

/**
 * Created by milespeele on 7/5/15.
 */
public class ParseErrorHandler {

    public static void handleParseError(ParseException e) {
        switch (e.getCode()) {
            default:
                Logger.log("UNHANDLED PARSE EXCEPTION: " + e.getCode());
        }
    }
}