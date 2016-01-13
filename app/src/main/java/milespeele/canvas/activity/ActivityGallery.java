package milespeele.canvas.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Iterator;

import butterknife.Bind;
import io.realm.RealmResults;
import milespeele.canvas.R;
import milespeele.canvas.adapter.GalleryPagerAdapter;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.util.DepthPageTransformer;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ZoomOutPageTransformer;
import rx.functions.Action1;

/**
 * Created by mbpeele on 1/11/16.
 */
public class ActivityGallery extends ActivityBase implements GalleryPagerAdapter.PagerListener {

    public static void newIntent(Context context) {
        context.startActivity(new Intent(context, ActivityGallery.class));
    }

    @Bind(R.id.activity_gallery_pager) ViewPager pager;

    private GalleryPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        adapter = new GalleryPagerAdapter(this);
        pager.setAdapter(adapter);
//        pager.setPageTransformer(false, new ZoomOutPageTransformer());
        pager.setPageTransformer(false, new DepthPageTransformer());

        loadImages();
    }

    @Override
    public void onPaletteReady(Palette palette) {
        Palette.Swatch swatch = palette.getLightMutedSwatch();
        if (swatch != null) {
            ValueAnimator background = ValueAnimator.ofArgb(
                    getWindow().getStatusBarColor(),
                    swatch.getRgb());
            background.setDuration(350);
            background.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    getWindow().setStatusBarColor((int) animation.getAnimatedValue());
                }
            });
            background.setInterpolator(new FastOutSlowInInterpolator());
            background.start();
        }
    }

    private void loadImages() {
        realm.where(Sketch.class)
                .findAllAsync()
                .asObservable()
                .subscribe(new Action1<RealmResults<Sketch>>() {
                    @Override
                    public void call(RealmResults<Sketch> sketches) {
                        if (sketches.isLoaded()) {
                            if (sketches.isEmpty()) {
                                setEmptyView();
                            } else {
                                for (Sketch sketch : sketches) {
                                    adapter.add(sketch);
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logg.log(throwable);
                    }
                });
    }

    private void setEmptyView() {
        FrameLayout layout =
                (FrameLayout) LayoutInflater.from(this).inflate(R.layout.adapter_gallery_empty, null);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Glide.with(this)
                .load(R.drawable.painting)
                .asGif()
                .into((ImageView) layout.getChildAt(0));

        setContentView(layout);
    }
}
