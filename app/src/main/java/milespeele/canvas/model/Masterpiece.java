package milespeele.canvas.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by milespeele on 7/5/15.
 */
@ParseClassName("Masterpiece")
public class Masterpiece extends ParseObject {

    private final static String IMAGE_KEY = "image";

    public Masterpiece() {}

    public ParseFile getImage() {
        return getParseFile(IMAGE_KEY);
    }

    public void setImage(ParseFile file) {
        put(IMAGE_KEY, file);
    }
}
