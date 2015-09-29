package milespeele.canvas.util;

import milespeele.canvas.view.ViewCanvas;

/**
 * Created by mbpeele on 9/29/15.
 */
public class EnumStore {

    private ViewCanvas.State state;

    private EnumListener mListener;

    public EnumStore() {}

    public void setListener(EnumListener listener) {
        mListener = listener;
    }

    public void setValue(ViewCanvas.State newValue) {
        state = newValue;
        if (mListener != null) {
            mListener.onValueChanged(state);
        }
    }

    public ViewCanvas.State getValue() {
        return state;
    }

    public interface EnumListener {
        void onValueChanged(ViewCanvas.State newValue);
    }
}
