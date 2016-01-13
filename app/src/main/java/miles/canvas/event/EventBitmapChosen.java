package miles.canvas.event;

import android.net.Uri;

/**
 * Created by mbpeele on 12/25/15.
 */
public class EventBitmapChosen {

    public Uri data;
    public String path;

    public EventBitmapChosen(Uri data) {
        this.data = data;
    }
}
