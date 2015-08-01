package milespeele.canvas.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.flipboard.bottomsheet.BottomSheetLayout;

import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 8/1/2015.
 */
public class ViewBottomSheet extends BottomSheetLayout {

    public ViewBottomSheet(Context context) {
        super(context);
    }

    public ViewBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewBottomSheet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent ev) {
        if (isSheetShowing()) {
            float eventY = ev.getY();
            float sheetHeight = getSheetHeight();
            if (sheetHeight != 0) {
                if (eventY < sheetHeight) {
                    dismissSheet();
                    return true;
                }
            }
        }
        return false;
    }

    public float getSheetHeight() {
        return getSheetView() != null ? getSheetView().getTranslationY() : 0;
    }
}
