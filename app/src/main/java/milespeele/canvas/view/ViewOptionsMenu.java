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

/**
 * Created by mbpeele on 12/23/15.
 */
public class ViewOptionsMenu extends LinearLayout implements View.OnClickListener {

    @Bind(R.id.view_options_menu_cancel) ViewTypefaceButton cancel;
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
            R.id.view_options_menu_switch})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_options_menu_cancel:
                for (ViewOptionsMenuListener listener: listeners) {
                    listener.onOptionsMenuCancel();
                }
                break;
            case R.id.view_options_menu_accept:
                for (ViewOptionsMenuListener listener: listeners) {
                    listener.onOptionsMenuAccept();
                }
                break;
            case R.id.view_options_menu_switch:
                for (ViewOptionsMenuListener listener: listeners) {
                    listener.onOptionsMenuButtonClicked(v);
                }
                break;
        }
    }

    public void addListener(ViewOptionsMenuListener optionsMenuListener) {
        listeners.add(optionsMenuListener);
    }
}