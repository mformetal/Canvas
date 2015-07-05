package milespeele.canvas.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.asynctask.AsyncSave;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentListener;
import milespeele.canvas.util.ParseUtils;


public class ActivityHome extends AppCompatActivity
    implements FragmentListener {

    private final static String TAG_FRAGMENTDRAWER = "fragd";
    private final static String TAG_FRAGMENTCOLOR = "color";

    @Inject ParseUtils parseUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        addDrawerFragment();

        parseUtils.checkActiveUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_activity_home_clear_canvas:
                tellFragmentToClearCanvas();
                break;

            case R.id.menu_activity_home_pick_color:
                showColorPicker();
                break;

            case R.id.menu_activity_home_undo:
                tellFragmentToUndo();
                break;

            case R.id.menu_activity_home_save:
                saveImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENTDRAWER);
        if (frag != null) {
            Bitmap art = frag.giveBitmapToActivity();
            Integer[] dimens = getScreenDimens();
            new AsyncSave(this, dimens[0], dimens[1]).execute(art);
        }
    }

    private void tellFragmentToClearCanvas() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENTDRAWER);
        if (frag != null) {
            frag.clearCanvas();
        }
    }

    private void tellFragmentToChangeColor(int color) {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENTDRAWER);
        if (frag != null) {
            frag.changeColor(color);
        }
    }

    private void tellFragmentToStartErasing() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENTDRAWER);
        if (frag != null) {
            frag.startErasing();
        }
    }

    private void tellFragmentToUndo() {
        FragmentDrawer frag = (FragmentDrawer) getFragmentManager().findFragmentByTag(TAG_FRAGMENTDRAWER);
        if (frag != null) {
            frag.undo();
        }
    }

    private void addDrawerFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENTDRAWER)
                .commit();
    }

    private void showColorPicker() {
        FragmentColorPicker picker = FragmentColorPicker.newInstance();
        picker.show(getFragmentManager(), TAG_FRAGMENTCOLOR);
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
