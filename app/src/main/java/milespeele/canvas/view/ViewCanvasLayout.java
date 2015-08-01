package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 7/10/2015.
 */
public class ViewCanvasLayout extends CoordinatorLayout
        implements View.OnClickListener, OnSheetDismissedListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_bottom_sheet) ViewBottomSheet bottomSheetLayout;
    @InjectView(R.id.fragment_drawer_show_menu) ViewFab toggle;
    private ViewSheetView menu;

    private ObjectAnimator rotateOpen;
    private ObjectAnimator rotateClose;
    private ObjectAnimator moveDown;

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
        if (bottomSheetLayout.isSheetShowing()) {
            bottomSheetLayout.dismissSheet();
            rotateClose.start();
            moveDown.start();
        } else {
            ObjectAnimator.ofFloat(toggle, "translationY", -500).start();
            bottomSheetLayout.showWithSheetView(menu, null, this);
            rotateOpen.start();
        }
    }

    public void inflateMenu(FragmentDrawer drawer) {
        if (menu == null) {
            menu = (ViewSheetView) LayoutInflater.from(getContext()).inflate(R.layout.sheet_view,
                    null, true);
            menu.setListener(drawer);
        }
    }

    public void dismissSheet() {
        bottomSheetLayout.dismissSheet();
    }

    @Override
    public void onDismissed(BottomSheetLayout bottomSheetLayout) {
        if (!moveDown.isRunning()) {
            moveDown.start();
            rotateClose.start();
        }
    }
}