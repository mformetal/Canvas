package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/23/15.
 */
public class ViewOptionsMenu extends LinearLayout implements View.OnClickListener {

    @Bind(R.id.view_options_menu_cancel) ViewTypefaceButton cancel;
    @Bind(R.id.view_options_menu_1) ViewTypefaceButton option1;
    @Bind(R.id.view_options_menu_2) ViewTypefaceButton option2;
    @Bind(R.id.view_options_menu_accept) ViewTypefaceButton accept;

    private ArrayList<ViewOptionsMenuListener> listeners;
    public interface ViewOptionsMenuListener {
        void onOptionsMenuCancel();
        void onOptionsMenuButtonClicked(View view);
        void onOptionsMenuAccept();
    }

    public ViewOptionsMenu(Context context) {
        super(context);
        init();
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        listeners = new ArrayList<>();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    @OnClick({R.id.view_options_menu_cancel, R.id.view_options_menu_accept,
            R.id.view_options_menu_1})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_options_menu_cancel:
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuCancel();
                }
                break;
            case R.id.view_options_menu_accept:
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuAccept();
                }
                break;
            case R.id.view_options_menu_1:
            case R.id.view_options_menu_2:
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuButtonClicked(v);
                }
                break;
        }
    }

    public void addListener(ViewOptionsMenuListener optionsMenuListener) {
        listeners.add(optionsMenuListener);
    }

    public void setState(DrawingCurve.State state) {
        switch (state) {
            case TEXT:
                if (option1.getParent() == null) {
                    addView(option1, getChildCount() - 1);
                }

                setWeightSum(4);

                option1.setText(R.string.view_options_menu_edit_text);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_text_format_24dp));

                option2.setText(R.string.view_options_menu_edit_color);
                option2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_format_paint_24dp));
                break;
            case IMPORT:

                removeView(option1);

                setWeightSum(3);

                option2.setText(R.string.view_options_menu_edit_import);
                option2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_photo_24dp));

                requestLayout();
                break;
        }

        if (getVisibility() == View.GONE) {
            ViewUtils.visible(this);
        } else {
            ObjectAnimator.ofFloat(this, View.TRANSLATION_Y,
                    getTranslationY() - getHeight())
                    .setDuration(350)
                    .start();
        }
    }
}