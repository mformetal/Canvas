package milespeele.canvas.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.widget.ViewUtils;
import android.transition.ArcMotion;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDashboard;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.ErrorDialog;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewToolbar;

public class ActivityHome extends ActivityBase {

    private final static String TAG_FRAGMENT_DRAWER = "fragd";
    private final static String TAG_FRAGMENT_STROKE = "Stroke Color";
    private final static String TAG_FRAGMENT_FILL = "New Canvas Color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";
    private final static String TAG_FRAGMENT_DASHBOARD = "dash";

    @Inject ParseUtils parseUtils;
    @Inject EventBus bus;
    @Inject Picasso picasso;

    @Bind(R.id.activity_home_root) CoordinatorLayout root;
    @Bind(R.id.activity_home_toolbar) ViewToolbar toolbar;

    private FragmentManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        bus.register(this);

        manager = getFragmentManager();

        addDashboardFragment();
    }

    @Override
    public void onBackPressed() {
        int count = manager.getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(count - 1);
            if (entry.getName().equals(TAG_FRAGMENT_DRAWER)) {
                toolbar.animateIn();
            }

            manager.popBackStack();
        }
    }

    private void addDrawerFragment(float cx, float cy) {
        manager.beginTransaction()
                .replace(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(cx, cy), TAG_FRAGMENT_DRAWER)
                .addToBackStack(TAG_FRAGMENT_DRAWER)
                .commit();

        toolbar.animateOut();
    }

    private void addDashboardFragment() {
        manager.beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDashboard.newInstance(), TAG_FRAGMENT_DASHBOARD)
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

    public void showSavedImageSnackbar(Masterpiece object) {
        ((ViewFab) findViewById(R.id.menu_save)).stopPulse();
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null && frag.getView() != null) {
            Snackbar.make(frag.getView(), R.string.snackbar_activity_home_image_saved_title, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_activity_home_imaged_saved_body, v -> {
                    })
                    .show();
        }
    }

    public void showBrushChooser() {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator,
                            FragmentBrushPicker.newInstance(frag.getRootView().getBrushWidth(), frag.getRootView().getBrushColor()))
                    .addToBackStack(TAG_FRAGMENT_BRUSH)
                    .commit();
        }
    }

    public void showStrokeColorChooser() {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator,
                            FragmentColorPicker.newInstance(TAG_FRAGMENT_STROKE, frag.getRootView().getBrushColor()))
                    .addToBackStack(TAG_FRAGMENT_STROKE)
                    .commit();
        }
    }

    public void showNewCanvasColorChooser() {
        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator,
                        FragmentColorPicker.newInstance(TAG_FRAGMENT_FILL, 0))
                .addToBackStack(TAG_FRAGMENT_FILL)
                .commit();
    }

    public void showFilenameFragment() {
        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, FragmentFilename.newInstance(), TAG_FRAGMENT_FILENAME)
                .addToBackStack(TAG_FRAGMENT_FILENAME)
                .commit();
    }

    public void onDashboardButtonClicked(int clickedId, float cx, float cy) {
        switch (clickedId) {
            case R.id.dashboard_draw:
                addDrawerFragment(cx, cy);
                break;
            case R.id.dashboard_import:
            case R.id.dashboard_profile:
            case R.id.dashboard_social:
        }
    }

    public void onFabMenuButtonClicked(ViewFab view) {
        switch (view.getId()) {
            case R.id.menu_size:
                showBrushChooser();
                break;
            case R.id.menu_stroke_color:
                showStrokeColorChooser();
                break;
            case R.id.menu_new_canvas:
                showNewCanvasColorChooser();
                break;
            case R.id.menu_save:
                showFilenameFragment();
                break;
        }
    }
}
