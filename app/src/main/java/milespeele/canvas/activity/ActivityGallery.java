package milespeele.canvas.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.Bind;
import io.realm.Realm;
import io.realm.RealmResults;
import milespeele.canvas.R;
import milespeele.canvas.adapter.GalleryAdapter;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.RecyclerClickListener;
import milespeele.canvas.util.SpacingDecoration;
import milespeele.canvas.util.ViewUtils;
import rx.functions.Action1;

/**
 * Created by mbpeele on 1/11/16.
 */
public class ActivityGallery extends ActivityBase implements RecyclerClickListener.OnItemClickListener {

    public static void newIntent(Context context) {
        context.startActivity(new Intent(context, ActivityGallery.class));
    }

    @Bind(R.id.activity_gallery_masterpieces) RecyclerView recyclerView;
    @Bind(R.id.activity_gallery_loading) ProgressBar progressBar;
    @Bind(R.id.activity_gallery_root) CoordinatorLayout layout;

    private GalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerClickListener.addTo(recyclerView).setOnItemClickListener(this);

        adapter = new GalleryAdapter(this);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SpacingDecoration(10));
        recyclerView.setHasFixedSize(true);

        loadImages();
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, RecyclerView.ViewHolder holder, View v) {
        GalleryAdapter.SketchViewHolder viewHolder =
                (GalleryAdapter.SketchViewHolder) holder;

        int pos = viewHolder.getAdapterPosition();
        Sketch sketch = adapter.get(pos);

       ActivitySketch.newIntent(this, sketch, viewHolder);
    }

    private void loadImages() {
        realm.where(Sketch.class)
                .findAllAsync()
                .asObservable()
                .subscribe(new Action1<RealmResults<Sketch>>() {
                    @Override
                    public void call(RealmResults<Sketch> sketches) {
                        for (Sketch sketch : sketches) {
                            adapter.addMasterpiece(sketch);
                        }

                        ViewUtils.gone(progressBar);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logg.log(throwable);
                    }
                });
    }
}
