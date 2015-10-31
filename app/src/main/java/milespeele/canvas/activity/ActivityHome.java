package milespeele.canvas.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateDecelerateInterpolator;

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
import milespeele.canvas.event.EventRevealFinished;
import milespeele.canvas.event.EventShowBrushPicker;
import milespeele.canvas.event.EventShowCanvasColorPicker;
import milespeele.canvas.event.EventShowFilenameFragment;
import milespeele.canvas.event.EventShowShapeChooser;
import milespeele.canvas.event.EventShowStrokePickerColor;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDashboard;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;
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

    @Bind(R.id.activity_home_toolbar) ViewToolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        bus.register(this);

        addDashboardFragment();
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getFragmentManager();
        int count = manager.getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(count - 1);
            if (entry.getName().equals(TAG_FRAGMENT_DRAWER)) {
                toolbar.animateIn();
                FragmentDrawer drawer = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
                drawer.unreveal();
            } else {
                manager.popBackStack();
            }
        }
    }

    private void addDrawerFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .addToBackStack(TAG_FRAGMENT_DRAWER)
                .commit();

        toolbar.animateOut();
    }

    private void addDashboardFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_home_fragment_frame, FragmentDashboard.newInstance(), TAG_FRAGMENT_DASHBOARD)
                .commit();
    }

    public void showSavedImageSnackbar(Masterpiece object) {
        ((ViewFab) findViewById(R.id.menu_save)).stopPulse();
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null && frag.getView() != null) {
            Snackbar.make(frag.getView(), R.string.snackbar_activity_home_image_saved_title, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_activity_home_imaged_saved_body, v -> {})
                    .show();
        }
    }

    public void onEvent(EventParseError eventParseError) {
        ErrorDialog.createDialogFromCode(this, eventParseError.getErrorCode()).show();
    }

    public void onEvent(EventShowStrokePickerColor test) {
        FragmentColorPicker.newInstance(TAG_FRAGMENT_STROKE, test.color)
                .show(getFragmentManager(), TAG_FRAGMENT_STROKE);
    }

    public void onEvent(EventShowBrushPicker test) {
        FragmentBrushPicker.newInstance(test.size, test.color)
                .show(getFragmentManager(), TAG_FRAGMENT_BRUSH);
    }

    public void onEvent(EventShowCanvasColorPicker eventNewCanvasColor) {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.alert_dialog_new_canvas_title))
                .setMessage(getResources().getString(R.string.alert_dialog_new_canvas_body))
                .setPositiveButton(getResources().getString(R.string.alert_dialog_new_canvas_pos_button),
                        (dialog, which) -> {
                            FragmentColorPicker picker =
                                    FragmentColorPicker.newInstance(TAG_FRAGMENT_FILL, 0);
                            picker.show(getFragmentManager(), TAG_FRAGMENT_FILL);
                        })
                .setNegativeButton(getResources().getString(R.string.fragment_color_picker_nah),
                        (dialog, which) -> {
                            dialog.dismiss();
                        })
                .create();
        alert.show();
    }

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            parseUtils.saveImageToServer(eventFilenameChosen.filename,
                    new WeakReference<>(this), frag.giveBitmapToActivity());
        }
    }

    public void onEvent(EventShowFilenameFragment eventShowFilenameFragment) {
        FragmentFilename.newInstance().show(getFragmentManager(), TAG_FRAGMENT_FILENAME);
    }

    public void onEvent(EventShowShapeChooser eventShowShapeChooser) {

    }

    public void onDashboardButtonClicked(int clickedId) {
        switch (clickedId) {
            case R.id.dashboard_draw:
                addDrawerFragment();
                break;
            case R.id.dashboard_import:
            case R.id.dashboard_profile:
            case R.id.dashboard_social:
        }
    }

    public void onEvent(EventRevealFinished eventRevealFinished) {
        if (!eventRevealFinished.bool) {
            getFragmentManager().popBackStack();
        }
    }
}
