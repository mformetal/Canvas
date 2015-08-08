package milespeele.canvas.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventStrokeColor;
import milespeele.canvas.event.EventStrokeSize;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Util;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ActivityHome extends ActivityBase {

    @Bind(R.id.activity_home_drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.activity_home_navigation_drawer) NavigationView navigationView;

    private final static String TAG_FRAGMENT_DRAWER = "fragd";
    private final static String TAG_FRAGMENT_STROKE = "Stroke Color";
    private final static String TAG_FRAGMENT_FILL = "New Canvas Color";
    private final static String TAG_FRAGMENT_SHAPE = "shape";
    private final static String TAG_FRAGMENT_MASTERPIECE = "art";
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_home, menu);
        final MenuItem item = menu.findItem(R.id.menu_activity_home_save_canvas);
        item.getActionView().setOnClickListener(v -> showFilenameFragment());
        return super.onCreateOptionsMenu(menu);
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
            case R.id.menu_activity_home_new_canvas:
                showNewCanvasFragment();
                break;
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

    private void showFilenameFragment() {
        FragmentFilename.newInstance().show(getFragmentManager(), TAG_FRAGMENT_FILENAME);
    }

    private void imageToByteArray(String filename) {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            Bitmap art = frag.giveBitmapToActivity();
            Util.compressBitmap(art)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> onByteArrayReceived(bytes, filename),
                            this::onErrorReceived);
        }
    }

    public void onByteArrayReceived(byte[] result, String filename) {
        parseUtils.saveImageToServer(filename, new WeakReference<>(this), result);
    }

    public void onErrorReceived(Throwable error) {
        error.printStackTrace();
    }

    public void showSavedImageSnackbar(Masterpiece object) {
        findViewById(R.id.menu_activity_home_save_canvas).setAnimation(null);
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            Snackbar.make(frag.getView(), R.string.snackbar_activity_home_image_saved_title, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_activity_home_imaged_saved_body, v -> {})
                    .show();
        }
    }

    private void showNewCanvasFragment() {
        FragmentColorPicker picker = FragmentColorPicker.newInstance(TAG_FRAGMENT_FILL);
        picker.show(getFragmentManager(), TAG_FRAGMENT_FILL);
    }

    public void onEvent(EventStrokeColor test) {
        FragmentColorPicker picker = FragmentColorPicker.newInstance(TAG_FRAGMENT_STROKE);
        picker.show(getFragmentManager(), TAG_FRAGMENT_STROKE);
    }

    public void onEvent(EventStrokeSize test) {
        FragmentBrushPicker picker = FragmentBrushPicker.newInstance(test.size);
        picker.show(getFragmentManager(), TAG_FRAGMENT_BRUSH);
    }

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        imageToByteArray(eventFilenameChosen.filename);
    }
}
