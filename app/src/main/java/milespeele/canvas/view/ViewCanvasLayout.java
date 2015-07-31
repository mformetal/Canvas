package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.flipboard.bottomsheet.BottomSheetLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/10/2015.
 */
public class ViewCanvasLayout extends CoordinatorLayout
        implements View.OnClickListener, BottomSheetLayout.OnSheetStateChangeListener {

    @InjectView(R.id.fragment_drawer_bottom_sheet) BottomSheetLayout bottomSheetLayout;
    @InjectView(R.id.fragment_drawer_show_menu) ViewFab toggle;
    private boolean isMoving = false;
    private boolean isSheetVisible = false;

    private ObjectAnimator rotateOpen;
    private ObjectAnimator rotateClose;
    private static Handler handler = new Handler();
    private static final int MOVING_DELAY = 750;

    public ViewCanvasLayout(Context context) {
        super(context);
        init();
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                isMoving = true;
                ifStillMoving();
                break;
            case MotionEvent.ACTION_UP:
                isMoving = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                isMoving = false;
                break;
        }
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        rotateOpen = ObjectAnimator.ofFloat(toggle, "rotation", 0f, 135f);
        rotateClose = ObjectAnimator.ofFloat(toggle, "rotation", 135f, 270f);
        bottomSheetLayout.setOnSheetStateChangeListener(this);
    }

    private void ifStillMoving() {
        handler.postDelayed(() -> {
            if (isMoving && isSheetVisible) {
                rotateClose.start();
                bottomSheetLayout.dismissSheet();
            }
        }, MOVING_DELAY);
    }

    @Override
    @OnClick(R.id.fragment_drawer_show_menu)
    public void onClick(View v) {
        if (isSheetVisible) {
            rotateClose.start();
        } else {
            rotateOpen.start();
        }
    }

    @Override
    public void onSheetStateChanged(BottomSheetLayout.State state) {
        if (state == BottomSheetLayout.State.HIDDEN ||
                state == BottomSheetLayout.State.EXPANDED || state == BottomSheetLayout.State.PEEKED) {
            isSheetVisible = true;
        } else {
            isSheetVisible = false;
        }
    }
}
