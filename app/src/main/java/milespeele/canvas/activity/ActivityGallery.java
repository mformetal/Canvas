package milespeele.canvas.activity;

<<<<<<< HEAD
<<<<<<< HEAD
import android.os.Bundle;

import milespeele.canvas.R;

/**
 * Created by mbpeele on 1/9/16.
 */
public class ActivityGallery extends ActivityBase {

=======
=======
import android.app.ActivityOptions;
>>>>>>> NewBranch
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
public class ActivityGallery extends ActivityBase {

    public static void newIntent(Context context) {
        context.startActivity(new Intent(context, ActivityGallery.class));
    }

    @Bind(R.id.activity_gallery_masterpieces) RecyclerView recyclerView;

    private GalleryAdapter adapter;

>>>>>>> Realm
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
<<<<<<< HEAD
=======

        adapter = new GalleryAdapter(this);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SpacingDecoration(10));
        recyclerView.setHasFixedSize(true);

        loadImages();
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
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logg.log(throwable);
                    }
                });
>>>>>>> Realm
    }
}
