package miles.canvas.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.UUID;

import butterknife.Bind;
import butterknife.OnClick;
import io.realm.RealmResults;
import miles.canvas.R;
import miles.canvas.data.adapter.DepthPageTransformer;
import miles.canvas.data.adapter.GalleryPagerAdapter;
import miles.canvas.data.Sketch;
import miles.canvas.data.adapter.ZoomOutPageTransformer;
import miles.canvas.data.event.EventClearCanvas;
import miles.canvas.util.Logg;
import miles.canvas.util.ViewUtils;
import rx.functions.Action1;

/**
 * Created by mbpeele on 1/11/16.
 */
public class GalleryActivity extends BaseActivity
        implements OnClickListener {

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
        ObjectAnimator.ofInt(pager, ViewUtils.ALPHA, 64).setDuration(350).start();
        pager.setAdapter(adapter);
//        pager.setPageTransformer(false, new ZoomOutPageTransformer());
//        pager.setPageTransformer(false, new DepthPageTransformer());

        loadImages();
    }

    @Override
    @OnClick({R.id.activity_gallery_options_menu_cut})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_gallery_options_menu_cut:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.alert_dialog_delete_sketch_title)
                        .setMessage(R.string.alert_dialog_delete_sketch_body)
                        .setPositiveButton(R.string.alert_dialog_delete_sketch_pos_button, (dialog, which) -> {
                            deleteSketch();
                        })
                        .setNegativeButton(R.string.alert_dialog_delete_sketch_neg_button, (dialog, which) -> {
                            dialog.dismiss();
                        });
                builder.create().show();
                break;
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

    private void deleteSketch() {
        int itemCount = adapter.getCount();

        int curItem = pager.getCurrentItem();
        Sketch sketch = adapter.remove(curItem);
        adapter.notifyDataSetChanged();
        pager.setAdapter(adapter);

        if (curItem == 0) {
            pager.setCurrentItem(1, true);
        } else {
            pager.setCurrentItem(curItem - 1, true);
        }

        realm.beginTransaction();
        sketch.removeFromRealm();
        realm.commitTransaction();

        if (itemCount == 1) {
            setEmptyView();
        }
    }
}
