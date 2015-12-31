package milespeele.canvas.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/23/15.
 */
public class ViewOptionsMenu extends LinearLayout implements View.OnClickListener {

    @Bind(R.id.view_options_menu_cancel) ViewTypefaceButton cancel;
    @Bind(R.id.view_options_menu_1) ViewTypefaceButton option1;
    @Bind(R.id.view_options_menu_2) ViewTypefaceButton option2;
    @Bind(R.id.view_options_menu_accept) ViewTypefaceButton accept;

    private DrawingCurve.State mState = null;

    private ArrayList<ViewOptionsMenuListener> listeners;
    public interface ViewOptionsMenuListener {
        void onOptionsMenuCancel();
        void onOptionsMenuButtonClicked(View view, DrawingCurve.State state);
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
    @OnClick({R.id.view_options_menu_cancel, R.id.view_options_menu_accept, R.id.view_options_menu_2,
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
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuButtonClicked(v, mState);
                }
                break;
            case R.id.view_options_menu_2:
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuButtonClicked(v, mState);
                }
                break;
        }
    }

    public void addListener(ViewOptionsMenuListener optionsMenuListener) {
        listeners.add(optionsMenuListener);
    }

    public void setState(DrawingCurve.State state) {
        mState = state;

        switch (state) {
            case TEXT:
                if (option1.getParent() == null) {
                    addView(option1, getChildCount() - 1);
                    setWeightSum(4);
                }

                option1.setText(R.string.view_options_menu_edit_text);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_text_format_24dp));

                option2.setText(R.string.view_options_menu_edit_color);
                option2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_format_paint_24dp));
                break;
            case PICTURE:
                removeView(option1);

                setWeightSum(3);

                option2.setText(R.string.view_options_menu_edit_import);
                option2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_photo_24dp));

                requestLayout();
                break;
        }

        ViewUtils.visible(this);
    }
}