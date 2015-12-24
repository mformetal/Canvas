package milespeele.canvas.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.Snackbar;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.fragment.FragmentText;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.transition.TransitionHelper;
import milespeele.canvas.util.ErrorDialog;
import milespeele.canvas.util.NetworkUtils;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewRoundedFrameLayout;

public class ActivityHome extends ActivityBase {

    private final static String TAG_FRAGMENT_DRAWER = "drawer";
    private final static String TAG_FRAGMENT_COLOR_PICKER = "color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";
    private final static String TAG_FRAGMENT_TEXT = "text";

    @Inject ParseUtils parseUtils;
    @Inject EventBus bus;
    @Inject Picasso picasso;

    private ViewRoundedFrameLayout fabFrame;
    private FragmentManager manager;

    private int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        getWindow().setBackgroundDrawable(null);

        bus.register(this);

        manager = getFragmentManager();

        manager.beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (count == 0) {
            super.onBackPressed();
        } else {
            // UGLY, but popBackStack() results in a weird exception
            count--;

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_drawer_animator, new Fragment());
            ft.commit();
        }
    }

    public void onEvent(EventParseError eventParseError) {
        ErrorDialog.createDialogFromCode(this, eventParseError.getErrorCode()).show();
    }

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            parseUtils.saveImageToServer(eventFilenameChosen.filename,
                    new WeakReference<>(this), frag.getDrawingBitmap());
        }
    }

    public void showSavedImageSnackbar(Masterpiece object) {
        ((ViewFab) findViewById(R.id.menu_save)).stopSaveAnimation(); // Ugly but whatever
        FragmentDrawer fragmentDrawer = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (fragmentDrawer != null && fragmentDrawer.getRootView() != null) {
            Snackbar.make(fragmentDrawer.getRootView(), R.string.snackbar_activity_home_image_saved_title, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public void showBrushChooser(ViewFab view) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            FragmentBrushPicker picker = FragmentBrushPicker.newInstance();

            TransitionHelper.makeFabDialogTransitions(ActivityHome.this, view, fabFrame, picker);

            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator, picker)
//                    .addToBackStack(TAG_FRAGMENT_BRUSH)
                    .commit();

            count++;
        }
    }

    public void showStrokeColorChooser(ViewFab view, boolean toFill) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            FragmentColorPicker picker = FragmentColorPicker
                    .newInstance(frag.getRootView().getBrushColor(), toFill);

            TransitionHelper.makeFabDialogTransitions(ActivityHome.this, view, fabFrame, picker);

            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator, picker)
//                    .addToBackStack(TAG_FRAGMENT_COLOR_PICKER)
                    .commit();

            count++;
        }
    }

    public void showTextFragment(ViewFab fab) {
        FragmentText text = FragmentText.newInstance();

        TransitionHelper.makeFabDialogTransitions(this, fab, fabFrame, text);

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, text, TAG_FRAGMENT_TEXT)
//                .addToBackStack(TAG_FRAGMENT_TEXT)
                .commit();

        count++;
    }

    public void showFilenameFragment(ViewFab view) {
        FragmentFilename filename = FragmentFilename.newInstance();

        TransitionHelper.makeFabDialogTransitions(ActivityHome.this, view, fabFrame, filename);

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, filename, TAG_FRAGMENT_FILENAME)
//                .addToBackStack(TAG_FRAGMENT_FILENAME)
                .commit();

        count++;
    }

    public void onFabMenuButtonClicked(ViewFab view) {
        if (fabFrame == null) {
            fabFrame = (ViewRoundedFrameLayout) findViewById(R.id.fragment_drawer_animator);
        }

        switch (view.getId()) {
            case R.id.menu_brush:
                showBrushChooser(view);
                break;
            case R.id.menu_stroke_color:
                showStrokeColorChooser(view, false);
                break;
            case R.id.menu_text:
                showTextFragment(view);
                break;
            case R.id.menu_save:
                showFilenameFragment(view);
                break;
            case R.id.menu_canvas_color:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.alert_dialog_new_canvas_title)
                        .setMessage(R.string.alert_dialog_new_canvas_body)
                        .setPositiveButton(R.string.alert_dialog_new_canvas_pos_button, (dialog, which) -> {
                            showStrokeColorChooser(view, true);
                        })
                        .setNegativeButton(R.string.alert_dialog_new_canvas_neg_button, (dialog, which) -> {
                            dialog.dismiss();
                        });
                builder.create().show();
                break;
        }
    }
}
