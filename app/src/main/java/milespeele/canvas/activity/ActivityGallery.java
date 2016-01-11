package milespeele.canvas.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.adapter.AdapterMasterpieces;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseSubscriber;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.SpacingDecoration;
import milespeele.canvas.util.ViewUtils;
import rx.Subscriber;

/**
 * Created by mbpeele on 1/11/16.
 */
public class ActivityGallery extends ActivityBase {

    public static Intent newIntent(Context context) {
        return new Intent(context, ActivityGallery.class);
    }

    @Bind(R.id.activity_galleries_masterpieces) RecyclerView recyclerView;

    private AdapterMasterpieces mMasterpieceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        mMasterpieceAdapter = new AdapterMasterpieces(this, picasso);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mMasterpieceAdapter);

        loadImages();
    }

    private void loadImages() {
        ParseSubscriber<Masterpiece> subscriber = new ParseSubscriber<Masterpiece>(this) {
            @Override
            public void onNext(Masterpiece masterpiece) {
                super.onNext(masterpiece);
                mMasterpieceAdapter.addMasterpiece(masterpiece);
            }
        };

        parseUtils.getMasterpieces().subscribe(subscriber);
    }
}
