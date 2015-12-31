package milespeele.canvas.event;

import android.content.Intent;
import android.net.Uri;

import java.io.InputStream;

/**
 * Created by mbpeele on 12/25/15.
 */
public class EventBitmapChosen {

    public Uri data;
    public String path;

    public EventBitmapChosen(Uri data) {
        this.data = data;
    }

    public EventBitmapChosen(String path) {
        this.path = path;
    }
}
