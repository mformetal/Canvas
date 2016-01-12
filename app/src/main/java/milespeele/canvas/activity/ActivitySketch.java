package milespeele.canvas.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.util.Pair;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.Bind;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import milespeele.canvas.R;
import milespeele.canvas.adapter.GalleryAdapter;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewAspectRatioImage;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by mbpeele on 1/11/16.
 */
public class ActivitySketch extends ActivityBase {

    public static void newIntent(Activity context, Sketch sketch, GalleryAdapter.SketchViewHolder holder) {
        Intent intent = new Intent(context, ActivitySketch.class);
        intent.putExtra(REALM_ID, sketch.getId());

        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(context,
                new Pair<View, String>(holder.getImageView(), IMAGE),
                new Pair<View, String>(holder.getTextView(), TITLE));
        context.startActivity(intent, activityOptions.toBundle());
    }

    private final static String REALM_ID = "canvas:id";
    private final static String IMAGE = "canvas:sketch:image";
    private final static String TITLE = "canvas:sketch:title";

    @Bind(R.id.activity_sketch_image) ViewAspectRatioImage image;
    @Bind(R.id.activity_sketch_title) ViewTypefaceTextView title;

    private Sketch sketch;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);

        realm = Realm.getDefaultInstance();

        Intent intent = getIntent();

        RealmQuery<Sketch> realmResults = realm.where(Sketch.class).equalTo("id", intent.getExtras().getString(REALM_ID));
        sketch = realmResults.findFirst();

        ViewCompat.setTransitionName(image, IMAGE);
        ViewCompat.setTransitionName(title, TITLE);

        Glide.with(this)
                .fromBytes()
                .asBitmap()
                .animate(android.R.anim.fade_in)
                .load(sketch.getBytes())
                .listener(new RequestListener<byte[], Bitmap>() {
                    @Override
                    public boolean onException(Exception e, byte[] model, Target<Bitmap> target, boolean isFirstResource) {
                        Logg.log("GLIDE:", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, byte[] model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch vibrant = palette.getLightVibrantSwatch();
                                if (vibrant != null) {
                                    ViewUtils.animateBackground(title, 350, vibrant.getRgb()).start();
                                    title.animateTextColor(vibrant.getTitleTextColor(), 350).start();
                                }
                            }
                        });
                        return false;
                    }
                })
                .into(image);
    }
}
