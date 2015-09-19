package milespeele.canvas.pojo;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class PojoPaintExample {

    private String which;

    public PojoPaintExample(String which) {
        this.which = which;
    }

    public String getPaintName() {
        return which;
    }

}
