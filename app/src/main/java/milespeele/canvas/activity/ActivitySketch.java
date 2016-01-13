package milespeele.canvas.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.graphics.Palette;
import android.util.Pair;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.Bind;
import io.realm.Realm;
import io.realm.RealmQuery;
import milespeele.canvas.R;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.view.ViewAspectRatioImage;
import milespeele.canvas.view.ViewFab;

/**
 * Created by mbpeele on 1/11/16.
 */
public class ActivitySketch extends ActivityBase {

    public static void newIntent(Activity context, Sketch sketch, View image) {
        Intent intent = new Intent(context, ActivitySketch.class);
        intent.putExtra(REALM_ID, sketch.getId());

        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(context,
                new Pair<View, String>(image, IMAGE));
        context.startActivity(intent, activityOptions.toBundle());
    }

    private final static String REALM_ID = "canvas:id";
    private final static String IMAGE = "canvas:sketch:image";

    @Bind(R.id.activity_sketch_image) ViewAspectRatioImage image;
    @Bind(R.id.activity_sketch_fab) ViewFab fab;
    @Bind(R.id.activity_sketch_root) CoordinatorLayout root;

    private Sketch sketch;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);

        if (savedInstanceState == null) {
            animateReveal();
        }

        realm = Realm.getDefaultInstance();

        Intent intent = getIntent();

        RealmQuery<Sketch> realmResults = realm.where(Sketch.class)
                .equalTo("id", intent.getExtras().getString(REALM_ID));
        sketch = realmResults.findFirst();

        loadImage();
    }

    private void loadImage() {
        Glide.with(this)
                .fromBytes()
                .asBitmap()
                .load(sketch.getBytes())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .listener(new RequestListener<byte[], Bitmap>() {
                    @Override
                    public boolean onException(Exception e, byte[] model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, byte[] model,
                                                   Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch vibrant = palette.getDarkMutedSwatch();
                                if (vibrant != null) {
                                    int statusBarColor = getWindow().getStatusBarColor();
                                    ValueAnimator valueAnimator =
                                            ValueAnimator.ofArgb(statusBarColor, vibrant.getRgb());
                                    valueAnimator.setDuration(450);
                                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            getWindow().setStatusBarColor((int) animation.getAnimatedValue());
                                        }
                                    });
                                    valueAnimator.start();
                                }
                            }
                        });
                        return false;
                    }
                })
                .into(image);
    }

    private void animateReveal() {
    }
}
