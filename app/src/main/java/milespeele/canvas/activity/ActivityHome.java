package milespeele.canvas.activity;

import android.app.FragmentManager;
import android.os.Bundle;
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
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentColors;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.transition.TransitionHelper;
import milespeele.canvas.util.ErrorDialog;
import milespeele.canvas.view.ViewFab;

public class ActivityHome extends ActivityBase {

    private final static String TAG_FRAGMENT_DRAWER = "drawer";
    private final static String TAG_FRAGMENT_COLOR = "color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";

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
        fabFrame = (FrameLayout) findViewById(R.id.fragment_drawer_animator);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        bus.register(this);

        manager = getFragmentManager();

        addDrawerFragment(-1, -1);
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

    private void addDrawerFragment(float cx, float cy) {
        manager.beginTransaction()
                .replace(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(cx, cy), TAG_FRAGMENT_DRAWER)
                .addToBackStack(TAG_FRAGMENT_DRAWER)
                .commit();
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

    public void showBrushChooser(ViewFab view) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            FragmentBrushPicker picker = FragmentBrushPicker.newInstance(frag.getRootView().getBrushWidth(),
                    frag.getRootView().getBrushColor());

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
            FragmentColorPicker picker = FragmentColorPicker.newInstance(frag.getRootView().getBrushColor());

            TransitionHelper.makeFabDialogTransitions(ActivityHome.this, view, fabFrame, picker);

            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator, picker)
                    .addToBackStack(TAG_FRAGMENT_COLOR)
                    .commit();
        }
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
            case R.id.menu_size:
                showBrushChooser(view);
                break;
            case R.id.menu_color:
                showStrokeColorChooser(view);
                break;
        }
    }
}
