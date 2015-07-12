package milespeele.canvas.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.asynctask.AsyncSave;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentListener;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logger;


public class ActivityHome extends AppCompatActivity
    implements FragmentListener, View.OnClickListener {

    @InjectView(R.id.activity_home_drawer_layout) DrawerLayout drawerLayout;
    @InjectView(R.id.activity_home_navigation_drawer) NavigationView navigationView;

    private final static String TAG_FRAGMENT_DRAWER = "fragd";
    private final static String TAG_FRAGMENT_STROKE = "stroke";
    private final static String TAG_FRAGMENT_FILL = "fill";
    private final static String TAG_BITMAP_FILE_DIR = "bitmap";
    private final static String TAG_BITMAP_FILE_NAME = "canvas.png";

    @Inject ParseUtils parseUtils;

    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        addDrawerFragment();

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.brush);
        ab.setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.activity_home_actionbar_toggle_open, R.string.activity_home_actionbar_toggle_close);
        drawerLayout.setDrawerListener(toggle);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        parseUtils.checkActiveUser();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveImageToTempDir();
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
                saveImageToParse();
                break;
            case R.id.menu_activity_home_erase_canvas:
                tellFragmentToEraseCanvas();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Logger.log("ON CLICK");
    }

    private void addDrawerFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();
    }

    private void saveImageToTempDir() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            Bitmap bmp = frag.giveBitmapToActivity();
            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + TAG_BITMAP_FILE_DIR;
            File dir = new File(file_path);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir,TAG_BITMAP_FILE_NAME);
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImageToParse() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            Bitmap art = frag.giveBitmapToActivity();
            Integer[] dimens = getScreenDimens();
            new AsyncSave(this, dimens[0], dimens[1]).execute(art);
        }
    }

    public void showSavedImageSnackbar() {
        Snackbar.make(drawerLayout, R.string.snackbar_activity_home_image_saved_title, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_activity_home_imaged_saved_body, this)
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
    public void showWidthPicker() {

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
