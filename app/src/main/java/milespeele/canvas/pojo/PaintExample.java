package milespeele.canvas.pojo;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class PaintExample {

    private String which;

    public PaintExample(String which) {
        this.which = which;
    }

    public String getPaintName() {
        return which;
    }

}
