package milespeele.canvas.parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by milespeele on 7/5/15.
 */
@ParseClassName("Masterpiece")
public class Masterpiece extends ParseObject {

    private final static String IMAGE_KEY = "image";
    private final static String TITLE_KEY = "title";

    public Masterpiece() {}

    public ParseFile getImage() {
        return getParseFile(IMAGE_KEY);
    }

    public void setImage(Object file) {
        put(IMAGE_KEY, file);
    }

    public void setTitle(String name) { put(TITLE_KEY, name); }
}
