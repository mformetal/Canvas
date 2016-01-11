package milespeele.canvas.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toolbar;


import butterknife.Bind;
import milespeele.canvas.R;
import milespeele.canvas.adapter.AdapterMasterpieces;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseSubscriber;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.SpacingDecoration;
import milespeele.canvas.util.ViewUtils;

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

    private AdapterMasterpieces mMasterpieceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.accent)));

        setActionBar(toolbar);

        ViewUtils.systemUIVisibile(getWindow().getDecorView());

        mMasterpieceAdapter = new AdapterMasterpieces(this, picasso);
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
        ParseSubscriber<Masterpiece> subscriber = new ParseSubscriber<Masterpiece>(this) {
            @Override
            public void onCompleted() {
                super.onCompleted();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onNext(Masterpiece masterpiece) {
                super.onNext(masterpiece);
                mMasterpieceAdapter.addMasterpiece(masterpiece);

                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        };

        parseUtils.getMasterpieces().subscribe(subscriber);
    }
}
