package milespeele.canvas.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;

/**
 * Created by mbpeele on 12/23/15.
 */
public class ViewOptionsMenu extends LinearLayout implements View.OnClickListener {

    @Bind(R.id.view_options_menu_cancel) ViewTypefaceButton cancel;
    @Bind(R.id.view_options_menu_accept) ViewTypefaceButton accept;

    private ViewOptionsMenuListener listener;
    public interface ViewOptionsMenuListener {
        void onOptionsMenuCancel();
        void onOptionsMenuButtonClicked();
        void onOptionsMenuAccept();
    }

    public ViewOptionsMenu(Context context) {
        super(context);
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    @OnClick({R.id.view_options_menu_cancel, R.id.view_options_menu_accept})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_options_menu_cancel:
                listener.onOptionsMenuCancel();
                break;
            case R.id.view_options_menu_accept:
                listener.onOptionsMenuAccept();
                break;
            default:
                listener.onOptionsMenuButtonClicked();
                break;
        }
    }

    public void setListener(ViewOptionsMenuListener optionsMenuListener) {
        listener = optionsMenuListener;
    }
}
