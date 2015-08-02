package milespeele.canvas.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.flipboard.bottomsheet.BottomSheetLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentDrawer;

/**
 * Created by Miles Peele on 8/1/2015.
 */
public class ViewBottomSheet extends BottomSheetLayout
        implements View.OnClickListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_show_menu) ViewFab toggle;
    @InjectView(R.id.fragment_drawer_coordinator) ViewCanvasLayout coordinator;
    private ViewBottomSheetMenu menu;

    private static AnimatorSet close;
    private static AnimatorSet open;

    public ViewBottomSheet(Context context) {
        super(context);
        init();
    }

    public ViewBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewBottomSheet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
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
        open = new AnimatorSet();
        close = new AnimatorSet();
        close.playTogether(ObjectAnimator.ofFloat(toggle, "rotation", 135f, 270f),
                ObjectAnimator.ofFloat(toggle, "translationY", 12));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        open.playTogether(ObjectAnimator.ofFloat(toggle, "translationY", -(h / 3)),
                ObjectAnimator.ofFloat(toggle, "rotation", 0f, 135f));
    }

    @Override
    @OnClick(R.id.fragment_drawer_show_menu)
    public void onClick(View v) {
        if (isSheetShowing()) {
            dismissSheet();
        } else {
            showWithSheetView(menu);
        }
    }

    public void inflateMenu(FragmentDrawer drawer) {
        if (menu == null) {
            menu = (ViewBottomSheetMenu) LayoutInflater.from(getContext())
                    .inflate(R.layout.bottom_sheet_menu, null, false);
            menu.setListener(drawer);
        }
    }

    @Override
    public void showWithSheetView(View sheetView) {
        open.start();
        super.showWithSheetView(sheetView);
    }


    @Override
    public void dismissSheet() {
        close.start();
        super.dismissSheet();
    }

}
