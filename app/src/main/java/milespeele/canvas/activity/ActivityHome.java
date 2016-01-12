package milespeele.canvas.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
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
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.event.EventBitmapChosen;
import milespeele.canvas.event.EventClearCanvas;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.fragment.FragmentBase;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.fragment.FragmentText;
import milespeele.canvas.transition.TransitionHelper;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.SimpleDrawerLayoutListener;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewRoundedFrameLayout;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ActivityHome extends ActivityBase implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG_FRAGMENT_DRAWER = "drawer";
    private final static String TAG_FRAGMENT_COLOR_PICKER = "color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";
    private final static String TAG_FRAGMENT_TEXT = "text";
    private final static int REQUEST_IMPORT_CODE = 2001;
    private final static int REQUEST_CAMERA_CODE = 2002;
    private final static int REQUEST_PERMISSION_CAMERA_CODE = 2003;

    @Bind(R.id.activity_home_fragment_frame) FrameLayout frameLayout;
    @Bind(R.id.activity_home_drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.activity_home_navigation_view) NavigationView navigationView;

    private ViewRoundedFrameLayout fabFrame;
    private FragmentManager manager;
    private String filePath;

    private int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ViewUtils.systemUIGone(getWindow().getDecorView());

        bus.register(this);

        drawerLayout.setDrawerListener(new SimpleDrawerLayoutListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                ViewUtils.systemUIGone(getWindow().getDecorView());
            }
        });
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView.setNavigationItemSelectedListener(this);

        manager = getFragmentManager();

        manager.beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();

        setupHeaderView();
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
    public void onBackPressed() {
        // UGLY, but popBackStack() results in a weird exception on certain devices
        // https://code.google.com/p/android/issues/detail?id=82832

        if (count == 0) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                Dialog builder = new Dialog(this);
                builder.setContentView(R.layout.dialog_save);
                builder.findViewById(R.id.dialog_save_drawing).setOnClickListener(v -> {
                    builder.dismiss();
                    saveAndExit();
                });
                builder.findViewById(R.id.dialog_exit_app).setOnClickListener(v -> {
                    super.onBackPressed();
                });
                builder.show();
            }
        } else {
            FragmentBase fragment = (FragmentBase)
                    manager.findFragmentById(R.id.fragment_drawer_animator);
            boolean shouldLetFragmentHandle = fragment.onBackPressed();
            if (!shouldLetFragmentHandle) {
                ViewUtils.systemUIGone(getWindow().getDecorView());

                count--;

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_drawer_animator, new Fragment());
                ft.commit();
            }
        }
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
    protected void onResume() {
        super.onResume();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            ViewUtils.systemUIVisibile(getWindow().getDecorView());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        FileUtils.deleteTemporaryFiles(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_drawer_header_profile:
                break;
            case R.id.menu_drawer_header_gallery:
                break;
            case R.id.menu_drawer_header_feed:
                break;
        }
        return false;
    }

    public void onFabMenuButtonClicked(View view) {
        if (fabFrame == null) {
            fabFrame = (ViewRoundedFrameLayout) findViewById(R.id.fragment_drawer_animator);
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
            case R.id.menu_clear_canvas:
                showClearCanvasDialog();
                break;
            case R.id.menu_navigation:
                showNavigationView();
                break;
        }
    }

    public void onOptionsMenuClicked(View view, DrawingCurve.State state) {
        if (state != null) {
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
    }

    private void saveAndExit() {
        FragmentDrawer fragmentDrawer = getFragmentDrawer();

        Subscriber<byte[]> subscriber = new Subscriber<byte[]>() {

            @Override
            public void onCompleted() {
                removeSubscription(this);

                AnimatorListenerAdapter listenerAdapter = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finishAndRemoveTask();
                    }
                };

                if (fragmentDrawer != null) {
                    fragmentDrawer.getRootView().stopSaveAnimation(listenerAdapter);
                }
            }

            @Override
            public void onError(Throwable e) {
                removeSubscription(this);
            }

            @Override
            public void onNext(byte[] bytes) {

            }

            @Override
            public void onStart() {
                super.onStart();
                fragmentDrawer.getRootView().startSaveBitmapAnimation();
            }
        };

        addSubscription(FileUtils.cache(fragmentDrawer.getDrawingBitmap(), this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));
    }

    @SuppressWarnings("unused, unchecked")
    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        if (hasInternet()) {
            FragmentDrawer drawer = getFragmentDrawer();

                Bitmap root = drawer.getDrawingBitmap();
                Bitmap bitmap = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(drawer.getRootView().getBackgroundColor());
                canvas.drawBitmap(root, 0, 0, null);
//                Bitmap bitmap = root.copy(Bitmap.Config.ARGB_8888, false);
//                root.eraseColor(drawer.getRootView().getBackgroundColor());
        } else {
            showSnackBar(R.string.snackbar_no_internet, Snackbar.LENGTH_LONG);
        }
    }

    private void showBrushChooser(View view) {
        FragmentDrawer frag = getFragmentDrawer();

        FragmentBrushPicker picker = FragmentBrushPicker.newInstance(frag.getRootView().getPaint());

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker);

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, picker, TAG_FRAGMENT_BRUSH)
                .commit();

        count++;
    }

    private void showColorChooser(View view, boolean toFill) {
        FragmentDrawer frag = getFragmentDrawer();
        ViewCanvasLayout layout = frag.getRootView();
        int color = toFill ? layout.getBackgroundColor() : layout.getBrushColor();
        FragmentColorPicker picker = FragmentColorPicker.newInstance(color, toFill);

        if (view instanceof ViewFab) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker);
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, picker);
        }

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, picker, TAG_FRAGMENT_COLOR_PICKER)
                .commit();

        count++;
    }

    private void showTextFragment(View view) {
        FragmentText text = FragmentText.newInstance();

        if (view instanceof ViewFab) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, text);
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, text);
        }

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, text, TAG_FRAGMENT_TEXT)
                .commit();

        count++;
    }

    private void showFilenameFragment(View view) {
        FragmentFilename filename = FragmentFilename.newInstance();

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, filename);

        manager.beginTransaction()
                .replace(R.id.fragment_drawer_animator, filename, TAG_FRAGMENT_FILENAME)
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

    private void showClearCanvasDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.alert_dialog_new_canvas_title)
                .setMessage(R.string.alert_dialog_new_canvas_body)
                .setPositiveButton(R.string.alert_dialog_new_canvas_pos_button, (dialog, which) -> {
                    bus.post(new EventClearCanvas());
                })
                .setNegativeButton(R.string.alert_dialog_new_canvas_neg_button, (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    private void showImageChooser() {
        Dialog builder = new Dialog(this);
        builder.setContentView(R.layout.dialog_image_chooser);
        builder.findViewById(R.id.dialog_from_camera).setOnClickListener(v -> {
            builder.dismiss();
            showCamera();
        });
        builder.findViewById(R.id.dialog_from_gallery).setOnClickListener(v -> {
            builder.dismiss();
            showGallery();
        });
        builder.show();
    }

    private void showNavigationView() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            ViewUtils.systemUIGone(getWindow().getDecorView());
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            ViewUtils.systemUIVisibile(getWindow().getDecorView());
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void showSnackBar(@StringRes int id, int duration) {
        FragmentDrawer drawer = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (drawer != null && drawer.getRootView() != null) {
            Snackbar.make(drawer.getRootView(), id, duration).show();
        }
    }

    private FragmentDrawer getFragmentDrawer() {
        return (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
    }

    private void setupHeaderView() {
    }
}
