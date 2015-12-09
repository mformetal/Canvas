package milespeele.canvas.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.event.EventTextChosen;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.fragment.FragmentText;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.transition.TransitionHelper;
import milespeele.canvas.util.ErrorDialog;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.NetworkUtils;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.view.ViewCanvasSurface;
import milespeele.canvas.view.ViewFab;

public class ActivityHome extends ActivityBase {

    private final static String TAG_FRAGMENT_DRAWER = "drawer";
    private final static String TAG_FRAGMENT_COLOR_PICKER = "color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";
    private final static String TAG_FRAGMENT_TEXT = "text";

    @Inject ParseUtils parseUtils;
    @Inject EventBus bus;
    @Inject Picasso picasso;

    private FrameLayout fabFrame;

    private FragmentManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        bus.register(this);

        manager = getFragmentManager();

        manager.beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();
    }

    @Override
    public void onBackPressed() {
        int count = manager.getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            manager.popBackStack();
        }
    }

    public void onEvent(EventParseError eventParseError) {
        ((ViewFab) findViewById(R.id.menu_save)).stopPulse();

        ErrorDialog.createDialogFromCode(this, eventParseError.getErrorCode()).show();
    }

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        if (NetworkUtils.hasInternet(this)) {
            ((ViewFab) findViewById(R.id.menu_save)).startPulse();

            FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
            if (frag != null) {
                parseUtils.saveImageToServer(eventFilenameChosen.filename,
                        new WeakReference<>(this), frag.getDrawingBitmap());
            }
        } else {
            ErrorDialog.createDialogFromCode(this, ErrorDialog.NO_INTERNET).show();
        }
    }

    public void showSavedImageSnackbar(Masterpiece object) {
        ((ViewFab) findViewById(R.id.menu_save)).stopPulse();
        FragmentDrawer fragmentDrawer = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (fragmentDrawer != null && fragmentDrawer.getRootView() != null) {
            Snackbar.make(fragmentDrawer.getRootView(), R.string.snackbar_activity_home_image_saved_title, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public void showBrushChooser(ViewFab view) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            FragmentBrushPicker picker = FragmentBrushPicker.newInstance(frag.getRootView().getCurrentPaint());

            TransitionHelper.makeFabDialogTransitions(ActivityHome.this, view, fabFrame, picker);

            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator, picker)
                    .addToBackStack(TAG_FRAGMENT_BRUSH)
                    .commit();
        }
    }

    public void showStrokeColorChooser(ViewFab view) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            FragmentColorPicker picker = FragmentColorPicker.
                    newInstance(frag.getRootView().getBrushColor(), frag.getRootView().getCurrentColors());

            TransitionHelper.makeFabDialogTransitions(ActivityHome.this, view, fabFrame, picker);

            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator, picker)
                    .addToBackStack(TAG_FRAGMENT_COLOR_PICKER)
                    .commit();
        }
    }

    public void showTextFragment(ViewFab fab) {
        FragmentText text = FragmentText.newInstance();

        TransitionHelper.makeFabDialogTransitions(this, fab, fabFrame, text);

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, text, TAG_FRAGMENT_TEXT)
                .addToBackStack(TAG_FRAGMENT_TEXT)
                .commit();
    }

    public void showFilenameFragment(ViewFab view) {
        FragmentFilename filename = FragmentFilename.newInstance();

        TransitionHelper.makeFabDialogTransitions(ActivityHome.this, view, fabFrame, filename);

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, filename, TAG_FRAGMENT_FILENAME)
                .addToBackStack(TAG_FRAGMENT_FILENAME)
                .commit();
    }

    public void onFabMenuButtonClicked(ViewFab view) {
        fabFrame = (FrameLayout) findViewById(R.id.fragment_drawer_animator);

        switch (view.getId()) {
            case R.id.menu_brush:
                showBrushChooser(view);
                break;
            case R.id.menu_color:
                showStrokeColorChooser(view);
                break;
            case R.id.menu_text:
                showTextFragment(view);
                break;
            case R.id.menu_save:
                showFilenameFragment(view);
                break;
        }
    }
}
