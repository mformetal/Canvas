package milespeele.canvas.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.asynctask.AsyncBitmap;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.fragment.FragmentListener;
import milespeele.canvas.fragment.FragmentMasterpiece;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;

public class ActivityHome extends ActivityBase implements FragmentListener {

    @InjectView(R.id.activity_home_drawer_layout) DrawerLayout drawerLayout;
    @InjectView(R.id.activity_home_navigation_drawer) NavigationView navigationView;

    private final static String TAG_FRAGMENT_DRAWER = "fragd";
    private final static String TAG_FRAGMENT_STROKE = "stroke";
    private final static String TAG_FRAGMENT_FILL = "fill";
    private final static String TAG_FRAGMENT_SHAPE = "shape";
    private final static String TAG_FRAGMENT_MASTERPIECE = "art";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";

    private final static String TAG_SERVER_SAVE = "there";

    @Inject ParseUtils parseUtils;

    private ActionBarDrawerToggle toggle;

    private AsyncBitmap asyncBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.brush);
        ab.setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.activity_home_actionbar_toggle_open, R.string.activity_home_actionbar_toggle_close);
        drawerLayout.setDrawerListener(toggle);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        setupDrawerContent(navigationView);

        parseUtils.checkActiveUser();

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
            case R.id.menu_activity_home_save_canvas:
                addFilenameFragment();
                break;
            case R.id.menu_activity_home_erase_canvas:
                tellFragmentToEraseCanvas();
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
                startGalleryActivity();
                break;
        }
    }

    private void addDrawerFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();
    }

    private void addFragmentMasterpiece(Masterpiece object) {
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_home_fragment_frame, FragmentMasterpiece.newInstance(object), TAG_FRAGMENT_MASTERPIECE)
                .addToBackStack(TAG_FRAGMENT_MASTERPIECE)
                .setCustomAnimations(R.anim.fab_in, R.anim.fade_out)
                .commit();
    }

    private void addFilenameFragment() {
        FragmentFilename.newInstance().show(getFragmentManager(), TAG_FRAGMENT_FILENAME);
    }

    private void startGalleryActivity() {
//        Intent gallery = new Intent(this, ActivityGallery.class);
//        startActivity(gallery);
    }

    private boolean checkAsyncStatus() {
        if (asyncBitmap == null)  {
            return true;
        }

        AsyncTask.Status status = asyncBitmap.getStatus();
        return status != AsyncTask.Status.RUNNING && status != AsyncTask.Status.PENDING;
    }

    private void imageToByteArray(String whereSaving, String filename) {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null && checkAsyncStatus()) {
            Bitmap art = frag.giveBitmapToActivity();
            Integer[] dimens = getScreenDimens();
            asyncBitmap = new AsyncBitmap(filename, this, dimens[0], dimens[1]);
            asyncBitmap.execute(art);
        }
    }

    public void onByteArrayReceived(byte[] result, String filename) {
        parseUtils.saveImageToServer(filename, new WeakReference<>(this), result);
    }

    public void showSavedImageSnackbar(Masterpiece object) {
        Snackbar.make(drawerLayout, R.string.snackbar_activity_home_image_saved_title, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_activity_home_imaged_saved_body, v -> {
                    //addFragmentMasterpiece(object);
                })
                .show();
    }

    private void tellFragmentToChangeColor(int color) {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.changeColor(color);
        }
    }

    private void tellFragmentToFillCanvas(int color) {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.fillCanvas(color);
        }
    }

    private void tellFragmentToEraseCanvas() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.eraseCanvas();
        }
    }

    @Override
    public void showColorPicker(int viewId) {
        FragmentColorPicker picker;
        switch (viewId) {
            case R.id.palette_fill_canvas:
                picker = FragmentColorPicker.newInstance(TAG_FRAGMENT_FILL);
                picker.show(getFragmentManager(), TAG_FRAGMENT_FILL);
                break;
            case R.id.palette_paint:
                picker = FragmentColorPicker.newInstance(TAG_FRAGMENT_STROKE);
                picker.show(getFragmentManager(), TAG_FRAGMENT_STROKE);
                break;
        }
    }

    @Override
    public void showShapePicker() {

    }

    @Override
    public void showBrushPicker(float currentWidth) {
        FragmentBrushPicker picker = FragmentBrushPicker.newInstance(currentWidth);
        picker.show(getFragmentManager(), TAG_FRAGMENT_BRUSH);
    }

    @Override
    public void onBrushSizeChosen(float size) {
        ((FragmentBrushPicker) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_BRUSH)).dismiss();
        if (size != 0) {
            FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
            if (frag != null) {
                frag.setBrushWidth(size);
            }
        }
    }

    @Override
    public void onFilenameChosen(String fileName) {
        ((FragmentFilename) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_FILENAME)).dismiss();
        if (!fileName.isEmpty()) {
            imageToByteArray(TAG_SERVER_SAVE, fileName);
        }
    }

    @Override
    public void onColorChosen(int color, String whichColor) {
        ((FragmentColorPicker) getFragmentManager().findFragmentByTag(whichColor)).dismiss();
        if (color != -1) {
            switch (whichColor) {
                case TAG_FRAGMENT_FILL:
                    tellFragmentToFillCanvas(color);
                    break;

                case TAG_FRAGMENT_STROKE:
                    tellFragmentToChangeColor(color);
                    break;
            }
        }
    }

    private Integer[] getScreenDimens() {
        Point mPoint = new Point();
        getWindowManager().getDefaultDisplay().getSize(mPoint);
        int width = mPoint.x;
        int height = mPoint.y;
        return new Integer[] {width, height};
    }
}
