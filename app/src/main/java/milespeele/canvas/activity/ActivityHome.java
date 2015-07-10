package milespeele.canvas.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
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
import milespeele.canvas.util.Logger;
import milespeele.canvas.util.ParseUtils;


public class ActivityHome extends AppCompatActivity
    implements FragmentListener {

    @InjectView(R.id.activity_home_drawer_layout) DrawerLayout drawerLayout;
    @InjectView(R.id.activity_home_navigation_drawer) NavigationView navigationView;

    private final static String TAG_FRAGMENT_DRAWER = "fragd";
    private final static String TAG_FRAGMENT_COLOR = "color";
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
                R.string.menu_drawer_items_canvas_options, R.string.menu_drawer_items_canvas_options_undo);
        drawerLayout.setDrawerListener(toggle);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        parseUtils.checkActiveUser();
    }

    @Override
    protected void onPostCreate (Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        //saveImageToTempDir();
        super.onDestroy();
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
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
            } catch (IOException e) {

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

    private void tellFragmentToChangeColor(int color) {
        // CHANGE MENU ITEM COLOR
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.changeColor(color);
        }
    }

    private void addDrawerFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();
    }

    @Override
    public void showColorPicker() {
        FragmentColorPicker picker = FragmentColorPicker.newInstance();
        picker.show(getFragmentManager(), TAG_FRAGMENT_COLOR);
    }

    @Override
    public void showWidthPicker() {

    }

    @Override
    public void onColorChosen(int color) {
        if (color != -1) {
            tellFragmentToChangeColor(color);
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
