package miles.canvas.ui.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import io.realm.RealmResults;
import miles.canvas.R;
import miles.canvas.data.adapter.DepthPageTransformer;
import miles.canvas.data.adapter.GalleryPagerAdapter;
import miles.canvas.data.Sketch;
import miles.canvas.util.Logg;
import rx.functions.Action1;

/**
 * Created by mbpeele on 1/11/16.
 */
public class GalleryActivity extends BaseActivity implements GalleryPagerAdapter.PagerListener {

    public static void newIntent(Context context) {
        context.startActivity(new Intent(context, GalleryActivity.class));
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
