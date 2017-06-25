package miles.scribble.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import miles.scribble.R;
import miles.scribble.data.event.EventBitmapChosen;
import miles.scribble.data.event.EventFilenameChosen;
import miles.scribble.data.model.Sketch;
import miles.scribble.ui.drawing.DrawingCurve;
import miles.scribble.ui.fragment.BaseFragment;
import miles.scribble.ui.fragment.BrushPickerFragment;
import miles.scribble.ui.fragment.ColorPickerFragment;
import miles.scribble.ui.fragment.FilenameFragment;
import miles.scribble.ui.fragment.TextFragment;
import miles.scribble.ui.transition.TransitionHelper;
import miles.scribble.ui.widget.CanvasLayout;
import miles.scribble.ui.widget.CanvasLayout.CanvasLayoutListener;
import miles.scribble.ui.widget.Fab;
import miles.scribble.ui.widget.RoundedFrameLayout;
import miles.scribble.util.FileUtils;
import miles.scribble.util.Logg;
import miles.scribble.rx.SafeSubscription;
import miles.scribble.util.ViewUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class HomeActivity extends BaseActivity
        implements CanvasLayoutListener, NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG_FRAGMENT_COLOR_PICKER = "color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";
    private final static String TAG_FRAGMENT_TEXT = "text";
    private final static int REQUEST_IMPORT_CODE = 2001;
    private final static int REQUEST_CAMERA_CODE = 2002;
    private final static int REQUEST_PERMISSION_CAMERA_CODE = 2003;

    DrawerLayout drawerLayout;
    CanvasLayout canvasLayout;
    NavigationView navigationView;

    private RoundedFrameLayout fabFrame;
    private FragmentManager manager;
    private String filePath;

    private int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ViewUtils.systemUIGone(getWindow().getDecorView());

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                ViewUtils.systemUIVisibile(getWindow().getDecorView());
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                ViewUtils.systemUIGone(getWindow().getDecorView());
            }
        });

        bus.register(this);

        manager = getFragmentManager();

        canvasLayout.setActivityListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("filePath", filePath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filePath = savedInstanceState.getString("filePath");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMPORT_CODE:
                    bus.post(new EventBitmapChosen(data.getData()));
                    break;
                case REQUEST_CAMERA_CODE:
                    Uri uri = FileUtils.addFileToGallery(this, filePath);
                    bus.post(new EventBitmapChosen(uri));
                    break;
                case REQUEST_AUTHENTICATION_CODE:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showCamera();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.deleteTemporaryFiles(this);
    }

    @Override
    public void onFabMenuButtonClicked(View view) {
        if (fabFrame == null) {
            fabFrame = (RoundedFrameLayout) findViewById(R.id.canvas_framelayout_animator);
        }

        switch (view.getId()) {
            case R.id.menu_brush:
                showBrushChooser(view);
                break;
            case R.id.menu_stroke_color:
                showColorChooser(view, false);
                break;
            case R.id.menu_text:
                showTextFragment(view);
                break;
            case R.id.menu_upload:
                showFilenameFragment(view);
                break;
            case R.id.menu_canvas_color:
                showColorChooser(view, true);
                break;
            case R.id.menu_image:
                showImageChooser();
                break;
        }
    }

    @Override
    public void onOptionsMenuButtonClicked(View view, DrawingCurve.State state) {
        switch (state) {
            case TEXT:
                if (view.getId() == R.id.view_options_menu_1) {
                    showTextFragment(view);
                } else {
                    showColorChooser(view, false);
                }
                break;
            case PICTURE:
                if (view.getId() == R.id.view_options_menu_1) {
                    showCamera();
                } else {
                    showGallery();
                }
                break;
        }
    }

    @Override
    public void onNavigationIconClicked() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_home_menu_gallery:
                startActivity(new Intent(this, GalleryActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressWarnings("unused, unchecked")
    public void onEvent(final EventFilenameChosen eventFilenameChosen) {
        final Fab saver = (Fab) findViewById(R.id.menu_upload);
        SafeSubscription<byte[]> safeSubscription = new SafeSubscription<byte[]>(this) {
            @Override
            public void onError(Throwable e) {
                super.onError(e);
                saver.stopSaveAnimation();
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                saver.stopSaveAnimation();
                showSnackbar(canvasLayout,
                        R.string.snackbar_activity_home_image_saved_title,
                        Snackbar.LENGTH_LONG,
                        null);
            }

            @Override
            public void onNext(byte[] o) {
                super.onNext(o);
                realm.beginTransaction();
                Sketch sketch = realm.createObject(Sketch.class);
                sketch.setBytes(o);
                sketch.setTitle(eventFilenameChosen.filename);
                sketch.setId(UUID.randomUUID().toString());
                realm.commitTransaction();
            }

            @Override
            public void onStart() {
                super.onStart();
                saver.startSaveAnimation();
            }
        };

        Bitmap root = canvasLayout.getDrawerBitmap();
        Bitmap bitmap = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(canvasLayout.getBackgroundColor());
        canvas.drawBitmap(root, 0, 0, null);

        FileUtils.compress(bitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(safeSubscription);
    }

    private void showBrushChooser(View view) {
        BrushPickerFragment picker = BrushPickerFragment.newInstance(canvasLayout.getPaint());

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker);

        manager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, picker, TAG_FRAGMENT_BRUSH)
                .commit();

        count++;
    }

    private void showColorChooser(View view, boolean toFill) {
        int color = toFill ? canvasLayout.getBackgroundColor() : canvasLayout.getBrushColor();
        ColorPickerFragment picker = ColorPickerFragment.newInstance(color, toFill);

        if (view instanceof Fab) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker);
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, picker);
        }

        manager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, picker, TAG_FRAGMENT_COLOR_PICKER)
                .commit();

        count++;
    }

    private void showTextFragment(View view) {
        TextFragment text = TextFragment.newInstance();

        if (view instanceof Fab) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, text);
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, text);
        }

        manager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, text, TAG_FRAGMENT_TEXT)
                .commit();

        count++;
    }

    private void showFilenameFragment(View view) {
        FilenameFragment filename = FilenameFragment.newInstance();

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, filename);

        manager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, filename, TAG_FRAGMENT_FILENAME)
                .commit();

        count++;
    }

    private void showGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMPORT_CODE);
    }

    private void showCamera() {
        String[] permissions = new String[] {android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (checkPermissions(permissions)) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = FileUtils.createPhotoFile();
                        filePath = photoFile.getAbsolutePath();
                    } catch (IOException e) {
                        Logg.log(e);
                    }

                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_CAMERA_CODE);
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CAMERA_CODE);
        }
    }

    private void showImageChooser() {
        final Dialog builder = new Dialog(this);
        builder.setContentView(R.layout.dialog_image_chooser);
        builder.findViewById(R.id.dialog_from_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
                showCamera();
            }
        });
        builder.findViewById(R.id.dialog_from_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
                showGallery();
            }
        });
        builder.show();
    }
}