package milespeele.canvas.util;

import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.view.ViewCanvasSurface;

/**
 * Created by mbpeele on 9/29/15.
 */
public class EnumStore {

    private DrawingCurve.State state;

    private EnumListener mListener;

    public EnumStore() {}

    public void setListener(EnumListener listener) {
        mListener = listener;
    }

    public void setValue(DrawingCurve.State newValue) {
        state = newValue;
        if (mListener != null) {
            mListener.onValueChanged(state);
        }
    }

    public  DrawingCurve.State getValue() {
        return state;
    }

    public interface EnumListener {
        void onValueChanged(DrawingCurve.State newValue);
    }
}
