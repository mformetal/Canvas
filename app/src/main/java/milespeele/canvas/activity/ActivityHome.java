package milespeele.canvas.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.dialog.ErrorDialog;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.event.EventShowBrushPicker;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventShowCanvasColorPicker;
import milespeele.canvas.event.EventShowFilenameFragment;
import milespeele.canvas.event.EventShowStrokePickerColor;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Util;
import milespeele.canvas.view.ViewFab;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ActivityHome extends ActivityBase {

    @Bind(R.id.activity_home_drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.activity_home_navigation_drawer) NavigationView navigationView;

    private final static String TAG_FRAGMENT_DRAWER = "fragd";
    private final static String TAG_FRAGMENT_STROKE = "Stroke Color";
    private final static String TAG_FRAGMENT_FILL = "New Canvas Color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";

    @Inject ParseUtils parseUtils;
    @Inject EventBus bus;

    private ActionBarDrawerToggle toggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        bus.register(this);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.activity_home_actionbar_toggle_open, R.string.activity_home_actionbar_toggle_close);
        drawerLayout.setDrawerListener(toggle);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        setupDrawerContent(navigationView);

        addDrawerFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            parseUtils.pinImage("Image", frag.giveBitmapToActivity());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        parseUtils.getPinnedImage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_drawer_gallery:
                break;
        }
    }

    private void addDrawerFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
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

    public void onSaveImageError(Throwable throwable) {
        Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
        throwable.printStackTrace();
    }

    public void onEvent(EventParseError eventParseError) {
        ((ViewFab) findViewById(R.id.menu_save)).stopPulse();
        ErrorDialog.createDialogFromCode(this, eventParseError.getErrorCode()).show();
    }

    public void onEvent(EventShowStrokePickerColor test) {
        FragmentColorPicker.newInstance(TAG_FRAGMENT_STROKE, test.color)
                .show(getFragmentManager(), TAG_FRAGMENT_STROKE);
    }

    public void onEvent(EventShowBrushPicker test) {
        FragmentBrushPicker.newInstance(test.size)
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
        if (!eventFilenameChosen.filename.isEmpty()) {
            FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
            if (frag != null) {
                parseUtils.saveImageToServer(eventFilenameChosen.filename,
                        new WeakReference<>(this), frag.giveBitmapToActivity());
            }
        }
    }

    public void onEvent(EventShowFilenameFragment eventShowFilenameFragment) {
        FragmentFilename.newInstance().show(getFragmentManager(), TAG_FRAGMENT_FILENAME);
    }
}
