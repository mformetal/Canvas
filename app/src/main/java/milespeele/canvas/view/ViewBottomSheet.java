package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentDrawer;

/**
 * Created by Miles Peele on 8/1/2015.
 */
public class ViewBottomSheet extends BottomSheetLayout
        implements View.OnClickListener, OnSheetDismissedListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_show_menu) ViewFab toggle;
    private ViewBottomSheetMenu menu;

    private ObjectAnimator rotateOpen;
    private ObjectAnimator rotateClose;
    private ObjectAnimator moveDown;

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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        rotateOpen = ObjectAnimator.ofFloat(toggle, "rotation", 0f, 135f);
        rotateClose = ObjectAnimator.ofFloat(toggle, "rotation", 135f, 270f);
        moveDown = ObjectAnimator.ofFloat(toggle, "translationY", 12);
    }

    @Override
    @OnClick(R.id.fragment_drawer_show_menu)
    public void onClick(View v) {
        if (isSheetShowing()) {
            dismissSheet();
            rotateClose.start();
            moveDown.start();
        } else {
            ObjectAnimator.ofFloat(toggle, "translationY", -500).start();
            showWithSheetView(menu, null, this);
            rotateOpen.start();
        }
    }

    public void inflateMenu(FragmentDrawer drawer) {
        if (menu == null) {
            menu = (ViewBottomSheetMenu) LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_menu,
                    null, true);
            menu.setListener(drawer);
        }
    }

    @Override
    public void onDismissed(BottomSheetLayout bottomSheetLayout) {
        if (!moveDown.isRunning()) {
            moveDown.start();
            rotateClose.start();
        }
    }
}
