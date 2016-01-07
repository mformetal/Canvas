package milespeele.canvas.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.event.EventBitmapChosen;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.fragment.FragmentText;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.transition.TransitionHelper;
import milespeele.canvas.util.ErrorDialog;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.NetworkUtils;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewRoundedFrameLayout;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ActivityHome extends ActivityBase {

    private final static String TAG_FRAGMENT_DRAWER = "drawer";
    private final static String TAG_FRAGMENT_COLOR_PICKER = "color";
    private final static String TAG_FRAGMENT_FILENAME = "name";
    private final static String TAG_FRAGMENT_BRUSH = "brush";
    private final static String TAG_FRAGMENT_TEXT = "text";
    private final static int REQUEST_IMPORT_CODE = 2001;
    private final static int REQUEST_CAMERA_CODE = 2002;

    @Inject ParseUtils parseUtils;
    @Inject EventBus bus;

    @Bind(R.id.activity_home_fragment_frame) FrameLayout frameLayout;

    private ViewRoundedFrameLayout fabFrame;
    private FragmentManager manager;
    private String filePath;

    private int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        getWindow().setBackgroundDrawable(null);

        bus.register(this);

        manager = getFragmentManager();

        manager.beginTransaction()
                .add(R.id.activity_home_fragment_frame, FragmentDrawer.newInstance(), TAG_FRAGMENT_DRAWER)
                .commit();
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
        if (count == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setPositiveButton(R.string.alert_dialog_save_exit, (dialog, which) -> {
                        saveAndExit();
                    })
                    .setNeutralButton(R.string.alert_dialog_cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.alert_dialog_exit, (dialog, which) -> {
                        dialog.dismiss();
                        super.onBackPressed();
                    });
            builder.create().show();
        } else {
            // UGLY, but popBackStack() results in a weird exception on certain devices
            // https://code.google.com/p/android/issues/detail?id=82832
            count--;

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_drawer_animator, new Fragment());
            ft.commit();
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
                        super.onAnimationEnd(animation);
                        finish();
                    }
                };
                fragmentDrawer.getRootView().stopSaveAnimation(listenerAdapter);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(byte[] bytes) {

            }

            @Override
            public void onStart() {
                super.onStart();
                fragmentDrawer.getRootView().startSaveAnimation();
            }
        };

        addSubscription(FileUtils.cacheAsObservable(fragmentDrawer.getDrawingBitmap(), this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));
    }

    @SuppressWarnings("unused")
    public void onEvent(EventParseError eventParseError) {
        ErrorDialog.createDialogFromCode(this, eventParseError.getErrorCode()).show();
    }

    @SuppressWarnings("unused, unchecked")
    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        if (NetworkUtils.hasInternet(this)) {
            FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
            if (frag != null) {
                parseUtils.saveImageToServer(this, eventFilenameChosen.filename, frag.getDrawingBitmap())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((Action1) o -> {
                            ((ViewFab) findViewById(R.id.menu_save)).stopSaveAnimation();

                            FragmentDrawer fragmentDrawer =
                                    (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);

                            if (fragmentDrawer != null && fragmentDrawer.getRootView() != null) {
                                Snackbar.make(fragmentDrawer.getRootView(),
                                        R.string.snackbar_activity_home_image_saved_title,
                                        Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        });
            }
        } else {
            showSnackBar(R.string.snackbar_no_internet, Snackbar.LENGTH_LONG);
        }
    }

    private void showBrushChooser(View view) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            FragmentBrushPicker picker = FragmentBrushPicker.newInstance(frag.getRootView().getPaint());

            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker);

            manager.beginTransaction()
                    .replace(R.id.fragment_drawer_animator, picker, TAG_FRAGMENT_BRUSH)
                    .commit();

            count++;
        }
    }

    private void showStrokeColorChooser(View view, boolean toFill) {
        FragmentDrawer frag = (FragmentDrawer) manager.findFragmentByTag(TAG_FRAGMENT_DRAWER);
        if (frag != null) {
            FragmentColorPicker picker = FragmentColorPicker
                    .newInstance(frag.getRootView().getBrushColor(), toFill);

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

    private void showGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMPORT_CODE);
    }

    private void showCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = FileUtils.createPhotoFile(this);
                    filePath = photoFile.getAbsolutePath();
                } catch (IOException e) {
                    Logg.log(e);
                    showSnackBar(R.string.error_dialog_no_camera_body, Snackbar.LENGTH_SHORT);
                }

                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA_CODE);
                }
            }
        } else {
            showSnackBar(R.string.error_dialog_no_camera_body, Snackbar.LENGTH_SHORT);
        }
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
                showStrokeColorChooser(view, false);
                break;
            case R.id.menu_text:
                showTextFragment(view);
                break;
            case R.id.menu_save:
                showFilenameFragment(view);
                break;
            case R.id.menu_canvas_color:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.alert_dialog_new_canvas_title)
                        .setMessage(R.string.alert_dialog_new_canvas_body)
                        .setPositiveButton(R.string.alert_dialog_new_canvas_pos_button, (dialog, which) -> {
                            showStrokeColorChooser(view, true);
                        })
                        .setNegativeButton(R.string.alert_dialog_new_canvas_neg_button, (dialog, which) -> {
                            dialog.dismiss();
                        });
                builder.create().show();
                break;
            case R.id.menu_import:
                showGalleryChooser();
                break;
            case R.id.menu_camera:
                showCamera();
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
                        showStrokeColorChooser(view, false);
                    }
                    break;
                case PICTURE:
                    if (view.getId() == R.id.view_options_menu_1) {
                        showCamera();
                    } else {
                        showGalleryChooser();
                    }
                    break;
            }
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
}
