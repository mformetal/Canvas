package milespeele.canvas.activity;

<<<<<<< HEAD
import android.os.Bundle;

import milespeele.canvas.R;

/**
 * Created by mbpeele on 1/9/16.
 */
public class ActivityGallery extends ActivityBase {

=======
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import butterknife.Bind;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import milespeele.canvas.R;
import milespeele.canvas.adapter.GalleryAdapter;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.util.SafeSubscription;
import milespeele.canvas.util.SpacingDecoration;
import milespeele.canvas.util.ViewUtils;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by mbpeele on 1/11/16.
 */
public class ActivityGallery extends ActivityBase {

    public static Intent newIntent(Context context) {
        return new Intent(context, ActivityGallery.class);
    }

    @Bind(R.id.activity_gallery_masterpieces) RecyclerView recyclerView;
    @Bind(R.id.activity_gallery_toolbar) Toolbar toolbar;
    @Bind(R.id.activity_gallery_loading) ProgressBar progressBar;
    @Bind(R.id.activity_gallery_root) FrameLayout frameLayout;

    private GalleryAdapter mMasterpieceAdapter;

>>>>>>> Realm
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
<<<<<<< HEAD
=======

        getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.accent)));

        setActionBar(toolbar);

        ViewUtils.systemUIVisibile(getWindow().getDecorView());

        mMasterpieceAdapter = new GalleryAdapter(this);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mMasterpieceAdapter);
        recyclerView.addItemDecoration(new SpacingDecoration(10));
        recyclerView.setHasFixedSize(true);

        frameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                        .getLayoutParams();
                lpToolbar.topMargin += insets.getSystemWindowInsetTop();
                lpToolbar.rightMargin += insets.getSystemWindowInsetRight();
                toolbar.setLayoutParams(lpToolbar);

                recyclerView.setPadding(
                        recyclerView.getPaddingTop(),
                        insets.getSystemWindowInsetTop() +
                                ViewUtils.actionBarSize(v.getContext()),
                        recyclerView.getPaddingRight(),
                        ViewUtils.actionBarSize(v.getContext()));
                return insets.consumeSystemWindowInsets();
            }
        });

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
                            mMasterpieceAdapter.addMasterpiece(sketch);
                        }

                        ViewUtils.gone(progressBar);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
>>>>>>> Realm
    }
}
