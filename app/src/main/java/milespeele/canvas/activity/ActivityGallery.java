package milespeele.canvas.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toolbar;

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
import milespeele.canvas.util.WrapContentLinearLayoutManager;
import milespeele.canvas.view.ViewFab;
import rx.functions.Action1;

/**
 * Created by mbpeele on 1/11/16.
 */
public class ActivityGallery extends ActivityBase implements RecyclerClickListener.OnItemClickListener {

    public static Intent newIntent(Context context) {
        return new Intent(context, ActivityGallery.class);
    }

    @Bind(R.id.activity_gallery_masterpieces) RecyclerView recyclerView;
    @Bind(R.id.activity_gallery_loading) ProgressBar progressBar;
    @Bind(R.id.activity_gallery_root) CoordinatorLayout layout;

    private GalleryAdapter mMasterpieceAdapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        realm = Realm.getDefaultInstance();

        RecyclerClickListener.addTo(recyclerView).setOnItemClickListener(this);

        mMasterpieceAdapter = new GalleryAdapter(this);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mMasterpieceAdapter);
        recyclerView.addItemDecoration(new SpacingDecoration(10));
        recyclerView.setHasFixedSize(true);

        loadImages();
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void loadImages() {
        realm.where(Sketch.class)
                .findAllAsync()
                .asObservable()
                .subscribe(new Action1<RealmResults<Sketch>>() {
                    @Override
                    public void call(RealmResults<Sketch> sketches) {
                        for (Sketch sketch : sketches) {
                            mMasterpieceAdapter.addMasterpiece(sketch);
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
