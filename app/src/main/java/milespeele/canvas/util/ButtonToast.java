package milespeele.canvas.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Miles Peele on 9/14/2015.
 */
public class ButtonToast extends Toast {
    
    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public ButtonToast(Context context) {
        super(context);
    }
}
