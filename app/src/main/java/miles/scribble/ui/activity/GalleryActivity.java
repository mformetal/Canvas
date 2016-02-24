package miles.scribble.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.OnClick;
import io.realm.RealmResults;
import miles.scribble.R;
import miles.scribble.data.adapter.GalleryPagerAdapter;
import miles.scribble.data.model.Sketch;
import miles.scribble.data.event.EventUpdateDrawingCurve;
import miles.scribble.util.Logg;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by mbpeele on 1/11/16.
 */
public class GalleryActivity extends BaseActivity implements OnClickListener {

    @Bind(R.id.activity_gallery_pager) ViewPager pager;

    private GalleryPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        adapter = new GalleryPagerAdapter(this);
        pager.setAdapter(adapter);
//        pager.setPageTransformer(false, new ZoomOutPageTransformer());
//        pager.setPageTransformer(false, new DepthPageTransformer());

        loadImages();
    }

    @Override
    @OnClick({R.id.activity_gallery_options_menu_cut, R.id.activity_gallery_options_menu_set})
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
            case R.id.activity_gallery_options_menu_set:
                int curItem = pager.getCurrentItem();
                Sketch sketch = adapter.get(curItem);
                bus.post(new EventUpdateDrawingCurve(sketch.getId()));
                onBackPressed();
                break;
        }
    }

    private void loadImages() {
        realm.where(Sketch.class)
                .findAllAsync()
                .asObservable()
                .filter(sketches -> sketches.isValid() && sketches.isLoaded())
                .subscribe(new Action1<RealmResults<Sketch>>() {
                    @Override
                    public void call(RealmResults<Sketch> sketches) {
                        if (sketches.isEmpty()) {
                            setEmptyView();
                        } else {
                            for (Sketch sketch : sketches) {
                                adapter.add(sketch);
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
