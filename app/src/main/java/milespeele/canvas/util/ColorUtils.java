package milespeele.canvas.util;

import android.graphics.Color;

/**
 * Created by mbpeele on 10/26/15.
 */
public class ColorUtils {

    public static int darken(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(Color.alpha(color),
                (int)Math.max(red - (red * fraction), 0),
                (int)Math.max(green - (green * fraction), 0),
                (int)Math.max(blue - (blue * fraction), 0));
    }

}
