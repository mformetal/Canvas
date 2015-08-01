package milespeele.canvas.view;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.util.Logg;

/**
 * Created by milespeele on 8/1/15.
 */
public class ViewFabMenu extends GridLayout implements View.OnClickListener {

    private FabMenuListener mListener;
    public interface FabMenuListener {
        void onColorizeClicked();
        void onEraseClicked();
        void onPaintColorClicked(int viewId);
        void onBrushClicked();
        void onUndoClicked();
        void onRedoClicked();
    }

    public ViewFabMenu(Context context) {
        super(context);
        init();
    }

    public ViewFabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewFabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override
    @OnClick({R.id.fab_menu_stroke_color, R.id.fab_menu_brush,
            R.id.fab_menu_undo, R.id.fab_menu_redo, R.id.fab_menu_erase})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_menu_colorize:
                mListener.onColorizeClicked();
            case R.id.fab_menu_erase:
                mListener.onEraseClicked();
            case R.id.fab_menu_stroke_color:
                mListener.onPaintColorClicked(v.getId());
            case R.id.fab_menu_brush:
                mListener.onBrushClicked();
            case R.id.fab_menu_undo:
                mListener.onUndoClicked();
                break;
            case R.id.fab_menu_redo:
                mListener.onRedoClicked();
                break;
        }
    }

    public void setListener(FragmentDrawer drawer) {
        mListener = drawer;
        if (mListener == null) {
            Logg.log("WTF");
        } else {
            Logg.log("NOT WTF");
        }
    }
}
