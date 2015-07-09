package milespeele.canvas.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.asynctask.AsyncSave;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentListener;
import milespeele.canvas.util.ParseUtils;


public class ActivityHome extends AppCompatActivity
    implements FragmentListener {

    @InjectView(R.id.activity_home_toolbar) Toolbar toolbar;
    @InjectView(R.id.activity_home_drawer_layout) DrawerLayout drawerLayout;
    @InjectView(R.id.activity_home_navigation_drawer) NavigationView navigationView;

    private final static String TAG_FRAGMENT_DRAWER = "fragd";
    private final static String TAG_FRAGMENT_COLOR = "color";

    @Inject ParseUtils parseUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        addDrawerFragment();

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_brush_white_18dp);
        ab.setDisplayHomeAsUpEnabled(true);

        parseUtils.checkActiveUser();

        setupDrawerContent(navigationView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawerLayout.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_drawer_color:
                showColorPicker();
                break;

            case R.id.menu_drawer_undo:
                tellFragmentToUndo();
                break;

            case R.id.menu_drawer_save:
                saveImage();
                break;

            case R.id.menu_drawer_erase:
                tellFragmentToClearCanvas();
                break;
        }
    }

    private void saveImage() {
        // SHOW DIALOG FIRST
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            Bitmap art = frag.giveBitmapToActivity();
            Integer[] dimens = getScreenDimens();
            new AsyncSave(this, dimens[0], dimens[1]).execute(art);
        }
    }

    private void tellFragmentToClearCanvas() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.clearCanvas();
        }
    }

    private void tellFragmentToChangeColor(int color) {
        // CHANGE MENU ITEM COLOR
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.changeColor(color);
        }
    }

    private void tellFragmentToStartErasing() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.startErasing();
        }
    }

    private void tellFragmentToUndo() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            frag.undo();
        }
    }

    private void addDrawerFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();
    }

    private void showColorPicker() {
        FragmentColorPicker picker = FragmentColorPicker.newInstance();
        picker.show(getFragmentManager(), TAG_FRAGMENT_COLOR);
    }

    @Override
    public void onColorChosen(int color) {
        if (color != -1) {
            tellFragmentToChangeColor(color);
        }
    }

    @Override
    public void onPaletteClicked() {

    }

    private Integer[] getScreenDimens() {
        Point mPoint = new Point();
        getWindowManager().getDefaultDisplay().getSize(mPoint);
        int width = mPoint.x;
        int height = mPoint.y;
        return new Integer[] {width, height};
    }
}
